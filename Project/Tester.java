package Project;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List; // Tambahan untuk List

public class Tester {
    
    // ==========================================
    // 1. SYSTEM INITIALIZATION (Semua Sistem Dimuat Disini)
    // ==========================================
    
    // Core Systems
    static LoginSystem loginSystem = new LoginSystem();
    static InventorySystem inventorySystem = new InventorySystem();
    static AttendanceLog attendanceLog = new AttendanceLog();
    
    // Feature Systems (Dependency Injection)
    static SalesSystem salesSystem = new SalesSystem(inventorySystem);
    static StockCountSystem stockCountSystem = new StockCountSystem(inventorySystem);
    static SearchSystem searchSystem = new SearchSystem(inventorySystem);
    static EditSystem editSystem = new EditSystem(inventorySystem);
    
    // --- FITUR BARU: ANALYTICS ---
    static PerformanceAnalytics analytics = new PerformanceAnalytics(inventorySystem);

    public static void main(String[] args) {
        // Mengatur tampilan agar terlihat modern (sesuai OS Windows/Mac)
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        // Tampilkan LoginGUI dulu sebelum masuk loop
        SwingUtilities.invokeLater(() -> {
            new LoginGUI(loginSystem, () -> {
                // Setelah login sukses, baru jalankan loop yang sudah ada
                mainMenuLoop();
            });
        });
    }

    // ==========================================
    // MAIN MENU LOOP
    // ==========================================
    private static void mainMenuLoop() {
        while (true) {
            if (!loginSystem.isLoggedIn()) {
                // Kalau logout, tampilkan login GUI lagi
                SwingUtilities.invokeLater(() -> {
                    new LoginGUI(loginSystem, () -> mainMenuLoop());
                });
                break;
            } else {
                User currentUser = loginSystem.getCurrentUser();

                // ==========================================
                // 3. MANAGER DASHBOARD
                // ==========================================
                if (loginSystem.isManager()) {
                    String[] mgrOptions = {
                        "Register Employee",        // 0
                        "View Stock",               // 1
                        "Add Product",              // 2
                        "Stock In/Out",             // 3
                        "Edit Information",         // 4
                        "Sales Reports",            // 5
                        "Count Logs",               // 6
                        "Performance Analytics",    // 7 <--- FITUR BARU
                        "Logout"                    // 8
                    };
                    
                    int mgrChoice = showMenu("MANAGER DASHBOARD\nUser: " + currentUser.getName(), mgrOptions);

                    switch (mgrChoice) {
                        case 0: performRegister(); break;
                        case 1: showProductTable(inventorySystem); break;
                        case 2: performAddProduct(); break;
                        case 3: managerStockOps(inventorySystem, loginSystem, currentUser.getName()); break;
                        case 4: performEditInfoGUI(); break;
                        case 5: showScrollMsg("Sales Report", salesSystem.readSalesHistory()); break;
                        case 6: showScrollMsg("Count Logs", stockCountSystem.readCountHistory()); break;
                        case 7: showPerformanceReport((Manager) currentUser); break; // <--- PANGGIL FITUR BARU
                        case 8: loginSystem.logout(); break;
                        default: break;
                    }

                // ==========================================
                // 4. EMPLOYEE DASHBOARD
                // ==========================================
                } else {
                    Employee emp = (Employee) currentUser;
                    String[] empOptions = {
                        "Attendance",        // 0
                        "Daily Stock Count", // 1
                        "Search Info",       // 2
                        "Edit Information",  // 3
                        "Stock Ops",         // 4
                        "Record Sale",       // 5
                        "Logout"             // 6
                    };
                    
                    int empChoice = showMenu("STAFF: " + emp.getName() + " (" + emp.getOutletId() + ")", empOptions);

                    switch (empChoice) {
                        case 0: attendanceGUI(emp); break;
                        case 1: performStaffStockCount(stockCountSystem, inventorySystem, emp); break;
                        case 2: performSearch(searchSystem); break;
                        case 3: performEditInfoGUI(); break;
                        case 4: employeeStockOps(inventorySystem, emp); break;
                        case 5: recordSale(salesSystem, inventorySystem, emp); break;
                        case 6: loginSystem.logout(); break;
                        default: break;
                    }
                }
            }
        }
    }

    // =========================================================
    // === EDIT INFORMATION GUI ===
    // =========================================================
    private static void performEditInfoGUI() {
        String[] types = {"Edit Sales Transaction", "Edit Stock Level (Model)", "Edit Stock Count Log"};
        int type = JOptionPane.showOptionDialog(null, "Select correction type:", "Edit Information",
                0, 3, null, types, types[0]);
        
        if (type == 0) {
            JTextField searchDate = new JTextField();
            JTextField searchName = new JTextField();
            Object[] searchForm = {
                "Enter Date (YYYY-MM-DD):", searchDate,
                "Enter Customer Name to Find:", searchName
            };
            
            int searchOk = JOptionPane.showConfirmDialog(null, searchForm, "Find Transaction", JOptionPane.OK_CANCEL_OPTION);
            if (searchOk != JOptionPane.OK_OPTION) return;
            
            String targetDate = searchDate.getText();
            String targetName = searchName.getText();

            String check = searchSystem.searchSalesRecord(targetName, "ANY");
            if (check.contains("No records")) {
                JOptionPane.showMessageDialog(null, "No records found matching that name (Check spelling/case).");
                return;
            }
            
            JTextField custField = new JTextField(targetName);
            JTextField methodField = new JTextField();
            JTextField totalField = new JTextField();
            
            Object[] editForm = {
                "Record Found (Potential Match).",
                "CORRECT Customer Name:", custField,
                "CORRECT Payment Method:", methodField,
                "CORRECT Total Price (RM):", totalField
            };
            
            int ok = JOptionPane.showConfirmDialog(null, editForm, "Edit Transaction Data", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                try {
                    double newTot = Double.parseDouble(totalField.getText());
                    String res = editSystem.editSalesTransaction(targetDate, targetName,
                                                                 custField.getText(), methodField.getText(), newTot);
                    JOptionPane.showMessageDialog(null, res);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Invalid Price Format!");
                }
            }
            
        } else if (type == 1) {
            String pid = JOptionPane.showInputDialog("Enter Product ID to correct:");
            if (pid == null) return;
            
            Product p = inventorySystem.findProduct(pid);
            if (p == null) { JOptionPane.showMessageDialog(null, "Product not found."); return; }
            
            String[] outlets = loginSystem.getActiveOutlets();
            if (outlets.length == 0) { JOptionPane.showMessageDialog(null, "No outlets available."); return; }
            
            int o = JOptionPane.showOptionDialog(null, "Select Outlet:", "Location", 0, 3, null, outlets, outlets[0]);
            if (o == -1) return;
            
            String currentQty = String.valueOf(p.getQuantity(outlets[o]));
            String newQtyStr = JOptionPane.showInputDialog("Current System Stock: " + currentQty + "\nEnter CORRECT Quantity:");
            
            if (newQtyStr != null) {
                try {
                    int nq = Integer.parseInt(newQtyStr);
                    String res = editSystem.editStockLevel(pid, outlets[o], nq);
                    JOptionPane.showMessageDialog(null, res);
                } catch (Exception e) { JOptionPane.showMessageDialog(null, "Invalid Number"); }
            }

        } else if (type == 2) {
            String date = JOptionPane.showInputDialog("Enter Date of Count (YYYY-MM-DD):");
            String pid = JOptionPane.showInputDialog("Enter Product ID:");
            
            String[] sess = {"Morning Count", "Night Count"};
            int s = JOptionPane.showOptionDialog(null, "Session:", "Select", 0,3,null,sess,sess[0]);
            
            String[] outlets = loginSystem.getActiveOutlets();
            int o = JOptionPane.showOptionDialog(null, "Select Outlet:", "Location", 0, 3, null, outlets, outlets[0]);
            
            if (date!=null && pid!=null && s!=-1 && o!=-1) {
                String newPhys = JOptionPane.showInputDialog("Enter Correct PHYSICAL Quantity:");
                if (newPhys != null) {
                    try {
                        int np = Integer.parseInt(newPhys);
                        String res = editSystem.editCountLog(date, sess[s], outlets[o], pid, np);
                        JOptionPane.showMessageDialog(null, res);
                    } catch(Exception e) { JOptionPane.showMessageDialog(null, "Invalid Number"); }
                }
            }
        }
    }

    // =========================================================
    // === HELPER METHODS ===
    // =========================================================

    private static int showMenu(String title, String[] opts) {
        return JOptionPane.showOptionDialog(null, title, "Menu",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
    }

    private static void performRegister() {
        JTextField id = new JTextField();
        JTextField name = new JTextField();
        JPasswordField pass = new JPasswordField();
        String[] roles = {"Part-time", "Full-time"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        
        String[] outlets = loginSystem.getActiveOutlets();
        JComboBox<String> outletBox = new JComboBox<>(outlets.length > 0 ? outlets : new String[]{"New..."});

        Object[] form = {"ID:", id, "Name:", name, "Password:", pass, "Role:", roleBox, "Outlet:", outletBox};

        if (JOptionPane.showConfirmDialog(null, form, "Register Employee", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String selectedOutlet = (String) outletBox.getSelectedItem();
            if (selectedOutlet.equals("New...") || outlets.length == 0) {
                selectedOutlet = JOptionPane.showInputDialog("Enter New Outlet Name:");
            }
            
            if (selectedOutlet != null && !selectedOutlet.isEmpty()) {
                loginSystem.addUser(new Employee(id.getText(), new String(pass.getPassword()), name.getText(), (String) roleBox.getSelectedItem(), selectedOutlet));
                JOptionPane.showMessageDialog(null, "Employee Registered!");
            }
        }
    }
    
    private static void performAddProduct() {
        try {
            String pid = JOptionPane.showInputDialog("New Product ID:");
            String nam = JOptionPane.showInputDialog("Product Name:");
            double prc = Double.parseDouble(JOptionPane.showInputDialog("Price (RM):"));
            
            String[] outs = loginSystem.getActiveOutlets();
            if (outs.length == 0) { JOptionPane.showMessageDialog(null, "No Outlets. Add employee first."); return; }
            
            int o = JOptionPane.showOptionDialog(null, "Initialize Stock at:", "Select Outlet", 0,3,null,outs,outs[0]);
            int q = Integer.parseInt(JOptionPane.showInputDialog("Initial Quantity:"));
            
            Product p = new Product(pid, nam, prc);
            p.setQuantity(outs[o], q);
            inventorySystem.addProduct(p);
            
            JOptionPane.showMessageDialog(null, "Product Added & Saved!");
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid Input");
        }
    }

    private static void managerStockOps(InventorySystem inv, LoginSystem ls, String mgrName) {
         String[] outs = ls.getActiveOutlets();
         if (outs.length == 0) return;
         
         String pid = JOptionPane.showInputDialog("Product ID:");
         int o = JOptionPane.showOptionDialog(null, "Select Outlet", "Location", 0,3,null,outs,outs[0]);
         
         String[] ops = {"IN (Restock)", "OUT (Remove)"};
         int op = JOptionPane.showOptionDialog(null, "Action Type", "Operations", 0,3,null,ops,ops[0]);
         
         try {
             int qty = Integer.parseInt(JOptionPane.showInputDialog("Quantity:"));
             if (op == 0)
                 JOptionPane.showMessageDialog(null, inv.stockIn(pid, qty, outs[o], mgrName));
             else
                 JOptionPane.showMessageDialog(null, inv.stockOut(pid, qty, outs[o], mgrName, "Manager Ops"));
         } catch(Exception e) {
             JOptionPane.showMessageDialog(null, "Invalid Number");
         }
    }
    
    private static void employeeStockOps(InventorySystem inv, Employee emp) {
        String pid = JOptionPane.showInputDialog("Product ID:");
        String[] ops = {"IN (Return)", "OUT (Usage/Damage)"};
        int op = JOptionPane.showOptionDialog(null, "Action Type", "Operations", 0,3,null,ops,ops[0]);
        
        try {
            int qty = Integer.parseInt(JOptionPane.showInputDialog("Quantity:"));
            if (op == 0)
                JOptionPane.showMessageDialog(null, inv.stockIn(pid, qty, emp.getOutletId(), emp.getName()));
            else
                JOptionPane.showMessageDialog(null, inv.stockOut(pid, qty, emp.getOutletId(), emp.getName(), "Staff Ops"));
        } catch(Exception e) {
             JOptionPane.showMessageDialog(null, "Invalid Number");
        }
    }

    private static void attendanceGUI(Employee emp) {
        String[] a = {"Clock In", "Clock Out", "Check Status"};
        int c = JOptionPane.showOptionDialog(null, "Attendance Menu", "Time Clock", 0,1,null,a,a[0]);
        
        if (c == 0) JOptionPane.showMessageDialog(null, attendanceLog.clockIn(emp));
        else if (c == 1) JOptionPane.showMessageDialog(null, attendanceLog.clockOut(emp));
        else if (c == 2) JOptionPane.showMessageDialog(null, attendanceLog.checkStatus(emp));
    }
    
    private static void performStaffStockCount(StockCountSystem scs, InventorySystem inv, Employee emp) {
        String[] s = {"Morning Count", "Night Count"};
        int c = JOptionPane.showOptionDialog(null, "Select Session", "Stock Count", 0, 1, null, s, s[0]);
        if (c != -1) {
            StringBuilder rep = new StringBuilder("=== COUNT SUMMARY ===\n");
            for(Product p : inv.getAllProducts()) {
                String input = JOptionPane.showInputDialog(null,
                        "Item: " + p.getName() + "\nSystem Stock: " + p.getQuantity(emp.getOutletId()) + "\n\nEnter PHYSICAL Qty:");
                
                if (input == null) break;
                try {
                    int phys = Integer.parseInt(input);
                    scs.logCount(s[c], emp.getOutletId(), emp.getName(), p.getProductID(), p.getName(), p.getQuantity(emp.getOutletId()), phys);
                    rep.append(p.getName()).append(": ").append(phys).append("\n");
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Invalid number for " + p.getName());
                }
            }
            showScrollMsg("Count Complete", rep.toString());
        }
    }

    private static void performSearch(SearchSystem searchSystem) {
        String[] types = {"Check Stock by Model", "Search Sales History"};
        int t = JOptionPane.showOptionDialog(null, "Search Type", "Search", 0,3,null,types,types[0]);
        
        if (t == 0) {
            String q = JOptionPane.showInputDialog("Enter Model Name / ID:");
            if (q != null) showScrollMsg("Stock Results", searchSystem.searchStockByModel(q));
        } else {
            String q = JOptionPane.showInputDialog("Enter Keyword (Date / Cust Name / ID):");
            if (q != null) showScrollMsg("Sales Results", searchSystem.searchSalesRecord(q, "ANY"));
        }
    }

    private static void recordSale(SalesSystem ss, InventorySystem inv, Employee emp) {
        String cust = JOptionPane.showInputDialog("Customer Name (or 'Walk-in'):");
        if (cust == null) return;
        
        SalesTransaction tx = ss.createNewTransaction(cust, emp);
        
        while (true) {
            String pid = JOptionPane.showInputDialog("Current Total: RM" + tx.getTotalPrice() + "\n\nEnter Product ID (Cancel to Finish):");
            if (pid == null) break;
            
            try {
                String qtyStr = JOptionPane.showInputDialog("Quantity:");
                if (qtyStr == null) continue;
                int q = Integer.parseInt(qtyStr);
                
                String result = ss.addItemToTransaction(tx, pid, q);
                JOptionPane.showMessageDialog(null, result);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid Input");
            }
        }
        
        if (!tx.getItems().isEmpty()) {
            String[] m = {"Cash", "Card", "QR Pay"};
            int p = JOptionPane.showOptionDialog(null, "Select Payment Method", "Checkout", 0,3,null,m,m[0]);
            if (p == -1) return;
            
            double paid = tx.getTotalPrice();
            if (p == 0) {
                 String in = JOptionPane.showInputDialog("Total: RM" + tx.getTotalPrice() + "\nCash Received:");
                 if (in != null) paid = Double.parseDouble(in);
            }
            
            showScrollMsg("Receipt", ss.finalizeTransaction(tx, paid, m[p]));
        } else {
            JOptionPane.showMessageDialog(null, "Transaction Cancelled.");
        }
    }

    private static void showProductTable(InventorySystem inv) {
        ArrayList<Product> list = inv.getAllProducts();
        String[][] data = new String[list.size()][4];
        String[] cols = {"ID", "Name", "Price", "Stock Info"};
        
        for (int i = 0; i < list.size(); i++){
             Product p = list.get(i);
             data[i][0] = p.getProductID();
             data[i][1] = p.getName();
             data[i][2] = String.format("%.2f", p.getPrice());
             data[i][3] = p.getStockDisplay();
        }
        JOptionPane.showMessageDialog(null, new JScrollPane(new JTable(data, cols)));
    }
    
    // --- METHOD HELPER BARU: TAMPILKAN PERFORMANCE ---
    private static void showPerformanceReport(Manager mgr) {
        try {
            List<EmployeeStats> report = analytics.getEmployeePerformanceReport(mgr);
            
            StringBuilder sb = new StringBuilder("=== EMPLOYEE SALES LEADERBOARD ===\n\n");
            sb.append(String.format("%-5s %-15s %-10s %s\n", "Rank", "Name", "Trans.", "Total Sales"));
            sb.append("----------------------------------------------------\n");
            
            int rank = 1;
            for (EmployeeStats s : report) {
                sb.append(String.format("#%-4d %-15s %-10d RM %,.2f\n", 
                    rank++, s.getEmployeeName(), s.getTransactionCount(), s.getTotalSales()));
            }
            showScrollMsg("Performance Analytics", sb.toString());
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void showScrollMsg(String title, String msg) {
        JTextArea ta = new JTextArea(msg);
        ta.setRows(15);
        ta.setColumns(40);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(null, new JScrollPane(ta), title, JOptionPane.INFORMATION_MESSAGE);
    }
}