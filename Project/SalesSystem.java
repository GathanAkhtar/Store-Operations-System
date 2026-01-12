package Project;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SalesSystem {
    private InventorySystem inventorySystem;
    private final String HISTORY_FILE = "sales_history.csv";

    public SalesSystem(InventorySystem inventorySystem) {
        this.inventorySystem = inventorySystem;
    }

    public SalesTransaction createNewTransaction(String customerName, Employee employee) {
        return new SalesTransaction(customerName, employee);
    }

    public String addItemToTransaction(SalesTransaction transaction, String productID, int quantity) {
        // 1. Ambil produk dari inventory
        Product p = inventorySystem.getProduct(productID);
        if (p == null) return "Error: Product ID '" + productID + "' not found!";

        // 2. Ambil Outlet ID dari karyawan yang sedang login
        String staffOutlet = transaction.getEmployee().getOutletId().trim();

        // 3. Cek stok di outlet spesifik tersebut
        int currentStock = p.getQuantity(staffOutlet);

        if (currentStock < quantity) {
            return "Insufficient Stock!\n" +
                   "Product: " + p.getName() + "\n" +
                   "Outlet: " + staffOutlet + "\n" +
                   "Available: " + currentStock + "\n" +
                   "Requested: " + quantity;
        }

        transaction.addItem(p, quantity);
        return "Item added to cart.";
    }

    public String finalizeTransaction(SalesTransaction transaction, double amountPaid, String paymentMethod) {
        if (amountPaid < transaction.getTotalPrice()) return "ERROR: Insufficient Payment.";
        
        String outlet = transaction.getEmployee().getOutletId();
        
        // 4. Update stok di file inventory secara otomatis
        for (SalesTransaction.CartItem item : transaction.getItems()) {
            inventorySystem.stockOut(
                item.getProduct().getProductID(), 
                item.getQuantity(), 
                outlet, 
                "SALE", 
                "Sold to " + transaction.getCustomerName()
            );
        }

        saveToHistory(transaction, paymentMethod);
        
        // Mengembalikan teks struk (receipt) untuk ditampilkan di popup
        return generateReceipt(transaction, amountPaid, (amountPaid - transaction.getTotalPrice()), paymentMethod);
    }

    private void saveToHistory(SalesTransaction t, String method) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_FILE, true))) {
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            StringBuilder itemsStr = new StringBuilder("[");
            for (SalesTransaction.CartItem item : t.getItems()) {
                itemsStr.append(item.getProduct().getName()).append(":").append(item.getQuantity()).append("; ");
            }
            itemsStr.append("]");

            // Simpan record sesuai format 9 kolom yang diminta
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%.2f,%s%n",
                now.format(dateFmt),           
                now.format(timeFmt),           
                t.getTransactionID(),          
                t.getCustomerName(),           
                t.getEmployee().getName(),     
                t.getEmployee().getOutletId(), 
                itemsStr.toString(),           
                t.getTotalPrice(),             
                method                         
            );
        } catch (IOException e) {
            System.out.println("Error saving sales: " + e.getMessage());
        }
    }

    private String generateReceipt(SalesTransaction t, double paid, double change, String method) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("==============================\n");
        sb.append("       GOLDEN HOUR RETAIL     \n");
        sb.append("==============================\n");
        sb.append("Date     : ").append(LocalDateTime.now().format(dtf)).append("\n");
        sb.append("Trans ID : ").append(t.getTransactionID()).append("\n");
        sb.append("Customer : ").append(t.getCustomerName()).append("\n");
        sb.append("Staff    : ").append(t.getEmployee().getName()).append("\n");
        sb.append("Outlet   : ").append(t.getEmployee().getOutletId()).append("\n");
        sb.append("------------------------------\n");
        
        for (SalesTransaction.CartItem item : t.getItems()) {
            sb.append(String.format("%-18s x%d\n", item.getProduct().getName(), item.getQuantity()));
            sb.append(String.format("                  RM %8.2f\n", item.getSubTotal()));
        }
        
        sb.append("------------------------------\n");
        sb.append(String.format("TOTAL          :  RM %8.2f\n", t.getTotalPrice()));
        sb.append(String.format("PAID (%-8s):  RM %8.2f\n", method, paid));
        sb.append(String.format("CHANGE         :  RM %8.2f\n", change));
        sb.append("==============================\n");
        sb.append("   Thank You for Shopping!    \n");
        sb.append("==============================\n");
        
        return sb.toString();
    }

    public String readSalesHistory() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        } catch (IOException e) { return "No sales history found."; }
        return sb.toString();
    }
}