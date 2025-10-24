package domain;

/**
 * Defines the available packing heuristics for building a picking plan.
 */

public enum PackingMode {
    /** First Fit : Place the item in the first available trolley, where it fits. The
     scan order should be the input order of the allocation rows. */
    FF,

    /** First Fit Decreasing: Sort unpicked allocations from US02 by weight
     from largest to smallest and place the item in the first available batch where
     it fits */
    FFD,

    /** First Fit Decreasing: Sort unpicked allocations from US02 by weight
     from largest to smallest and place the item in the first available batch where
     it fits */
    BFD
}
