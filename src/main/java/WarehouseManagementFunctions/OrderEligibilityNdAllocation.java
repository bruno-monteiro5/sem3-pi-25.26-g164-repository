package WarehouseManagementFunctions;

import domain.*;
import ui.WarehouseManagementMenu;
import repository.WarehouseRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class OrderEligibilityNdAllocation implements Runnable{
    Warehouse warehouse = WarehouseRepository.getCurrentWarehouse();
    List<Order> orders = new ArrayList<>();
    String filenameorders = "warehouseMngt/train_station_dataset/orders.csv";
    String filenameorderlines = "warehouseMngt/train_station_dataset/order_lines.csv";
    Map<Order, Map<OrderLine, List<Box>>> allocation = new HashMap<>();

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

        allocateBoxesChoice();


        allocation.forEach((k,v) -> System.out.println(k.toString() + " - " + v.toString()));

        Runnable WarehouseManagementMenu = new WarehouseManagementMenu();
        WarehouseManagementMenu.run();
    }

    public void fillOrders() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filenameorders));
        String line = sc.nextLine();
        while(sc.hasNextLine()){
            line = sc.nextLine();
            String[] linesplit = line.split(",");
            orders.add(new Order(linesplit[0], Integer.parseInt(linesplit[2]), linesplit[1]));
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
        orders.sort(Comparator.comparing(Order::getPriority));
    }

    public void allocateBoxesChoice(){
        System.out.println("############ALLOCATION############");
        System.out.println("Do you wish to perform strict allocation or partial?[DEFAULT: Strict]");
        System.out.println("1. Strict");
        System.out.println("2. Partial");
        Scanner sc = new Scanner(System.in);
        int choice = sc.nextInt();

        while(choice != 1 && choice != 2){
            System.out.println("Invalid input. Please enter 1 or 2");
            choice = sc.nextInt();
        }
        if(choice == 1){
            allocateBoxes(true);
        }else{
            allocateBoxes(false);
        }

    }

    public void allocateBoxes(boolean strict){
        if(!strict) {
            for (Order o : orders) {
                System.out.println("###########ORDER: " + o.toString());
                Map<OrderLine, List<Box>> temp = new HashMap<>();
                for (OrderLine ol : o.getLines()) {
                    System.out.println("###########ORDERLINE: " + ol.toString());
                    int quant = ol.getQuantity();
                    List<Box> boxt = new ArrayList<>();
                    for (Aisle a : warehouse.getAisles()) {
                        for (Bay b : a.getBays()) {
                            for (Box bo : b.getInventory()) {
                                if (bo.getProduct().getSkuID().equals(ol.getSku()) && (bo.getExpDate() == null || o.getDueDate().compareTo(bo.getExpDate()) < 0)) {
                                    System.out.println(bo.toString() + " - PARTIAL");
                                    if(quant > bo.getQuantity() && bo.getQuantity() > 0) {
                                        boxt.add(new Box(bo.getProduct(), bo.getQuantity()));
                                        quant = quant - bo.getQuantity();
                                        bo.setQuantity(0);
                                    }else {
                                        int tempe = bo.getQuantity();
                                        bo.setQuantity(bo.getQuantity() - quant);
                                        tempe = tempe-bo.getQuantity();
                                        quant = 0;
                                        boxt.add(new Box(bo.getProduct(), tempe));
                                    }
                                }else if ((bo.getExpDate() != null && bo.getExpDate().compareTo(o.getDueDate()) > 0)) {
                                    System.out.println(bo.toString() + " - UNDISPATCHBLE");
                                } else if (quant == 0) {
                                    System.out.println(bo.toString() + " - UNDISPATCHBLE");
                                } else {
                                    System.out.println(bo.toString() + " - UNDISPATCHBLE");
                                }
                            }
                        }
                    }
                    System.out.println("ORDER LINE FINISHED###############");
                    temp.put(ol, boxt);
                }
                System.out.println("###########ORDER FINISHED: " + o.toString());
                allocation.put(o, temp);
            }
        }else{
            for (Order o : orders) {
                System.out.println("###########ORDER: " + o.toString());
                Map<OrderLine, List<Box>> temp = new HashMap<>();
                for (OrderLine ol : o.getLines()) {
                    System.out.println("###########ORDERLINE: " + ol.toString());
                    List<Box> boxt = new ArrayList<>();
                    for(Aisle a : warehouse.getAisles()){
                        for(Bay b : a.getBays()){
                            for(Box bo : b.getInventory()){
                                if((bo.getExpDate() == null || bo.getExpDate().compareTo(o.getDueDate()) > 0) && bo.getProduct().getSkuID().equals(ol.getSku()) && bo.getQuantity() == ol.getQuantity()){
                                    System.out.println(bo.toString() + " - ELIGIBLE");
                                    boxt.add(bo);
                                }else{
                                    System.out.println(bo.toString() + " - UNDISPATCHBLE");
                                }
                            }
                        }
                    }
                    System.out.println("ORDER LINE FINISHED###############");
                    temp.put(ol, boxt);
                }
                System.out.println("###########ORDER FINISHED: " + o.toString());
                allocation.put(o, temp);
            }
        }
    }
}
