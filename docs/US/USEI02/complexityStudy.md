# Complexity Study - WagonUnloading.java

## 1. Identification
**File:** `OrderEligibilityNdAllocation.java`  
**Description:** Class responsible for creating and storing an instance of a list of `Order` classes and a list of `Allocation` classes, filling that list with data taken from a  `.csv` file, and printing the list to the console, then verifying the orders with the current `Warehouse` instance's inventory, and filling the `Allocation` list if a match is found, then printing the contents and storing the list of allocations.

---

## 2. General Code Structure
The `OrderEligibilityNdAllocation` class includes:
- A method that creates an list of instance of the `Order` class, and another method that creates a list of `Allocation` class;
- A method that retrieves orders from a `.csv` file.

The code follows an **iterative** structure, with no explicit recursion.

---

## 3. Time Complexity Analysis

### 3.1 Main Structure (Simulation)
**Complexity:**

The logic for the creation of the list of the `Order` instance, and reading of the `.csv` file:
- Creates all orders and orders lines;
- fills the `Order` instance list with them.

Each iteration depends on reading a CSV file of `n` lines.

**Complexity:**
- Best case: O(n) — when the file is well organized.
- Worst case: O(n²) — when the file is poorly formatted or disorganized, requiring multiple passes through the file.
- Average case: O(n log n) — when the file is formatted but not perfectly organized.

The logic for verifying the orders with the current `Warehouse` instance's inventory, and filling the `Allocation` list if a match is found:
- Uses user input to determine if the order is strict or partial;
- If strict, verifies if the order is eligible for allocation only if the quantity is the required;
- If partial, verifies if the order is eligible for allocation at any quantity from the box;
- Adds the box to the allocation list if the order is eligible for allocation;

Each iteration depends linearly on the number of `Orders` `n`, `OrderLines` `n`, and boxes in the `Warehouse` instance `n`.

**Complexity:**
- Best case: O(n^3) — if the list is small and the program is run on a Strict manner.
- Worst case: O(n^3) — if the list is large and the program is run on a Partial manner.
- Average case: O(n^3) — typical sized list and either a strict or partial manner..

### 3.2 Operations on Data Structures
|   | Structure                      | Operation | Complexity          |
|---|--------------------------------|-----------|---------------------|
|   | `List.contains()`              | O(n)      | Linear              |
|   | `List.add()` / `List.remove()` | O(1)–O(n) | Depends on position |

### 3.3 Verification or Sorting Method
If the method performs verifications of matches between the order's requests and the box's contents, the global complexity may reach O(n^3).

---

## 4. Space Complexity Analysis
The program uses:
- Auxiliary structures to temporarily store a list of `Order` instances and a list of `Allocation` instances;
- Control variables and counters.

**Space Complexity:**
- Auxiliary structures with up to `n^3` elements: O(n^3)
  **Total:** O(n^3)

---

## 5. Case Analysis
| Case             | Description                                   | Complexity |
|------------------|-----------------------------------------------|------------|
| **Best case**    | Sequence already ordered (minimal operations) | O(n^3)     |
| **Average case** | Part of the sequence requires reorganization  | O(n^3)     |
| **Worst case**   | Fully inverted sequence / repeated operations | O(n^3)     |

---

## 6. Conclusion
The `OrderEligibilityNdAllocation.java` file presents a **time complexity set in O(n^3)**, although the actual time needed to fully run may depend on the case and data structure used.  
The **space complexity** is O(n^3), proportional to the number of Products, Orders, and OrderLines processed. 

