package com.rave.yunwang.widget.camera;

import java.util.ArrayList;
import java.util.List;

public class CallBackUtils<T> {

    private List<T> callbacks = new ArrayList<>();

    public void addCallBack(T callback) {
        if (callbacks != null && !callbacks.contains(callback))
            callbacks.add(callback);
    }

    public void removeCallBack(T callback) {
        if (callbacks != null && callbacks.contains(callback))
            callbacks.remove(callback);
    }

    public void removeCallBacks() {
        if (callbacks != null && callbacks.size() != 0)
            callbacks.clear();
    }

    public List<T> getCallbacks() {
        return callbacks;
    }

    public boolean hasCallBacks() {

        if (callbacks == null || callbacks.size() == 0)
            return false;

        return true;
    }
}
