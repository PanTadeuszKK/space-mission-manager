package org.example.model;


import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class MissionSpecialist extends CrewMember{
    private int evaHours;

    protected MissionSpecialist() {
        super();
    }

    public MissionSpecialist(String firstName, String lastName, LocalDate dateOfBirth, int evaHours) {
        super(firstName, lastName, dateOfBirth);
        setEvaHours(evaHours);
    }


    public int getEvaHours() {
        return evaHours;
    }

    public void setEvaHours(int evaHours) {
        if (evaHours < 15) {
            throw new IllegalArgumentException("Wymagane jest 15 godzin na tym stanowisku. Podano: " + evaHours);
        }
        this.evaHours = evaHours;
    }
}
