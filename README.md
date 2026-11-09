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
mvn exec:java -Dexec.mainClass=RentalTracker
```

**Or directly with the JDK:**

```bash
javac -d target/classes src/main/java/RentalTracker.java
java -cp target/classes RentalTracker
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
