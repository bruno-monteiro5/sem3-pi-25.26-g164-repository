package Repository;

import Classes.Warehouse;

public class WarehouseRepository {
    public static Warehouse currentWarehouse = null;

    public static Warehouse getCurrentWarehouse(){
        return currentWarehouse;
    }

    public static void setCurrentWarehouse(Warehouse warehouse){
        currentWarehouse = warehouse;
    }
}
