package domain;

/**
 * Represents a product item stored or handled in the warehouse.
 * <p>
 * Each {@code Items} instance contains identifying and descriptive information
 * about a product, including its SKU code, name, category, packaging type,
 * volume, and weight. This class provides standard getters, setters, and
 * a formatted string representation for display or debugging purposes.
 */
public class Items {

    /** The stock-keeping unit (SKU) identifier of this product. */
    private String skuID;

    /** The descriptive name of the product. */
    private String name;

    /** The product category (e.g., food, electronics, tools). */
    private String category;

    /** The type of packaging (e.g., box, bag, bottle). */
    private String pack;

    /** The product’s physical volume in cubic meters or liters. */
    private float volume;

    /** The product’s weight in kilograms. */
    private float weight;

    /**
     * Constructs a new {@code Items} object with all fields initialized.
     *
     * @param skuID     the unique SKU code of the product
     * @param name      the product name
     * @param category  the product category
     * @param pack      the packaging type
     * @param volume    the physical volume of the product
     * @param weight    the physical weight of the product
     */
    public Items(String skuID, String name, String category, String pack, float volume, float weight) {
        this.skuID = skuID;
        this.name = name;
        this.category = category;
        this.pack = pack;
        this.volume = volume;
        this.weight = weight;
    }

    /**
     * Returns the SKU identifier of this product.
     *
     * @return the SKU code
     */
    public String getSkuID() {
        return skuID;
    }

    /**
     * Updates the SKU identifier of this product.
     *
     * @param skuID the new SKU code
     */
    public void setSkuID(String skuID) {
        this.skuID = skuID;
    }

    /**
     * Returns the name of this product.
     *
     * @return the product name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the name of this product.
     *
     * @param name the new product name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the category of this product.
     *
     * @return the product category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Updates the category of this product.
     *
     * @param category the new product category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the packaging type of this product.
     *
     * @return the packaging description
     */
    public String getPack() {
        return pack;
    }

    /**
     * Updates the packaging type of this product.
     *
     * @param pack the new packaging description
     */
    public void setPack(String pack) {
        this.pack = pack;
    }

    /**
     * Returns the product volume.
     *
     * @return the volume of the product
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Updates the product volume.
     *
     * @param volume the new volume value
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * Returns the product weight.
     *
     * @return the weight of the product
     */
    public float getWeight() {
        return weight;
    }

    /**
     * Updates the product weight.
     *
     * @param weight the new weight value
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    /**
     * Returns a formatted string describing the product, including all attributes.
     * <p>
     * The decimal separator is always a dot ({@code .}), independent of system locale.
     *
     * @return a formatted string describing this product
     */
    @Override
    public String toString() {
        return String.format(
                java.util.Locale.US,
                "Product %s, SKU: %s, Category: %s, Pack: %s, Volume: %.2f, Weight: %.2f",
                this.name, this.skuID, this.category, this.pack, this.volume, this.weight
        );
    }
}
