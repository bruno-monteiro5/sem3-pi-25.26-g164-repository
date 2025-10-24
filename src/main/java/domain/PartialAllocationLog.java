package domain;

/**
 * Log entry that records a partial allocation decision.
 * <p>
 * It captures how much quantity was allocated immediately and how much
 * was deferred to a subsequent trolley for the same order line and SKU.
 */

public class PartialAllocationLog {

    /** The order identifier. */
    private String orderId;

    /** The order line number. */
    private int lineNo;

    /** The SKU (product code). */
    private String sku;

    /** Quantity allocated now (in units). */
    private double quantityNow;

    /** Quantity deferred to a later trolley (in units). */
    private double quantityDeferred;


    /**
            * Creates a new partial allocation log entry.
            *
            * @param orderId          the order identifier
     * @param lineNo           the order line number
     * @param sku              the product SKU
     * @param quantityNow      the quantity allocated immediately
     * @param quantityDeferred the quantity deferred to a later trolley
     */
    public PartialAllocationLog(String orderId, int lineNo, String sku, double quantityNow, double quantityDeferred) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.quantityNow = quantityNow;
        this.quantityDeferred = quantityDeferred;
    }

    /** @return the order identifier */
    public String getOrderId() { return orderId; }

    /** @return the order line number */
    public int getLineNo() { return lineNo; }

    /** @return the SKU (product code) */
    public String getSku() { return sku; }

    /** @return the quantity allocated immediately */
    public double getQuantityNow() { return quantityNow; }

    /** @return the quantity deferred to a later trolley */
    public double getQuantityDeferred() { return quantityDeferred; }
}


