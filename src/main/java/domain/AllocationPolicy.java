package domain;

/**
 * Defines the policy applied when an allocation does not fit entirely in a trolley.
 */

public enum AllocationPolicy {

    /** Split the allocation across multiple trolleys if necessary. */
    SPLIT_LINE,

    /** Defer the allocation to a later trolley if it does not fit. */
    DEFER_LINE
}
