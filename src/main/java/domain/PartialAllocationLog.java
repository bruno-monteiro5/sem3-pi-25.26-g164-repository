package domain;

public class PartialAllocationLog {
    private String orderId;
    private int lineNo;
    private String sku;
    private double quantityNow;
    private double quantityDeferred;

    public PartialAllocationLog(String orderId, int lineNo, String sku, double quantityNow, double quantityDeferred) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.quantityNow = quantityNow;
        this.quantityDeferred = quantityDeferred;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getLineNo() {
        return lineNo;
    }

    public String getSku() {
        return sku;
    }

    public double getQuantityNow() {
        return quantityNow;
    }

    public double getQuantityDeferred() {
        return quantityDeferred;
    }


}
