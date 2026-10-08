package org.example.service;

import org.example.dao.DatabaseManager;
import org.example.model.*;
import org.example.model.enums.ParticipationStatus;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;

/** Owns the transaction boundary; callers receive no session-bound result. */
public final class CrewAssignmentService {
    private final Supplier<Session> sessions;

    public CrewAssignmentService() {
        this(DatabaseManager::getSession);
    }

    public CrewAssignmentService(Supplier<Session> sessions) {
        this.sessions = java.util.Objects.requireNonNull(sessions);
    }

    public void add(Long missionId, Long memberId) {
        inTransaction(session -> {
            Mission mission = session.get(Mission.class, missionId);
            if (mission == null) throw new IllegalArgumentException("Misja nie istnieje.");
            // Fixed lock order: ship, then member. Serialize capacity and schedule checks.
            session.refresh(mission.getSpacecraft(), LockMode.PESSIMISTIC_WRITE);
            CrewMember member = session.get(CrewMember.class, memberId, LockMode.PESSIMISTIC_WRITE);
            if (member == null) throw new IllegalArgumentException("Astronauta nie istnieje.");
            CrewAssignment assignment = new CrewAssignment(mission, member, LocalDate.now(),
                    ParticipationStatus.ACTIVE, 6500.0, "PASS-" + UUID.randomUUID());
            session.persist(assignment);
        });
    }

    public void remove(Long assignmentId) {
        inTransaction(session -> {
            CrewAssignment assignment = session.get(CrewAssignment.class, assignmentId);
            if (assignment == null) throw new IllegalArgumentException("Przydział nie istnieje.");
            session.refresh(assignment.getMission().getSpacecraft(), LockMode.PESSIMISTIC_WRITE);
            session.refresh(assignment.getCrewMember(), LockMode.PESSIMISTIC_WRITE);
            session.remove(assignment);
            assignment.remove();
        });
    }

    private void inTransaction(java.util.function.Consumer<Session> operation) {
        try (Session session = sessions.get()) {
            Transaction transaction = session.beginTransaction();
            try {
                operation.accept(session);
                transaction.commit();
            } catch (RuntimeException ex) {
                if (transaction.isActive()) {
                    try { transaction.rollback(); } catch (RuntimeException rollbackError) {
                        ex.addSuppressed(rollbackError);
                    }
                }
                throw ex;
            }
        }
    }
}
