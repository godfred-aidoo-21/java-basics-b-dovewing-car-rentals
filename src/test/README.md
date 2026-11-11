# Core rental regression checks

Run from the repository root:

```powershell
mvn test
```

Use `mvn clean test` after replacing an older test suite to remove stale compiled test classes. Maven downloads the test-only JUnit dependency on first use. There are no additional application runtime dependencies.

`DepotRegressionTest` checks the four existing pricing/membership combinations, duplicate-booking rejection without partial changes or policy calls, correct return and re-rental, protected collection snapshots, registration boundaries, invalid durations, and invalid calculated amounts.

`ModelValidationTest` checks missing details, membership types, invalid amounts, null arguments, invalid days, blank policy categories, zero amounts, overflow, and ID preservation.

Tests use fresh fixtures and relative ID assertions. Maven explicitly disables parallel JUnit execution because tests capture System.out and the application uses shared static counters. The output stream is restored in a finally block, including when assertions fail.

Receipts remain the extra business feature. These checks provide repeatable verification rather than replacing that feature. Cross-depot history compatibility and the final submission documentation are separate follow-up steps; this suite does not assert the existing depot-scoped behaviour as the desired cross-depot contract.
