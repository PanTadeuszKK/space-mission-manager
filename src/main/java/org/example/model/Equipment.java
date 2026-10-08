package org.example.model;

import jakarta.persistence.*;

@Entity
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_member_id")
    private CrewMember assignedTo;

    public Equipment() {
    }

    public Equipment(String serialNumber, String name, double weight) {
        setSerialNumber(serialNumber);
        setName(name);
        setWeight(weight);
    }

    public Long getId() {
        return id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.isBlank()) throw new IllegalArgumentException("Numer seryjny jest wymagany.");
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nazwa sprzętu jest wymagana.");
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        if (!Double.isFinite(weight) || weight < 0) throw new IllegalArgumentException("Masa musi być skończona i nieujemna.");
        this.weight = weight;
    }

    public CrewMember getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(CrewMember assignedTo) {
        if (this.assignedTo == assignedTo) return;
        CrewMember previous = this.assignedTo;
        this.assignedTo = assignedTo;
        if (previous != null) previous.removeEquipmentInternal(this);
        if (assignedTo != null) assignedTo.addEquipmentInternal(this);
    }
}
