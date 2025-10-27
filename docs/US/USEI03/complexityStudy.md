# Complexity Study - PickingPlanUI.java & PickingPlanController.java

## 1. Identification
**File:** `PickingPlanUI.java`  & `PickingPlanController.java`
**Description:** Class responsible for creating and storing an instance of the `PickingPlanController` class, which in itself picks up the made allocations in the former UserStory, and creating enough instances of the `Trolley` classes to carry out all allocations without exceeding the maximum capacity of each trolley.

---

## 2. General Code Structure
The `PickingPlanUI.java` class includes:
- A method that creates an instance of the `PickingPlanController` class;
- A method that prints the contents of the `PickingPlanController` instance.

The code follows an **iterative** structure, with no explicit recursion.

The `PickingPlanController.java` class includes:
- A method that generates the picking plan and it's parameters depending on the input;
- A large amount of methods thhat generate the plan dependant on the form chosen ("First fit", "Best fit decreasing", etc...)

The code follows an **iterative** structure, with no explicit recursion.

---

## 3. Time Complexity Analysis

### 3.1 Main Structure (Simulation)
**Complexity:**

Creation of the `PickingPlanUI` instance:
- Creates the `PickingPlanController` instance;
- Prints it's contents.

Each iteration depends on one run of the PickingPlanController instance, a total of `1` line.

**Complexity:**
- Best case: O(1) — the file runs fast and is already in the correct order.
- Worst case: O(1) — the file takes a while to run due to the running of the `PickingPlanController` instance.
- Average case: O(1) — the file takes a while to run, but not much.

The logic for running the `PickingPlanController` instance is:
- verifying which type of allocation should be chosen;
- running the method that generates the plan depending on the form chosen;
- printing the contents of the `PickingPlanController` instance, by sending it to the `PickingPlanUI` instance.

Each iteration depends linearly on the number of allocations,`n`, which leads to the creation of the number of trolleys `n`.

**Complexity:**
- Best case: O(n²) — the input is relatively small and well formatted
- Worst case: O(n²) — the input is large and poorly formatted, leading to issues, specifically with the requirement of the execution of other User Story's functions
- Average case: O(n²) — typical behavior when the input is large yet well formatted

### 3.2 Operations on Data Structures
`PickingPlanUI.java`:

|   | Structure                                                              | Operation | Complexity          |
|---|------------------------------------------------------------------------|-----------|---------------------|
|   | `PickingPlan.getTrolleys()`                                            | O(1)      | Uniform             |
|   | `Trolley.get*()` / `Allocation.get*()` / `PartialAllocationLog.get*()` | O(1)      | Uniform             |


`PickingPlanController.java`:

|   | Structure                                                              | Operation | Complexity                        |
|---|------------------------------------------------------------------------|-----------|-----------------------------------|
|   | `Trolley.get*()` / `Allocation.get*()` / `PartialAllocationLog.get*()` | O(1)      | Uniform                           |
|   | `List.add()` / `List.remove()` / `List.get()`                          | O(1-n)    | Depends on the amount of elements |
### 3.3 Verification or Sorting Method
If the method performs allocations and additions between nested loops, the global complexity may reach O(n²).

---

## 4. Space Complexity Analysis
These programs use:
- Auxiliary structures to temporarily store trolleys, allocations, partial allocations and identification for the processess to be followed;

**Space Complexity:**
- Double Auxiliary structures with up to `n` elements: O(n²)
- Simple variables (counters, indexes): O(1)  
  **Total:** O(n²)

---

## 5. Case Analysis
| Case             | Description                                                                                                                         | Complexity |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------|------------|
| **Best case**    | The input is small, and well formatted, the processing is fast, yet the complexity is the same                                      | O(n²)      |
| **Average case** | The input is large, and well formatted, although certain errors may be present, leading to a slower processing and equal complexity | O(n²)      |
| **Worst case**   | Large input, badly formatter, equal complexity, yet long processing times                                                           | O(n²)      |

---

## 6. Conclusion
The `PickingPlanController.java` file presents a **time complexity set in O(n²)** depending on the case and data structure used.  
The **space complexity** is O(n²), proportional to the number of allocations and trolleys processed.

The algorithm is **efficient for small to medium datasets** and acceptable in a **discrete simulation context**.  
For large data volumes, optimization is recommended in data access and verification (e.g., using a `HashSet` for O(1) access).
