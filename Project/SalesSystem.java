package Project;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SalesSystem {
    private InventorySystem inventorySystem;
    private final String HISTORY_FILE = "sales_history.csv"; // File Database

    public SalesSystem(InventorySystem inventorySystem) {
        this.inventorySystem = inventorySystem;
    }

    // 1. Buat Transaksi Baru
    public SalesTransaction createNewTransaction(String customerName, Employee employee) {
        return new SalesTransaction(customerName, employee);
    }

    // 2. Tambah Barang ke Keranjang
    public String addItemToTransaction(SalesTransaction transaction, String productID, int quantity) {
        Product p = inventorySystem.getProduct(productID);
        if (p == null) return "Product not found!";
        
        int currentStock = p.getQuantity(transaction.getEmployee().getOutletId());
        if (currentStock < quantity) return "Insufficient Stock! Available: " + currentStock;

        transaction.addItem(p, quantity);
        return "Item added to cart.";
    }

    // 3. Finalisasi (Bayar & Simpan)
    public String finalizeTransaction(SalesTransaction transaction, double amountPaid, String paymentMethod) {
        double total = transaction.getTotalPrice();
        if (amountPaid < total) return "ERROR: Insufficient Payment.";

        // A. Potong Stok
        String outlet = transaction.getEmployee().getOutletId();
        for (SalesTransaction.CartItem item : transaction.getItems()) {
            inventorySystem.stockOut(item.getProduct().getProductID(), item.getQuantity(), outlet, "SALE", "Sold to " + transaction.getCustomerName());
        }

        // B. Simpan ke CSV (INI YANG PENTING DIPERBAIKI)
        saveToHistory(transaction, paymentMethod);

        double change = amountPaid - total;
        return generateReceipt(transaction, amountPaid, change, paymentMethod);
    }

    // --- LOGIC SIMPAN CSV YANG BENAR ---
    private void saveToHistory(SalesTransaction t, String method) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_FILE, true))) {
            File f = new File(HISTORY_FILE);
            
            // Format Waktu
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            // Format List Item: "[Kemeja:2; Celana:1]"
            StringBuilder itemsStr = new StringBuilder("[");
            for (SalesTransaction.CartItem item : t.getItems()) {
                itemsStr.append(item.getProduct().getName())
                        .append(":").append(item.getQuantity()).append("; ");
            }
            itemsStr.append("]");

            // TULIS KE FILE (9 Kolom)
            // Urutan: Date, Time, ID, Cust, Staff, Outlet, Items, Total, Method
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%.2f,%s%n",
                now.format(dateFmt),           // Kolom 0
                now.format(timeFmt),           // Kolom 1
                t.getTransactionID(),          // Kolom 2
                t.getCustomerName(),           // Kolom 3
                t.getEmployee().getName(),     // Kolom 4
                t.getEmployee().getOutletId(), // Kolom 5 (PENTING)
                itemsStr.toString(),           // Kolom 6 (PENTING BUAT SEARCH ITEM)
                t.getTotalPrice(),             // Kolom 7
                method                         // Kolom 8
            );
        } catch (IOException e) {
            System.out.println("Error saving sales history: " + e.getMessage());
        }
    }

    // 4. Baca History (Opsional, untuk Manager View)
    public String readSalesHistory() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            return "No sales history found.";
        }
        return sb.toString();
    }

    // 5. Cetak Struk
    private String generateReceipt(SalesTransaction t, double paid, double change, String method) {
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------\n");
        sb.append("       GOLDEN HOUR RETAIL     \n");
        sb.append("------------------------------\n");
        sb.append("Trans ID: ").append(t.getTransactionID()).append("\n");
        sb.append("Customer: ").append(t.getCustomerName()).append("\n");
        sb.append("Outlet:   ").append(t.getEmployee().getOutletId()).append("\n");
        sb.append("------------------------------\n");
        for (SalesTransaction.CartItem item : t.getItems()) {
            sb.append(item.getProduct().getName())
              .append(" x").append(item.getQuantity())
              .append(" = RM ").append(String.format("%.2f", item.getSubTotal())).append("\n");
        }
        sb.append("------------------------------\n");
        sb.append("TOTAL:     RM ").append(String.format("%.2f", t.getTotalPrice())).append("\n");
        sb.append("PAID (").append(method).append("): RM ").append(String.format("%.2f", paid)).append("\n");
        sb.append("CHANGE:    RM ").append(String.format("%.2f", change)).append("\n");
        sb.append("------------------------------\n");
        sb.append("   Thank you for shopping!    \n");
        return sb.toString();
    }
}