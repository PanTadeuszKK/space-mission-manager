package org.example.model;

import jakarta.persistence.Entity;

@Entity
public class Planet extends CelestialBody {

    private boolean hasAtmosphere;

    private String surfaceType;

    protected Planet() {
        super();
    }

    public Planet(String name, Coordinates stellarCoordinates, boolean hasAtmosphere, String surfaceType) {
        super(name, stellarCoordinates);
        this.hasAtmosphere = hasAtmosphere;
        setSurfaceType(surfaceType);
    }

    public boolean isHasAtmosphere() {
        return hasAtmosphere;
    }
    public String getSurfaceType() {
        return surfaceType;
    }

    public void setHasAtmosphere(boolean hasAtmosphere) {
        this.hasAtmosphere = hasAtmosphere;
    }
    public void setSurfaceType(String surfaceType) {
        if (surfaceType == null || surfaceType.trim().isEmpty()) {
            throw new IllegalArgumentException("Typ powierzchni planety nie może być pusty.");
        }
        this.surfaceType = surfaceType;
    }

    @Override
    public double calculateDangerLevel() {
        double baseDanger = 15;

        if (!hasAtmosphere) {
            baseDanger += 35;
        }
        if ("Gazowa".equalsIgnoreCase(surfaceType)) {
            baseDanger += 50;
        }
        return baseDanger;
    }

}
