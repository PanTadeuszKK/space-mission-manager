package org.example.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Entity
public class Spaceship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registryNumer", unique = true, nullable = false)
    private String registryNumber;

    private int crewCapacity;

    @OneToMany(mappedBy = "spacecraft")
    private List<Mission> missions = new ArrayList<>();

    @OneToMany(mappedBy = "spaceship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipModule> modules = new ArrayList<>();

    public Spaceship() {
    }

    public Spaceship(String registryNumber, int crewCapacity) {
        setRegistryNumber(registryNumber);
        setCrewCapacity(crewCapacity);
    }

    public void addModule(ShipModule module) {
        Objects.requireNonNull(module, "Moduł nie może być nullem.");

        if (module.getSpaceship() != null && module.getSpaceship() != this) {
            throw new IllegalStateException("Moduł należy już do innego statku.");
        }
        if (modules.contains(module)) return;
        modules.add(module);
        module.setSpaceship(this);
    }

    public void removeModule(ShipModule module) {
        if (module != null && modules.contains(module)) {
            modules.remove(module);
            module.setSpaceship(null);
        }
    }

    public Long getId() {
        return id;
    }

    public String getRegistryNumber() {
        return registryNumber;
    }

    public int getCrewCapacity() {
        return crewCapacity;
    }

    public void setRegistryNumber(String registryNumber) {
        if (registryNumber == null || registryNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Numer rejestracyjny statku nie może być pusty.");
        }
        this.registryNumber = registryNumber;
    }

    public void setCrewCapacity(int crewCapacity) {
        if (crewCapacity < 0) {
            throw new IllegalArgumentException("Pojemność statku nie może być mniejsza od zera. Podano: " + crewCapacity);
        }
        boolean tooSmall = missions.stream().anyMatch(m -> m.getAssignments().stream()
                .filter(a -> a.getStatus().occupiesSeat()).count() > crewCapacity);
        if (tooSmall) throw new IllegalStateException("Pojemność jest mniejsza niż przypisana załoga.");
        this.crewCapacity = crewCapacity;
    }

    void addMissionInternal(Mission mission) {
        if (!missions.contains(mission)) missions.add(mission);
    }

    void removeMissionInternal(Mission mission) { missions.remove(mission); }

    public List<ShipModule> getModules() {
        return Collections.unmodifiableList(modules);
    }
}
