package org.example.model;


import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Pilot extends CrewMember{
    private String flightLicense;

    protected Pilot() {
        super();
    }

    public Pilot(String firstName, String lastName, LocalDate dateOfBirth, String flightLicense) {
        super(firstName, lastName, dateOfBirth);
        setFlightLicenses(flightLicense);
    }

    public String getFlightLicenses() {
        return flightLicense;
    }

    public void setFlightLicenses(String flightLicenses) {
        if (flightLicenses == null || flightLicenses.trim().isEmpty()) {
            throw new IllegalArgumentException("Pilot musi posiadać określone licencje lotnicze (pole nie może być puste).");
        }
        this.flightLicense = flightLicenses;
    }
}
