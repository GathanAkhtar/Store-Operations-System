package Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SalesFilterSystem {

    private final String SALES_FILE = "sales_history.csv";
    
    // Class kecil untuk menampung data baris agar mudah di-sort
    private class SalesRecord {
        LocalDate date;
        String originalLine;
        String customerName;
        double amount;

        public SalesRecord(LocalDate date, String customerName, double amount, String originalLine) {
            this.date = date;
            this.customerName = customerName;
            this.amount = amount;
            this.originalLine = originalLine;
        }
    }

    public String filterAndSortSales(String startDateStr, String endDateStr, int sortOption) {
        List<SalesRecord> records = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        double totalCumulativeSales = 0;

        try {
            LocalDate start = LocalDate.parse(startDateStr, formatter);
            LocalDate end = LocalDate.parse(endDateStr, formatter);

            try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length < 8) continue; // Pastikan data lengkap

                    // Ambil Data (Sesuai struktur CSV kamu)
                    String dateStr = parts[0].trim();
                    String custName = parts[3].trim();
                    String amountStr = parts[7].trim().replace("RM", "").replace(" ", "");

                    try {
                        LocalDate recordDate = LocalDate.parse(dateStr, formatter);
                        double amount = Double.parseDouble(amountStr);

                        // 1. FILTER: Cek apakah tanggal masuk dalam range
                        if (!recordDate.isBefore(start) && !recordDate.isAfter(end)) {
                            records.add(new SalesRecord(recordDate, custName, amount, line));
                            totalCumulativeSales += amount;
                        }
                    } catch (Exception e) {
                        continue; // Skip jika tanggal/angka error
                    }
                }
            }
        } catch (Exception e) {
            return "Error: Invalid Date Format. Use YYYY-MM-DD (e.g., 2026-01-01).";
        }

        if (records.isEmpty()) {
            return "No transactions found between " + startDateStr + " and " + endDateStr;
        }

        // 2. SORTING: Urutkan berdasarkan pilihan user
        switch (sortOption) {
            case 1: // Date Ascending (Oldest first)
                records.sort(Comparator.comparing(r -> r.date));
                break;
            case 2: // Date Descending (Newest first)
                records.sort((r1, r2) -> r2.date.compareTo(r1.date));
                break;
            case 3: // Amount Lowest to Highest
                records.sort(Comparator.comparingDouble(r -> r.amount));
                break;
            case 4: // Amount Highest to Lowest
                records.sort((r1, r2) -> Double.compare(r2.amount, r1.amount));
                break;
            case 5: // Customer Name (A-Z)
                records.sort((r1, r2) -> r1.customerName.compareToIgnoreCase(r2.customerName));
                break;
        }

        // 3. DISPLAY: Format Tabular
        StringBuilder sb = new StringBuilder();
        sb.append("=== FILTERED SALES REPORT ===\n");
        sb.append("Range: ").append(startDateStr).append(" to ").append(endDateStr).append("\n");
        sb.append("Sort : ").append(getSortName(sortOption)).append("\n\n");
        
        // Header Tabel Rapi
        sb.append(String.format("%-12s | %-15s | %-20s | %-10s\n", "Date", "Customer", "Items", "Total"));
        sb.append("----------------------------------------------------------------------\n");

        for (SalesRecord r : records) {
            String[] p = r.originalLine.split(",");
            String items = (p.length > 6) ? p[6].trim() : "-";
            // Potong nama item jika terlalu panjang agar tabel rapi
            if (items.length() > 20) items = items.substring(0, 17) + "...";
            
            sb.append(String.format("%-12s | %-15s | %-20s | RM %-10.2f\n", 
                    r.date, r.customerName, items, r.amount));
        }
        
        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format("TOTAL CUMULATIVE SALES: RM %,.2f", totalCumulativeSales));
        
        return sb.toString();
    }

    private String getSortName(int opt) {
        switch(opt) {
            case 1: return "Date (Oldest First)";
            case 2: return "Date (Newest First)";
            case 3: return "Amount (Low -> High)";
            case 4: return "Amount (High -> Low)";
            case 5: return "Customer Name (A-Z)";
            default: return "Unsorted";
        }
    }
}