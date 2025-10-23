package Classes;

public class Allocation implements Comparable<Allocation> {
    private  String orderId;
    private  int lineNo;
    private  String sku;
    private  String boxId;
    private  int aisle;
    private  int bay;
    private  double quantity;
    private  double unitWeight;


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
        return quantity * unitWeight; }


    @Override
    public int compareTo(Allocation other) {
        return Double.compare(other.totalWeight(), this.totalWeight());

    }



}