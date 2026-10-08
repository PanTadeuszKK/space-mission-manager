package org.example;

import org.example.dao.DatabaseManager;
import org.example.gui.MainWindow;
import org.example.model.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import javax.swing.*;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::close, "database-shutdown"));
        try {
            DatabaseManager.init();
            seedData();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            DatabaseManager.close();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Nie udało się uruchomić aplikacji: " + ex.getMessage(),
                    "Błąd uruchomienia", JOptionPane.ERROR_MESSAGE));
            return;
        }

        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }

    private static void seedData() {
        try (Session session = DatabaseManager.getSession()) {
            if (session.createQuery("SELECT count(*) FROM Mission", Long.class).uniqueResult() == 0) {
                Transaction tx = session.beginTransaction();

                try {
                    Spaceship ship = new Spaceship("ORION-X", 5);
                    new ShipModule("ENG-99", "Nuclear Engine", ship);
                    session.persist(ship);

                    Spaceship ship2 = new Spaceship("Orion-y", 10);
                    new ShipModule("UIQ-01", "Habitable", ship2);
                    session.persist(ship2);

                    Commander cmd = new Commander("Jan", "Twardowski", LocalDate.of(1980, 5, 20), 10);
                    Pilot pilot = new Pilot("Elena", "Vance", LocalDate.of(1992, 3, 15), "Class-A Shuttle");
                    MissionSpecialist missionSpecialist = new MissionSpecialist("Ian", "Smith", LocalDate.of(1982,9,2), 25);
                    Pilot pilot2 = new Pilot("Frank","Sinatra", LocalDate.of(1980, 1, 10),"Class-B Shuttle");

                    session.persist(pilot2);
                    session.persist(missionSpecialist);
                    session.persist(cmd);
                    session.persist(pilot);

                    Mission m1 = new Mission("APOLLO-20", LocalDate.now(), LocalDate.now().plusMonths(3), ship);
                    Mission m3 = new Mission("APOLLO-21", LocalDate.now(), LocalDate.now().plusMonths(5), ship2);
                    Mission m2 = new Mission("MARS-ONE", LocalDate.now().plusYears(1), LocalDate.now().plusYears(2), ship);
                    session.persist(m1);
                    session.persist(m2);
                    session.persist(m3);

                    tx.commit();
                    System.out.println("Dane testowe zostały wygenerowane.");
                } catch (RuntimeException ex) {
                    if (tx.isActive()) tx.rollback();
                    throw ex;
                }
            }
        }
    }
}