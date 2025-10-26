package repository;

import controller.InventoryPort;
import domain.Aisle;
import domain.Bay;
import domain.Box;
import domain.Items;
import domain.Warehouse;
import java.time.LocalDateTime;

public class InventoryAdapter implements InventoryPort {

    @Override
    public void restockReturnedBox(String boxId,
                                   String sku,
                                   int quantity,
                                   String expiryDate,
                                   LocalDateTime receivedAt) throws Exception {
        // Obtém o armazém atual
        Warehouse warehouse = WarehouseRepository.currentWarehouse;
        if (warehouse == null) {
            throw new IllegalStateException("Warehouse não foi inicializado.");
        }

        // Procura o objeto Items correspondente ao SKU
        Items product = null;
        for (Aisle aisle : warehouse.getAisles()) {
            for (Bay bay : aisle.getBays()) {
                for (Box box : bay.getBoxes()) {
                    if (box.getProduct().getSkuID().equals(sku)) {
                        product = box.getProduct();
                        break;
                    }
                }
                if (product != null) break;
            }
            if (product != null) break;
        }
        if (product == null) {
            // Se o produto não estiver presente no estoque, cria um novo Items básico
            // (usando o SKU como identificador e placeholders para outros campos)
            product = new Items(sku, sku, "", "", 0.0f, 0.0f);
        }

        // Cria a nova caixa do produto devolvido
        Box newBox;
        if (expiryDate != null) {
            newBox = new Box(product, quantity, expiryDate);
        } else {
            newBox = new Box(product, quantity);
        }

        // Insere a caixa em um bay com espaço disponível (primeiro disponível)
        boolean placed = false;
        for (Aisle aisle : warehouse.getAisles()) {
            for (Bay bay : aisle.getBays()) {
                if (bay.addProductBox(newBox)) {  // addProductBox retorna true se adicionou com sucesso
                    placed = true;
                    break;
                }
            }
            if (placed) break;
        }

        if (!placed) {
            throw new Exception("Sem espaço disponível no armazém para a caixa devolvida.");
        }
    }
}
