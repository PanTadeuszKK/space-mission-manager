package org.example.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String missionCodename;

    @Column(nullable = false)
    private LocalDate launchDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "spaceship_id", nullable = false)
    private Spaceship spacecraft;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrewAssignment> assignments = new ArrayList<>();

    protected Mission() {
    }

    public Mission(String missionCodename, LocalDate launchDate, LocalDate endDate, Spaceship spacecraft) {
        setMissionCodename(missionCodename);
        setDates(launchDate, endDate);
        setSpacecraft(spacecraft);
    }

    public int getMissionDurationDays() {
        if (launchDate == null || endDate == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(launchDate, endDate);
    }

    public Long getId() {
        return id;
    }
    public String getMissionCodename() {
        return missionCodename;
    }
    public LocalDate getLaunchDate() {
        return launchDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public Spaceship getSpacecraft() {
        return spacecraft;
    }

    public void setMissionCodename(String missionCodename) {
        if (missionCodename == null || missionCodename.trim().isEmpty()) {
            throw new IllegalArgumentException("Kryptonim misji nie może być pusty.");
        }
        this.missionCodename = missionCodename;
    }
    public void setDates(LocalDate launchDate, LocalDate endDate) {
        if (launchDate == null || endDate == null) {
            throw new IllegalArgumentException("Obie daty (startu i zakończenia misji) są wymagane.");
        }
        if (endDate.isBefore(launchDate)) {
            throw new IllegalArgumentException("Data zakończenia misji nie może być wcześniejsza niż data startu.");
        }
        if (!assignments.isEmpty()
                && (!launchDate.equals(this.launchDate) || !endDate.equals(this.endDate))) {
            throw new IllegalStateException("Usuń przydziały przed zmianą terminów misji.");
        }
        this.launchDate = launchDate;
        this.endDate = endDate;
    }
    public void setSpacecraft(Spaceship spacecraft) {
        if (spacecraft == null) {
            throw new IllegalArgumentException("Statek kosmiczny jest wymagany do zdefiniowania misji.");
        }
        if (this.spacecraft == spacecraft) return;
        if (!assignments.isEmpty()) {
            throw new IllegalStateException("Usuń przydziały przed zmianą statku.");
        }
        if (this.spacecraft != null) this.spacecraft.removeMissionInternal(this);
        this.spacecraft = spacecraft;
        spacecraft.addMissionInternal(this);
    }


    public List<CrewAssignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    void addAssignmentInternal(CrewAssignment assignment) {
        if (assignment != null && !assignments.contains(assignment)) {
            this.assignments.add(assignment);
        }
    }

    void removeAssignmentInternal(CrewAssignment assignment) {
        if (assignment != null) {
            this.assignments.remove(assignment);
        }
    }

    @Override
    public String toString() {
        return missionCodename + " (" + getLaunchDate() + ")";
    }
}