package Project;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// ============================================
// CLASS 1: SalesItem (Item dalam 1 transaksi)
// ============================================
class SalesItem {
    private String productID;
    private String productName;
    private int quantity;
    private double unitPrice;
    
    public SalesItem(String productID, String productName, int quantity, double unitPrice) {
        this.productID = productID;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    public String getProductID() { return productID; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    
    public double getSubtotal() {
        return quantity * unitPrice;
    }
    
    // Format untuk CSV
    public String toCSV() {
        return productID + ";" + productName + ";" + quantity + ";" + unitPrice;
    }
    
    @Override
    public String toString() {
        return String.format("%-10s %-25s %3d x RM%-8.2f = RM%-8.2f", 
            productID, productName, quantity, unitPrice, getSubtotal());
    }
}


// ============================================
// CLASS 2: SalesTransaction
// ============================================
class SalesTransaction {
    private String transactionID;
    private LocalDateTime dateTime;
    private String customerName;
    private ArrayList<SalesItem> items;
    private String paymentMethod;
    private double totalAmount;
    private String employeeName;
    private String employeeID;
    
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public SalesTransaction(String customerName, String employeeName, String employeeID) {
        this.transactionID = generateTransactionID();
        this.dateTime = LocalDateTime.now();
        this.customerName = customerName;
        this.employeeName = employeeName;
        this.employeeID = employeeID;
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
    }
    
    private String generateTransactionID() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "TRX" + LocalDateTime.now().format(fmt);
    }
    
    public void addItem(SalesItem item) {
        items.add(item);
        calculateTotal();
    }
    
    public void setPaymentMethod(String method) {
        this.paymentMethod = method;
    }
    
    private void calculateTotal() {
        totalAmount = 0.0;
        for (SalesItem item : items) {
            totalAmount += item.getSubtotal();
        }
    }
    
    // Getters
    public String getTransactionID() { return transactionID; }
    public String getCustomerName() { return customerName; }
    public ArrayList<SalesItem> getItems() { return items; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getTotalAmount() { return totalAmount; }
    public String getEmployeeName() { return employeeName; }
    
    public String getFormattedDate() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.format(fmt);
    }
    
    public String getFormattedTime() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
        return dateTime.format(fmt);
    }
    
    // Generate receipt text
    public String generateReceipt() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=".repeat(70)).append("\n");
        sb.append("                    GOLDENHOUR SALES RECEIPT\n");
        sb.append("=".repeat(70)).append("\n\n");
        
        sb.append(String.format("Transaction ID : %s\n", transactionID));
        sb.append(String.format("Date           : %s\n", getFormattedDate()));
        sb.append(String.format("Time           : %s\n", getFormattedTime()));
        sb.append(String.format("Customer       : %s\n", customerName));
        sb.append(String.format("Served By      : %s (%s)\n", employeeName, employeeID));
        sb.append("\n");
        
        sb.append("-".repeat(70)).append("\n");
        sb.append(String.format("%-10s %-25s %10s %12s %12s\n", 
            "ITEM ID", "NAME", "QTY", "UNIT PRICE", "SUBTOTAL"));
        sb.append("-".repeat(70)).append("\n");
        
        for (SalesItem item : items) {
            sb.append(item.toString()).append("\n");
        }
        
        sb.append("-".repeat(70)).append("\n");
        sb.append(String.format("%58s RM%-10.2f\n", "TOTAL:", totalAmount));
        sb.append(String.format("Payment Method : %s\n", paymentMethod));
        sb.append("-".repeat(70)).append("\n\n");
        
        sb.append("                 Thank you for shopping at GoldenHour!\n");
        sb.append("=".repeat(70)).append("\n\n\n");
        
        return sb.toString();
    }
    
    // Format untuk CSV log
    public String toCSV() {
        StringBuilder itemsStr = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            itemsStr.append(items.get(i).toCSV());
            if (i < items.size() - 1) itemsStr.append("|");
        }
        
        return dateTime.format(dtf) + "," + transactionID + "," + customerName + "," +
               itemsStr.toString() + "," + totalAmount + "," + paymentMethod + "," +
               employeeName + "," + employeeID;
    }
}


// ============================================
// CLASS 3: SalesSystem (Main)
// ============================================
public class SalesSystem {
    private ArrayList<SalesTransaction> allTransactions;
    private InventorySystem inventory;
    
    private final String SALES_FILE = "sales.csv";
    private final String RECEIPT_PREFIX = "receipt_";
    
    public SalesSystem(InventorySystem inventory) {
        this.inventory = inventory;
        this.allTransactions = new ArrayList<>();
        loadSalesFromFile();
    }
    
    // === CREATE NEW TRANSACTION ===
    public SalesTransaction createNewTransaction(String customerName, Employee employee) {
        return new SalesTransaction(customerName, employee.getName(), employee.getUserID());
    }
    
    // === ADD ITEM TO TRANSACTION ===
    public String addItemToTransaction(SalesTransaction transaction, String productID, int quantity) {
        // Get product dari inventory
        Product product = inventory.getProduct(productID);
        
        if (product == null) {
            return "ERROR: Product ID not found!";
        }
        
        if (product.getQuantity() < quantity) {
            return "ERROR: Insufficient stock! Available: " + product.getQuantity();
        }
        
        // Create sales item
        SalesItem item = new SalesItem(
            product.getProductID(),
            product.getName(),
            quantity,
            product.getPrice()
        );
        
        transaction.addItem(item);
        return "Item added: " + product.getName() + " x" + quantity;
    }
    
    // === FINALIZE TRANSACTION ===
    public String finalizeTransaction(SalesTransaction transaction, String paymentMethod, String employeeName) {
        if (transaction.getItems().isEmpty()) {
            return "ERROR: No items in transaction!";
        }
        
        // Set payment method
        transaction.setPaymentMethod(paymentMethod);
        
        // Update stock untuk setiap item (menggunakan stockOut dari InventorySystem)
        for (SalesItem item : transaction.getItems()) {
            String result = inventory.stockOut(
                item.getProductID(), 
                item.getQuantity(), 
                employeeName, 
                "SALE to " + transaction.getCustomerName()
            );
            
            if (result.startsWith("Error")) {
                return "ERROR: Failed to update stock for " + item.getProductName();
            }
        }
        
        // Save transaction
        allTransactions.add(transaction);
        saveSalesToFile(transaction);
        
        // Generate receipt
        String receipt = transaction.generateReceipt();
        saveReceiptToFile(transaction, receipt);
        
        return "SUCCESS: Transaction completed!\n" +
               "Total: RM" + String.format("%.2f", transaction.getTotalAmount()) + "\n" +
               "Receipt saved to: " + RECEIPT_PREFIX + transaction.getFormattedDate() + ".txt";
    }
    
    // === GET TRANSACTION SUMMARY ===
    public String getTransactionSummary(SalesTransaction transaction) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== TRANSACTION SUMMARY ===\n\n");
        sb.append("Customer: ").append(transaction.getCustomerName()).append("\n");
        sb.append("Date: ").append(transaction.getFormattedDate()).append("\n");
        sb.append("Time: ").append(transaction.getFormattedTime()).append("\n\n");
        
        sb.append("Items:\n");
        for (SalesItem item : transaction.getItems()) {
            sb.append("- ").append(item.getProductName())
              .append(" x").append(item.getQuantity())
              .append(" @ RM").append(String.format("%.2f", item.getUnitPrice()))
              .append(" = RM").append(String.format("%.2f", item.getSubtotal()))
              .append("\n");
        }
        
        sb.append("\nTOTAL: RM").append(String.format("%.2f", transaction.getTotalAmount()));
        
        return sb.toString();
    }
    
    // === FILE OPERATIONS ===
    
    private void loadSalesFromFile() {
        File file = new File(SALES_FILE);
        if (!file.exists()) return;
        
        // Load existing sales (optional - untuk search/report feature nanti)
        // Implementasi nanti kalau butuh search sales history
    }
    
    private void saveSalesToFile(SalesTransaction transaction) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SALES_FILE, true))) {
            writer.write(transaction.toCSV());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Failed to save sales data: " + e.getMessage());
        }
    }
    
    private void saveReceiptToFile(SalesTransaction transaction, String receipt) {
        String filename = RECEIPT_PREFIX + transaction.getFormattedDate() + ".txt";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(receipt);
        } catch (IOException e) {
            System.out.println("Failed to save receipt: " + e.getMessage());
        }
    }
    
    // === READ SALES HISTORY (untuk manager) ===
    public String readSalesHistory() {
        File file = new File(SALES_FILE);
        if (!file.exists()) return "No sales history found.";
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== SALES HISTORY ===\n\n");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 8) {
                    sb.append(String.format("Time: %s | TRX: %s | Customer: %s | Total: RM%.2f | Staff: %s\n",
                        data[0], data[1], data[2], Double.parseDouble(data[4]), data[6]));
                }
            }
        } catch (IOException e) {
            return "Error reading sales history.";
        }
        
        return sb.toString();
    }
}