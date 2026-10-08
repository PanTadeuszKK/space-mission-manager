package org.example.model.enums;

public enum ParticipationStatus {
    ACTIVE,
    BACKUP,
    MEDICAL_HOLD,
    INACTIVE;

    public boolean occupiesSeat() {
        return this == ACTIVE || this == BACKUP;
    }
}
