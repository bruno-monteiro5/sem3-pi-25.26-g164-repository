package domain;

/**
 * Represents a physical box containing a quantity of a specific {@link Items} product.
 * <p>
 * A {@code Box} can optionally include an expiration date (for perishable goods),
 * and stores information about the product it contains, its quantity, and—if applicable—
 * when it expires.
 * <p>
 * The class provides constructors for boxes with or without an expiration date,
 * along with standard getters, setters, and a formatted {@link #toString()} method
 * describing the box and its contents.
 */
public class Box {

    /** The product stored inside this box. */
    private Items items;

    /** The quantity of the product stored in this box. */
    private int quantity;

    /** The expiration date of the product, or {@code null} if not applicable. */
    private String expDate;

    /**
     * Constructs a new {@code Box} with all attributes explicitly defined.
     *
     * @param items   the product stored in this box
     * @param quantity the quantity of the product units contained in the box
     * @param expDate  the expiration date of the product (may be {@code null} if not perishable)
     */
    public Box(Items items, int quantity, String expDate) {
        this.items = items;
        this.quantity = quantity;
        this.expDate = expDate;
    }

    /**
     * Constructs a new {@code Box} without an expiration date.
     * <p>
     * This constructor is suitable for non-perishable goods.
     *
     * @param items    the product stored in this box
     * @param quantity the quantity of the product units contained in the box
     */
    public Box(Items items, int quantity) {
        this.items = items;
        this.quantity = quantity;
        this.expDate = null;
    }

    /**
     * Returns the {@link Items} instance representing the product stored in this box.
     *
     * @return the product inside the box
     */
    public Items getProduct() {
        return items;
    }

    /**
     * Updates the {@link Items} product stored in this box.
     *
     * @param items the new product to associate with this box
     */
    public void setProduct(Items items) {
        this.items = items;
    }

    /**
     * Returns the quantity of product units contained in this box.
     *
     * @return the number of product units
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Updates the quantity of product units in this box.
     *
     * @param quantity the new quantity value
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns the expiration date of this box’s contents.
     * <p>
     * If the product does not expire, this method returns {@code null}.
     *
     * @return the expiration date, or {@code null} if not applicable
     */
    public String getExpDate() {
        return expDate;
    }

    /**
     * Updates the expiration date of this box’s contents.
     *
     * @param expDate the new expiration date (may be {@code null})
     */
    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    /**
     * Returns a formatted string describing this box.
     * <p>
     * If the expiration date is available, it is appended at the end of the description;
     * otherwise, only the product and quantity are shown.
     * <p>
     * Example output:
     * <ul>
     *   <li>{@code Box of 5 units of Product Apple, SKU: A123, Category: Fruit, Pack: Bag, Volume: 1.20, Weight: 0.50}</li>
     *   <li>{@code Box of 10 units of Product Milk, SKU: M101, Category: Dairy, Pack: Bottle, Volume: 2.00, Weight: 1.00, expires on 2025-05-01}</li>
     * </ul>
     *
     * @return a string describing the box, including its product and optional expiration date
     */
    @Override
    public String toString() {
        if (expDate != null) {
            return String.format(
                    "Box of %d units of %s, expires on %s",
                    this.quantity,
                    this.items.toString(),
                    this.expDate
            );
        }
        return String.format(
                "Box of %d units of %s",
                this.quantity,
                this.items.toString()
        );
    }
}
