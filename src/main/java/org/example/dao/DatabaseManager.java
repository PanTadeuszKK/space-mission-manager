package org.example.dao;

import org.example.model.CrewMember;
import org.example.model.Mission;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class DatabaseManager {

    private static SessionFactory sessionFactory;

    public static void init() {
        if (sessionFactory != null && !sessionFactory.isClosed()) return;
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
            System.out.println("Hibernate zainicjowany pomyślnie!");
        } catch (RuntimeException ex) {
            System.err.println("Błąd inicjalizacji SessionFactory: " + ex);
            throw new IllegalStateException("Nie udało się uruchomić bazy danych.", ex);
        }
    }

    public static Session getSession() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            throw new IllegalStateException("Baza danych nie jest zainicjalizowana.");
        }
        return sessionFactory.openSession();
    }

    public static void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }


    public static List<Mission> getAllMissions() {
        try (Session session = getSession()) {
            return session.createQuery("FROM Mission ORDER BY missionCodename", Mission.class).list();
        }
    }

    public static Mission getMissionWithAssignments(Long missionId) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT m FROM Mission m LEFT JOIN FETCH m.assignments a "
                            + "LEFT JOIN FETCH a.crewMember WHERE m.id = :id", Mission.class)
                    .setParameter("id", missionId).uniqueResult();
        }
    }

    public static List<CrewMember> getAllAvailableCrew() {
        try (Session session = getSession()) {
            return session.createQuery("FROM CrewMember ORDER BY lastName, firstName", CrewMember.class).list();
        }
    }
}