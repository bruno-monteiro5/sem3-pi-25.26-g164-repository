package domain;

/**
 * Represents a specific product allocation to be picked from the warehouse.
 * <p>
 * Each allocation belongs to one order and line, refers to a product (SKU),
 * and identifies the physical location of the item (aisle, bay, and box).
 * It also stores the quantity and weight information required for packing.
 */

public class Allocation implements Comparable<Allocation> {

    /** The unique identifier of the order. */
    private  String orderId;

    /** The line number of the order. */
    private  int lineNo;

    /** The SKU (product code) of the allocated item. */
    private  String sku;

    /** The box identifier where the item is stored. */
    private  String boxId;

    /** The aisle number in the warehouse. */
    private  int aisle;

    /** The bay number in the aisle. */
    private  int bay;

    /** The quantity of items allocated. */
    private  double quantity;

    /** The unit weight (kg) of the product. */
    private  double unitWeight;


    /**
     * Creates a new allocation with all its identifying and quantitative information.
     *
     * @param orderId    the order identifier
     * @param lineNo     the order line number
     * @param sku        the product SKU
     * @param boxId      the box where the product is stored
     * @param aisle      the aisle number
     * @param bay        the bay number
     * @param quantity   the quantity of units allocated
     * @param unitWeight the weight of a single unit (kg)
     */

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

    /** @return the order identifier */
    public String getOrderId() {
        return orderId;
    }

    /** @return the line number of the order */
    public int getLineNo() {
        return lineNo;
    }

    /** @return the SKU */
    public String getSku() {
        return sku;
    }

    /** @return the box identifier */
    public String getBoxId() { return boxId; }

    /** @return the aisle number */
    public int getAisle() { return aisle; }

    /** @return the bay number */
    public int getBay() { return bay; }

    /** @return the quantity of allocated units */
    public double getQuantity() { return quantity; }

    /** @return the unit weight (kg) */
    public double getUnitWeight() { return unitWeight; }

    /**
     * Calculates the total weight of this allocation (quantity × unit weight).
     *
     * @return the total weight (kg)
     */
    public double totalWeight() {
        return quantity * unitWeight; }

    /**
     * Compares this allocation with another by total weight, in descending order.
     *
     * @param other the other allocation to compare with
     * @return a negative integer, zero, or a positive integer as this allocation
     *         is heavier, equal, or lighter than the other
     */
    @Override
    public int compareTo(Allocation other) {
        return Double.compare(other.totalWeight(), this.totalWeight());

    }



}