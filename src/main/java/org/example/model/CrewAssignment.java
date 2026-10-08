package org.example.model;

import jakarta.persistence.*;
import org.example.model.enums.ParticipationStatus;

import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mission_id", "crew_member_id"})
})
public class CrewAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate assignmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status;

    private double missionSalaryBonus;

    @Column(nullable = false, unique = true)
    private String boardingPassCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(optional = false)
    @JoinColumn(name = "crew_member_id", nullable = false)
    private CrewMember crewMember;

    protected CrewAssignment() {
    }

    public CrewAssignment(Mission mission, CrewMember crewMember, LocalDate assignmentDate,
                          ParticipationStatus status, double missionSalaryBonus, String boardingPassCode) {

        if (mission == null) throw new IllegalArgumentException("Misja jest wymagana.");
        if (crewMember == null) throw new IllegalArgumentException("Członek załogi jest wymagany.");

        this.mission = mission;
        this.crewMember = crewMember;
        setAssignmentDate(assignmentDate);
        setStatus(status);
        setMissionSalaryBonus(missionSalaryBonus);
        setBoardingPassCode(boardingPassCode);

        this.mission.addAssignmentInternal(this);
        this.crewMember.addAssignmentInternal(this);
    }

    private void validateParticipation(ParticipationStatus proposedStatus) {
        for (CrewAssignment existing : mission.getAssignments()) {
            if (existing != this && sameMember(existing.crewMember, crewMember)) {
                throw new IllegalStateException("Astronauta jest już przypisany do tej misji.");
            }
        }
        if (!proposedStatus.occupiesSeat()) return;
        long seats = mission.getAssignments().stream()
                .filter(a -> a != this && a.status.occupiesSeat()).count();
        if (seats >= mission.getSpacecraft().getCrewCapacity()) {
            throw new IllegalStateException("Brak wolnych miejsc na statku.");
        }
        for (CrewAssignment existing : crewMember.getAssignments()) {
            if (existing == this || !existing.status.occupiesSeat()) continue;
            Mission other = existing.mission;
            if (!mission.getLaunchDate().isAfter(other.getEndDate())
                    && !mission.getEndDate().isBefore(other.getLaunchDate())) {
                throw new IllegalStateException(String.format(
                        "Astronauta ma już przydział do misji [%s] (%s do %s).",
                        other.getMissionCodename(), other.getLaunchDate(), other.getEndDate()));
            }
        }
    }

    private static boolean sameMember(CrewMember a, CrewMember b) {
        return a == b || (a.getId() != null && a.getId().equals(b.getId()));
    }

    public void remove() {
        if (this.mission != null) {
            this.mission.removeAssignmentInternal(this);
            this.mission = null;
        }
        if (this.crewMember != null) {
            this.crewMember.removeAssignmentInternal(this);
            this.crewMember = null;
        }
    }

    public Long getId() {
        return id;
    }
    public Mission getMission() {
        return mission;
    }
    public CrewMember getCrewMember() {
        return crewMember;
    }
    public LocalDate getAssignmentDate() {
        return assignmentDate;
    }
    public ParticipationStatus getStatus() {
        return status;
    }
    public double getMissionSalaryBonus() {
        return missionSalaryBonus;
    }

    public String getBoardingPassCode() {
        return boardingPassCode;
    }

    public void setAssignmentDate(LocalDate assignmentDate) {
        if (assignmentDate == null) {
            throw new IllegalArgumentException("Data przydziału nie może być pusta.");
        }
        this.assignmentDate = assignmentDate;
    }
    public void setStatus(ParticipationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status uczestnictwa jest wymagany.");
        }
        if (mission != null && crewMember != null) validateParticipation(status);
        this.status = status;
    }
    public void setMissionSalaryBonus(double missionSalaryBonus) {
        if (!Double.isFinite(missionSalaryBonus) || missionSalaryBonus < 0) {
            throw new IllegalArgumentException("Premia za misję nie może być ujemna.");
        }
        this.missionSalaryBonus = missionSalaryBonus;
    }

    public void setBoardingPassCode(String boardingPassCode) {
        if (boardingPassCode == null || boardingPassCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Kod karty pokładowej nie może być pusty.");
        }
        this.boardingPassCode = boardingPassCode;
    }

    @Override
    public String toString() {
        return crewMember.getClass().getSimpleName() + ": " + crewMember.getName() +
                " - Status: " + status + " (Kod: " + boardingPassCode + ")";
    }
}