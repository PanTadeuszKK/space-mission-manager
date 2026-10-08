package org.example.model;
import jakarta.persistence.*;
import org.example.model.enums.DisciplineType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class CrewMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private LocalDate dateOfBirth;

    @Column(nullable = true)
    private String factionAlias;

    private static int minimumFlightHours = 100;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DisciplineType> scientificDisciplines = EnumSet.noneOf(DisciplineType.class);

    private String specializedFlora; //BIOLOGIST
    private String certifiedSystems; //MECHANIC
    private String medicalLicenseNumber; //DOCTOR


    @OneToMany(mappedBy = "assignedTo", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Equipment> specializedEquipment = new ArrayList<>();


    @OneToMany(mappedBy = "crewMember", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrewAssignment> assignments = new ArrayList<>();

    protected CrewMember() {
    }

    public CrewMember(String firstName, String lastName, LocalDate dateOfBirth) {
        setFirstName(firstName);
        setLastName(lastName);
        setDateOfBirth(dateOfBirth);
    }

    public void addDiscipline(DisciplineType type) {
        if (type != null) {
            scientificDisciplines.add(type);
        }
    }

    public void removeDiscipline(DisciplineType type) {
        if (type == null) throw new IllegalArgumentException("Dyscyplina jest wymagana.");
        scientificDisciplines.remove(type);
        switch (type) {
            case BIOLOGIST -> setSpecializedFlora(null);
            case MECHANIC -> setCertifiedSystems(null);
            case DOCTOR -> setMedicalLicenseNumber(null);
        }
    }

    public void setSpecializedFlora(String flora) {
        if (flora != null && !scientificDisciplines.contains(DisciplineType.BIOLOGIST)) {
            throw new IllegalStateException("Nie można przypisać specjalistycznej flory osobie, która nie jest biologiem.");
        }
        this.specializedFlora = flora;
    }

    public void setCertifiedSystems(String systems) {
        if (systems != null && !scientificDisciplines.contains(DisciplineType.MECHANIC)) {
            throw new IllegalStateException("Nie można przypisać certyfikowanych systemów osobie, która nie jest mechanikiem.");
        }
        this.certifiedSystems = systems;
    }

    public void setMedicalLicenseNumber(String license) {
        if (license != null && !scientificDisciplines.contains(DisciplineType.DOCTOR)) {
            throw new IllegalStateException("Nie można przypisać numeru licencji medycznej osobie, która nie jest lekarzem.");
        }
        this.medicalLicenseNumber = license;
    }

    public static void updateMinimumFlightHours(int newHours) {
        if (newHours < 0) {
            throw new IllegalArgumentException("Wymagane godziny lotu nie mogą być ujemne.");
        }
        minimumFlightHours = newHours;
    }

    public static int getMinimumFlightHours() {
        return minimumFlightHours;
    }

    public Long getId() {
        return id;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public String getFactionAlias() {
        return factionAlias;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Imię nie może być puste.");
        }
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwisko nie może być puste.");
        }
        this.lastName = lastName;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data urodzenia jest wymagana i nie może być przyszła.");
        }
        this.dateOfBirth = dateOfBirth;
    }
    public void setFactionAlias(String factionAlias) {
        this.factionAlias = factionAlias;
    }

    public Set<DisciplineType> getScientificDisciplines() {
        return Set.copyOf(scientificDisciplines);
    }

    public String getSpecializedFlora() {
        return specializedFlora;
    }

    public String getCertifiedSystems() {
        return certifiedSystems;
    }

    public String getMedicalLicenseNumber() {
        return medicalLicenseNumber;
    }

    public List<CrewAssignment> getAssignments() {
        return java.util.Collections.unmodifiableList(assignments);
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

    public String getName(){
        return firstName + " " + lastName;
    }

    public void addEquipment(Equipment equipment) {
        java.util.Objects.requireNonNull(equipment, "Sprzęt jest wymagany.").setAssignedTo(this);
    }

    public void removeEquipment(Equipment equipment) {
        if (equipment != null && equipment.getAssignedTo() == this) equipment.setAssignedTo(null);
    }

    void addEquipmentInternal(Equipment equipment) {
        if (!specializedEquipment.contains(equipment)) specializedEquipment.add(equipment);
    }

    void removeEquipmentInternal(Equipment equipment) {
        specializedEquipment.remove(equipment);
    }

    public List<Equipment> getEquipment() {
        return java.util.Collections.unmodifiableList(specializedEquipment);
    }

    @Override
    public String toString() {
        String rola = this.getClass().getSimpleName();
        int rokUrodzenia = (dateOfBirth != null) ? dateOfBirth.getYear() : 0;

        return rola + " " + firstName + " " + lastName + " " + rokUrodzenia;
    }
}
