package Project;

public class Product {
    private String productID;
    private String name;
    private double price;
    private int quantity;

    public Product(String productID, String name, double price, int quantity) {
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public String getProductID() { return productID; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    // Setters (Untuk update stok)
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Format CSV: ID,Nama,Harga,Qty
    @Override
    public String toString() {
        return productID + "," + name + "," + price + "," + quantity;
    }
}