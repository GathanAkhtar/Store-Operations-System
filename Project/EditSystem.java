package Project;

import java.io.*;
import java.util.ArrayList;

public class EditSystem {
    private InventorySystem inventorySystem;

    public EditSystem(InventorySystem inv) {
        this.inventorySystem = inv;
    }

    public String editSalesTransaction(String targetDate, String targetCustName, 
                                       String newCustomer, String newMethod, double newTotal) {
        File file = new File("sales_history.csv");
        if (!file.exists()) return "Error: No sales data found.";

        ArrayList<String> lines = new ArrayList<>();
        boolean found = false;
        String cleanDate = targetDate.trim();
        String cleanName = targetCustName.toLowerCase().trim();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    // Cek Tanggal (Indeks 0) dan Nama Pelanggan (Indeks 3)
                    boolean matchDate = parts[0].trim().equals(cleanDate);
                    boolean matchName = parts[3].toLowerCase().trim().contains(cleanName);

                    if (matchDate && matchName) {
                        found = true;
                        parts[3] = newCustomer; // Update Nama Pelanggan
                        parts[7] = String.format("%.2f", newTotal); // Update Total Harga
                        parts[8] = newMethod;   // Update Metode Pembayaran
                        line = String.join(",", parts);
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) { return "Error reading file."; }

        if (!found) return "Transaction NOT FOUND for " + targetDate + " and " + targetCustName;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) { return "Error saving file."; }

        return "Success! Sales record updated.";
    }

    public String editStockLevel(String productID, String outlet, int newQty) {
        Product p = inventorySystem.findProduct(productID);
        if (p != null) {
            p.setQuantity(outlet, newQty);
            inventorySystem.saveInventory();
            return "Stock updated to " + newQty;
        }
        return "Error: Product ID not found.";
    }
}