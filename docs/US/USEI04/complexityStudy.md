# Complexity Study - PickPathUI.java & PickPathController.java

## 1. Identification
**File:** `PickPathUI.java` & `PickPathController.java`  
**Description:** Class responsible for creating and storing an instance of the `PickPathController` class and printing the results. The instance created has the role of defining and setting the pick plan for the warehouse's products, doing each and every necessary calculus.

---

## 2. General Code Structure
The `PickPathUI` class includes:
- A method that runs it's class;
- A method that prints the results of the calculations done by the `PickPathController` class.

The code follows an **iterative** structure, with no explicit recursion.

---

## 3. Time Complexity Analysis

### 3.1 Main Structure (Simulation)
**Complexity:**

Creation of the `PickPathController` instance:
- Begins the process of running the main simulation.

Each iteration depends on reading a CSV file of `1` lines.

**Complexity:**
- Best case: O(1) — when the file runs quickkly.
- Worst case: O(1) — when file runs slowly.
- Average case: O(1) — when the file runs at average speed.

The logic for printing the results of the calculations envolved:
- running through each and every PickPath instance and printing the results.

Each iteration depends linearly on the number of PickPath instances `n`.

**Complexity:**
- Best case: O(n) — if the sequence of instances is relatively short and simple.
- Worst case: O(n) — if the sequence of instances is long and complex.
- Average case: O(n) — if the sequence of instances is medium-sized, and relatively complex.

The calculation of stops merged requires:
- A number of allocatons to pass through;

Each iteration depends linearly on the number allocations `n`.

**Complexity:**
- Best case: O(n) — the allocation list is relatively small.
- Worst case: O(n) — the allocation list is quite big.
- Average case: O(n) — the allocation list is medium-sized.

The calculation of the total distance requires:
- A number of stops to pass through;
- A number of allocations to pass through;

Each iteration depends linearly on the number of stops and allocations `n`, totaling `n²`.

**Complexity:**
- Best case: O(n²) — the allocation and stop list are relatively small
- Worst case: O(n²) — the allocation and stop list are large
- Average case: O(n²) — the allocation and stop list are medium-sized.

The calculation of the diffrent sequence strategy requires:
- A number of stops to pass through;

Each iteration depends linearly on the number of stops `n`.

**Complexity:**
- Best case: O(n) — the stop list are relatively small
- Worst case: O(n) — the stop list are large
- Average case: O(n) — the stop list are medium-sized.

### 3.2 Operations on Data Structures
|   | Structure                                     | Operation | Complexity          |
|---|-----------------------------------------------|-----------|---------------------|
|   | `List.contains()`                             | O(n)      | Linear              |
|   | `List.add()` / `List.remove()` / `List.get()` | O(1)–O(n) | Depends on position |
|   | `Allocation.get*()`                           | O(1)      | Constant            |


### 3.3 Verification or Sorting Method
If the method performs element comparisons or validations within nested loops, the global complexity may reach O(n²).

---

## 4. Space Complexity Analysis
The program uses:
- Auxiliary structures to store the PickPath instances and path stops;
- Simple variables (counters, indexes). 

**Space Complexity:**
- Auxiliary structures with up to `n²` elements: O(n²)
- Simple variables (counters, indexes): O(1)  
  **Total:** O(n²)

---

## 5. Case Analysis
| Case             | Description                                                   | Complexity |
|------------------|---------------------------------------------------------------|------------|
| **Best case**    | Sequence properly ordered and formattted                      | O(n²)      |
| **Average case** | Sequence relatibely well ordered, maybe not as well formatted | O(n²)      |
| **Worst case**   | Fully inverted sequence / repeated operations                 | O(n²)      |

---

## 6. Conclusion
The `PickPathUI.java` file presents a **time complexity set in O(1)** depending on the duration of the PickPathController running time.
The **space complexity** is O(1), proportional to the number lines in the file.

The `PickPathController.java` file presents a **time complexity set in O(n²)** depending on the amount of Allocation and PickStop variables

The algorithm is **efficient for small to medium datasets** and acceptable in a **discrete simulation context**.  
For large data volumes, optimization is recommended in data access and verification (e.g., using a `HashSet` for O(1) access).
