package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer or production order containing multiple {@link OrderLine} entries.
 * <p>
 * Each {@code Order} has a unique identifier, a priority level, a due date,
 * and a list of {@link OrderLine} objects representing the individual items
 * or quantities requested in that order.
 * <p>
 * The class provides operations to manage the order lines,
 * update order attributes, and produce a concise string representation.
 */
public class Order {

    /** Unique identifier for this order. */
    private String orderID;

    /** Priority level assigned to this order (higher values may indicate higher urgency). */
    private int priority;

    /** The due date for the order, formatted as a string (e.g., "2025-10-24"). */
    private String dueDate;

    /** The list of order lines (individual products or items) associated with this order. */
    private List<OrderLine> lines = new ArrayList<>();

    /**
     * Constructs a new {@code Order} with the specified ID, priority, and due date.
     * <p>
     * The list of order lines is initialized as empty.
     *
     * @param orderID  the unique order identifier
     * @param priority the priority level of the order
     * @param dueDate  the due date of the order (string format)
     */
    public Order(String orderID, int priority, String dueDate) {
        this.orderID = orderID;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    /**
     * Default constructor that creates an empty order.
     * <p>
     * Used when the order will be populated later.
     */
    public Order() {
    }

    /**
     * Returns the unique identifier of this order.
     *
     * @return the order ID
     */
    public String getOrderID() {
        return orderID;
    }

    /**
     * Updates the unique identifier of this order.
     *
     * @param orderID the new order ID
     */
    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    /**
     * Returns the priority level of this order.
     *
     * @return the order priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Updates the priority level of this order.
     *
     * @param priority the new priority level
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Returns the due date of this order.
     *
     * @return the order due date
     */
    public String getDueDate() {
        return dueDate;
    }

    /**
     * Updates the due date of this order.
     *
     * @param dueDate the new due date
     */
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Returns the list of order lines in this order.
     * <p>
     * The returned list is live — modifications will affect the internal state.
     *
     * @return the list of {@link OrderLine} objects
     */
    public List<OrderLine> getLines() {
        return lines;
    }

    /**
     * Replaces the internal list of order lines with a new list.
     *
     * @param lines the new list of {@link OrderLine} objects
     */
    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
    }

    /**
     * Adds a new {@link OrderLine} to this order.
     *
     * @param l the order line to add
     */
    public void addLine(OrderLine l) {
        this.lines.add(l);
    }

    /**
     * Returns a formatted string representation of this order,
     * including its ID, priority, due date, and number of lines.
     *
     * @return a string describing the order
     */
    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderID + '\'' +
                ", priority=" + priority +
                ", dueDate='" + dueDate + '\'' +
                ", lines=" + lines.size() +
                '}';
    }
}