package Project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class Tester {
    public static void main(String[] args) {
        // --- 1. INISIALISASI SISTEM ---
        LoginSystem loginSystem = new LoginSystem();
        InventorySystem inventorySystem = new InventorySystem();
        AttendanceLog attendanceLog = new AttendanceLog();

        // --- 2. LOOP UTAMA PROGRAM ---
        while (true) {
            
            // ==========================================
            // STATE 1: BELUM LOGIN (MENU UTAMA)
            // ==========================================
            if (!loginSystem.isLoggedIn()) {
                String[] options = {"Login", "Exit"};
                int choice = JOptionPane.showOptionDialog(
                    null, 
                    "Welcome to GoldenHour System\nStore Operations Management", 
                    "Main Menu", 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.INFORMATION_MESSAGE, 
                    null, options, options[0]
                );

                if (choice == 0) { // PILIH LOGIN
                    String id = JOptionPane.showInputDialog("Enter User ID:");
                    if (id == null) continue;

                    // Menggunakan Password Field agar bintang-bintang (***)
                    JPasswordField pf = new JPasswordField();
                    int okCxl = JOptionPane.showConfirmDialog(null, pf, "Enter Password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    String pass = "";
                    if (okCxl == JOptionPane.OK_OPTION) {
                        pass = new String(pf.getPassword());
                    } else { 
                        continue; 
                    }

                    if (loginSystem.validateLogin(id, pass)) {
                        JOptionPane.showMessageDialog(null, "Login Successful!\nWelcome, " + loginSystem.getCurrentUser().getName());
                    } else {
                        JOptionPane.showMessageDialog(null, "Login Failed! Invalid ID or Password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else { // PILIH EXIT
                    JOptionPane.showMessageDialog(null, "Goodbye! Shutting down...");
                    System.exit(0);
                }

            } else {
                // ==========================================
                // STATE 2: SUDAH LOGIN (DASHBOARD)
                // ==========================================
                User currentUser = loginSystem.getCurrentUser();

                // ------------------------------------------
                // A. MANAGER DASHBOARD
                // ------------------------------------------
                if (loginSystem.isManager()) {
                    String[] mgrOptions = {"Register Employee", "View Stock", "Add New Product", "View History Log", "Logout"};
                    
                    int mgrChoice = JOptionPane.showOptionDialog(
                        null, 
                        "MANAGER DASHBOARD\nUser: " + currentUser.getName(), 
                        "Admin Menu", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, mgrOptions, mgrOptions[0]
                    );

                    // 1. Register Employee
                    if (mgrChoice == 0) {
                        String newId = JOptionPane.showInputDialog("Enter New Employee ID:");
                        if (newId != null && !newId.isEmpty()) {
                            if (loginSystem.getUsers().containsKey(newId)) {
                                JOptionPane.showMessageDialog(null, "Error: User ID exists!");
                            } else {
                                String newPass = JOptionPane.showInputDialog("Create Password:");
                                String newName = JOptionPane.showInputDialog("Full Name:");
                                String[] roles = {"Part-time", "Full-time"};
                                int r = JOptionPane.showOptionDialog(null, "Select Role", "Role", 0, 3, null, roles, roles[0]);
                                String newRole = (r == 0) ? "Part-time" : "Full-time";
                                String outlet = JOptionPane.showInputDialog("Outlet Location:");
                                
                                Employee newEmp = new Employee(newId, newPass, newName, newRole, outlet);
                                loginSystem.addUser(newEmp);
                                JOptionPane.showMessageDialog(null, "Employee Registered Successfully!");
                            }
                        }
                    }
                    // 2. View Stock (Tabel)
                    else if (mgrChoice == 1) {
                        showProductTable(inventorySystem);
                    }
                    // 3. Add New Product
                    else if (mgrChoice == 2) {
                        String pid = JOptionPane.showInputDialog("Product ID (e.g., W001):");
                        String pname = JOptionPane.showInputDialog("Product Name:");
                        String pprice = JOptionPane.showInputDialog("Price (RM):");
                        String pqty = JOptionPane.showInputDialog("Initial Quantity:");

                        try {
                            double price = Double.parseDouble(pprice);
                            int qty = Integer.parseInt(pqty);
                            inventorySystem.addProduct(new Product(pid, pname, price, qty));
                            JOptionPane.showMessageDialog(null, "Product Added to Database!");
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "Invalid Input! Price/Qty must be numbers.");
                        }
                    }
                    // 4. View History Log (BACA FILE TRANSAKSI)
                    else if (mgrChoice == 3) {
                        String logs = inventorySystem.readTransactionLogs();
                        
                        JTextArea textArea = new JTextArea(logs);
                        textArea.setEditable(false);
                        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        
                        JScrollPane scroll = new JScrollPane(textArea);
                        scroll.setPreferredSize(new Dimension(600, 400));
                        
                        JOptionPane.showMessageDialog(null, scroll, "Transaction Logs", JOptionPane.INFORMATION_MESSAGE);
                    }
                    // 5. Logout
                    else {
                        loginSystem.logout();
                    }

                // ------------------------------------------
                // B. EMPLOYEE DASHBOARD
                // ------------------------------------------
                } else {
                    Employee emp = (Employee) currentUser;
                    String[] empOptions = {"Attendance", "Stock Operations", "Stock Count (Audit)", "Logout"};
                    
                    int empChoice = JOptionPane.showOptionDialog(
                        null, 
                        "STAFF DASHBOARD\nName: " + emp.getName() + "\nOutlet: " + emp.getOutletId(), 
                        "Staff Menu", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, empOptions, empOptions[0]
                    );

                    // 1. Attendance
                    if (empChoice == 0) {
                        String status = attendanceLog.checkStatus(emp);
                        String[] attOpts = {"Clock In", "Clock Out", "Check Status", "Back"};
                        int attChoice = JOptionPane.showOptionDialog(null, "Attendance Menu", "Time Clock", 0, 1, null, attOpts, attOpts[0]);
                        
                        if (attChoice == 0) JOptionPane.showMessageDialog(null, attendanceLog.clockIn(emp));
                        else if (attChoice == 1) JOptionPane.showMessageDialog(null, attendanceLog.clockOut(emp));
                        else if (attChoice == 2) JOptionPane.showMessageDialog(null, status);
                    }
                    
                    // 2. Stock Operations (In/Out)
                    else if (empChoice == 1) {
                        String[] stockTypes = {"Stock In (Receive)", "Stock Out (Sale/Transfer)", "Back"};
                        int typeChoice = JOptionPane.showOptionDialog(null, "Choose Action:", "Stock Ops", 0, 1, null, stockTypes, stockTypes[0]);
                        
                        if (typeChoice == 0 || typeChoice == 1) {
                            // Tampilkan daftar produk dulu biar gampang
                            showProductSimpleList(inventorySystem);
                            
                            String pid = JOptionPane.showInputDialog("Enter Product ID:");
                            String qtyStr = JOptionPane.showInputDialog("Enter Quantity:");
                            
                            try {
                                int qty = Integer.parseInt(qtyStr);
                                if (typeChoice == 0) { // IN
                                    String res = inventorySystem.stockIn(pid, qty, emp.getName());
                                    JOptionPane.showMessageDialog(null, res);
                                } else { // OUT
                                    String reason = JOptionPane.showInputDialog("Reason for Stock Out:");
                                    String res = inventorySystem.stockOut(pid, qty, emp.getName(), reason);
                                    JOptionPane.showMessageDialog(null, res);
                                }
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(null, "Invalid Number!");
                            }
                        }
                    }

                    // 3. Stock Count (Audit Morning/Night)
                    else if (empChoice == 2) {
                        String[] sessions = {"Morning Count", "Night Count", "Back"};
                        int sessChoice = JOptionPane.showOptionDialog(null, "Select Audit Session:", "Audit", 0, 1, null, sessions, sessions[0]);

                        if (sessChoice == 0 || sessChoice == 1) {
                            String session = (sessChoice == 0) ? "MORNING" : "NIGHT";
                            
                            // Tampilkan daftar produk
                            showProductSimpleList(inventorySystem);

                            String pid = JOptionPane.showInputDialog("Enter Product ID to Audit:");
                            if (pid != null) {
                                String physQtyStr = JOptionPane.showInputDialog("Enter ACTUAL Physical Qty:");
                                try {
                                    int physQty = Integer.parseInt(physQtyStr);
                                    String res = inventorySystem.performStockCount(session, pid, physQty, emp.getName());
                                    JOptionPane.showMessageDialog(null, res);
                                } catch (Exception e) {
                                    JOptionPane.showMessageDialog(null, "Invalid Number!");
                                }
                            }
                        }
                    }

                    // 4. Logout
                    else {
                        loginSystem.logout();
                    }
                }
            }
        }
    }

    // --- HELPER METHOD: MENAMPILKAN TABEL GUI ---
    private static void showProductTable(InventorySystem inv) {
        ArrayList<Product> list = inv.getAllProducts();
        String[] columns = {"ID", "Name", "Price (RM)", "Qty"};
        Object[][] data = new Object[list.size()][4];
        
        for(int i=0; i<list.size(); i++){
            Product p = list.get(i);
            data[i][0] = p.getProductID();
            data[i][1] = p.getName();
            data[i][2] = p.getPrice();
            data[i][3] = p.getQuantity();
        }
        
        JTable table = new JTable(new DefaultTableModel(data, columns));
        JOptionPane.showMessageDialog(null, new JScrollPane(table), "Current Inventory", JOptionPane.PLAIN_MESSAGE);
    }

    // --- HELPER METHOD: MENAMPILKAN LIST SIMPLE (TEXT) ---
    private static void showProductSimpleList(InventorySystem inv) {
        ArrayList<Product> list = inv.getAllProducts();
        StringBuilder sb = new StringBuilder("=== PRODUCT REFERENCE ===\n\n");
        for (Product p : list) {
            sb.append(String.format("[%s] %s - System Qty: %d\n", p.getProductID(), p.getName(), p.getQuantity()));
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(null, new JScrollPane(textArea), "Product List", JOptionPane.INFORMATION_MESSAGE);
    }
}