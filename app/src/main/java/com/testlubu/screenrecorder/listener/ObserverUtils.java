package com.testlubu.screenrecorder.listener;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class ObserverUtils<T> implements Subject<ObserverInterface<T>, T>
{
    private static ObserverUtils instance;
    private List<ObserverInterface> observers;

    public ObserverUtils() {
        this.observers = new ArrayList<ObserverInterface>();
    }

    public static ObserverUtils getInstance() {
        if (ObserverUtils.instance == null) {
            ObserverUtils.instance = new ObserverUtils();
        }
        return ObserverUtils.instance;
    }

    @Override // com.testlubu.screenrecorder.listener.Subject
    public void notifyObservers(T t) {
        for (ObserverInterface observerInterface : this.observers) {
            observerInterface.notifyAction(t);
        }
    }

    @Override
    public void registerObserver(final ObserverInterface<T> observerInterface) {
        if (!this.observers.contains(observerInterface)) {
            this.observers.add(observerInterface);
        }
    }

    @Override
    public void removeObserver(final ObserverInterface<T> observerInterface) {
        if (this.observers.contains(observerInterface)) {
            this.observers.remove(observerInterface);
        }
    }
}
