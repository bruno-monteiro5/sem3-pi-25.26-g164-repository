package domain;

public class Box {
    private Items items;
    private int quantity;
    private String expDate;

    public Box(Items items, int quantity, String expDate){
        this.items = items;
        this.quantity = quantity;
        this.expDate = expDate;
    }

    public Box(Items items, int quantity){
        this.items = items;
        this.quantity = quantity;
        this.expDate = null;
    }

    public Items getProduct() {
        return items;
    }
    public void setProduct(Items items) {
        this.items = items;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public String getExpDate() {
        return expDate;
    }
    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String toString(){
        if(expDate != null){
            return String.format("Box of %d units of %s, expires on %s", this.quantity, this.items.toString(), this.expDate);
        }
        return String.format("Box of %d units of %s", this.quantity, this.items.toString());
    }

}
