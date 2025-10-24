package domain;


import java.util.Objects;

public class PickStop {
    private final int aisle;
    private final int bay;
    private double totalQty; // opcional: poderás mostrar no plano

    public PickStop(int aisle, int bay, double totalQty) {
        this.aisle = aisle;
        this.bay = bay;
        this.totalQty = totalQty;
    }

    public int getAisle() { return aisle; }
    public int getBay() { return bay; }
    public double getTotalQty() { return totalQty; }
    public void addQty(double q) { this.totalQty += q; }

    @Override
    public String toString() {
        return "(" + aisle + "," + bay + ") qty=" + totalQty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PickStop)) return false;
        PickStop that = (PickStop) o;
        return aisle == that.aisle && bay == that.bay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aisle, bay);
    }
}

