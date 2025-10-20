package Classes;

public class Items {
    private String skuID;
    private String name;
    private String category;
    private String pack;
    private float volume;
    private float weight;

    public Items(String skuID, String name, String category, String pack, float volume, float weight) {
        this.skuID = skuID;
        this.name = name;
        this.category = category;
        this.pack = pack;
        this.volume = volume;
        this.weight = weight;
    }

    public String getSkuID() {
        return skuID;
    }

    public void setSkuID(String skuID) {
        this.skuID = skuID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPack() {
        return pack;
    }
    public void setPack(String pack) {
        this.pack = pack;
    }
    public float getVolume() {
        return volume;
    }
    public void setVolume(float volume) {
        this.volume = volume;
    }
    public float getWeight() {
        return weight;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String toString(){
        return String.format("Product %s, SKU: %s, Category: %s, Pack: %s, Volume: %.2f, Weight: %.2f", this.name, this.skuID, this.category, this.pack, this.volume, this.weight);
    }
}
