# Verification — 2026-10-08

## Result

The JUnit Jupiter 5.12.1 engine executed 13 tests: 13 passed, 0 failed, 0 skipped. Tests ran headlessly on Java 17.0.18, Hibernate ORM 6.4.4.Final and H2 2.2.224, using only cached local libraries.

Eight domain tests cover constructors, relationships, assignment statuses and scheduling (including seven overlap/boundary cases). Five integration tests cover persistence and deletion, rejected operations, rollback after a real SQL constraint failure, equipment transfer, and two concurrent attempts to reserve one remaining seat.

The SQL error logged during the rollback test is intentional: the test temporarily narrows the boarding-pass column in its disposable database, verifies rollback, restores the column and successfully adds a new assignment.

## Build limitations

A full Maven build was not verified. Offline Maven stopped because dependencies of maven-resources-plugin were absent from the local cache. No dependencies were downloaded.

Direct Java compilation generated the application and test classes, but the restricted Windows environment reported AccessDeniedException while closing JAR file systems and resolving real paths. Consequently the compiler process did not report a clean successful exit. The generated classes were packaged into a temporary verification JAR and the actual JUnit engine executed the tests successfully.

The temporary verification runner and artifacts live under the ignored target directory and are not application source code. A conventional mvn test / mvn package run on an unrestricted development environment remains necessary before publication.

## Not verified

- Interactive GUI operation, rendering and responsiveness.
- Interactive launch through IntelliJ and the packaged artifact.

The optional exec-maven-plugin was subsequently removed because the IDE could not resolve it. The documented launch path now uses IntelliJ to run org.example.Main.
- Migration of an existing on-disk H2 database; tests used new in-memory databases.
- Cross-process concurrency beyond the two-thread integration test.
- Original authorship, team contributions, asset rights and license.

No application database was opened, no software was installed and nothing was published.
