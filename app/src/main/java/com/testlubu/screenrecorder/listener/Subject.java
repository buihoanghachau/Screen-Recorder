package com.testlubu.screenrecorder.listener;

public interface Subject<T, K> {
    void notifyObservers(K k);

    void registerObserver(T t);

    void removeObserver(T t);
}
