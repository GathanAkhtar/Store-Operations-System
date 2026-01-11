package Project;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

public class Tester {

    // --- GLOBAL SYSTEMS ---
    // Semua sistem di-load di sini agar data terintegrasi
    private static LoginSystem loginSystem;
    private static InventorySystem inventorySystem;
    private static EditSystem editSystem;
    private static AttendanceLog attendanceLog;
    private static PerformanceAnalytics analytics; // Fitur Baru (Ranking)

    public static void main(String[] args) {
        System.out.println("Initializing System...");

        // 1. Initialize Backend Logic
        loginSystem = new LoginSystem();
        inventorySystem = new InventorySystem();
        editSystem = new EditSystem(inventorySystem);
        attendanceLog = new AttendanceLog();
        analytics = new PerformanceAnalytics(inventorySystem);

        // 2. Launch GUI (Login Screen)
        SwingUtilities.invokeLater(() -> {
            // Callback: Setelah login sukses, panggil method launchDashboard()
            new LoginGUI(loginSystem, () -> launchDashboard());
        });
    }

    /**
     * Method ini berjalan otomatis SETELAH Login Berhasil
     */
    private static void launchDashboard() {
        User currentUser = loginSystem.getCurrentUser();
        
        if (currentUser == null) {
            JOptionPane.showMessageDialog(null, "Login Error: User not found.");
            return;
        }

        // Router: Cek jabatan user untuk menentukan menu mana yang dibuka
        if (currentUser instanceof Manager) {
            managerMenu((Manager) currentUser);
        } else if (currentUser instanceof Employee) {
            employeeMenu((Employee) currentUser);
        }
    }

    // ==================================================================================
    // 1. MANAGER DASHBOARD (Full Access + Add User + Reports)
    // ==================================================================================
    private static void managerMenu(Manager mgr) {
        boolean isRunning = true;
        
        // Menu Lengkap Manager
        String[] options = {
            "1. View Stock",
            "2. Search Product",
            "3. Stock In (Restock)",
            "4. Stock Out (Sales/Remove)",
            "5. Edit / Correction Menu",
            "6. Add New Employee (User)",  // <--- FITUR INI SUDAH KEMBALI
            "7. Employee Performance Report", // <--- FITUR BARU
            "8. Logout"
        };

        while (isRunning) {
            int choice = JOptionPane.showOptionDialog(null, 
                "HEAD OFFICE DASHBOARD\nUser: " + mgr.getName(), 
                "Manager Control Panel", 
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, options, options[0]);

            switch (choice) {
                case 0: // View All
                    displayInventoryTable(inventorySystem.getAllProducts());
                    break;
                case 1: // Search
                    handleSearch();
                    break;
                case 2: // Stock In
                    handleTransaction(mgr.getName(), "HeadOffice", true);
                    break;
                case 3: // Stock Out
                    handleTransaction(mgr.getName(), "HeadOffice", false);
                    break;
                case 4: // Edit Menu (Fix Parameter)
                    handleEditMenu();
                    break;
                case 5: // Add User (Fitur yang sempat hilang)
                    handleAddUser();
                    break;
                case 6: // Performance Report
                    showPerformanceReport(mgr);
                    break;
                case 7: // Logout
                case -1:
                    performLogout();
                    isRunning = false;
                    break;
            }
        }
    }

    // ==================================================================================
    // 2. EMPLOYEE DASHBOARD (Access Terbatas)
    // ==================================================================================
    private static void employeeMenu(Employee emp) {
        boolean isRunning = true;
        String outletId = emp.getOutletId();

        String[] options = {
            "1. Attendance (Clock In/Out)",
            "2. View Outlet Stock",
            "3. Process Sale (Stock Out)",
            "4. Logout"
        };

        while (isRunning) {
            int choice = JOptionPane.showOptionDialog(null, 
                "STAFF DASHBOARD\nName: " + emp.getName() + "\nOutlet: " + outletId, 
                "Staff Menu", 
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, options, options[0]);

            switch (choice) {
                case 0: // Attendance
                    handleAttendance(emp);
                    break;
                case 1: // View Stock
                    displayOutletStock(outletId);
                    break;
                case 2: // Sales
                    // Cek harus Clock In dulu
                    if (attendanceLog.checkStatus(emp).contains("Not clocked in")) {
                        JOptionPane.showMessageDialog(null, "Please Clock In first!");
                    } else {
                        handleTransaction(emp.getName(), outletId, false);
                    }
                    break;
                case 3: // Logout
                case -1:
                    performLogout();
                    isRunning = false;
                    break;
            }
        }
    }

    // ==================================================================================
    // LOGIC METHODS (Jantung Aplikasi)
    // ==================================================================================

    // --- A. ADD USER LOGIC (YANG SEMPAT HILANG) ---
    private static void handleAddUser() {
        String[] roles = {"Employee", "Manager"};
        int roleChoice = JOptionPane.showOptionDialog(null, "Select Role:", "Add User",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);
        
        if (roleChoice == -1) return;
        String role = roles[roleChoice];

        String id = JOptionPane.showInputDialog("Enter New User ID (e.g., EMP05):");
        if (id == null || id.isEmpty()) return;
        
        String name = JOptionPane.showInputDialog("Enter Full Name:");
        if (name == null || name.isEmpty()) return;
        
        String pass = JOptionPane.showInputDialog("Enter Password:");
        if (pass == null || pass.isEmpty()) return;

        String outlet = "HeadOffice";
        if (role.equals("Employee")) {
            outlet = JOptionPane.showInputDialog("Enter Outlet (e.g., KLCC):");
            if (outlet == null || outlet.isEmpty()) return;
        }

        // Tulis ke CSV
        try (PrintWriter pw = new PrintWriter(new FileWriter("users.csv", true))) {
            pw.println(id + "," + pass + "," + name + "," + role + "," + outlet);
            JOptionPane.showMessageDialog(null, "User " + name + " added successfully!");
            
            // Reload Login System supaya user baru terdeteksi
            loginSystem = new LoginSystem();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error saving user: " + e.getMessage());
        }
    }

    // --- B. EDIT MENU (SUDAH DIPERBAIKI: 5 PARAMETER) ---
    private static void handleEditMenu() {
        String[] editOps = {"Edit Stock Quantity", "Edit Sales Transaction", "Back"};
        
        int c = JOptionPane.showOptionDialog(null, "Choose Edit Mode", "Edit System", 
            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, editOps, editOps[0]);

        if (c == 0) {
            // Edit Stock Level
            String pid = JOptionPane.showInputDialog("Product ID:");
            String out = JOptionPane.showInputDialog("Outlet Name (e.g., KLCC):");
            String q = JOptionPane.showInputDialog("New Quantity (Total):");
            
            if (pid != null && out != null && q != null) {
                try {
                    String res = editSystem.editStockLevel(pid, out, Integer.parseInt(q));
                    JOptionPane.showMessageDialog(null, res);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Input Error: " + e.getMessage());
                }
            }
        } 
        else if (c == 1) {
            // Edit Sales Transaction (Meminta 5 Parameter sesuai Class EditSystem kamu)
            JOptionPane.showMessageDialog(null, "Step 1: Find Old Data\nStep 2: Enter New Data");

            // Input Data Lama (Target)
            String targetDate = JOptionPane.showInputDialog("Target Date (YYYY-MM-DD):");
            String targetName = JOptionPane.showInputDialog("Target Customer Name (Old Name):");

            // Input Data Baru
            String newName = JOptionPane.showInputDialog("NEW Customer Name:");
            String newMethod = JOptionPane.showInputDialog("NEW Payment Method (Cash/Card):");
            String newTotalStr = JOptionPane.showInputDialog("NEW Total Price (e.g. 150.00):");

            try {
                double newTotal = Double.parseDouble(newTotalStr); // Convert ke double
                
                // Panggil method di EditSystem dengan 5 parameter
                String result = editSystem.editSalesTransaction(targetDate, targetName, newName, newMethod, newTotal);
                
                JOptionPane.showMessageDialog(null, result);
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Error: Price must be a number!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }
    }

    // --- C. TRANSACTION (STOCK IN/OUT) ---
    private static void handleTransaction(String user, String outlet, boolean isStockIn) {
        String type = isStockIn ? "STOCK IN" : "STOCK OUT";
        String pid = JOptionPane.showInputDialog("Enter Product ID for " + type + ":");
        if (pid == null) return;

        Product p = inventorySystem.findProduct(pid);
        if (p == null) {
            JOptionPane.showMessageDialog(null, "Product ID Not Found!");
            return;
        }

        String qtyStr = JOptionPane.showInputDialog(null, 
            "Item: " + p.getName() + "\nCurrent " + outlet + " Stock: " + p.getQuantity(outlet) + 
            "\n\nEnter Quantity:");
        if (qtyStr == null) return;

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(null, "Quantity must be positive!");
                return;
            }

            String result;
            if (isStockIn) {
                result = inventorySystem.stockIn(pid, qty, outlet, user);
            } else {
                String reason = "Sales";
                if (user.equals("Manager")) {
                    reason = JOptionPane.showInputDialog("Reason (Sales/Damaged/etc):", "Sales");
                }
                result = inventorySystem.stockOut(pid, qty, outlet, user, reason);
            }
            JOptionPane.showMessageDialog(null, result);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Number!");
        }
    }

    // --- D. DISPLAY & SEARCH ---
    private static void displayInventoryTable(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s | %-25s | %-10s | %s\n", "ID", "Name", "Price", "Stock Distribution"));
        sb.append("----------------------------------------------------------------------------------\n");
        
        for (Product p : products) {
            sb.append(String.format("%-10s | %-25s | RM%7.2f | %s\n", 
                p.getProductID(), 
                p.getName().length() > 22 ? p.getName().substring(0,22)+"..." : p.getName(), 
                p.getPrice(), 
                p.getStockDisplay()));
        }
        showScrollableText(sb.toString(), "Inventory Overview");
    }

    private static void displayOutletStock(String outletId) {
        StringBuilder sb = new StringBuilder("Stock at " + outletId + ":\n\n");
        boolean found = false;
        for (Product p : inventorySystem.getAllProducts()) {
            int qty = p.getQuantity(outletId);
            if (qty > 0) {
                sb.append("• ").append(p.getName()).append(" (ID: ").append(p.getProductID()).append(")\n");
                sb.append("  Qty: ").append(qty).append("\n------------------\n");
                found = true;
            }
        }
        if (!found) sb.append("No stock available.");
        showScrollableText(sb.toString(), "Outlet Stock");
    }

    private static void handleSearch() {
        String query = JOptionPane.showInputDialog("Search Product Name:");
        if (query == null) return;

        List<Product> results = new ArrayList<>();
        for (Product p : inventorySystem.getAllProducts()) {
            if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                results.add(p);
            }
        }
        if (results.isEmpty()) JOptionPane.showMessageDialog(null, "No products found.");
        else displayInventoryTable(results);
    }

    // --- E. ATTENDANCE ---
    private static void handleAttendance(Employee emp) {
        String status = attendanceLog.checkStatus(emp);
        int confirm = JOptionPane.showConfirmDialog(null, 
            "Current Status: " + status + "\nChange Status?", "Attendance", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            String msg;
            if (status.contains("Not clocked in")) msg = attendanceLog.clockIn(emp);
            else if (status.contains("Clocked In")) msg = attendanceLog.clockOut(emp);
            else msg = "Shift already ended.";
            JOptionPane.showMessageDialog(null, msg);
        }
    }

    // --- F. PERFORMANCE REPORT (FITUR BARU) ---
    private static void showPerformanceReport(Manager mgr) {
        try {
            List<EmployeeStats> report = analytics.getEmployeePerformanceReport(mgr);
            
            StringBuilder sb = new StringBuilder("=== EMPLOYEE SALES LEADERBOARD ===\n");
            sb.append("(Based on transactions.csv)\n\n");
            sb.append(String.format("%-5s %-15s %-10s %s\n", "Rank", "Name", "Trans.", "Total Sales"));
            sb.append("----------------------------------------------------\n");
            
            int rank = 1;
            for (EmployeeStats s : report) {
                sb.append(String.format("#%-4d %-15s %-10d RM %,.2f\n", 
                    rank++, s.getEmployeeName(), s.getTransactionCount(), s.getTotalSales()));
            }
            showScrollableText(sb.toString(), "Performance Report");

        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- UTILITIES ---
    private static void performLogout() {
        loginSystem.logout();
        JOptionPane.showMessageDialog(null, "Logged Out Successfully.");
        SwingUtilities.invokeLater(() -> new LoginGUI(loginSystem, () -> launchDashboard()));
    }

    private static void showScrollableText(String text, String title) {
        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(null, scroll, title, JOptionPane.INFORMATION_MESSAGE);
    }
}