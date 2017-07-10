package org.mayukh.rivet.model;

/**
 * Created by mayukh42 on 11/6/17.
 *
 * Battery is a type to be created using DI. It should follow Java Bean conventions.
 */
public class Battery {

    private String name;
    private Double chargeLeft;

    public Battery() {}

    public Battery(String name, Double chargeLeft) {
        this.name = name;
        this.chargeLeft = chargeLeft;
    }

    public String getName() {
        return name;
    }

    public Double getChargeLeft() {
        return chargeLeft;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChargeLeft(Double chargeLeft) {
        this.chargeLeft = chargeLeft;
    }

    public void charge() {
        if (chargeLeft <= 90.0) chargeLeft += 10d;
    }

    @Override
    public String toString() {
        return name + "(" + chargeLeft + "%)";
    }
}
