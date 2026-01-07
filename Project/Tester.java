package Project;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Tester {
    public static void main(String[] args) {
        // --- 1. INISIALISASI SISTEM ---
        LoginSystem loginSystem = new LoginSystem();
        InventorySystem inventorySystem = new InventorySystem();
        AttendanceLog attendanceLog = new AttendanceLog();
        SalesSystem salesSystem = new SalesSystem(inventorySystem);

        // --- 2. LOOP UTAMA PROGRAM ---
        while (true) {
            if (!loginSystem.isLoggedIn()) {
                String[] options = {"Login", "Exit"};
                int choice = JOptionPane.showOptionDialog(null, "Welcome to GoldenHour System", "Main Menu", 
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

                if (choice == 0) {
                    String id = JOptionPane.showInputDialog("Enter User ID:");
                    if (id == null) continue;
                    JPasswordField pf = new JPasswordField();
                    int ok = JOptionPane.showConfirmDialog(null, pf, "Enter Password:", JOptionPane.OK_CANCEL_OPTION);
                    if (ok == JOptionPane.OK_OPTION) {
                        if (loginSystem.validateLogin(id, new String(pf.getPassword()))) {
                            JOptionPane.showMessageDialog(null, "Login Successful!\nWelcome, " + loginSystem.getCurrentUser().getName());
                        } else {
                            JOptionPane.showMessageDialog(null, "Login Failed!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    System.exit(0);
                }
            } else {
                User currentUser = loginSystem.getCurrentUser();

                // ==========================
                // === MANAGER DASHBOARD ====
                // ==========================
                if (loginSystem.isManager()) {
                    String[] mgrOptions = {"Register Employee", "View Stock", "Add New Product", "Stock In/Out (Restock)", "Reports", "Logout"};
                    int mgrChoice = JOptionPane.showOptionDialog(null, "MANAGER DASHBOARD", "Admin Menu", 
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, mgrOptions, mgrOptions[0]);

                    if (mgrChoice == 0) { 
                        // --- Register Employee ---
                        String newId = JOptionPane.showInputDialog("ID:");
                        if (newId != null) {
                            String newPass = JOptionPane.showInputDialog("Password:");
                            String newName = JOptionPane.showInputDialog("Name:");
                            String[] roles = {"Part-time", "Full-time"};
                            int r = JOptionPane.showOptionDialog(null, "Role", "Select", 0, 3, null, roles, roles[0]);
                            
                            // LOGIC: Manager bisa buat cabang baru ATAU pilih yg sudah ada
                            String[] existingOutlets = loginSystem.getActiveOutlets();
                            String targetOutlet;
                            
                            if (existingOutlets.length == 0) {
                                // Belum ada cabang, harus ketik baru
                                targetOutlet = JOptionPane.showInputDialog("No active outlets found.\nEnter NEW Outlet Name (e.g., KLCC):");
                            } else {
                                // Ada pilihan: Pilih Existing atau Create New
                                String[] outletOptions = new String[existingOutlets.length + 1];
                                System.arraycopy(existingOutlets, 0, outletOptions, 0, existingOutlets.length);
                                outletOptions[existingOutlets.length] = "[Create New Outlet]";
                                
                                int outChoice = JOptionPane.showOptionDialog(null, "Assign Outlet", "Location", 0, 3, null, outletOptions, outletOptions[0]);
                                if (outChoice == -1) continue;
                                
                                if (outChoice == existingOutlets.length) {
                                    targetOutlet = JOptionPane.showInputDialog("Enter NEW Outlet Name:");
                                } else {
                                    targetOutlet = existingOutlets[outChoice];
                                }
                            }
                            
                            if (targetOutlet != null && !targetOutlet.trim().isEmpty()) {
                                loginSystem.addUser(new Employee(newId, newPass, newName, (r==0?"Part-time":"Full-time"), targetOutlet));
                                JOptionPane.showMessageDialog(null, "Employee Registered to " + targetOutlet + "!");
                            }
                        }
                        
                    } else if (mgrChoice == 1) { 
                        // --- View Stock Table ---
                        showProductTable(inventorySystem);
                        
                    } else if (mgrChoice == 2) { 
                        // --- ADD NEW PRODUCT (DYNAMIC OUTLETS) ---
                        try {
                            // Cek dulu apakah ada cabang aktif?
                            String[] activeOutlets = loginSystem.getActiveOutlets();
                            if (activeOutlets.length == 0) {
                                JOptionPane.showMessageDialog(null, "No Active Outlets Found!\nPlease Register an Employee to an Outlet first.");
                                continue;
                            }

                            // 1. Input Detail Produk Dasar
                            String pid = JOptionPane.showInputDialog("Enter New Product ID:");
                            if (pid == null) continue;
                            String nam = JOptionPane.showInputDialog("Enter Product Name:");
                            double prc = Double.parseDouble(JOptionPane.showInputDialog("Enter Price (RM):"));
                            
                            // 2. Pilih Cabang (Hanya dari yang ada user-nya)
                            int outChoice = JOptionPane.showOptionDialog(null, "Select Outlet for Initial Stock:", "Initial Location", 
                                    0, 3, null, activeOutlets, activeOutlets[0]);
                            
                            if (outChoice == -1) continue; 
                            String selectedOutlet = activeOutlets[outChoice];
                            
                            // 3. Input Jumlah Stok Awal
                            int qty = Integer.parseInt(JOptionPane.showInputDialog("Enter Initial Quantity for " + selectedOutlet + ":"));

                            // 4. Buat Produk & Set Stok Cabang
                            Product newP = new Product(pid, nam, prc);
                            newP.setQuantity(selectedOutlet, qty); 
                            
                            inventorySystem.addProduct(newP);
                            JOptionPane.showMessageDialog(null, "Product Added Successfully!\n" + nam + " created at " + selectedOutlet);
                            
                        } catch (Exception e) { 
                            JOptionPane.showMessageDialog(null, "Invalid Input! Please try again."); 
                        }
                        
                    } else if (mgrChoice == 3) { 
                        // --- Stock In/Out Manager (DYNAMIC OUTLETS) ---
                        // Kirim loginSystem supaya bisa ambil outlet list
                        managerStockOps(inventorySystem, loginSystem, currentUser.getName());
                        
                    } else if (mgrChoice == 4) { 
                        // --- Reports ---
                         JOptionPane.showMessageDialog(null, new JScrollPane(new JTextArea(salesSystem.readSalesHistory())));
                    } else {
                        loginSystem.logout();
                    }

                // ===========================
                // === EMPLOYEE DASHBOARD ====
                // ===========================
                } else {
                    Employee emp = (Employee) currentUser;
                    String[] empOptions = {"Attendance", "Stock Operations", "Record Sale", "Logout"};
                    int empChoice = JOptionPane.showOptionDialog(null, "STAFF: " + emp.getName() + " (" + emp.getOutletId() + ")", "Staff Menu", 
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, empOptions, empOptions[0]);

                    if (empChoice == 0) { // Attendance
                        String[] att = {"Clock In", "Clock Out", "Status"};
                        int c = JOptionPane.showOptionDialog(null, "Attendance", "Clock", 0, 1, null, att, att[0]);
                        if (c==0) JOptionPane.showMessageDialog(null, attendanceLog.clockIn(emp));
                        else if (c==1) JOptionPane.showMessageDialog(null, attendanceLog.clockOut(emp));
                        else if (c==2) JOptionPane.showMessageDialog(null, attendanceLog.checkStatus(emp));
                        
                    } else if (empChoice == 1) { // Stock Ops
                        employeeStockOps(inventorySystem, emp);
                        
                    } else if (empChoice == 2) { // Sale
                        recordSale(salesSystem, inventorySystem, emp);
                        
                    } else {
                        loginSystem.logout();
                    }
                }
            }
        }
    }

    // === MANAGER STOCK OPS (UPDATED: Dynamic Outlets) ===
    private static void managerStockOps(InventorySystem inv, LoginSystem login, String mgrName) {
        // Cek Outlet Aktif
        String[] activeOutlets = login.getActiveOutlets();
        if (activeOutlets.length == 0) {
            JOptionPane.showMessageDialog(null, "No Active Outlets Found. Register an Employee first.");
            return;
        }

        String[] ops = {"Stock In", "Stock Out"};
        int op = JOptionPane.showOptionDialog(null, "Operation Type", "Manager Stock", 0, 1, null, ops, ops[0]);
        if (op == -1) return;

        showProductSimpleList(inv);
        String pid = JOptionPane.showInputDialog("Product ID:");
        if (pid == null) return;

        // Pilih Cabang dari list User
        int outChoice = JOptionPane.showOptionDialog(null, "Select Outlet:", "Outlet Selection", 
                0, 3, null, activeOutlets, activeOutlets[0]);
        if (outChoice == -1) return;
        String selectedOutlet = activeOutlets[outChoice];

        String qtyS = JOptionPane.showInputDialog("Quantity:");
        try {
            int q = Integer.parseInt(qtyS);
            if (op == 0) {
                JOptionPane.showMessageDialog(null, inv.stockIn(pid, q, selectedOutlet, mgrName));
            } else {
                String r = JOptionPane.showInputDialog("Reason:");
                JOptionPane.showMessageDialog(null, inv.stockOut(pid, q, selectedOutlet, mgrName, r));
            }
        } catch(Exception e) { JOptionPane.showMessageDialog(null, "Invalid Input"); }
    }

    // === EMPLOYEE STOCK OPS (Fixed Branch) ===
    private static void employeeStockOps(InventorySystem inv, Employee emp) {
        String[] ops = {"Stock In", "Stock Out"};
        int op = JOptionPane.showOptionDialog(null, "Operation Type", "Stock Ops (" + emp.getOutletId() + ")", 0, 1, null, ops, ops[0]);
        
        showProductSimpleList(inv);
        String pid = JOptionPane.showInputDialog("Product ID:");
        String qtyS = JOptionPane.showInputDialog("Quantity:");
        try {
            int q = Integer.parseInt(qtyS);
            if (op == 0) JOptionPane.showMessageDialog(null, inv.stockIn(pid, q, emp.getOutletId(), emp.getName()));
            else {
                String r = JOptionPane.showInputDialog("Reason:");
                JOptionPane.showMessageDialog(null, inv.stockOut(pid, q, emp.getOutletId(), emp.getName(), r));
            }
        } catch(Exception e) { JOptionPane.showMessageDialog(null, "Invalid Input"); }
    }

    private static void recordSale(SalesSystem salesSystem, InventorySystem inventory, Employee employee) {
        String customerName = JOptionPane.showInputDialog("Enter Customer Name:");
        if (customerName == null || customerName.trim().isEmpty()) return;
        
        SalesTransaction transaction = salesSystem.createNewTransaction(customerName, employee);
        boolean addingItems = true;
        
        while (addingItems) {
            // Tampilkan Stok KHUSUS Cabang Staff
            showProductListForStaff(inventory, employee.getOutletId());
            
            String productID = JOptionPane.showInputDialog("Cart: RM " + String.format("%.2f", transaction.getTotalPrice()) + "\nEnter ID:");
            if (productID == null) break;
            
            String qtyStr = JOptionPane.showInputDialog("Quantity:");
            if (qtyStr == null) break;
            
            try {
                int quantity = Integer.parseInt(qtyStr);
                String result = salesSystem.addItemToTransaction(transaction, productID, quantity);
                JOptionPane.showMessageDialog(null, result);
            } catch (Exception e) { JOptionPane.showMessageDialog(null, "Invalid Number!"); }

            int confirm = JOptionPane.showConfirmDialog(null, "Add another item?", "Next?", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.NO_OPTION) addingItems = false;
        }

        if (transaction.getItems().isEmpty()) return;
        
        String[] methods = {"Cash", "Debit Card", "Credit Card", "E-Wallet"};
        int methodChoice = JOptionPane.showOptionDialog(null, "Total: RM " + transaction.getTotalPrice(), "Payment", 0, 3, null, methods, methods[0]);
        if (methodChoice == -1) return;
        
        String paymentStr = JOptionPane.showInputDialog("Enter Amount (" + methods[methodChoice] + "):");
        if (paymentStr != null) {
            try {
                double cash = Double.parseDouble(paymentStr);
                String res = salesSystem.finalizeTransaction(transaction, cash, methods[methodChoice]);
                if (!res.startsWith("ERROR")) {
                    JTextArea ta = new JTextArea("=== RECEIPT ===\nOutlet: " + employee.getOutletId() + "\n" + res);
                    JOptionPane.showMessageDialog(null, new JScrollPane(ta));
                } else JOptionPane.showMessageDialog(null, res);
            } catch (Exception e) { JOptionPane.showMessageDialog(null, "Invalid Amount"); }
        }
    }

    // Menampilkan Stok Global (Semua Cabang) - Untuk Manager
    private static void showProductSimpleList(InventorySystem inv) {
        StringBuilder sb = new StringBuilder("Global Stock View:\n");
        for (Product p : inv.getAllProducts()) {
            sb.append(p.getProductID()).append(" - ").append(p.getName()).append(" [").append(p.getStockDisplay()).append("]\n");
        }
        JTextArea textArea = new JTextArea(sb.toString());
        JOptionPane.showMessageDialog(null, new JScrollPane(textArea), "All Stocks", JOptionPane.INFORMATION_MESSAGE);
    }

    // Menampilkan Stok HANYA Cabang Staff - Untuk Sales
    private static void showProductListForStaff(InventorySystem inv, String outletId) {
        StringBuilder sb = new StringBuilder("Stock at " + outletId + ":\n");
        for (Product p : inv.getAllProducts()) {
            int qty = p.getQuantity(outletId);
            sb.append(p.getProductID()).append(" - ").append(p.getName()).append(" (Qty: ").append(qty).append(")\n");
        }
        JTextArea textArea = new JTextArea(sb.toString());
        JOptionPane.showMessageDialog(null, new JScrollPane(textArea), "Outlet Stock", JOptionPane.INFORMATION_MESSAGE);
    }

    // Tabel Lengkap untuk Manager
    private static void showProductTable(InventorySystem inv) {
        ArrayList<Product> list = inv.getAllProducts();
        String[][] data = new String[list.size()][4];
        String[] cols = {"ID", "Name", "Price", "Stock Distribution"};
        for(int i=0; i<list.size(); i++){
            Product p = list.get(i);
            data[i][0] = p.getProductID(); 
            data[i][1] = p.getName();
            data[i][2] = String.format("%.2f", p.getPrice()); 
            data[i][3] = p.getStockDisplay();
        }
        JTable table = new JTable(data, cols);
        table.getColumnModel().getColumn(3).setPreferredWidth(300); // Lebarkan kolom stok
        JOptionPane.showMessageDialog(null, new JScrollPane(table), "Inventory Table", JOptionPane.INFORMATION_MESSAGE);
    }
}