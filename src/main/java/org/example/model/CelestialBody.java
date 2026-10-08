package org.example.model;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class CelestialBody {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Embedded
    private Coordinates stellarCoordinates;

    protected CelestialBody() {
    }

    public CelestialBody(String name, Coordinates stellarCoordinates) {
        setName(name);
        setStellarCoordinates(stellarCoordinates);
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Coordinates getStellarCoordinates() {
        return stellarCoordinates;
    }

    public void setName(String name){
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa ciała niebieskiego nie może być pusta.");
        }
        this.name = name;
    }
    public void setStellarCoordinates(Coordinates stellarCoordinates) {
        if (stellarCoordinates == null) {
            throw new IllegalArgumentException("Współrzędne (Coordinates) są wymagane.");
        }
        this.stellarCoordinates = stellarCoordinates;
    }

    public abstract double calculateDangerLevel();
}
