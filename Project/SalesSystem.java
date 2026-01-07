package Project;

import java.io.*;
import java.util.Scanner;

public class SalesSystem {
    private InventorySystem inventorySystem;
    private final String SALES_FILE = "sales_history.csv";

    public SalesSystem(InventorySystem inventorySystem) {
        this.inventorySystem = inventorySystem;
    }

    public SalesTransaction createNewTransaction(String customerName, Employee employee) {
        return new SalesTransaction(customerName, employee);
    }

    public String addItemToTransaction(SalesTransaction tx, String productID, int qty) {
        Product p = inventorySystem.getProduct(productID);
        if (p == null) return "ERROR: Product ID not found!";
        
        // --- LOGIC BARU: Cek Stok Cabang ---
        String outlet = tx.getEmployee().getOutletId(); 
        int currentStock = p.getQuantity(outlet);
        
        // Hitung item yang sudah ada di keranjang saat ini
        int inCart = 0;
        for (SalesTransaction.LineItem item : tx.getItems()) {
            if (item.product.getProductID().equals(productID)) inCart += item.quantity;
        }

        if ((qty + inCart) > currentStock) {
            return "ERROR: Insufficient Stock at " + outlet + "! (Avail: " + currentStock + ")";
        }

        tx.addItem(p, qty);
        return "Added: " + p.getName() + " x" + qty;
    }

    public String finalizeTransaction(SalesTransaction tx, double cashGiven, String paymentMethod) {
        if (cashGiven < tx.getTotalPrice()) return "ERROR: Insufficient Payment!";

        tx.setPaymentMethod(paymentMethod);
        String outlet = tx.getEmployee().getOutletId();

        // --- Potong Stok Spesifik Cabang ---
        for (SalesTransaction.LineItem item : tx.getItems()) {
            inventorySystem.stockOut(
                item.product.getProductID(), 
                item.quantity, 
                outlet, // Outlet ID
                tx.getEmployee().getName(), 
                "SALE:" + tx.getTransactionID()
            );
        }
        
        saveSaleToFile(tx);
        double change = cashGiven - tx.getTotalPrice();
        return "Transaction Success (" + paymentMethod + ")!\nChange: RM " + String.format("%.2f", change);
    }

    private void saveSaleToFile(SalesTransaction tx) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SALES_FILE, true))) {
            String record = tx.getFormattedDate() + "," + 
                            tx.getTransactionID() + "," + 
                            tx.getCustomerName() + "," + 
                            tx.getEmployee().getName() + "," + 
                            tx.getTotalPrice() + "," + 
                            tx.getPaymentMethod();
            writer.write(record);
            writer.newLine();
        } catch (IOException e) {}
    }

    public String readSalesHistory() {
        File file = new File(SALES_FILE);
        if (!file.exists()) return "No sales history yet.";
        
        StringBuilder sb = new StringBuilder("=== SALES HISTORY ===\n");
        sb.append(String.format("%-20s %-15s %-10s %-10s %-10s\n", "Date", "Customer", "Staff", "Total", "Method"));
        sb.append("--------------------------------------------------------------------------\n");

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] d = line.split(",");
                if (d.length >= 5) {
                    String method = (d.length > 5) ? d[5] : "Cash";
                    sb.append(String.format("%-20s %-15s %-10s %-10s %-10s\n", d[0], d[2], d[3], d[4], method));
                }
            }
        } catch (Exception e) {}
        return sb.toString();
    }
}