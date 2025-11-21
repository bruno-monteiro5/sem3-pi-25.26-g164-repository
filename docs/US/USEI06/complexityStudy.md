# Complexity Study - BuildIndexesControler.java

## 1. Identification
**File:** `BuildIndexesController.java` && `StationRepository.java`
**Description:** Class responsible for handling the processes requested in USEI06. Among these are the loading of all stations from a csv into a List of `Station` objects, the query by latitude, Logitude, Time Zone Group and Country. 

---

## 2. General Code Structure
The `BuildIndexesController` class includes:
- A method that runs its class:
    - Retrieves and processes Station information from a CSV file.
    - Calls operations based on the `StationRepository.java` file.

The `StationRepository` class includes:
- A method that runs its class:
    - Stores all stations in a `StationIndexes` object.
    - Handles operations based on the given information in the `BuildIndexesController.java` file.

The code follows an **iterative** structure, with no explicit recursion. The overall structure involves processing return entries sequentially.

---

## 3. Time Complexity Analysis

### 3.1 Main Structure (Inspection)
**Complexity:**

Loading return entries from the CSV file:
- Reads and enqueues all return entries from the `warehouseMngt/train_station_dataset/train_stations_europe.csv`.

**Complexity:**
- Best case: O(n) — where `n` is the number of rows in the file, all correctly formatted.
- Worst case: O(n) — including validation and parsing for all rows.
- Average case: O(n) — accounting for normal formatting and validation.

Processing the entries in the list of station information:
- For each entry `n`, the following operations are conducted:
    - Previewing an entry: **O(1)**.
    - Decision-making:
        - Add to list of stations: **O(1)**.

**Total Complexity:** O(n) for `n` entries being processed.

---

### 3.2 Controller Operations
|   | Method                                             | Operation                                                                                         | Complexity |
|---|----------------------------------------------------|---------------------------------------------------------------------------------------------------|------------|
| 1 | `BuildIndexesController.buildFromCsvPath`          | Loading of station information                                                                    | **O(n)**   |
| 2 | `BuildIndexesController.queryByLatitude`           | Query the `StationRepository.findByLatitudeWindow` class                                          | **O(1)**   |
| 3 | `StationRepository.findByLatitudeWindow`           | Query the `StationIndexes` class to return a list of stations in that latitudew window            | **O(n)**   |
| 4 | `BuildIndexesController.queryByLongitude`          | Query the `StationRepository.findByLongitudeWindow` class                                         | **O(1)**   |
| 5 | `StationRepository.findByLongitudeWindow`          | Query the `StationIndexes` class to return a list of stations in that longitude window            | **O(n)**   |
| 6 | `BuildIndexesController.queryByTimeZoneGroup`      | Query the `StationRepository.findByTimeZoneGroup` class                                           | **O(1)**   |
| 7 | `StationRepository.findByTimeZoneGroup`            | Query the `StationIndexes` class to return a list of stations in that time zone group             | **O(n)**   |
| 8 | `BuildIndexesController.queryByTimeZoneAndCountry` | Query the `StationRepository.findByTimeZoneAndCountry` class                                      | **O(1)**   |
| 9 | `StationRepository.findByTimeZoneAndCountry`       | Query the `StationIndexes` class to return a list of stations in that country and time zone group | **O(n)**   |

The above methods depend on the dimension the csv file with the Station information and, consequentially, the storage `StationIndexes` class.

**Complexity (per entry): O(n).**

### 3.3 Verification or Sorting Method
- No sorting operations are performed on the station entries, as they are processed sequentially in the order received.

---

## 4. Space Complexity Analysis
The program uses:
- A collection (likely a queue) in the `StationRepository` class to store the Station information.

**Space Complexity:**
- Storing `n` return entries: O(n).
- Simple variables and temporary objects: O(1).  
  **Total:** O(n).

---

## 5. Case Analysis
| Case             | Description                                               | Complexity |
|------------------|-----------------------------------------------------------|------------|
| **Best case**    | Small, well-formatted CSV, simple entry processing        | O(n)       |
| **Average case** | Moderate `n`, mix of decision types and normal processing | O(n)       |
| **Worst case**   | Large file with extensive data validation or edge cases   | O(n)       |

---

## 6. Conclusion
The `BuildIndexesController.java` and the `StationRepository.java` files present a **time complexity set in O(n)**, as they process `n` return entries sequentially. The **space complexity** is **O(n)**, proportional to the size of the return entries stored in memory.

The `ReturnsInspectionController.java` and the `StationRepository.java` files also exhibit a **time complexity of O(n)** for `n` operations, as each controller method executes in **O(1)** time for a single entry.

The algorithm is **efficient for small to medium datasets**. For larger datasets, the sequential architecture scales linearly, and improvements could focus on optimizing CSV loading and Station updates when necessary.