package org.example.service;

import org.example.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CrewAssignmentServiceTest {
    private SessionFactory factory;
    private CrewAssignmentService service;
    private Long missionId, otherMissionId, memberId, otherMemberId;

    @BeforeEach void setup() {
        factory = new Configuration().configure()
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:test_" + UUID.randomUUID())
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "false")
                .buildSessionFactory();
        service = new CrewAssignmentService(factory::openSession);
        try (Session s = factory.openSession()) {
            var tx = s.beginTransaction();
            Spaceship ship = new Spaceship("TEST", 1);
            new ShipModule("engine", "Engine", ship);
            s.persist(ship);
            Mission m = new Mission("M1", LocalDate.of(2030,1,1), LocalDate.of(2030,1,10), ship);
            Mission other = new Mission("M2", LocalDate.of(2030,1,5), LocalDate.of(2030,1,20), ship);
            Pilot p = new Pilot("Ada", "Nowak", LocalDate.of(1990,1,1), "A");
            Pilot p2 = new Pilot("Jan", "Nowak", LocalDate.of(1990,1,1), "B");
            s.persist(m); s.persist(other); s.persist(p); s.persist(p2);
            tx.commit();
            missionId = m.getId(); otherMissionId = other.getId();
            memberId = p.getId(); otherMemberId = p2.getId();
        }
    }

    @AfterEach void close() { if (factory != null) factory.close(); }

    @Test void persistsReloadsAndRemovesAssignment() {
        service.add(missionId, memberId);
        Long assignmentId;
        try (Session s = factory.openSession()) {
            Mission m = s.get(Mission.class, missionId);
            assertEquals(1, m.getAssignments().size());
            CrewAssignment a = m.getAssignments().get(0);
            assignmentId = a.getId();
            assertEquals(memberId, a.getCrewMember().getId());
            assertEquals(1, a.getCrewMember().getAssignments().size());
            assertEquals(1, m.getSpacecraft().getModules().size());
        }
        service.remove(assignmentId);
        try (Session s = factory.openSession()) {
            assertNull(s.get(CrewAssignment.class, assignmentId));
            assertTrue(s.get(Mission.class, missionId).getAssignments().isEmpty());
            assertTrue(s.get(CrewMember.class, memberId).getAssignments().isEmpty());
            assertNotNull(s.get(CrewMember.class, memberId));
        }
        service.add(missionId, otherMemberId);
        assertEquals(1L, count());
    }

    @Test void rejectsDuplicateFullMissionAndOverlappingMission() {
        service.add(missionId, memberId);
        assertThrows(IllegalStateException.class, () -> service.add(missionId, memberId));
        assertThrows(IllegalStateException.class, () -> service.add(missionId, otherMemberId));
        assertThrows(IllegalStateException.class, () -> service.add(otherMissionId, memberId));
        assertEquals(1L, count());
    }

    @Test void rollsBackDatabaseFailureAndAllowsNextOperation() {
        alterPassCodeColumn(4);
        assertThrows(org.hibernate.HibernateException.class, () -> service.add(missionId, memberId));
        assertEquals(0L, count());
        alterPassCodeColumn(255);
        service.add(missionId, memberId);
        assertEquals(1L, count());
    }

    private void alterPassCodeColumn(int length) {
        try (Session s = factory.openSession()) {
            var tx = s.beginTransaction();
            s.createNativeMutationQuery("ALTER TABLE CrewAssignment ALTER COLUMN boardingPassCode VARCHAR("
                    + length + ")").executeUpdate();
            tx.commit();
        }
    }

    @Test void concurrentAddsCannotOverfillMission() throws Exception {
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        var start = new java.util.concurrent.CountDownLatch(1);
        try {
            java.util.concurrent.Callable<Boolean> first = () -> tryAdd(start, memberId);
            java.util.concurrent.Callable<Boolean> second = () -> tryAdd(start, otherMemberId);
            var a = executor.submit(first);
            var b = executor.submit(second);
            start.countDown();
            int successes = (a.get(15, java.util.concurrent.TimeUnit.SECONDS) ? 1 : 0)
                    + (b.get(15, java.util.concurrent.TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, successes);
            assertEquals(1L, count());
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean tryAdd(java.util.concurrent.CountDownLatch start, Long member) throws Exception {
        start.await();
        try { service.add(missionId, member); return true; }
        catch (IllegalStateException expected) { return false; }
    }

    @Test void equipmentTransferSurvivesReload() {
        Long equipmentId;
        try (Session s = factory.openSession()) {
            var tx = s.beginTransaction();
            Equipment equipment = new Equipment("sensor", "Sensor", 2);
            s.get(CrewMember.class, memberId).addEquipment(equipment);
            s.persist(equipment);
            tx.commit(); equipmentId = equipment.getId();
        }
        try (Session s = factory.openSession()) {
            var tx = s.beginTransaction();
            s.get(CrewMember.class, otherMemberId).addEquipment(s.get(Equipment.class, equipmentId));
            tx.commit();
        }
        try (Session s = factory.openSession()) {
            assertTrue(s.get(CrewMember.class, memberId).getEquipment().isEmpty());
            assertEquals(1, s.get(CrewMember.class, otherMemberId).getEquipment().size());
            assertEquals(otherMemberId, s.get(Equipment.class, equipmentId).getAssignedTo().getId());
        }
    }

    private long count() {
        try (Session s = factory.openSession()) {
            return s.createQuery("SELECT count(a) FROM CrewAssignment a", Long.class).getSingleResult();
        }
    }
}
