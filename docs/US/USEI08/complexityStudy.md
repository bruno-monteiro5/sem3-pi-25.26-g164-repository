# Complexity Study - BuildIndexesControler.java

## 1. Identification
**File:** `Search2DTreeController.java`
**Description:** Class responsible for handling the search of specific stations amongst the whole 2DTree created on the former US.

---

## 2. General Code Structure
The `Search2DTreeController` class includes:
- A method that creates an instance of its class:
    - Retrieves and processes the main instance of a KDTreeRepository.
    - Uses the station information to search throughout all of the 2D Tree, based on latitude and longitude arrays and optional extra filters.
- A set of methods that search through filters
    - Retrieves and processes each node in a recursive manner.
    - Verifies the node's values and adds it to the resultant String if it fits the parameters.

The code follows an **iterative** structure, with no explicit recursion. The overall structure involves processing return entries sequentially.

---

## 3. Time Complexity Analysis

### 3.1 Main Structure (Inspection)
**Complexity:**

Searcing of a 2DTree:
- Examination of each `Node` in the running configuration's `KDTree2D` entry and verification of its values in order to add it or not to the query.

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
|   | Method                                  | Operation                                                                                                                    | Complexity |
|---|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------|------------|
|   | `Search2DTreeController.searchInArea()` | Searches throughout the 2DTree for entries equaling to the set parameters, and adds it's description to the resultant String | O(n)       |

The above methods depend on the dimension the KDTree2D instance on the running program.

**Complexity (per entry): O(n).**

### 3.3 Verification or Sorting Method
- Sorting operations are performed on the entries, as they need to be sorted and positioned on the 2DTree.

---

## 4. Space Complexity Analysis
The program uses:
- A instance of the `KDTree2D` class taken by it's repository.
- A instance of the `KDTreeRepository` to store the created 2DTree.
- A string containing the description of the searched stations that fit the parameters.

**Space Complexity:**
- Going through `n` entries: O(n).
- Using them to fill the String: O(n).
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
The `Search2DTreeController.java` file presents a **time complexity set in O(n)**, as they process `n` station entries sequentially. The **space complexity** is **O(n²)**, proportional to the size of the station entries stored in memory and their insertions in the 2DTree.

The `Search2DTreeController.java` file also exhibits a **time complexity of O(n)** for `n` operations, as each controller method executes in **O(1)** time for a single entry.

The algorithm is **efficient for small to medium datasets**. For larger datasets, the sequential architecture scales linearly, and improvements could focus on optimizing CSV loading and Station updates when necessary.