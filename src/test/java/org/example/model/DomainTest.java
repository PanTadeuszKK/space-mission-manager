package org.example.model;

import org.example.model.enums.ParticipationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DomainTest {
    private static final LocalDate DAY = LocalDate.of(2030, 1, 1);
    private Pilot pilot() { return new Pilot("Ada", "Nowak", DAY.minusYears(30), "A"); }
    private Mission mission(String name, int start, int end, int capacity) {
        return new Mission(name, DAY.plusDays(start), DAY.plusDays(end), new Spaceship(name, capacity));
    }
    private CrewAssignment assign(Mission m, CrewMember c, ParticipationStatus status) {
        return new CrewAssignment(m, c, DAY, status, 0, UUID.randomUUID().toString());
    }

    @Test void rejectsEveryOverlapIncludingSharedBoundary() {
        for (int[] range : new int[][] {{5,15}, {-5,5}, {-5,15}, {2,8}, {0,10}, {10,20}, {-10,0}}) {
        int start = range[0], end = range[1];
        Pilot member = pilot();
        assign(mission("first", 0, 10, 2), member, ParticipationStatus.ACTIVE);
        Mission second = mission("second", start, end, 2);
        assertThrows(IllegalStateException.class, () -> assign(second, member, ParticipationStatus.BACKUP));
        assertTrue(second.getAssignments().isEmpty());
        assertEquals(1, member.getAssignments().size());
        }
    }

    @Test void permitsSeparateDatesAndDifferentMembers() {
        Pilot member = pilot();
        Mission first = mission("first", 0, 10, 2);
        assign(first, member, ParticipationStatus.ACTIVE);
        assign(first, pilot(), ParticipationStatus.ACTIVE);
        assign(mission("next", 11, 20, 1), member, ParticipationStatus.BACKUP);
        assertEquals(2, first.getAssignments().size());
        assertEquals(2, member.getAssignments().size());
    }

    @Test void validatesDuplicatesCapacityAndStatusTransitions() {
        Mission m = mission("limited", 0, 10, 1);
        Pilot member = pilot();
        CrewAssignment first = assign(m, member, ParticipationStatus.BACKUP);
        assertThrows(IllegalStateException.class, () -> assign(m, member, ParticipationStatus.INACTIVE));
        CrewAssignment standby = assign(m, pilot(), ParticipationStatus.MEDICAL_HOLD);
        assertThrows(IllegalStateException.class, () -> standby.setStatus(ParticipationStatus.ACTIVE));
        assertEquals(ParticipationStatus.MEDICAL_HOLD, standby.getStatus());
        first.setStatus(ParticipationStatus.INACTIVE);
        standby.setStatus(ParticipationStatus.ACTIVE);
        assertThrows(IllegalStateException.class, () -> m.getSpacecraft().setCrewCapacity(0));
    }

    @Test void reactivationChecksOtherMissions() {
        Pilot member = pilot();
        assign(mission("active", 0, 10, 1), member, ParticipationStatus.ACTIVE);
        CrewAssignment inactive = assign(mission("inactive", 0, 10, 1), member, ParticipationStatus.INACTIVE);
        assertThrows(IllegalStateException.class, () -> inactive.setStatus(ParticipationStatus.BACKUP));
        assertEquals(ParticipationStatus.INACTIVE, inactive.getStatus());
    }

    @Test void removesBothSidesAndProtectsMissionEdits() {
        Mission m = mission("mission", 0, 10, 1);
        Pilot member = pilot();
        CrewAssignment a = assign(m, member, ParticipationStatus.ACTIVE);
        assertThrows(IllegalStateException.class, () -> m.setDates(DAY, DAY.plusDays(20)));
        assertThrows(IllegalStateException.class, () -> m.setSpacecraft(new Spaceship("other", 5)));
        a.remove(); a.remove();
        assertTrue(m.getAssignments().isEmpty());
        assertTrue(member.getAssignments().isEmpty());
        m.setDates(DAY, DAY.plusDays(20));
    }

    @Test void modulesAreUniqueAndCannotBeShared() {
        Spaceship ship = new Spaceship("original", 2);
        ShipModule module = new ShipModule("serial", "engine", ship);
        ship.addModule(module);
        assertEquals(1, ship.getModules().size());
        assertThrows(IllegalStateException.class, () -> new Spaceship("other", 2).addModule(module));
        ship.setRegistryNumber("updated");
        assertEquals("updated", ship.getRegistryNumber());
    }

    @Test void equipmentTransferUpdatesBothSidesWithoutDuplicates() {
        Pilot first = pilot(), second = pilot();
        Equipment equipment = new Equipment("serial", "sensor", 1);
        first.addEquipment(equipment); first.addEquipment(equipment);
        assertEquals(1, first.getEquipment().size());
        second.addEquipment(equipment);
        assertTrue(first.getEquipment().isEmpty());
        assertSame(second, equipment.getAssignedTo());
        first.removeEquipment(equipment);
        assertSame(second, equipment.getAssignedTo());
        second.removeEquipment(equipment);
        assertNull(equipment.getAssignedTo());
        assertTrue(second.getEquipment().isEmpty());
    }

    @Test void constructorsEnforceValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Spaceship(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> new Spaceship("S", -1));
        assertThrows(IllegalArgumentException.class, () -> new Pilot(" ", "N", DAY.minusYears(30), "A"));
        assertThrows(IllegalArgumentException.class, () -> new Pilot("A", "N", null, "A"));
        assertThrows(IllegalArgumentException.class, () -> new Pilot("A", "N", LocalDate.now().plusDays(1), "A"));
        assertThrows(IllegalArgumentException.class, () -> new Pilot("A", "N", DAY.minusYears(30), " "));
        assertThrows(IllegalArgumentException.class, () -> new Commander("A", "N", DAY.minusYears(30), 2));
        assertThrows(IllegalArgumentException.class, () -> new MissionSpecialist("A", "N", DAY.minusYears(30), 14));
        assertThrows(IllegalArgumentException.class, () -> new Equipment("S", "sensor", Double.NaN));
        Mission m = mission("M", 0, 1, 1);
        Pilot p = pilot();
        assertThrows(IllegalArgumentException.class, () -> new CrewAssignment(m, p, DAY,
                ParticipationStatus.ACTIVE, Double.POSITIVE_INFINITY, "P"));
        assertTrue(m.getAssignments().isEmpty());
        assertTrue(p.getAssignments().isEmpty());
    }
}
