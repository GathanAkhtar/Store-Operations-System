package Project;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InventorySystem {
    // === 1. VARIABLES & CONSTANTS ===
    private HashMap<String, Product> products;
    
    // Database File Names
    private final String PRODUCT_FILE = "products.csv";       
    private final String TRANSACTION_FILE = "transactions.csv"; 
    private final String COUNT_FILE = "stock_counts.csv";       

    // Date Format
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // === 2. CONSTRUCTOR ===
    public InventorySystem() {
        products = new HashMap<>();
        // Load data from file when program starts
        loadProductsFromFile(); 
    }

    // ========================================================
    // === 3. PUBLIC METHODS (CALLED BY TESTER/GUI) ===
    // ========================================================

    // A. Manager Feature: Add New Product
    public void addProduct(Product p) {
        products.put(p.getProductID(), p);
        saveProductsToFile(); // Save changes to file
    }

    // B. Get Product Data
    public Product getProduct(String id) { 
        return products.get(id); 
    }
    
    public ArrayList<Product> getAllProducts() { 
        return new ArrayList<>(products.values()); 
    }

    // C. Staff Feature: Stock In (Receive Goods)
    public String stockIn(String productID, int qty, String employeeName) {
        Product p = products.get(productID);
        if (p == null) return "Error: Product ID not found.";

        // Update RAM
        p.setQuantity(p.getQuantity() + qty);
        
        // Save to File & Log Transaction
        saveProductsToFile();
        logTransaction("STOCK_IN", productID, qty, employeeName);
        
        return "Success: Stock In recorded. New Qty: " + p.getQuantity();
    }

    // D. Staff Feature: Stock Out (Sales/Transfer)
    public String stockOut(String productID, int qty, String employeeName, String reason) {
        Product p = products.get(productID);
        if (p == null) return "Error: Product ID not found.";
        if (p.getQuantity() < qty) return "Error: Insufficient Stock!";

        // Update RAM
        p.setQuantity(p.getQuantity() - qty);
        
        // Save to File & Log Transaction
        saveProductsToFile();
        logTransaction("STOCK_OUT (" + reason + ")", productID, qty, employeeName);
        
        return "Success: Stock Out recorded. New Qty: " + p.getQuantity();
    }

    // E. Staff Feature: Audit Stock (Physical Count)
    public String performStockCount(String session, String productID, int physicalQty, String employeeName) {
        Product p = products.get(productID);
        if (p == null) return "Error: Product ID not found.";

        int variance = physicalQty - p.getQuantity();
        String status = (variance == 0) ? "MATCH" : "MISMATCH";

        // Write directly to audit file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COUNT_FILE, true))) {
            String log = LocalDateTime.now().format(dtf) + "," + session + "," + employeeName + "," + 
                         productID + "," + physicalQty + "," + variance + "," + status;
            writer.write(log); 
            writer.newLine();
        } catch (IOException e) {
            return "Error saving count log.";
        }

        return (variance == 0) ? "Perfect Match!" : "Mismatch Recorded.";
    }

    // F. Manager Feature: View Transaction History
    public String readTransactionLogs() {
        File file = new File(TRANSACTION_FILE);
        if (!file.exists()) return "No transaction history found.";

        StringBuilder sb = new StringBuilder();
        sb.append("=== TRANSACTION HISTORY (FROM FILE) ===\n");
        sb.append(String.format("%-20s %-25s %-10s %-10s %-10s\n", "Time", "Type", "ID", "Qty", "Staff"));
        sb.append("--------------------------------------------------------------------------------\n");

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] d = line.split(",");
                if (d.length >= 5) {
                    sb.append(String.format("%-20s %-25s %-10s %-10s %-10s\n", d[0], d[1], d[2], d[3], d[4]));
                }
            }
        } catch (Exception e) { return "Error reading log file."; }
        
        return sb.toString(); 
    }

    // ========================================================
    // === 4. PRIVATE HELPER METHODS (INTERNAL USE ONLY) ===
    // ========================================================

    // Method 1: Load file when program starts
    private void loadProductsFromFile() {
        File file = new File(PRODUCT_FILE);
        
        if (!file.exists()) {
            // Create Dummy Data if file doesn't exist
            addProduct(new Product("W001", "Rolex Submariner", 45000, 10)); 
            return;
        }

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] d = line.split(",");
                if (d.length == 4) {
                    // CSV Format: ID, Name, Price, Qty
                    Product p = new Product(d[0], d[1], Double.parseDouble(d[2]), Integer.parseInt(d[3]));
                    products.put(d[0], p);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }

    // Method 2: Save all products to CSV
    private void saveProductsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUCT_FILE, false))) { // false = overwrite
            for (Product p : products.values()) {
                // Manually construct the string: ID,Name,Price,Qty
                String line = p.getProductID() + "," + p.getName() + "," + p.getPrice() + "," + p.getQuantity();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) { 
            System.out.println("Failed to save stock data."); 
        }
    }

    // Method 3: Log transaction (Append mode)
    private void logTransaction(String type, String pid, int qty, String name) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTION_FILE, true))) { // true = append
            String log = LocalDateTime.now().format(dtf) + "," + type + "," + pid + "," + qty + "," + name;
            writer.write(log);
            writer.newLine();
        } catch (IOException e) { 
            System.out.println("Failed to save transaction log."); 
        }
    }
}