package Classes;

public class OrderLine {
    private int lineNo;
    private String sku;
    private int quantity;


    public OrderLine(){

    }

    public OrderLine(int lineNo, String sku, int quantity){
        this.lineNo = lineNo;
        this.sku = sku;
        this.quantity = quantity;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String toString() {
        return "OrderLine{" +
                "lineNo=" + lineNo +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
