# Dovewing Car Rentals

A rebuilt fleet and rental tracker that prevents a second active booking for a vehicle within a depot. It keeps the original valid pricing and console behaviour, protects rental records, and separates charging policies so a future vehicle class can supply its own deposit rules.

The application includes an optional rental receipt and a repeatable JUnit regression suite. It is a console demonstration with in-memory data, not an interactive booking or payment system.

## Run the application

Requirements: JDK 21 and Maven. The application has no external runtime dependencies. Maven downloads test-only dependencies when running the suite.

From the repository root:

```powershell
mvn compile
java -cp target/classes com.dovewing.RentalTracker
```

To print receipts for the demonstration's successful bookings:

```powershell
java -cp target/classes com.dovewing.RentalTracker --receipt
```

In IntelliJ, use `com.dovewing.RentalTracker` as the main class. Put `--receipt` in Program arguments only when receipts are wanted.

## Run the regression checks

```powershell
mvn test
```

For a fresh build that also removes old generated classes:

```powershell
mvn clean test
```

**Verified on 11 November 2026:** 43 tests passed, with 0 failures, 0 errors, and 0 skipped. This result covers the implementation at commit `ed70349`, including restored cross-depot history. Reports are generated under the ignored `target/surefire-reports/` directory.

| Test class | Cases | Coverage |
| --- | --- | --- |
| [DepotRegressionTest](src/test/java/com/dovewing/service/DepotRegressionTest.java) | 22 | Four pricing/membership combinations; double-booking rejection without partial changes, consumed IDs, confirmation output, or policy calls; returns and re-rental; protected snapshots; registration and calculated-amount validation; combined history across depots. |
| [ModelValidationTest](src/test/java/com/dovewing/model/ModelValidationTest.java) | 21 | Missing details and objects; membership; negative and non-finite amounts; invalid days; blank policy categories; zero amounts; overflow; ID preservation. |

Parameterized inputs count as separate cases. Tests create fresh fixtures and compare IDs relatively rather than assuming counters start at 1. Execution is sequential because console capture and the application's static counters are shared. Console capture restores System.out even when a check fails. Maven fails if no tests are discovered.

The suite checks the original booking confirmation, but does not automatically compare every report line, receipt field, or a successful future van policy. Historical report comparisons are retained below. See [test instructions](src/test/README.md) for more detail. Tests support correctness; receipts remain the chosen extra business feature.

## Submission contents

| Item | Location |
| --- | --- |
| Ranked diagnosis, root causes, business risks, and original design sketch | [DIAGNOSIS.md](DIAGNOSIS.md) |
| Rebuilt application | [src/main/java/com/dovewing](src/main/java/com/dovewing) |
| Automated regression checks | [src/test](src/test) |
| Final design decisions and proposal reconciliation | Sections below and the final-status note in the diagnosis |
| Original/rebuilt manual comparison outputs | [docs/evidence](docs/evidence) |
| Incremental implementation history | Git history, starting with diagnosis commit `2bbb2ca` before source commit `aa94489` |

Repository: [godfred-aidoo-21/java-basics-b-dovewing-car-rentals](https://github.com/godfred-aidoo-21/java-basics-b-dovewing-car-rentals).

## File structure and responsibilities

```text
src/main/java/com/dovewing/
|-- RentalTracker.java             # Runs the demonstration and optional receipt flag.
|-- model/
|   |-- Vehicle.java               # Stores fleet details and delegates charging calculations.
|   |-- Renter.java                # Stores customer identity and combines read-only history.
|   `-- Rental.java                # Stores an immutable agreement and display snapshots.
|-- service/
|   `-- Depot.java                 # Coordinates registration, booking, return, and availability.
|-- pricing/
|   |-- ChargingRules.java         # Contract for category, cost, and deposit policies.
|   `-- StandardChargingRules.java # Shared economy and premium charging behaviour.
|-- reporting/
|   |-- RentalReport.java          # Formats fleet availability and active rentals.
|   `-- RentalReceipt.java         # Formats one optional rental-agreement receipt.
`-- validation/
    `-- InputValidation.java       # Checks text, amounts, and positive duration.

src/test/
|-- README.md                     # Test commands, scope, and isolation choices.
`-- java/com/dovewing/
    |-- model/ModelValidationTest.java
    `-- service/DepotRegressionTest.java
```

## Final design decisions

### One owner for booking state

Depot owns its rental collection. It rejects another active booking before pricing or record creation. Availability is derived from active records using the vehicle ID, and returns use the same lookup. Removing the separate vehicle-status field avoids contradictory availability values. This enforces the rule within the current single-threaded depot workflow.

### Protected data and immutable agreement values

Fields are private, fixed details are final, and collection access returns unmodifiable snapshots. A return replaces the matching agreement with a returned copy that preserves its ID, participant links, duration, charges, and display details. `Rental.returned()` can be called by another class, but its result cannot change the depot's private records. This avoids public status setters.

### Charging policies instead of more booking branches

Vehicle receives a `ChargingRules` implementation. Economy and premium currently share formulas, so `StandardChargingRules` provides two enum values with shared methods. The constructor accepts the interface rather than that enum specifically.

To add commercial vans, implement `ChargingRules` with the agreed label and calculations, then supply it when constructing a van. Booking, return, and report code need not acquire another category branch. Composition was chosen over vehicle subclasses because the known variation is charging behaviour. The actual van rules have not been supplied and are not invented.

### Stable identity and combined renter history

Rental stores vehicle and renter IDs rather than using names as identity. It also captures readable details for reports, receipts, and history. Duplicate registrations are rejected, while different people may share a name.

`Renter.getHistory()` combines that renter's recorded agreements across all depots where the renter is registered. It sorts by rental ID to retain booking order, includes returned rentals, and returns an unmodifiable snapshot. `Depot.getRenterHistory(renter)` delegates to the same combined history.

`Depot.addRenter()` records the depot association. `Renter.registerDepot()` accepts a source only after that depot has registered the actual renter and avoids duplicate associations. It cannot add arbitrary history strings. This restores the original cross-depot history scope without exposing a writable history list. The tradeoff is a read dependency from Renter back to Depot; there is no separate mutable history ledger to synchronise.

### Separate formatting where it helps

Report and receipt formatting have their own classes. Depot still prints the original booking/return confirmations and delegates its report method, so presentation is not completely isolated from coordination. The separation reduces pricing/reporting coupling while retaining the existing output.

## Additional improvement: optional rental receipts

A receipt gives front-desk staff and customers a clear summary of the agreed renter, vehicle, registration, category, days, cost, and deposit. It summarises a rental agreement, not payment received.

Calling code requests it separately:

```java
Rental rental = depot.rent(kofi, vitz, 3);
RentalReceipt.print(rental);
```

`rent()` returns the recorded agreement; existing source callers can ignore the return value. Receipt printing reads stored values without recalculating pricing or changing records. Money uses two displayed decimal places, while ordinary confirmations retain their original formatting.

The `--receipt` demonstration prints two successful-booking receipts. The conflicting Vitz attempt is rejected and produces no receipt. Without that flag, no receipt output is added. Output goes to the console; no physical-printer integration or payment tracking is provided.

## Behaviour preservation and exact differences

Historical manual captures compare the unchanged-behaviour split starter at `aa94489d0b1b0fe498e93d968ce7964ff880e54d` with rebuild `d23d5ac3db3c6831bbfc0e0b519d9e68c1679d02`. The original single-file source was not committed separately before the split. These captures identify their revisions; they are not presented as captures of every later commit.

| Valid scenario | Preserved result |
| --- | --- |
| Economy, casual, three days | GHS 450 cost; GHS 500 deposit. |
| Premium, member, two days | GHS 1,200 cost; no deposit. |
| Economy, member, two days | GHS 300 cost; no deposit. |
| Premium, casual, one day | GHS 600 cost; GHS 2,000 deposit. |
| Returns and re-rental | Same messages, retained returned agreements, and new booking IDs. |
| Fleet and active-rental reports | Matching captured formatting and ordering. |
| Repeated return of a registered vehicle | Original message; unchanged records when no active agreement exists. |
| Renter history | Original single-depot entries; cross-depot scope restored and checked by the current tests. |

The [original valid transcript](docs/evidence/valid-original.txt) and [rebuilt valid transcript](docs/evidence/valid-rebuilt.txt) match exactly as saved UTF-8 text. Their [original](docs/evidence/valid-scenario-original.java.txt) and [rebuilt](docs/evidence/valid-scenario-rebuilt.java.txt) manual scenario listings are included for inspection.

The [original demonstration](docs/evidence/demo-original.txt) double-books the Vitz. The [rebuilt demonstration](docs/evidence/demo-rebuilt.txt) rejects the conflicting booking, retains Kofi's active agreement, and still prints the report. After the receipt and history changes, the normal demonstration was again checked against the rebuilt transcript and matched; optional mode printed two receipts.

The required correction is duplicate-booking rejection. Other intentional differences are the validation rules below, protected source APIs and package names, and explicitly requested receipt output. Cross-depot renter history is no longer a behavioural departure. Matching these valid scenarios is not a claim of equivalence for invalid inputs newly rejected by validation.

## Validation rules and intentional rejections

These checks address diagnosis item 7 and replace unchecked assumptions or incidental failures with explicit rules. They are additional behaviour changes, not part of the single double-booking correction.

- Names, models, registrations, and policy labels must contain non-whitespace text. Valid text is preserved without trimming or changing case.
- A vehicle needs charging rules. Membership must be exactly `CASUAL` or `MEMBER`; a custom policy may use any non-blank category label.
- Rates, deposits, and calculated amounts must be finite and non-negative. Zero is allowed; negatives, NaN, infinity, and overflow are rejected.
- Duration must be a positive integer number of days. No maximum is imposed.
- Depot registration rejects null entries, exact duplicate vehicle registrations, and repeated registration of the same renter object. Distinct renters can share a name.
- Booking requires the actual registered vehicle and renter objects. Returning requires a registered vehicle, but still preserves the old message for a registered vehicle without an active agreement.
- Direct Rental construction checks participants, duration, and amounts before allocating its ID. Depot membership is enforced by Depot.

Missing object arguments raise `NullPointerException`; invalid values and registration violations raise `IllegalArgumentException`; duplicate active bookings raise `IllegalStateException`. Rejected normal validation paths do not consume entity/rental IDs or modify rental records or history. Policies remain responsible for their own external side effects.

## Reconciliation with the original diagnosis

[DIAGNOSIS.md](DIAGNOSIS.md) is the diagnosis-first proposal, with a clearly labelled final-status addendum. Its original defect analysis and sketch are retained rather than rewritten to look like a prediction of the final code.

The final implementation keeps its core plan: controlled lifecycle operations, derived availability, protected records, separated charging, and reporting. Explicit packages and a shared standard-policy enum make that structure concrete.

Receipts are the selected extra business improvement. Automated tests are also delivered as correctness evidence. Earlier experimental test work was removed before commit; the maintained suite was subsequently committed in `9d9291c` and extended with cross-depot history coverage in `ed70349`. Existing pushed history has not been rewritten or supplemented with invented experimental commits.

The original proposal included automated full-report comparisons and a successful test-only van policy. Those are not part of the focused 43-case suite: historical report evidence is manual, and van extensibility is explained by the policy interface. This distinction avoids claiming coverage that is not delivered.

## Scope and limitations

Data and IDs live in memory and reset when the program restarts. Availability enforcement is per depot; no global vehicle-ownership or concurrent-booking constraint is implemented. Monetary values retain the starter's `double` representation; receipt formatting does not change precision. The supplied model classes have private fields but are not final classes, so arbitrary subclass overrides are outside their guarantees.

The submission includes code, diagnosis/sketch, design reasoning, manual evidence, and executable regression checks. Submit the repository link after committing these documentation changes and pushing the final commits so the reviewer sees the same revision.
