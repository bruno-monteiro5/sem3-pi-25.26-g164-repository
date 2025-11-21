# Complexity Study - ProximitySearchControler.java

## 1. Identification
**File:** `ProximitySearchController.java`
**Description:** Class responsible for handling the search of the n nearest stations to a given point without filtering.

---

## 2. General Code Structure
The `ProximitySearchController` class includes:
- A method that creates an instance of its class:
    - Retrieves and processes the main instance of a KDTreeRepository.
    - Uses the station information to search throughout all of the 2D Tree, based on latitude and longitude arrays and optional extra filters.
- A method that finds the n nearsest stations to a given point:
    - Retrieves the nearest stations to the given point.
    - Returns a `ProximitySearchResult` containing the stations and their respective distances.
- A method that collects all stations:
    - Retrieves all stations in a KDTree.

The code follows an **iterative** structure, with no explicit recursion. The overall structure involves processing return entries sequentially.

---

## 3. Time Complexity Analysis

### 3.1 Main Structure (Inspection)
**Complexity:**

Search of a 2DTree for the n points closest to a given point:
- Examination of each `Node` in the running configuration's `KDTree2D` entry and verification of its proximity to the given point in order to add it or not to the query.

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
|   | Method                                       | Operation                                                             | Complexity |
|---|----------------------------------------------|-----------------------------------------------------------------------|------------|
|   | `ProximitySearchController.findNearestN()`   | Searches throughout the 2DTree for entries nearest to the given point | O(n)       |
|   | `ProximitySearchController.getAllStations()` | Retrieves all stations in the KDTree2D instance.                      | O(n)       |

The above methods depend on the dimension of the KDTree2D instance on the running program.

**Complexity (per entry): O(n).**

### 3.3 Verification or Sorting Method
- Sorting operations are performed on the entries, as they need to be sorted and positioned on the 2DTree.

---

## 4. Space Complexity Analysis
The program uses:
- A instance of the `StationRepository` class created by the running configuration.
- A instance of the `KDTreeRepository` class created by the running configuration.
- A `ProximitySearchResult` containing the result of the nearest stations to the given point found.

**Space Complexity:**
- Going through `n` entries: O(n).
- Checking them to verify their distance to the given point and adding them to the Query result: O(n).
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
The `ProximitySearchController.java` file presents a **time complexity set in O(n)**, as they process `n` station entries sequentially. The **space complexity** is **O(n)**, proportional to the size of the station entries stored in the `KDTree2D` instance.

The algorithm is **efficient for small to medium datasets**. For larger datasets, the sequential architecture scales linearly, and improvements could focus on optimizing CSV loading and Station updates when necessary.