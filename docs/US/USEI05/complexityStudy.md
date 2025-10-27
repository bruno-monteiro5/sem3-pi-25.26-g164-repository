# Complexity Study - ReturnsInspectionUI.java & ReturnsInspectionController.java

## 1. Identification
**File:** `ReturnsInspectionUI.java` & `ReturnsInspectionController.java`  
**Description:** Class responsible for handling the inspection and processing of returned goods. The `ReturnsInspectionUI` class serves as the entry point, overseeing the loading and automated inspection of the returned items, whereas the `ReturnsInspectionController` class facilitates decision-making and communicates with quarantine and inventory systems.

---

## 2. General Code Structure
The `ReturnsInspectionUI` class includes:
- A method that runs its class:
    - Retrieves and processes return entries from a CSV file.
    - Handles operations based on the return reasons using the `ReturnsInspectionController`.

The `ReturnsInspectionController` includes:
- Methods to:
    - Preview the next return entry (`previewNext`).
    - Discard all items of an entry (`discardAllNext`).
    - Restock all quantities of an entry (`restockAllNext`).
    - Partially restock a portion of the quantity (`partialRestockNext`).

The code follows an **iterative** structure, with no explicit recursion. The overall structure involves processing return entries sequentially.

---

## 3. Time Complexity Analysis

### 3.1 Main Structure (Inspection)
**Complexity:**

Loading return entries from the CSV file:
- Reads and enqueues all return entries from the `warehouseMngt/train_station_dataset/returns.csv`.

**Complexity:**
- Best case: O(n) — where `n` is the number of rows in the file, all correctly formatted.
- Worst case: O(n) — including validation and parsing for all rows.
- Average case: O(n) — accounting for normal formatting and validation.

Processing the return entries:
- For each entry `n`, the following operations are conducted:
    - Previewing an entry: **O(1)**.
    - Decision-making:
        - Discard all: **O(1)**.
        - Restock all: **O(1)** (basic validation and inventory update).
        - Partial restock: **O(1)**.

Printing the inspection results to logs adds a negligible constant operation for each entry:
- **Complexity (per entry): O(1)**.

**Total Complexity:** O(n) for `n` entries being processed.

---

### 3.2 Controller Operations
|   | Method                                           | Operation             | Complexity |
|---|--------------------------------------------------|-----------------------|------------|
| 1 | `ReturnsInspectionController.previewNext`        | Previewing next entry | **O(1)**   |
| 2 | `ReturnsInspectionController.discardAllNext`     | Discarding an entry   | **O(1)**   |
| 3 | `ReturnsInspectionController.restockAllNext`     | Restocking all items  | **O(1)**   |
| 4 | `ReturnsInspectionController.partialRestockNext` | Partial restocking    | **O(1)**   |

The above methods depend only on the actions to be performed for an individual entry, with constant-time complexity for decision-making, validation, and inventory update operations.

**Complexity (per entry): O(1).**

### 3.3 Verification or Sorting Method
- No sorting operations are performed on the return entries, as they are processed sequentially in the order received.

---

## 4. Space Complexity Analysis
The program uses:
- A collection (likely a queue) in the `QuarantineController` to store all `n` return entries.
- Simple variables for counters and intermediate temporary objects created during processing.

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
The `ReturnsInspectionUI.java` file presents a **time complexity set in O(n)**, as it processes `n` return entries sequentially. The **space complexity** is **O(n)**, proportional to the size of the return entries stored in memory.

The `ReturnsInspectionController.java` file also exhibits a **time complexity of O(n)** for `n` operations, as each controller method executes in **O(1)** time for a single entry.

The algorithm is **efficient for small to medium datasets**. For larger datasets, the sequential architecture scales linearly, and improvements could focus on optimizing CSV loading and inventory updates when necessary.