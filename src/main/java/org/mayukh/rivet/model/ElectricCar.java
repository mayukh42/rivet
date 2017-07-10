package org.mayukh.rivet.model;

import org.mayukh.rivet.core.Riveted;

/**
 * Created by mayukh42 on 11/6/17.
 *
 * ElectricCar is a type to be created using DI. It should follow Java Bean conventions.
 */
public class ElectricCar {

    private String name;

    @Riveted
    private Battery battery;

    public ElectricCar() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    public boolean run() {
        boolean runnable = battery.getChargeLeft() > 0d;
        if (runnable) System.out.println(this + " is running");
        else System.out.println(this + " cannot run. Charge the battery first!");
        return runnable;
    }

    public void ready() {
        battery.charge();
    }

    @Override
    public String toString() {
        return "ElectricCar{" + name + " running on " + battery + "}";
    }
}
