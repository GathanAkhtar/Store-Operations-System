package Project;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Product {
    private String productID;
    private String name;
    private double price;
    // Map: Key = Nama Outlet (KLCC), Value = Jumlah Stok
    private HashMap<String, Integer> stockDistribution;

    public Product(String productID, String name, double price) {
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.stockDistribution = new HashMap<>();
    }

    public String getProductID() { return productID; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    
    // Ambil stok spesifik cabang
    public int getQuantity(String outletId) {
        return stockDistribution.getOrDefault(outletId, 0);
    }
    
    // Ambil total stok semua cabang (untuk view global)
    public int getTotalQuantity() {
        int total = 0;
        for (int qty : stockDistribution.values()) total += qty;
        return total;
    }

    // Set stok cabang tertentu
    public void setQuantity(String outletId, int qty) {
        stockDistribution.put(outletId, qty);
    }

    // String untuk CSV: "ID,Name,Price,KLCC:10;UMCentral:5"
    @Override
    public String toString() {
        String stockStr = stockDistribution.entrySet().stream()
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining(";"));
            
        if (stockStr.isEmpty()) stockStr = "None:0";
        
        return productID + "," + name + "," + price + "," + stockStr;
    }
    
    // String untuk tampilan tabel
    public String getStockDisplay() {
        if (stockDistribution.isEmpty()) return "Out of Stock";
        return stockDistribution.entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .collect(Collectors.joining(" | "));
    }
}