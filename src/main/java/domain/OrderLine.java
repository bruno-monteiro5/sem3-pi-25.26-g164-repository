package domain;

/**
 * Represents a single line within an {@link Order}.
 * <p>
 * Each {@code OrderLine} corresponds to one product type in an order,
 * identified by its SKU (stock-keeping unit), along with the ordered quantity
 * and a sequential line number.
 * <p>
 * This class provides constructors for initialization, getters and setters
 * for each attribute, and a formatted string representation.
 */
public class OrderLine {

    /** Sequential number of this line within the order. */
    private int lineNo;

    /** The SKU (stock-keeping unit) code of the product. */
    private String sku;

    /** The quantity of the product requested in this order line. */
    private int quantity;

    /**
     * Default constructor that creates an empty order line.
     * <p>
     * Used when the object will be populated later.
     */
    public OrderLine() {
    }

    /**
     * Constructs a new {@code OrderLine} with all attributes defined.
     *
     * @param lineNo   the line number within the order
     * @param sku      the product SKU code
     * @param quantity the quantity ordered
     */
    public OrderLine(int lineNo, String sku, int quantity) {
        this.lineNo = lineNo;
        this.sku = sku;
        this.quantity = quantity;
    }

    /**
     * Returns the line number of this order line.
     *
     * @return the line number
     */
    public int getLineNo() {
        return lineNo;
    }

    /**
     * Updates the line number of this order line.
     *
     * @param lineNo the new line number
     */
    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    /**
     * Returns the product SKU of this order line.
     *
     * @return the SKU code
     */
    public String getSku() {
        return sku;
    }

    /**
     * Updates the product SKU of this order line.
     *
     * @param sku the new SKU code
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Returns the quantity of the product ordered in this line.
     *
     * @return the ordered quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Updates the quantity of the product ordered in this line.
     *
     * @param quantity the new quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns a formatted string describing this order line.
     *
     * @return a string representation including line number, SKU, and quantity
     */
    @Override
    public String toString() {
        return "OrderLine{" +
                "lineNo=" + lineNo +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
