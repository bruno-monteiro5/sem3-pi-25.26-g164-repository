package Classes;

public class Box {
    private Products products;
    private int quantity;
    private String expDate;

    public Box(Products products, int quantity, String expDate){
        this.products = products;
        this.quantity = quantity;
        this.expDate = expDate;
    }

    public Box(Products products, int quantity){
        this.products = products;
        this.quantity = quantity;
        this.expDate = null;
    }

    public Products getProduct() {
        return products;
    }
    public void setProduct(Products products) {
        this.products = products;
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
            return String.format("Box of %d units of %s, expires on %s", this.quantity, this.products.toString(), this.expDate);
        }
        return String.format("Box of %d units of %s", this.quantity, this.products.toString());
    }

}
