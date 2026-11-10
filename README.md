# Dovewing Car Rentals: Fleet and Rental Tracker

This is the starter repository for the **Java Basics** applied lab. Read the lab
brief alongside this repository; it explains the business, what you are being
asked to deliver, and how your work is assessed.

## What this is

A small program the Dovewing depot uses to track its fleet and its rentals. It works, but it has become risky to change. Your task is to diagnose it
and rebuild it well: **not** to change what it does, but to make it safe to
change and impossible to leave in an invalid state.

## Running it

The application uses plain Java with no external dependencies. You need JDK 21.

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

The validation step was checked by comparing demonstration output and running temporary boundary checks. No automated regression suite is included in the submission.

## Package structure

Production sources now live under `src/main/java/com/dovewing`:

```text
com/dovewing/
|-- RentalTracker.java             # Runs the demo and handles the optional receipt flag.
|-- model/
|   |-- Vehicle.java               # Stores vehicle details and delegates charge calculations.
|   |-- Renter.java                # Stores customer identity and membership details.
|   `-- Rental.java                # Stores an immutable rental agreement and its status.
|-- service/
|   `-- Depot.java                 # Manages registration, bookings, returns, availability, and history.
|-- pricing/
|   |-- ChargingRules.java         # Defines the contract for cost and deposit calculations.
|   `-- StandardChargingRules.java # Supplies the economy and premium charging rules.
|-- reporting/
|   |-- RentalReport.java          # Prints fleet availability and active rentals.
|   `-- RentalReceipt.java         # Prints an optional receipt from recorded rental details.
`-- validation/
    `-- InputValidation.java       # Checks required text, positive durations, and valid amounts.
```

This extends the diagnosis's separation of responsibilities into explicit packages. Cross-package types and stateless validation methods are public; fields and depot collections remain private.

Renter history is now derived from the depot's immutable rental records. Call `depot.getRenterHistory(renter)` instead of `renter.getHistory()`. Each rental retains the vehicle model at booking time so history keeps the same entries and ordering, including completed rentals. There is no public history-writing method. History is scoped to the queried depot; the demonstration uses one depot. An application using several depots would need to combine their histories explicitly.

`Rental.returned()` publicly returns a new immutable value; it cannot update the depot or an existing record. Only `Depot.returnVehicle()` replaces the stored rental. This preserves encapsulation across the model/service package boundary without exposing a status setter.

The application entry point is `com.dovewing.RentalTracker`. The package restructuring was verified by compilation and comparison of demonstration output. Optional rental receipts are the chosen additional improvement.

## Behaviour preservation

Manual comparisons used baseline revision `aa94489d0b1b0fe498e93d968ce7964ff880e54d` and rebuilt revision `d23d5ac3db3c6831bbfc0e0b519d9e68c1679d02`. The baseline is the earliest committed source snapshot: the unchanged starter classes split into files, before business-logic changes. Each scenario ran in a fresh Java process.

| Behaviour | Result in both versions |
| --- | --- |
| Economy, casual renter, three days | GHS 450 cost; GHS 500 deposit. |
| Premium, member renter, two days | GHS 1,200 cost; no deposit. |
| Economy, member renter, two days | GHS 300 cost; no deposit. |
| Premium, casual renter, one day | GHS 600 cost; GHS 2,000 deposit. |
| Returns and re-rental | Same messages; completed records retained; new rental IDs assigned. |
| Reports | Same formatting, fleet order, rental order, and active-rental details. |
| Renter history | Same entries and ordering within the captured single-depot scenario. |
| Repeated return of a registered vehicle | Same message; unrelated active rental remains unchanged. |

The [original valid output](docs/evidence/valid-original.txt) and [rebuilt valid output](docs/evidence/valid-rebuilt.txt) match exactly after capture as UTF-8 text with LF line endings. The [original scenario listing](docs/evidence/valid-scenario-original.java.txt) and [rebuilt listing](docs/evidence/valid-scenario-rebuilt.java.txt) show the manual steps. Their extra history lines expose records for comparison; they do not add application features.

**Intentional correction:** the [original demonstration](docs/evidence/demo-original.txt) double-books the Vitz. The [rebuilt demonstration](docs/evidence/demo-rebuilt.txt) rejects Ama's conflicting booking, retains Kofi's active rental, and still prints the report.

**Other changes:** [input validation](#input-validation-and-intentional-behaviour-changes) introduces additional rejected inputs. The entry point and constructors use the new package and charging-rule APIs. History is read through `depot.getRenterHistory(renter)` and is now scoped to that depot; multi-depot history requires explicit aggregation. These departures are not claims of identical behaviour for every possible input or caller.

These manual captures remain historical comparisons against the original source and do not constitute an automated regression suite. The default demonstration output is preserved; optional receipt output is an explicitly requested extension.

<details>
<summary>Reproduce the manual comparison</summary>

Run the following PowerShell commands from the repository root with Git and a JDK available. They export both recorded revisions into a new ignored target directory, leaving the working sources untouched. The scenario listings are manual drivers without assertions.

```powershell
$replay = Join-Path (Get-Location) ('target/evidence-replay-' + [guid]::NewGuid().ToString('N'))
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Export-JavaSources($revision, $destination) {
    $paths = git ls-tree -r --name-only $revision -- src/main/java
    foreach ($path in $paths) {
        if (-not $path.EndsWith('.java')) { continue }
        $relative = $path.Substring('src/main/java/'.Length)
        $outputPath = Join-Path $destination $relative
        New-Item -ItemType Directory -Path (Split-Path $outputPath) -Force | Out-Null
        $lines = git show ($revision + ':' + $path)
        if ($LASTEXITCODE -ne 0) { throw 'Source export failed.' }
        [System.IO.File]::WriteAllText($outputPath, ($lines -join "`n") + "`n", $utf8)
    }
}

$originalSrc = Join-Path $replay 'original-src'
$rebuiltSrc = Join-Path $replay 'rebuilt-src'
$originalClasses = Join-Path $replay 'original-classes'
$rebuiltClasses = Join-Path $replay 'rebuilt-classes'
Export-JavaSources 'aa94489d0b1b0fe498e93d968ce7964ff880e54d' $originalSrc
Export-JavaSources 'd23d5ac3db3c6831bbfc0e0b519d9e68c1679d02' $rebuiltSrc
Copy-Item docs/evidence/valid-scenario-original.java.txt (Join-Path $originalSrc 'ManualScenario.java')
Copy-Item docs/evidence/valid-scenario-rebuilt.java.txt (Join-Path $rebuiltSrc 'ManualScenario.java')
New-Item -ItemType Directory -Path $originalClasses, $rebuiltClasses -Force | Out-Null

$originalFiles = Get-ChildItem $originalSrc -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
$rebuiltFiles = Get-ChildItem $rebuiltSrc -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -d $originalClasses $originalFiles
if ($LASTEXITCODE -ne 0) { throw 'Original compilation failed.' }
javac -d $rebuiltClasses $rebuiltFiles
if ($LASTEXITCODE -ne 0) { throw 'Rebuilt compilation failed.' }

$originalOutput = java -cp $originalClasses ManualScenario
if ($LASTEXITCODE -ne 0) { throw 'Original scenario failed.' }
$rebuiltOutput = java -cp $rebuiltClasses ManualScenario
if ($LASTEXITCODE -ne 0) { throw 'Rebuilt scenario failed.' }
Compare-Object $originalOutput $rebuiltOutput -SyncWindow 0 -CaseSensitive
# No differences should be displayed.

java -cp $originalClasses RentalTracker
java -cp $rebuiltClasses com.dovewing.RentalTracker
# Inspect the intentional double-booking difference in these two runs.
```

</details>

## Additional improvement: optional rental receipts

The selected improvement beyond the brief is a rental receipt that staff can print after a successful booking. It presents the agreed vehicle, renter, duration, rental cost, and deposit together. It is a rental agreement summary, not proof of payment; the application does not track payments.

Receipts are optional so existing confirmations and reports stay unchanged. Run the usual demonstration without arguments for its existing output. To request receipts for its two successful bookings:

```powershell
mvn compile
java -cp target/classes com.dovewing.RentalTracker --receipt
```

Calling code can also request one explicitly:

```java
Rental rental = depot.rent(kofi, vitz, 3);
RentalReceipt.print(rental);
```

`Depot.rent()` now returns the immutable rental it records. Existing source callers can ignore the return value. `RentalReceipt` lives in the reporting package and only reads the rental's stored details. The category is captured with the rental and retained when it is returned. Costs and deposits are not recalculated or combined into an amount paid. Receipt printing changes no rental records, IDs, history, or availability. Money on the receipt uses two decimal places and a stable decimal point; existing booking messages retain their original formatting.

If booking is rejected, no rental is returned and the receipt call is not reached. The optional demonstration prints two receipts, then handles the conflicting booking as before. A stored receipt may be printed again without creating another booking.

### Departure from the original proposal

DIAGNOSIS.md remains the original pre-build proposal. Its suggested additional improvement was an automated regression suite. During implementation, the owner selected optional receipts instead because they provide a simple booking summary with direct value to front-desk staff. The uncommitted test suite and its JUnit/Maven runner configuration were removed. No regression suite or automated-test completion is claimed as a submission deliverable. Manual evidence remains under Behaviour preservation above.
