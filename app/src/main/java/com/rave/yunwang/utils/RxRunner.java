package com.rave.yunwang.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class RxRunner {

    private final String TAG = "RxRunner";

    private volatile static RxRunner instance = null;

    public static abstract class Runner {

        private Object inputData;
        private Object outputData;

        private ThreadState inputThreadState;

        private RunnerHandler runnerHandler;

        public Runner(Object data, ThreadState inputThreadState) {
            inputData = data;
            this.inputThreadState = inputThreadState;
        }

        public Runner(Object data, ThreadState inputThreadState, RunnerHandler runnerHandler) {
            this(data, inputThreadState);
            this.runnerHandler = runnerHandler;
        }

        public Runner(Object data, RunnerHandler runnerHandler) {
            this(data, ThreadState.self, runnerHandler);
        }

        public abstract Object run(Object data);

    }

    public static abstract class RunnerHandler {

        private ThreadState outputThreadState;

        public RunnerHandler() {
            this(ThreadState.self);
        }

        public RunnerHandler(ThreadState outputThreadState) {
            this.outputThreadState = outputThreadState;
        }

        public abstract void onError(Object data);

        public abstract void onResult(Object data);

    }

    public enum ThreadState {
        self, main, thread;
    }

    /**
     * 设备CPU核数
     */
    private final int KCPUCoreMaxCount = Runtime.getRuntime().availableProcessors();

    /**
     * 最佳线程数
     */
    private final int KMaxThreadCount = KCPUCoreMaxCount * 2;

    /**
     * 线程池最大值
     */
    private final int KRunnerThreadCount = KMaxThreadCount;

    private volatile int currentRunnerThreadCount = 1;

    private final LinkedBlockingQueue<Runner> runnerTaskQueue = new LinkedBlockingQueue<>();

    private final MainThreadHandler mainThreadHandler = new MainThreadHandler(Looper.getMainLooper());

    private final int KHandlerPush = 0x1;

    private final int KHandlerPop = 0x2;

    public static RxRunner getInstance() {
        if (instance == null) {
            synchronized (RxRunner.class) {
                if (instance == null) {
                    instance = new RxRunner();
                }
            }
        }
        return instance;
    }

    public boolean doTask(final Runner runner) {
        if (runner == null) {
            return false;
        }

        return doRunnerPush(getCurrentThreadState(), runner);
    }

    public boolean merge(final RunnerHandler runnerHandler, final Runner... runners) {
        if (runnerHandler == null || runners == null || runners.length <= 0) return false;

        for (Runner runner :
                runners) {
            runner.runnerHandler = runnerHandler;
            if (!doTask(runner))
                return false;
        }

        return true;
    }

    public boolean contact(final RunnerHandler runnerHandler, final Runner... runners) {
        if (runnerHandler == null || runners == null || runners.length <= 0) return false;

        final LinkedBlockingQueue<Runner> queue = new LinkedBlockingQueue();

        if (runnerHandler.outputThreadState == ThreadState.self)
            runnerHandler.outputThreadState = getCurrentThreadState();

        RunnerHandler handler = new RunnerHandler(runnerHandler.outputThreadState) {
            @Override
            public void onError(Object data) {
                runnerHandler.onError(data);
            }

            @Override
            public void onResult(Object data) {
                runnerHandler.onResult(data);
                if (queue.peek() != null)
                    doTask(queue.poll());

            }
        };

        for (Runner runner :
                runners) {
            queue.offer(runner);
            runner.runnerHandler = handler;
        }

        return doTask(queue.poll());
    }

    public boolean zip(final RunnerHandler runnerHandler, final Runner... runners) {
        if (runnerHandler == null || runners == null || runners.length <= 0) return false;

        final ConcurrentHashMap<Runner, Object> map = new ConcurrentHashMap();

        class WrapRunnerHandler extends RunnerHandler {

            private Runner runner;

            public WrapRunnerHandler(ThreadState outputThreadState) {
                super(outputThreadState);
            }

            @Override
            public void onError(Object data) {
                runnerHandler.onError(data);
            }

            @Override
            public void onResult(Object data) {
                map.put(runner, data);
                if (map.size() == runners.length) {
                    runnerHandler.onResult(map);
                }
            }
        }

        for (Runner runner :
                runners) {
            WrapRunnerHandler wrapRunnerHandler = new WrapRunnerHandler(runnerHandler.outputThreadState);
            wrapRunnerHandler.runner = runner;
            runner.runnerHandler = wrapRunnerHandler;
            if (!doTask(runner)) return false;
        }

        return true;
    }

    /*---------------------------------------------------inner functions---------------------------------------------------*/

    private RxRunner() {
    }

    private boolean isThread() {
        return Looper.myLooper() != Looper.getMainLooper();
    }

    private ThreadState getCurrentThreadState() {
        return isThread() ? ThreadState.thread : ThreadState.main;
    }

    /*---------------------------------------------------自己线程处理相关---------------------------------------------------*/


    private boolean doRunnerPush(ThreadState currentThreadState, Runner runner) {
        /**
         * 在子线程输入
         */
        if (runner.inputThreadState == ThreadState.thread && currentThreadState != ThreadState.thread)
            return pushRunnerTask(runner);

        /**
         * 在主线程输入
         */
        else if (runner.inputThreadState == ThreadState.main && currentThreadState != ThreadState.main)
            return mainThreadHandler.sendRunner(runner);

        /**
         * 在自己的线程输入
         */
        else
            return doRunnerPop(currentThreadState, runner);

    }

    private boolean doRunnerPop(ThreadState currentThreadState, Runner runner) {
        try {

            runner.outputData = runner.run(runner.inputData);

            if (runner.runnerHandler.outputThreadState == ThreadState.self)
                runner.runnerHandler.outputThreadState = currentThreadState;


            /**
             * 子线程输出
             */
            if (runner.runnerHandler.outputThreadState == ThreadState.thread && currentThreadState != ThreadState.thread) {
                return pushRunnerTask(runner);
            }
            /**
             * 主线程输出
             */
            else if (runner.runnerHandler.outputThreadState == ThreadState.main && currentThreadState != ThreadState.main) {
                return mainThreadHandler.sendRunner(runner);
            }
            /**
             * 该哪里就在哪里
             */
            else {
                runner.runnerHandler.onResult(runner.inputData);
                return true;
            }

        } catch (Exception e) {
            runner.runnerHandler.onError(e.toString());
            e.printStackTrace();
        }

        return false;
    }

    /*---------------------------------------------------主线程处理相关---------------------------------------------------*/

    private class MainThreadHandler extends Handler {

        public MainThreadHandler(Looper looper) {
            super(looper);
        }

        public boolean sendRunner(Runner runner) {
            if (runner == null) return false;

            Message msg = obtainMessage();

            if (runner.outputData == null)
                msg.what = KHandlerPush;
            else
                msg.what = KHandlerPop;

            msg.obj = runner;

            sendMessage(msg);

            return true;
        }


        @Override
        public void handleMessage(Message message) {

            Runner runner = null;

            try {

                runner = (Runner) message.obj;
                if (runner == null) {
                    return;
                }

                if (message.what == KHandlerPush)
                    doRunnerPop(ThreadState.main, runner);

                else if (message.what == KHandlerPop)
                    runner.runnerHandler.onResult(runner.outputData);


            } catch (Exception e) {
                e.printStackTrace();
                runner.runnerHandler.onError(e.toString());
            }
        }
    }

    /*---------------------------------------------------子线程处理相关---------------------------------------------------*/

    private boolean pushRunnerTask(Runner runner) {
        if (runner == null ||
                runnerTaskQueue.contains(runner)) {
            return false;
        }

        try {
            runnerTaskQueue.put(runner);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (currentRunnerThreadCount >= KRunnerThreadCount) {
            return true;
        }

        currentRunnerThreadCount++;
        RunnerThread runnerThread = new RunnerThread();
        runnerThread.start();

        return true;
    }

    private Runner popRunnerTask() {
        if (runnerTaskQueue.isEmpty()) {
            return null;
        }

        try {
            return runnerTaskQueue.take();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void onRunnerThreadExit() {

        currentRunnerThreadCount--;

        if (currentRunnerThreadCount <= 0 &&
                !runnerTaskQueue.isEmpty()) {

            currentRunnerThreadCount++;
            RunnerThread runnerThread = new RunnerThread();
            runnerThread.start();

        }
    }

    private class RunnerThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    Runner runner = popRunnerTask();

                    if (runner == null) {
                        onRunnerThreadExit();

                        break;
                    }

                    try {
                        /**
                         * 输入状态
                         */
                        if (runner.outputData == null)
                            doRunnerPop(ThreadState.thread, runner);

                        /**
                         * 输出状态
                         */
                        else
                            runner.runnerHandler.onResult(runner.outputData);

                    } catch (Exception e) {
                        e.printStackTrace();
                        runner.runnerHandler.onError(e.toString());
                    }
                }
            } catch (Exception e) {
                onRunnerThreadExit();
            }
        }
    }


}
