package Classes;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderID;
    private int priority;
    private String dueDate;
    private List<OrderLine> lines = new ArrayList<>();

    public Order(String orderID, int priority, String dueDate){
        this.orderID = orderID;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public Order () {
    }

    public String getOrderID() {
        return orderID;
    }
    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
    }

    public void addLine(OrderLine l){
        this.lines.add(l);
    }

    public String toString() {
        return "Order{" +
                "orderId='" + orderID + '\'' +
                ", priority=" + priority +
                ", dueDate='" + dueDate + '\'' +
                ", lines=" + lines.size() +
                '}';
    }
}
