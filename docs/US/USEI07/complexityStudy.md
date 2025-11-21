# Complexity Study - BuildIndexesControler.java

## 1. Identification
**File:** `Build2DTreeController.java`
**Description:** Class responsible for handling the construction of a `KDTree2D` 

---

## 2. General Code Structure
The `Build2DTreeController` class includes:
- A method that creates a instance of its class:
    - Retrieves and processes the main instance of a StationRepository.
    - Uses the station information to build a 2D Tree, based on latitude and longitude as respective dimensions.

The code follows an **iterative** structure, with no explicit recursion. The overall structure involves processing return entries sequentially.

---

## 3. Time Complexity Analysis

### 3.1 Main Structure (Inspection)
**Complexity:**

Creation of a 2DTree:
- Examination of each entry of the `StationIndex` class inbued in the main `StationRepository` instance.

**Complexity:**
- Best case: O(n) — where `n` is the number of stations existant, all correctly created.
- Worst case: O(n) — some stations are badly configured.
- Average case: O(n) — most stations are badly configured, and probably have missing information crucial to the operation.

Processing the entries in the list of stations:
- For each entry `n`, the following operations are conducted:
    - Previewing an entry: **O(1)**.
    - Decision-making:
        - Add to 2DTree of stations: **O(1)**.

**Total Complexity:** O(n) for `n` entries being processed.

---

### 3.2 Controller Operations
|   | Method                                  | Operation                                                                                                                 | Complexity |
|---|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------|------------|
| 1 | `Build2DTreeController.build2DTree()`   | Creation of a 2DTree based on the StationIndexes instance running in the program with the existant stations' information. | O(n)       |
| 2 | `Build2DTreeController.getKDTree()`     | Retrieval of the created 2DTree.                                                                                          | O(1)       |
| 3 | `Build2DTreeController.getStatistics()` | Retrieval of the statistics of the created 2DTree                                                                         | O(1)       |

The above methods depend on the dimension the StationIndex instance on the running program.

**Complexity (per entry): O(n).**

### 3.3 Verification or Sorting Method
- Sorting operations are performed on the entries, as they need to be sorted and positioned on the 2DTree.

---

## 4. Space Complexity Analysis
The program uses:
- A instance of the `StationRepository` class, which stores the `StationIndex` instance containing all stations.
- A instance of the `KDTreeRepository` to store the created 2DTree.

**Space Complexity:**
- Going through `n` entries: O(n).
- Using them to build the 2DTree: O(n²).  
  **Total:** O(n²).

---

## 5. Case Analysis
| Case             | Description                                                                                              | Complexity |
|------------------|----------------------------------------------------------------------------------------------------------|------------|
| **Best case**    | Small instance of `StationIndexes`, well/relatively well ordered in what comes to the x and y dimensions | O(n)       |
| **Average case** | Relatively big instance of `StationIndexes`, not as well ordered in what comes to the x and y dimensions | O(n)       |
| **Worst case**   | Large instance of `StationIndexes`, badly ordered in the terms of the x and y dimensions                 | O(n)       |

---

## 6. Conclusion
The `Build2DTreeController.java` file presents a **time complexity set in O(n)**, as they process `n` station entries sequentially. The **space complexity** is **O(n²)**, proportional to the size of the station entries stored in memory and their insertions in the 2DTree.

The `ReturnsInspectionController.java` and the `StationRepository.java` files also exhibit a **time complexity of O(n)** for `n` operations, as each controller method executes in **O(1)** time for a single entry.

The algorithm is **efficient for small to medium datasets**. For larger datasets, the sequential architecture scales linearly, and improvements could focus on optimizing CSV loading and Station updates when necessary.