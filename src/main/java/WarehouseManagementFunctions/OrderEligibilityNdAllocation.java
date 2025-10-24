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
    List<Allocation> allocation = new ArrayList<>();

    public void run(){
        try {
            // tenta preencher a lista de ordens com os dados do arquivo
            fillOrders();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        // escreve todos os conteúdos da lista se a mesma for preenchida com sucesso
        System.out.println("############ORDERS LIST############");
        for(Order o: orders){
            System.out.println(o.toString());
        }
        System.out.println("############ORDER LIST FINISHED############");

        // verifica quais caixas podem ser alocadas para os pedidos
        allocateBoxesChoice();


        // escreve todas as alocações feitas para os pedidos
        for(Allocation a : allocation){
            System.out.println("############ALLOCATION RESULT############");
            System.out.println(a.toString());
            System.out.println("############ALLOCATION RESULT FINISHED############");
        }

        WarehouseRepository.setAllocation(allocation);

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
    private static final double MAX_ALLOCATION_KG = 200.0;

    private int maxUnitsPerAllocation(double unitWeight) {
        if (unitWeight <= 0.0) return 0;
        return (int) Math.floor(MAX_ALLOCATION_KG / unitWeight);
    }

    private void addSlicedAllocations(Order o, OrderLine ol, Aisle a, Bay b, Box sourceBox, int takeQty, List<Allocation> out) {
        double unitW = sourceBox.getProduct().getWeight();
        int maxPerAlloc = maxUnitsPerAllocation(unitW);
        if (maxPerAlloc <= 0) {
            System.out.println(sourceBox + " - UNDISPATCHBLE (invalid unit weight)");
            return;
        }

        int remaining = takeQty;
        while (remaining > 0) {
            int chunk = Math.min(remaining, maxPerAlloc);
            Allocation part = new Allocation(
                    o, ol, ol.getSku(),
                    new Box(sourceBox.getProduct(), chunk, sourceBox.getExpDate()),
                    a, b,
                    chunk,
                    unitW
            );
            out.add(part);
            remaining -= chunk;
        }
    }


    public void allocateBoxes(boolean strict) {
        if (!strict) {
            for (Order o : orders) {
                System.out.println("###########ORDER: " + o);
                for (OrderLine ol : o.getLines()) {
                    System.out.println("###########ORDERLINE: " + ol);
                    int toAllocate = ol.getQuantity();

                    for (Aisle a : warehouse.getAisles()) {
                        if (toAllocate == 0) break;
                        for (Bay b : a.getBays()) {
                            if (toAllocate == 0) break;
                            for (Box bo : b.getInventory()) {
                                if (toAllocate == 0) break;

                                boolean skuOk = bo.getProduct().getSkuID().equals(ol.getSku());
                                boolean dateOk = (bo.getExpDate() == null) || (o.getDueDate().compareTo(bo.getExpDate()) < 0);
                                boolean hasQty = bo.getQuantity() > 0;

                                if (!skuOk || !dateOk || !hasQty) {
                                    System.out.println(bo + " - UNDISPATCHBLE");
                                    continue;
                                }

                                System.out.println(bo + " - PARTIAL");

                                int canTake = Math.min(toAllocate, bo.getQuantity());
                                if (canTake <= 0) continue;

                                addSlicedAllocations(o, ol, a, b, bo, canTake, allocation);

                                bo.setQuantity(bo.getQuantity() - canTake);
                                toAllocate -= canTake;
                            }
                        }
                    }
                    System.out.println("ORDER LINE FINISHED###############");
                }
                System.out.println("###########ORDER FINISHED: " + o);
            }
        } else {
            for (Order o : orders) {
                System.out.println("###########ORDER: " + o);
                for (OrderLine ol : o.getLines()) {
                    System.out.println("###########ORDERLINE: " + ol);

                    boolean allocated = false;

                    outer:
                    for (Aisle a : warehouse.getAisles()) {
                        for (Bay b : a.getBays()) {
                            for (Box bo : b.getInventory()) {
                                boolean skuOk = bo.getProduct().getSkuID().equals(ol.getSku());
                                boolean dateOk = (bo.getExpDate() == null) || (bo.getExpDate().compareTo(o.getDueDate()) > 0);
                                boolean exactQty = bo.getQuantity() == ol.getQuantity();

                                if (skuOk && dateOk && exactQty) {
                                    System.out.println(bo + " - ELIGIBLE");

                                    addSlicedAllocations(o, ol, a, b, bo, bo.getQuantity(), allocation);

                                    bo.setQuantity(0);
                                    allocated = true;
                                    break outer;
                                } else {
                                    System.out.println(bo + " - UNDISPATCHBLE");
                                }
                            }
                        }
                    }

                    if (!allocated) {
                        System.out.println("No exact box found for STRICT allocation of " + ol);
                    }

                    System.out.println("ORDER LINE FINISHED###############");
                }
                System.out.println("###########ORDER FINISHED: " + o);
            }
        }
    }
}