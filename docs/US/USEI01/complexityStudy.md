# Complexity Study - WagonUnloading.java

## 1. Identification
**File:** `WagonUnloading.java`  
**Description:** Class responsible for creating and storing an instance of the `Warehouse` class and all its internal spaces, a list containing all possible products that can be added, and reading the wagon unloading process from a `.csv` file to fill the available space within the `Warehouse` instance. All data used by these functions is retrieved from a CSV file.

---

## 2. General Code Structure
The `WagonUnloading` class includes:
- A method that creates an instance of the `Warehouse` class;
- A method that creates a list of all products retrieved from a `.csv` file;
- A method that reads a `.csv` file containing all products arriving at the warehouse and fills the spaces of the `Warehouse` instance accordingly.

The code follows an **iterative** structure, with no explicit recursion.

---

## 3. Time Complexity Analysis

### 3.1 Main Structure (Simulation)
**Complexity:**

Creation of the `Warehouse` instance:
- Creates all aisles and bays;
- Determines when to move from one aisle to another.

Each iteration depends on reading a CSV file of `n` lines.

**Complexity:**
- Best case: O(n) — when the file is well organized.
- Worst case: O(n²) — when the file is poorly formatted or disorganized, requiring multiple passes through the file.
- Average case: O(n log n) — when the file is formatted but not perfectly organized.

The logic for reading `wagons.csv` involves iterating through each wagon record once, and:
- Checking how many products are in each wagon and storing them temporarily;
- Verifying which products have expiration dates and which do not.

Each iteration depends linearly on the number of wagons `n`.

**Complexity:**
- Best case: O(n) — if the sequence is already in the correct order.
- Worst case: O(n²) — when multiple internal operations per element are needed (e.g., insertions/removals in stacks).
- Average case: O(n log n) — typical behavior when partial optimizations exist.

The organization of products involves reading each stored product at least once:
- Performing FEFO organization first when one or more products have expiration dates, and FIFO afterwards, or FIFO only if no product has expiration dates;
- Storing products while checking each organizational entry at least once.

Each iteration depends linearly on the number of products in FEFO organization `n` and the number of products in FIFO organization `n`.

**Complexity:**
- Best case: O(n) — all products are organized either by FEFO or FIFO, with a moderate quantity.
- Worst case: O(n) — both FEFO and FIFO groups have similar sizes and relatively high quantities.
- Average case: O(n log n) — most products are organized under one system (FEFO or FIFO), with a smaller portion under the other.

### 3.2 Operations on Data Structures
|   | Structure                      | Operation | Complexity          |
|---|--------------------------------|-----------|---------------------|
|   | `List.contains()`              | O(n)      | Linear              |
|   | `List.add()` / `List.remove()` | O(1)–O(n) | Depends on position |

### 3.3 Verification or Sorting Method
If the method performs element comparisons or validations within nested loops, the global complexity may reach O(n²).

---

## 4. Space Complexity Analysis
The program uses:
- Auxiliary structures to temporarily store wagons;
- Control variables and counters.

**Space Complexity:**
- Auxiliary structures with up to `n` elements: O(n)
- Simple variables (counters, indexes): O(1)  
  **Total:** O(n)

---

## 5. Case Analysis
| Case             | Description                                   | Complexity |
|------------------|-----------------------------------------------|------------|
| **Best case**    | Sequence already ordered (minimal operations) | O(n)       |
| **Average case** | Part of the sequence requires reorganization  | O(n log n) |
| **Worst case**   | Fully inverted sequence / repeated operations | O(n²)      |

---

## 6. Conclusion
The `WagonUnloading.java` file presents a **time complexity ranging from O(n) to O(n²)** depending on the case and data structure used.  
The **space complexity** is O(n), proportional to the number of wagons processed.

The algorithm is **efficient for small to medium datasets** and acceptable in a **discrete simulation context**.  
For large data volumes, optimization is recommended in data access and verification (e.g., using a `HashSet` for O(1) access).
