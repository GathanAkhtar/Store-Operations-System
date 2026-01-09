package Project;

import java.io.*;
import java.util.ArrayList;

public class EditSystem {
    
    private InventorySystem inventorySystem;

    public EditSystem(InventorySystem inv) {
        this.inventorySystem = inv;
    }

    // 1. EDIT STOCK LEVEL (Tidak Berubah)
    public String editStockLevel(String productID, String outlet, int newQty) {
        Product p = inventorySystem.findProduct(productID);
        if (p != null) {
            p.setQuantity(outlet, newQty);
            inventorySystem.saveInventory(); 
            return "Stock updated successfully to " + newQty;
        }
        return "Error: Product ID not found.";
    }

    // 2. KOREKSI SALES (Revised: AUTO-DETECT COLUMN)
    public String editSalesTransaction(String targetDate, String targetCustName, 
                                       String newCustomer, String newMethod, double newTotal) {
        
        File file = new File("sales_history.csv");
        if (!file.exists()) return "Error: No sales data found.";

        ArrayList<String> lines = new ArrayList<>();
        boolean found = false;
        String foundDetails = ""; 

        // Bersihkan input user
        String cleanDate = targetDate.trim();
        String cleanName = targetCustName.toLowerCase().trim();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                
                // Minimal ada 4 kolom untuk diproses
                if (parts.length > 3) {
                    String csvDate = parts[0]; // Tanggal biasanya di paling depan (Index 0)
                    
                    // --- LOGIKA PENCARIAN PINTAR ---
                    // Cek Tanggal (Index 0)
                    boolean matchDate = csvDate.startsWith(cleanDate); 

                    // Cek Nama (Bisa di Index 2 atau Index 3, tergantung format file)
                    int nameIndex = -1;
                    if (parts[2].toLowerCase().contains(cleanName)) {
                        nameIndex = 2; // Format Lama (DateTime, ID, Nama)
                    } else if (parts.length > 3 && parts[3].toLowerCase().contains(cleanName)) {
                        nameIndex = 3; // Format Baru (Date, Time, ID, Nama)
                    }

                    // Jika Tanggal COCOK dan Nama DITEMUKAN
                    if (matchDate && nameIndex != -1) {
                        found = true;
                        foundDetails = parts[nameIndex]; // Simpan nama asli

                        // --- UPDATE DATA ---
                        // 1. Ganti Nama di kolom yang ditemukan tadi
                        parts[nameIndex] = newCustomer;

                        // 2. Ganti Payment Method (Biasanya kolom TERAKHIR)
                        int lastIdx = parts.length - 1;
                        parts[lastIdx] = newMethod;

                        // 3. Ganti Total Harga (Biasanya kolom SEBELUM TERAKHIR)
                        int totalIdx = parts.length - 2;
                        parts[totalIdx] = String.valueOf(newTotal);

                        // Gabungkan kembali array menjadi string CSV
                        String newLine = String.join(",", parts);
                        lines.add(newLine);
                    } else {
                        lines.add(line); // Baris tidak cocok, simpan apa adanya
                    }
                } else {
                    lines.add(line); // Baris korup/kosong, simpan apa adanya
                }
            }
        } catch (IOException e) { return "Error reading sales file."; }

        if (!found) {
            return "Transaction not found!\nSystem looked for date: " + cleanDate + "\nAnd name: " + cleanName;
        }

        // Simpan perubahan ke file
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) { return "Error writing sales file."; }

        return "Success! Updated transaction for: " + foundDetails;
    }

    // 3. EDIT COUNT LOG (Tidak Berubah)
    public String editCountLog(String date, String session, String outlet, String prodID, int newPhysQty) {
        File file = new File("stock_counts.csv");
        if (!file.exists()) return "No logs found.";
        
        ArrayList<String> lines = new ArrayList<>();
        boolean found = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 8 &&
                    parts[0].equals(date) && parts[1].equalsIgnoreCase(session) && 
                    parts[2].equalsIgnoreCase(outlet) && parts[4].equalsIgnoreCase(prodID)) {
                    
                    found = true;
                    String newLine = parts[0]+","+parts[1]+","+parts[2]+","+parts[3]+","+
                                     parts[4]+","+parts[5]+","+parts[6]+","+newPhysQty;
                    lines.add(newLine);
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { return "Error reading logs."; }
        
        if (!found) return "Log entry not found.";
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) { return "Error writing logs."; }
        
        return "Stock Count Log Updated!";
    }
}