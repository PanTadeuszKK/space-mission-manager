package org.example.model;


import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Commander extends CrewMember{
    public static final int MINIMUM_EXPERIENCE_YEARS = 3;
    private int commandExperienceYears;

    protected Commander() {
        super();
    }
    public Commander(String firstName, String lastName, LocalDate dateOfBirth, int commandExperienceYears) {
        super(firstName, lastName, dateOfBirth);
        setCommandExperienceYears(commandExperienceYears);
    }

    public int getCommandExperienceYears() {
        return commandExperienceYears;
    }

    public void setCommandExperienceYears(int commandExperienceYears) {
        if (commandExperienceYears < 0) {
            throw new IllegalArgumentException("Lata doświadczenia dowódczego nie mogą być ujemne. Podano: " + commandExperienceYears);
        }
        if (commandExperienceYears < MINIMUM_EXPERIENCE_YEARS) {
            throw new IllegalArgumentException("Wymagane doświadczenie: " + MINIMUM_EXPERIENCE_YEARS + " lata.");
        }
        this.commandExperienceYears = commandExperienceYears;
    }
}
