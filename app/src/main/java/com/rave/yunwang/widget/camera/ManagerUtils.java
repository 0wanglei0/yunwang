package com.rave.yunwang.widget.camera;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

public abstract class ManagerUtils<T> {

    public static class Msg {
        private List<Object> list = new ArrayList<Object>();

        public Msg(Object... content) {
            for (Object o :
                    content) {
                list.add(o);
            }
        }

        public List<Object> getList() {
            return list;
        }
    }

    protected List<T> listeners = new ArrayList<>();

    private MessageHandler messageHandler;

    private MessageSender messageSender;

    public ManagerUtils() {
        messageHandler = new MessageHandler();
        messageSender = new MessageSender();
    }

    public void addListener(T linstener) {
        if (listeners != null && !listeners.contains(linstener))
            listeners.add(linstener);
    }

    public void removeListener(T linstener) {
        if (listeners != null && listeners.contains(linstener))
            listeners.remove(linstener);
    }

    public void removeListeners() {
        if (listeners != null && listeners.size() != 0)
            listeners.clear();
    }

    public void sendMsg(int type, String text) {
        messageSender.sendMessage(type, new Msg(text));
    }

    protected void sendMsg(int type, int text) {
        messageSender.sendMessage(type, new Msg(text));
    }

    protected void sendMsg(int type, Msg msg) {
        messageSender.sendMessage(type, msg);
    }

    public void sendMsg(int type) {
        messageSender.sendMessage(type, null);
    }

    protected void sendMsgDelayed(int type, long delayMillis) {
        messageSender.sendMessageDelayed(type, null, delayMillis);
    }

    protected void cancelMsg(int type) {
        messageSender.cancelMessage(type);
    }

    protected abstract void handleMsg(Message message);

    private class MessageHandler extends Handler {

        MessageHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message message) {
            if (listeners.size() == 0) {
                return;
            }

            try {
                handleMsg(message);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private class MessageSender {

        public void sendMessage(int type, Msg msg) {
            if (messageHandler == null) {
                return;
            }

            try {
                Message message = messageHandler.obtainMessage();
                message.what = type;
                message.obj = msg;
                messageHandler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void sendMessageDelayed(int type, Msg msg, long delayMillis) {
            if (messageHandler == null) {
                return;
            }

            try {
                Message message = messageHandler.obtainMessage();
                message.what = type;
                message.obj = msg;
                messageHandler.sendMessageDelayed(message, delayMillis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void cancelMessage(int type) {
            try {
                messageHandler.removeMessages(type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
