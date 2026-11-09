# Dovewing Car Rentals — Fleet and Rental Tracker

This is the starter repository for the **Java Basics** applied lab. Read the lab
brief alongside this repository; it explains the business, what you are being
asked to deliver, and how your work is assessed.

## What this is

A small program the Dovewing depot uses to track its fleet and its rentals. It works, but it has become risky to change. Your task is to diagnose it
and rebuild it well — **not** to change what it does, but to make it safe to
change and impossible to leave in an invalid state.

## Running it

The program is plain Java (no external dependencies). You need JDK 21.

**With Maven:**

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.dovewing.RentalTracker
```

**Or directly with the JDK:**

```bash
# PowerShell
$sourceFiles = Get-ChildItem src/main/java -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -d target/classes $sourceFiles
java -cp target/classes com.dovewing.RentalTracker
```

Run it first, before you change anything, so you can see how it currently
behaves. Look carefully at the end-of-day report.

## What to hand in

See the lab brief for the full list: a written diagnosis, your rebuilt version
(behaving exactly as the original does today), a short note on your key design
decisions, and one improvement beyond the brief. You will talk through your work
in a live review.

## Input validation and intentional behaviour changes

These safeguards implement diagnosis item 7. The starter accepted these invalid inputs; the rebuild rejects them before recording a rental or printing its confirmation.

- Depot names, vehicle models, registrations, charging-rule category labels, and renter names must contain non-whitespace text. Existing text is preserved without trimming or changing case.
- Vehicles require charging rules. Membership must be exactly CASUAL or MEMBER. A custom charging policy may supply a different non-blank category, including a future van category.
- Daily rates, standard deposits, calculated costs, and calculated deposits must be finite and non-negative. Zero is allowed; negative values, NaN, and infinity are rejected. Overflow in a calculated cost is rejected too.
- Rental durations must be positive whole numbers of days. No maximum duration is imposed.
- Depot registration rejects null entries, duplicate vehicle registrations, and repeated registration of the same renter. Registrations are compared exactly, including case. Distinct renters can share a name.
- Booking requires the actual vehicle and renter to be registered with that depot. Another object with the same name or registration does not substitute for them.
- Returning a vehicle requires it to be registered. For a registered vehicle with no active rental, the existing return message and unchanged records are preserved.
- Direct construction of a rental also validates its participants, duration, and amounts before allocating an ID. Depot membership is checked by the depot, not the rental constructor.

Missing object arguments raise NullPointerException with an explanatory message. Invalid values and registration violations raise IllegalArgumentException. Duplicate active bookings continue to raise IllegalStateException. Failed validation does not consume an entity or rental ID or update depot records or renter history. A custom charging policy remains responsible for its own external side effects; the depot does not record a rental if its calculations fail validation.

Verification for this step compares the demonstration output before and after validation and runs temporary boundary checks. The permanent regression suite is a separate planned step.

## Package structure

Production sources now live under `src/main/java/com/dovewing`:

```text
com/amalitech/dovewing/
|-- RentalTracker.java
|-- model/
|   |-- Vehicle.java
|   |-- Renter.java
|   `-- Rental.java
|-- service/
|   `-- Depot.java
|-- pricing/
|   |-- ChargingRules.java
|   `-- StandardChargingRules.java
|-- reporting/
|   `-- RentalReport.java
`-- validation/
    `-- InputValidation.java
```

This extends the diagnosis's separation of responsibilities into explicit packages. Cross-package types and stateless validation methods are public; fields and depot collections remain private.

Renter history is now derived from the depot's immutable rental records. Call `depot.getRenterHistory(renter)` instead of `renter.getHistory()`. Each rental retains the vehicle model at booking time so history keeps the same entries and ordering, including completed rentals. There is no public history-writing method. History is scoped to the queried depot; the demonstration uses one depot. An application using several depots would need to combine their histories explicitly.

`Rental.returned()` publicly returns a new immutable value; it cannot update the depot or an existing record. Only `Depot.returnVehicle()` replaces the stored rental. This preserves encapsulation across the model/service package boundary without exposing a status setter.

The application entry point is now `com.dovewing.RentalTracker`. A future regression suite will mirror these packages under `src/test/java/`. Testing remains on hold; this restructuring is checked by compilation and comparison of demonstration output.
