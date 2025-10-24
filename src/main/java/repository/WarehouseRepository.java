package repository;

import domain.*;

import java.util.List;
import java.util.Map;

public class WarehouseRepository {
    public static Warehouse currentWarehouse = null;

    public static List<Allocation> allocation = null;

    public static Warehouse getCurrentWarehouse(){
        return currentWarehouse;
    }

    public static void setCurrentWarehouse(Warehouse warehouse){
        currentWarehouse = warehouse;
    }

    public static List<Allocation> getAllocation(){
        return allocation;
    }

    public static void setAllocation(List<Allocation> allocation){
        WarehouseRepository.allocation = allocation;
    }
}
