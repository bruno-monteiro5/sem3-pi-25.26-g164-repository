package WarehouseManagementFunctions;

import Classes.*;
import Repository.WarehouseRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class OrderEligibilityNdAllocation implements Runnable{
    Warehouse warehouse = WarehouseRepository.getCurrentWarehouse();
    List<Order> orders = new ArrayList<>();
    String filenameorders = "warehouseMngt/train_station_dataset/orders.csv";
    String filenameorderlines = "warehouseMngt/train_station_dataset/order_lines.csv";
    Map<Order, List<Box>> allocation = new HashMap<>();

    public void run(){
        try {
            fillOrders();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("############ORDERS LIST############");
        for(Order o: orders){
            System.out.println(o.toString());
        }
        System.out.println("############ORDER LIST FINISHED############");
        allocateBoxes();


    }

    public void fillOrders() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filenameorders));
        String line = sc.nextLine();
        while(sc.hasNextLine()){
            line = sc.nextLine();
            String[] linesplit = line.split(",");
            orders.add(new Order(linesplit[0], Integer.parseInt(linesplit[2]), linesplit[1]));
        }
        for(Order o: orders){
            System.out.println(o.toString());
        }
        sc = new Scanner(new File(filenameorderlines));
        line = sc.nextLine();
        while(sc.hasNextLine()){
            line = sc.nextLine();
            String[] linesplit = line.split(",");
            Order order = null;
            for(Order o : orders){
                if(o.getOrderID().equals(linesplit[0])){
                    order = o;
                    break;
                }
            }
            order.addLine(new OrderLine(Integer.parseInt(linesplit[1]), linesplit[2], Integer.parseInt(linesplit[3])));
        }
        sc.close();
    }

    public void allocateBoxes(){
        for(Order o: orders){
            System.out.println("###########ORDER: " + o.toString());
            for(OrderLine ol: o.getLines()){
                System.out.println("###########ORDERLINE: " + ol.toString());
                int quant = ol.getQuantity();
                for(Aisle a: warehouse.getAisles()){
                    for(Bay b: a.getBays()){
                        for(Box bo: b.getInventory()){
                            if((bo.getExpDate() != null && bo.getExpDate().compareTo(o.getDueDate()) < 0) || !bo.getProduct().getSkuID().equals(ol.getSku())) {
                                System.out.println(bo.toString() + " - UNDISPATCHBLE");
                            }else if(quant == 0){
                                System.out.println(bo.toString() + " - UNDISPATCHBLE");
                            }else if(bo.getProduct().getSkuID().equals(ol.getSku()) && bo.getQuantity()<=quant){
                                System.out.println(bo.toString() + " - ELIGIBLE");
                                quant = quant - bo.getQuantity();
                            }else if(bo.getProduct().getSkuID().equals(ol.getSku()) && bo.getQuantity()>quant) {
                                System.out.println(bo.toString() + " - PARTIAL");
                                quant = 0;
                            }else{
                                System.out.println(bo.toString() + " - UNDISPATCHBLE");
                            }
                        }
                    }
                }
                System.out.println("ORDER LINE FINISHED###############");
            }
            System.out.println("###########ORDER FINISHED: " + o.toString());
        }
    }
}
