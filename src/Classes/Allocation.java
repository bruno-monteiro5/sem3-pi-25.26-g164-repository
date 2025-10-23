package Classes;

public class Allocation implements Comparable<Allocation> {
    private final String orderId;
    private final int lineNo;
    private final String sku;
    private final String boxId;
    private final int aisle;
    private final int bay;
    private final double quantity;
    private final double unitWeight;


    public Allocation (String orderId, int lineNo, String sku, String boxId, int aisle, int bay, double quantity, double unitWeight) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.boxId = boxId;
        this.aisle = aisle;
        this.bay = bay;
        this.quantity = quantity;
        this.unitWeight = unitWeight;
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
    public String getBoxId() {
        return boxId;
    }
    public int getAisle() {
        return aisle;
    }
    public int getBay() {
        return bay;
    }
    public double getQuantity() {
        return quantity;
    }
    public double getUnitWeight() {
        return unitWeight;
    }

    public double totalWeight() {
        return quantity * unitWeight; }        // [24]


    @Override
    public int compareTo(Allocation other) {
        return Double.compare(other.totalWeight(), this.totalWeight()); // [27]

    }



}
