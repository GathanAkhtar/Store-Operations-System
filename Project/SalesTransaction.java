package Project;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SalesTransaction {
    private String transactionID;
    private String customerName;
    private Employee employee;
    private ArrayList<LineItem> items;
    private double totalPrice;
    private LocalDateTime timestamp;
    
    // --- TAMBAHAN BARU ---
    private String paymentMethod; 

    public SalesTransaction(String customerName, Employee employee) {
        this.transactionID = "TX-" + System.currentTimeMillis();
        this.customerName = customerName;
        this.employee = employee;
        this.items = new ArrayList<>();
        this.totalPrice = 0.0;
        this.timestamp = LocalDateTime.now();
        this.paymentMethod = "Cash"; // Default
    }

    public void addItem(Product product, int qty) {
        items.add(new LineItem(product, qty));
        totalPrice += (product.getPrice() * qty);
    }
    
    // --- Setter & Getter Baru ---
    public void setPaymentMethod(String method) { this.paymentMethod = method; }
    public String getPaymentMethod() { return paymentMethod; }

    public ArrayList<LineItem> getItems() { return items; }
    public String getTransactionID() { return transactionID; }
    public String getCustomerName() { return customerName; }
    public Employee getEmployee() { return employee; }
    public double getTotalPrice() { return totalPrice; }
    public String getFormattedDate() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public class LineItem {
        Product product;
        int quantity;
        double subtotal;

        public LineItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
            this.subtotal = product.getPrice() * quantity;
        }
        
        @Override
        public String toString() {
            return String.format("%-15s x%d (RM %.2f)", product.getName(), quantity, subtotal);
        }
    }
}