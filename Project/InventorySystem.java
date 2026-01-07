package Project;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InventorySystem {
    private HashMap<String, Product> products;
    private final String PRODUCT_FILE = "products.csv";       
    private final String TRANSACTION_FILE = "transactions.csv"; 
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public InventorySystem() {
        products = new HashMap<>();
        loadProductsFromFile(); 
    }

    public void addProduct(Product p) {
        products.put(p.getProductID(), p);
        saveProductsToFile(); 
    }

    public Product getProduct(String id) { return products.get(id); }
    public ArrayList<Product> getAllProducts() { return new ArrayList<>(products.values()); }

    // --- STOCK IN (Perlu Outlet ID) ---
    public String stockIn(String productID, int qty, String outletId, String employeeName) {
        Product p = products.get(productID);
        if (p == null) return "Error: Product ID not found.";

        int current = p.getQuantity(outletId);
        p.setQuantity(outletId, current + qty);
        
        saveProductsToFile();
        logTransaction("STOCK_IN (" + outletId + ")", productID, qty, employeeName);
        return "Success: Added " + qty + " to " + outletId + ". Total: " + p.getQuantity(outletId);
    }

    // --- STOCK OUT (Perlu Outlet ID) ---
    public String stockOut(String productID, int qty, String outletId, String employeeName, String reason) {
        Product p = products.get(productID);
        if (p == null) return "Error: Product ID not found.";
        
        int current = p.getQuantity(outletId);
        if (current < qty) return "Error: Insufficient Stock at " + outletId + " (Current: " + current + ")";

        p.setQuantity(outletId, current - qty);
        saveProductsToFile();
        logTransaction("STOCK_OUT (" + outletId + " - " + reason + ")", productID, qty, employeeName);
        return "Success: Removed " + qty + " from " + outletId + ". Left: " + p.getQuantity(outletId);
    }

    public String readTransactionLogs() {
        File file = new File(TRANSACTION_FILE);
        if (!file.exists()) return "No transaction history found.";
        StringBuilder sb = new StringBuilder("=== TRANSACTION HISTORY ===\n");
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) sb.append(sc.nextLine()).append("\n");
        } catch (Exception e) {}
        return sb.toString(); 
    }

    // --- CSV LOADER (Updated for Multi-Branch) ---
    private void loadProductsFromFile() {
        File file = new File(PRODUCT_FILE);
        if (!file.exists()) {
            // Data Dummy Awal
            Product p1 = new Product("W001", "Rolex Submariner", 45000);
            p1.setQuantity("KLCC", 5);
            p1.setQuantity("UM_Central", 2);
            addProduct(p1);
            return;
        }
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] d = line.split(",");
                if (d.length >= 4) {
                    Product p = new Product(d[0], d[1], Double.parseDouble(d[2]));
                    
                    // Parse Stok: "KLCC:10;UM:5"
                    String[] branches = d[3].split(";");
                    for (String b : branches) {
                        String[] pair = b.split(":");
                        if (pair.length == 2) {
                            p.setQuantity(pair[0], Integer.parseInt(pair[1]));
                        }
                    }
                    products.put(d[0], p);
                }
            }
        } catch (Exception e) { 
            System.out.println("Error loading products: " + e.getMessage()); 
        }
    }

    private void saveProductsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUCT_FILE, false))) {
            for (Product p : products.values()) {
                writer.write(p.toString());
                writer.newLine();
            }
        } catch (IOException e) { System.out.println("Failed to save stock data."); }
    }

    private void logTransaction(String type, String pid, int qty, String name) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTION_FILE, true))) {
            writer.write(LocalDateTime.now().format(dtf) + "," + type + "," + pid + "," + qty + "," + name);
            writer.newLine();
        } catch (IOException e) {}
    }
}