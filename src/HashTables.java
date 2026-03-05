import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class FlashSaleInventoryManager {

    private ConcurrentHashMap<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Integer>> waitingList = new ConcurrentHashMap<>();

    public void addProduct(String productId, int stock) {
        stockMap.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new ConcurrentLinkedQueue<>());
    }

    public int checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return 0;
        return stock.get();
    }

    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return "Product not found";

        while (true) {
            int currentStock = stock.get();
            if (currentStock > 0) {
                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "Success, " + (currentStock - 1) + " units remaining";
                }
            } else {
                ConcurrentLinkedQueue<Integer> queue = waitingList.get(productId);
                queue.add(userId);
                return "Added to waiting list, position #" + queue.size();
            }
        }
    }

    public Integer getNextFromWaitingList(String productId) {
        ConcurrentLinkedQueue<Integer> queue = waitingList.get(productId);
        if (queue == null) return null;
        return queue.poll();
    }
}

public class HashTables {
    public static void main(String[] args) {

        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        manager.addProduct("IPHONE15_256GB", 100);

        System.out.println("Stock: " + manager.checkStock("IPHONE15_256GB"));

        System.out.println(manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 67890));

        for (int i = 0; i < 98; i++) {
            manager.purchaseItem("IPHONE15_256GB", 20000 + i);
        }

        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999));
    }
}