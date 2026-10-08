package org.example.model;
import jakarta.persistence.*;


@Entity
public class ShipModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serialNumber;

    @Column(nullable = false)
    private String moduleType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "spaceship_id", nullable = false)
    private Spaceship spaceship;

    protected ShipModule() {}

    public ShipModule(String serialNumber, String moduleType, Spaceship spaceship) {
        setSerialNumber(serialNumber);
        setModuleType(moduleType);
        if (spaceship == null) throw new IllegalArgumentException("Statek jest wymagany.");
        spaceship.addModule(this);
    }

    public Long getId() {
        return id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setSerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Numer seryjny modułu nie może być pusty.");
        }
        this.serialNumber = serialNumber;
    }

    public void setModuleType(String moduleType) {
        if (moduleType == null || moduleType.trim().isEmpty()) {
            throw new IllegalArgumentException("Typ modułu nie może być pusty.");
        }
        this.moduleType = moduleType;
    }

    public Spaceship getSpaceship() {
        return spaceship;
    }

    void setSpaceship(Spaceship spaceship) {
        this.spaceship = spaceship;
    }

}
