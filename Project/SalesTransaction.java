package Project;

import java.util.ArrayList;
import java.util.UUID;

public class SalesTransaction {
    private String transactionID;
    private String customerName;
    private Employee employee;
    private ArrayList<CartItem> items; // List barang belanjaan
    private double totalPrice;

    public SalesTransaction(String customerName, Employee employee) {
        this.transactionID = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.customerName = customerName;
        this.employee = employee;
        this.items = new ArrayList<>();
        this.totalPrice = 0.0;
    }

    // === METHOD PENTING (YANG TADI ERROR) ===
    
    // 1. Menambahkan barang ke keranjang
    public void addItem(Product product, int quantity) {
        double subTotal = product.getPrice() * quantity;
        CartItem item = new CartItem(product, quantity, subTotal);
        items.add(item);
        totalPrice += subTotal;
    }

    // 2. Getter untuk List Items (Ini yang dicari oleh t.getItems())
    public ArrayList<CartItem> getItems() {
        return items;
    }

    // === GETTERS LAINNYA ===
    public String getTransactionID() { return transactionID; }
    public String getCustomerName() { return customerName; }
    public Employee getEmployee() { return employee; }
    public double getTotalPrice() { return totalPrice; }

    // ==========================================
    // === INNER CLASS CART ITEM (PENTING!) =====
    // ==========================================
    // Class kecil ini bertugas membungkus Produk + Jumlah Beli
    public class CartItem {
        private Product product;
        private int quantity;
        private double subTotal;

        public CartItem(Product product, int quantity, double subTotal) {
            this.product = product;
            this.quantity = quantity;
            this.subTotal = subTotal;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public double getSubTotal() { return subTotal; }
    }
}