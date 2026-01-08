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
        StockCountSystem stockCountSystem = new StockCountSystem(inventorySystem);
        
        // [BARU] Inisialisasi Search System
        SearchSystem searchSystem = new SearchSystem(inventorySystem);

        // --- 2. LOOP UTAMA PROGRAM ---
        while (true) {
            // A. CEK LOGIN STATUS
            if (!loginSystem.isLoggedIn()) {
                String[] options = {"Login", "Exit"};
                int choice = JOptionPane.showOptionDialog(null, "Welcome to GoldenHour System", "Main Menu", 
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

                if (choice == 0) {
                    // --- FLOW LOGIN ---
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
                    // --- FLOW EXIT ---
                    JOptionPane.showMessageDialog(null, "Thank you for using GoldenHour System.\nHave a nice day, Goodbye!", "See You Soon", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }

            } else {
                User currentUser = loginSystem.getCurrentUser();

                // ==========================
                // === MANAGER DASHBOARD ====
                // ==========================
                if (loginSystem.isManager()) {
                    String[] mgrOptions = {"Register Employee", "View Stock", "Add New Product", "Stock In/Out", "Sales Reports", "Stock Count Logs", "Logout"};
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
                            
                            String[] existingOutlets = loginSystem.getActiveOutlets();
                            String targetOutlet;
                            
                            if (existingOutlets.length == 0) {
                                targetOutlet = JOptionPane.showInputDialog("No active outlets found.\nEnter NEW Outlet Name (e.g., KLCC):");
                            } else {
                                String[] outletOptions = new String[existingOutlets.length + 1];
                                System.arraycopy(existingOutlets, 0, outletOptions, 0, existingOutlets.length);
                                outletOptions[existingOutlets.length] = "[Create New Outlet]";
                                
                                int outChoice = JOptionPane.showOptionDialog(null, "Assign Outlet", "Location", 0, 3, null, outletOptions, outletOptions[0]);
                                if (outChoice == -1) continue;
                                targetOutlet = (outChoice == existingOutlets.length) ? JOptionPane.showInputDialog("Enter NEW Outlet Name:") : existingOutlets[outChoice];
                            }
                            
                            if (targetOutlet != null && !targetOutlet.trim().isEmpty()) {
                                loginSystem.addUser(new Employee(newId, newPass, newName, (r==0?"Part-time":"Full-time"), targetOutlet));
                                JOptionPane.showMessageDialog(null, "Employee Registered to " + targetOutlet + "!");
                            }
                        }
                        
                    } else if (mgrChoice == 1) { 
                        showProductTable(inventorySystem);
                        
                    } else if (mgrChoice == 2) { 
                        // --- ADD NEW PRODUCT ---
                        try {
                            String[] activeOutlets = loginSystem.getActiveOutlets();
                            if (activeOutlets.length == 0) {
                                JOptionPane.showMessageDialog(null, "No Active Outlets Found!\nPlease Register an Employee to an Outlet first.");
                                continue;
                            }
                            String pid = JOptionPane.showInputDialog("Enter New Product ID:");
                            if (pid == null) continue;
                            String nam = JOptionPane.showInputDialog("Enter Product Name:");
                            double prc = Double.parseDouble(JOptionPane.showInputDialog("Enter Price (RM):"));
                            
                            int outChoice = JOptionPane.showOptionDialog(null, "Select Outlet for Initial Stock:", "Initial Location", 
                                    0, 3, null, activeOutlets, activeOutlets[0]);
                            if (outChoice == -1) continue; 
                            
                            int qty = Integer.parseInt(JOptionPane.showInputDialog("Enter Initial Quantity for " + activeOutlets[outChoice] + ":"));
                            Product newP = new Product(pid, nam, prc);
                            newP.setQuantity(activeOutlets[outChoice], qty); 
                            inventorySystem.addProduct(newP);
                            JOptionPane.showMessageDialog(null, "Product Added Successfully!");
                            
                        } catch (Exception e) { JOptionPane.showMessageDialog(null, "Invalid Input!"); }
                        
                    } else if (mgrChoice == 3) { 
                        managerStockOps(inventorySystem, loginSystem, currentUser.getName());
                        
                    } else if (mgrChoice == 4) { 
                         JOptionPane.showMessageDialog(null, new JScrollPane(new JTextArea(salesSystem.readSalesHistory())));
                    
                    } else if (mgrChoice == 5) { 
                        JTextArea ta = new JTextArea(stockCountSystem.readCountHistory());
                        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        JOptionPane.showMessageDialog(null, new JScrollPane(ta), "Stock Count Reports", JOptionPane.INFORMATION_MESSAGE);

                    } else {
                        loginSystem.logout();
                    }

                // ===========================
                // === EMPLOYEE DASHBOARD ====
                // ===========================
                } else {
                    Employee emp = (Employee) currentUser;
                    // [UPDATE] Menu bertambah: "Search Info"
                    String[] empOptions = {"Attendance", "Daily Stock Count", "Search Info", "Stock Operations", "Record Sale", "Logout"};
                    int empChoice = JOptionPane.showOptionDialog(null, "STAFF: " + emp.getName() + " (" + emp.getOutletId() + ")", "Staff Menu", 
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, empOptions, empOptions[0]);

                    if (empChoice == 0) { // Attendance
                        String[] att = {"Clock In", "Clock Out", "Status"};
                        int c = JOptionPane.showOptionDialog(null, "Attendance", "Clock", 0, 1, null, att, att[0]);
                        if (c==0) JOptionPane.showMessageDialog(null, attendanceLog.clockIn(emp));
                        else if (c==1) JOptionPane.showMessageDialog(null, attendanceLog.clockOut(emp));
                        else if (c==2) JOptionPane.showMessageDialog(null, attendanceLog.checkStatus(emp));
                        
                    } else if (empChoice == 1) { // Daily Stock Count
                        String[] sessions = {"Morning Count", "Night Count"};
                        int s = JOptionPane.showOptionDialog(null, "Select Session:", "Stock Count", 0, 1, null, sessions, sessions[0]);
                        if (s != -1) performStaffStockCount(stockCountSystem, inventorySystem, emp, sessions[s]);

                    } else if (empChoice == 2) { 
                        // === [BARU] SEARCH INFO ===
                        performSearch(searchSystem);

                    } else if (empChoice == 3) { // Stock Ops (Geser index)
                        employeeStockOps(inventorySystem, emp);
                        
                    } else if (empChoice == 4) { // Sale (Geser index)
                        recordSale(salesSystem, inventorySystem, emp);
                        
                    } else {
                        loginSystem.logout();
                    }
                }
            }
        }
    }

    // =========================================================
    // ============= LOGIC METHODS (CORE FEATURES) =============
    // =========================================================

    // === [METHOD BARU] SEARCH INFORMATION LOGIC ===
    private static void performSearch(SearchSystem searchSystem) {
        String[] types = {"Stock Information", "Sales Information"};
        int typeChoice = JOptionPane.showOptionDialog(null, "What do you want to search?", "Search Information", 
                0, 3, null, types, types[0]);
        
        if (typeChoice == 0) {
            // 1. Stock Info: Search by Model Name
            String query = JOptionPane.showInputDialog("Enter Model Name or Product ID:");
            if (query != null && !query.trim().isEmpty()) {
                String result = searchSystem.searchStockByModel(query);
                JTextArea ta = new JTextArea(result);
                ta.setRows(15);
                ta.setColumns(40);
                JOptionPane.showMessageDialog(null, new JScrollPane(ta), "Stock Search Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (typeChoice == 1) {
            // 2. Sales Info: Verify Authenticity
            String[] filters = {"By Date (YYYY-MM-DD)", "By Customer Name", "By Model Name"};
            int filterChoice = JOptionPane.showOptionDialog(null, "Select Search Filter:", "Sales Records", 
                    0, 3, null, filters, filters[0]);
            
            if (filterChoice != -1) {
                String query = JOptionPane.showInputDialog("Enter search keyword:");
                if (query != null && !query.trim().isEmpty()) {
                    String category = "";
                    if (filterChoice == 0) category = "DATE";
                    else if (filterChoice == 1) category = "CUSTOMER";
                    else if (filterChoice == 2) category = "MODEL";
                    
                    String result = searchSystem.searchSalesRecord(query, category);
                    JTextArea ta = new JTextArea(result);
                    ta.setRows(15);
                    ta.setColumns(50);
                    JOptionPane.showMessageDialog(null, new JScrollPane(ta), "Sales Search Result", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    // --- FITUR STOCK COUNT (YANG LAMA) ---
    private static void performStaffStockCount(StockCountSystem scs, InventorySystem inv, Employee emp, String session) {
        ArrayList<Product> products = inv.getAllProducts();
        int totalProducts = products.size();
        StringBuilder summary = new StringBuilder("=== " + session.toUpperCase() + " SUMMARY ===\n");
        boolean anyDiscrepancy = false;
        int countIndex = 1;

        for (Product p : products) {
            int systemQty = p.getQuantity(emp.getOutletId());
            String input = JOptionPane.showInputDialog(null, 
                "Product (" + countIndex + "/" + totalProducts + ")\n" +
                "Name: " + p.getName() + " (" + p.getProductID() + ")\n" +
                "System Stock: " + systemQty + "\n\n" +
                "Enter PHYSICAL Quantity (Real Count):", 
                session + " - " + emp.getOutletId(), JOptionPane.QUESTION_MESSAGE);
            
            if (input == null) {
                JOptionPane.showMessageDialog(null, "Process Cancelled.");
                return;
            }

            try {
                int physQty = Integer.parseInt(input);
                scs.logCount(session, emp.getOutletId(), emp.getName(), p.getProductID(), p.getName(), systemQty, physQty);
                int diff = physQty - systemQty;
                String status = (diff == 0) ? "MATCH" : (diff < 0 ? "MISSING " + Math.abs(diff) : "EXTRA " + diff);
                if (diff != 0) anyDiscrepancy = true;
                summary.append(countIndex).append(". ").append(p.getName()).append(": ").append(status).append("\n");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid Number. Item skipped.");
                summary.append(countIndex).append(". ").append(p.getName()).append(": SKIPPED (Error)\n");
            }
            countIndex++;
        }
        summary.append("\nReport saved.");
        if (anyDiscrepancy) summary.append("\nWARNING: Discrepancies found!");
        JTextArea ta = new JTextArea(summary.toString());
        JOptionPane.showMessageDialog(null, new JScrollPane(ta), "Count Completed", anyDiscrepancy ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // --- FITUR RECORD SALE (YANG LAMA) ---
    private static void recordSale(SalesSystem salesSystem, InventorySystem inventory, Employee employee) {
        String customerName = JOptionPane.showInputDialog("Enter Customer Name:");
        if (customerName == null || customerName.trim().isEmpty()) return;
        SalesTransaction transaction = salesSystem.createNewTransaction(customerName, employee);
        boolean addingItems = true;
        
        while (addingItems) {
            showProductListForStaff(inventory, employee.getOutletId());
            String productID = JOptionPane.showInputDialog("Cart: RM " + String.format("%.2f", transaction.getTotalPrice()) + "\nEnter Product ID:");
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
        int methodChoice = JOptionPane.showOptionDialog(null, "Total: RM " + String.format("%.2f", transaction.getTotalPrice()), 
                "Payment Method", 0, 3, null, methods, methods[0]);
        if (methodChoice == -1) return;
        double amountPaid = 0.0;
        
        if (methods[methodChoice].equals("Cash")) {
            String paymentStr = JOptionPane.showInputDialog("Total Bill: RM " + String.format("%.2f", transaction.getTotalPrice()) + "\nEnter CASH amount received:");
            if (paymentStr == null) return;
            try { amountPaid = Double.parseDouble(paymentStr); } 
            catch (Exception e) { JOptionPane.showMessageDialog(null, "Invalid Amount"); return; }
        } else { amountPaid = transaction.getTotalPrice(); }
        
        String res = salesSystem.finalizeTransaction(transaction, amountPaid, methods[methodChoice]);
        if (!res.startsWith("ERROR")) {
            JTextArea ta = new JTextArea("=== RECEIPT ===\nOutlet: " + employee.getOutletId() + "\n" + res);
            JOptionPane.showMessageDialog(null, new JScrollPane(ta));
        } else { JOptionPane.showMessageDialog(null, res); }
    }

    // --- MANAGER OPS & HELPER (TETAP SAMA) ---
    private static void managerStockOps(InventorySystem inv, LoginSystem login, String mgrName) {
        String[] activeOutlets = login.getActiveOutlets();
        if (activeOutlets.length == 0) { JOptionPane.showMessageDialog(null, "No Active Outlets Found."); return; }
        String[] ops = {"Stock In", "Stock Out"};
        int op = JOptionPane.showOptionDialog(null, "Type", "Manager Stock", 0, 1, null, ops, ops[0]);
        if (op == -1) return;
        showProductSimpleList(inv);
        String pid = JOptionPane.showInputDialog("Product ID:");
        if (pid == null) return;
        int outChoice = JOptionPane.showOptionDialog(null, "Select Outlet:", "Outlet Selection", 0, 3, null, activeOutlets, activeOutlets[0]);
        if (outChoice == -1) return;
        try {
            int q = Integer.parseInt(JOptionPane.showInputDialog("Quantity:"));
            if (op == 0) JOptionPane.showMessageDialog(null, inv.stockIn(pid, q, activeOutlets[outChoice], mgrName));
            else JOptionPane.showMessageDialog(null, inv.stockOut(pid, q, activeOutlets[outChoice], mgrName, JOptionPane.showInputDialog("Reason:")));
        } catch(Exception e) { JOptionPane.showMessageDialog(null, "Invalid Input"); }
    }

    private static void employeeStockOps(InventorySystem inv, Employee emp) {
        String[] ops = {"Stock In", "Stock Out"};
        int op = JOptionPane.showOptionDialog(null, "Type", "Stock Ops (" + emp.getOutletId() + ")", 0, 1, null, ops, ops[0]);
        showProductSimpleList(inv);
        String pid = JOptionPane.showInputDialog("Product ID:");
        try {
            int q = Integer.parseInt(JOptionPane.showInputDialog("Quantity:"));
            if (op == 0) JOptionPane.showMessageDialog(null, inv.stockIn(pid, q, emp.getOutletId(), emp.getName()));
            else JOptionPane.showMessageDialog(null, inv.stockOut(pid, q, emp.getOutletId(), emp.getName(), JOptionPane.showInputDialog("Reason:")));
        } catch(Exception e) { JOptionPane.showMessageDialog(null, "Invalid Input"); }
    }

    private static void showProductSimpleList(InventorySystem inv) {
        StringBuilder sb = new StringBuilder("Global Stock View:\n");
        for (Product p : inv.getAllProducts()) {
            sb.append(p.getProductID()).append(" - ").append(p.getName()).append(" [").append(p.getStockDisplay()).append("]\n");
        }
        JTextArea textArea = new JTextArea(sb.toString());
        JOptionPane.showMessageDialog(null, new JScrollPane(textArea), "All Stocks", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showProductListForStaff(InventorySystem inv, String outletId) {
        StringBuilder sb = new StringBuilder("Stock at " + outletId + ":\n");
        for (Product p : inv.getAllProducts()) {
            int qty = p.getQuantity(outletId);
            sb.append(p.getProductID()).append(" - ").append(p.getName()).append(" (Qty: ").append(qty).append(")\n");
        }
        JTextArea textArea = new JTextArea(sb.toString());
        JOptionPane.showMessageDialog(null, new JScrollPane(textArea), "Outlet Stock", JOptionPane.INFORMATION_MESSAGE);
    }

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
        table.getColumnModel().getColumn(3).setPreferredWidth(300); 
        JOptionPane.showMessageDialog(null, new JScrollPane(table), "Inventory Table", JOptionPane.INFORMATION_MESSAGE);
    }
}