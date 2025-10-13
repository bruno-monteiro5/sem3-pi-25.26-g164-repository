package Classes;

public class Box {
    private Products products;
    private int quantity;

    public Box(Products products, int quantity){
        this.products = products;
        this.quantity = quantity;
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

    public String toString(){
        return String.format("Box of %d units of %s", this.quantity, this.products.toString());
    }

}
