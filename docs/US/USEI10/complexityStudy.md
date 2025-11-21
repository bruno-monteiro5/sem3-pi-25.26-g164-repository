# Complexity Study - RadiusSearchControler.java

## 1. Identification
**File:** `RadiusSearchController.java`
**Description:** Class responsible for handling the search of the n nearest stations to a radius of a given point without filtering.

---

## 2. General Code Structure
The `RadiusSearchController` class includes:
- A method that creates an instance of its class:
    - Retrieves and processes the main instance of a StationRepository, and a TreeController.
- A method that finds the n stations in a radium of a given point:
    - Verifies which of them are in a radium distance from the given point.
- A method that collects all stations:
    - Retrieves all stations in a KDTree.

The code follows an **iterative** structure, with no explicit recursion. The overall structure involves processing return entries sequentially.

---

## 3. Time Complexity Analysis

### 3.1 Main Structure (Inspection)
**Complexity:**

Search of a 2DTree for the n points in a radius of a distance from a given point:
- Examination of each `Node` in the running configuration's `KDTree2D` entry and verification of its proximity to the given point and if its in a given radius from the point.

**Complexity:**
- Best case: O(n) — where `n` is the number of KDTree nodes existent, all correctly created.
- Worst case: O(n) — some Nodes are badly configured.
- Average case: O(n) — most Nodes are badly configured.

Processing the entries in a KDTree2D(nodes):
- For each entry `n`, the following operations are conducted:
    - Previewing an entry: **O(1)**.
    - Decision-making:
        - Add to the resultant String: **O(1)**.

**Total Complexity:** O(n) for `n` entries being processed.

---

### 3.2 Controller Operations
|   | Method                                        | Operation                                                                     | Complexity |
|---|-----------------------------------------------|-------------------------------------------------------------------------------|------------|
|   | `RadiusSearchController.searchWithinRadius()` | Searches throughout the 2DTree for entries within a radius to the given point | O(n)       |
|   | `ProximitySearchController.getAllStations()`  | Retrieves all stations in the KDTree2D instance.                              | O(n)       |

The above methods depend on the dimension of the KDTree2D instance on the running program.

**Complexity (per entry): O(n).**

### 3.3 Verification or Sorting Method
- No sorting is performed on this method since the entries dont appear to be sorted..

---

## 4. Space Complexity Analysis
The program uses:
- A instance of the `StationRepository` class created by the running configuration.
- A instance of the `TreeController` class created by the running configuration.

**Space Complexity:**
- Going through `n` entries: O(n).
- Checking them to verify their distance to the given point and adding them to the Query result: O(1).
  **Total:** O(n).

---

## 5. Case Analysis
| Case             | Description                                                                                                                               | Complexity |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------------|------------|
| **Best case**    | Small instance of `KDTree2D`, well/relatively well formatted in what comes to the x and y dimensions, and other miscellaneous information | O(n)       |
| **Average case** | Relatively big instance of `KDTree2D`, not as well ordered in what comes to the x and y dimensions, and other miscellaneous information   | O(n)       |
| **Worst case**   | Large instance of `KDTree2D`, badly ordered in the terms of the x and y dimensions, and other miscellaneous information                   | O(n)       |

---

## 6. Conclusion
The `RadiusSearchController.java` file presents a **time complexity set in O(n)**, as they process `n` station entries sequentially. The **space complexity** is **O(n)**, proportional to the size of the station entries stored in the `KDTree2D` instance.

The algorithm is **efficient for small to medium datasets**. For larger datasets, the sequential architecture scales linearly, and improvements could focus on optimizing CSV loading and Station updates when necessary.