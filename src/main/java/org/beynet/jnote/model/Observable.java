package org.beynet.jnote.model;

import java.util.ArrayList;
import java.util.List;

public class Observable {

    protected void setChanged() {

    }

    protected synchronized void	notifyObservers(Object arg){
        for (Observer observer : observers) {
            observer.update(this,arg);
        }
    }

    public synchronized void addObserver(Observer obj){
        observers.add(obj);
    }

    public synchronized void deleteObserver(Observer o) {
        observers.remove(o);
    }


    private List<Observer> observers = new ArrayList<>();
}
