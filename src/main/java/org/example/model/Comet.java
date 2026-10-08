package org.example.model;

import jakarta.persistence.Entity;

@Entity
public class Comet extends CelestialBody {

    private double tailLength;

    private int orbitalPeriod;

    protected Comet() {
        super();
    }
    public Comet(String name, Coordinates stellarCoordinates, double tailLength, int orbitalPeriod) {
        super(name, stellarCoordinates);
        setTailLength(tailLength);
        setOrbitalPeriod(orbitalPeriod);
    }

    public double getTailLength() {
        return tailLength;
    }
    public int getOrbitalPeriod() {
        return orbitalPeriod;
    }

    public void setTailLength(double tailLength) {
        if (!Double.isFinite(tailLength) || tailLength < 0) {
            throw new IllegalArgumentException("Długość warkocza komety nie może być ujemna.");
        }
        this.tailLength = tailLength;
    }
    public void setOrbitalPeriod(int orbitalPeriod) {
        if (orbitalPeriod <= 0) {
            throw new IllegalArgumentException("Okres orbitalny musi być większy od zera.");
        }
        this.orbitalPeriod = orbitalPeriod;
    }

    @Override
    public double calculateDangerLevel() {
        double danger = 30;

        danger += (tailLength * 0.3);

        if (orbitalPeriod < 50) {
            danger += 20.0;
        }

        return Math.min(danger, 100.0);
    }
}
