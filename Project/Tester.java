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
        SalesSystem salesSystem = new SalesSystem(inventorySystem); // NEW! Pass inventory

        // --- 2. LOOP UTAMA PROGRAM ---
        while (true) {
            
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

                if (choice == 0) {
                    String id = JOptionPane.showInputDialog("Enter User ID:");
                    if (id == null) continue;

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
                } else {
                    JOptionPane.showMessageDialog(null, "Goodbye! Shutting down...");
                    System.exit(0);
                }

            } else {
                User currentUser = loginSystem.getCurrentUser();

                // === MANAGER DASHBOARD ===
                if (loginSystem.isManager()) {
                    String[] mgrOptions = {"Register Employee", "View Stock", "Add New Product", 
                                           "View History Log", "View Sales History", "Logout"}; // Added Sales History
                    
                    int mgrChoice = JOptionPane.showOptionDialog(
                        null, 
                        "MANAGER DASHBOARD\nUser: " + currentUser.getName(), 
                        "Admin Menu", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, mgrOptions, mgrOptions[0]
                    );

                    if (mgrChoice == 0) {
                        // Register Employee
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
                    else if (mgrChoice == 1) {
                        // View Stock
                        showProductTable(inventorySystem);
                    }
                    else if (mgrChoice == 2) {
                        // Add New Product
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
                    else if (mgrChoice == 3) {
                        // View Transaction History
                        String logs = inventorySystem.readTransactionLogs();
                        
                        JTextArea textArea = new JTextArea(logs);
                        textArea.setEditable(false);
                        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        
                        JScrollPane scroll = new JScrollPane(textArea);
                        scroll.setPreferredSize(new Dimension(600, 400));
                        
                        JOptionPane.showMessageDialog(null, scroll, "Transaction Logs", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if (mgrChoice == 4) {
                        // View Sales History (NEW!)
                        String salesLogs = salesSystem.readSalesHistory();
                        
                        JTextArea textArea = new JTextArea(salesLogs);
                        textArea.setEditable(false);
                        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        
                        JScrollPane scroll = new JScrollPane(textArea);
                        scroll.setPreferredSize(new Dimension(700, 400));
                        
                        JOptionPane.showMessageDialog(null, scroll, "Sales History", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else {
                        // Logout
                        loginSystem.logout();
                    }

                // === EMPLOYEE DASHBOARD ===
                } else {
                    Employee emp = (Employee) currentUser;
                    String[] empOptions = {"Attendance", "Stock Operations", "Stock Count (Audit)", 
                                          "Record Sale", "Logout"}; // Added Record Sale
                    
                    int empChoice = JOptionPane.showOptionDialog(
                        null, 
                        "STAFF DASHBOARD\nName: " + emp.getName() + "\nOutlet: " + emp.getOutletId(), 
                        "Staff Menu", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, empOptions, empOptions[0]
                    );

                    if (empChoice == 0) {
                        // Attendance
                        String[] attOpts = {"Clock In", "Clock Out", "Check Status", "Back"};
                        int attChoice = JOptionPane.showOptionDialog(null, "Attendance Menu", "Time Clock", 0, 1, null, attOpts, attOpts[0]);
                        
                        if (attChoice == 0) JOptionPane.showMessageDialog(null, attendanceLog.clockIn(emp));
                        else if (attChoice == 1) JOptionPane.showMessageDialog(null, attendanceLog.clockOut(emp));
                        else if (attChoice == 2) JOptionPane.showMessageDialog(null, attendanceLog.checkStatus(emp));
                    }
                    
                    else if (empChoice == 1) {
                        // Stock Operations
                        String[] stockTypes = {"Stock In (Receive)", "Stock Out (Sale/Transfer)", "Back"};
                        int typeChoice = JOptionPane.showOptionDialog(null, "Choose Action:", "Stock Ops", 0, 1, null, stockTypes, stockTypes[0]);
                        
                        if (typeChoice == 0 || typeChoice == 1) {
                            showProductSimpleList(inventorySystem);
                            
                            String pid = JOptionPane.showInputDialog("Enter Product ID:");
                            String qtyStr = JOptionPane.showInputDialog("Enter Quantity:");
                            
                            try {
                                int qty = Integer.parseInt(qtyStr);
                                if (typeChoice == 0) {
                                    String res = inventorySystem.stockIn(pid, qty, emp.getName());
                                    JOptionPane.showMessageDialog(null, res);
                                } else {
                                    String reason = JOptionPane.showInputDialog("Reason for Stock Out:");
                                    String res = inventorySystem.stockOut(pid, qty, emp.getName(), reason);
                                    JOptionPane.showMessageDialog(null, res);
                                }
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(null, "Invalid Number!");
                            }
                        }
                    }

                    else if (empChoice == 2) {
                        // Stock Count
                        String[] sessions = {"Morning Count", "Night Count", "Back"};
                        int sessChoice = JOptionPane.showOptionDialog(null, "Select Audit Session:", "Audit", 0, 1, null, sessions, sessions[0]);

                        if (sessChoice == 0 || sessChoice == 1) {
                            String session = (sessChoice == 0) ? "MORNING" : "NIGHT";
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

                    else if (empChoice == 3) {
                        // ========== RECORD SALE (NEW!) ==========
                        recordSale(salesSystem, inventorySystem, emp);
                    }

                    else {
                        // Logout
                        loginSystem.logout();
                    }
                }
            }
        }
    }

    // === HELPER: RECORD SALE ===
    private static void recordSale(SalesSystem salesSystem, InventorySystem inventory, Employee employee) {
        // Step 1: Customer name
        String customerName = JOptionPane.showInputDialog("Enter Customer Name:");
        if (customerName == null || customerName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Sale cancelled: Customer name required.");
            return;
        }
        
        // Step 2: Create transaction
        SalesTransaction transaction = salesSystem.createNewTransaction(customerName, employee);
        
        // Step 3: Add items (loop)
        boolean addingItems = true;
        while (addingItems) {
            // Show product list
            showProductSimpleList(inventory);
            
            String productID = JOptionPane.showInputDialog("Enter Product ID:");
            if (productID == null || productID.trim().isEmpty()) break;
            
            String qtyStr = JOptionPane.showInputDialog("Enter Quantity:");
            if (qtyStr == null) break;
            
            try {
                int quantity = Integer.parseInt(qtyStr);
                
                String result = salesSystem.addItemToTransaction(transaction, productID, quantity);
                JOptionPane.showMessageDialog(null, result);
                
                if (result.startsWith("ERROR")) {
                    continue; // Retry
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid number format!", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            int moreItems = JOptionPane.showConfirmDialog(null, "Add more items?", "Continue", JOptionPane.YES_NO_OPTION);
            if (moreItems != JOptionPane.YES_OPTION) {
                addingItems = false;
            }
        }
        
        if (transaction.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Sale cancelled: No items added.");
            return;
        }
        
        // Step 4: Payment method
        String[] paymentMethods = {"Cash", "Credit Card", "Debit Card", "E-wallet"};
        int paymentChoice = JOptionPane.showOptionDialog(null, "Select Payment Method:", "Payment", 
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, paymentMethods, paymentMethods[0]);
        
        if (paymentChoice == -1) {
            JOptionPane.showMessageDialog(null, "Sale cancelled: Payment method required.");
            return;
        }
        
        String paymentMethod = paymentMethods[paymentChoice];
        
        // Step 5: Confirm
        String summary = salesSystem.getTransactionSummary(transaction);
        summary += "\n\nPayment Method: " + paymentMethod;
        summary += "\n\nConfirm transaction?";
        
        int confirm = JOptionPane.showConfirmDialog(null, summary, "Confirm Sale", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String result = salesSystem.finalizeTransaction(transaction, paymentMethod, employee.getName());
            JOptionPane.showMessageDialog(null, result, "Transaction Complete", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Sale cancelled.");
        }
    }

    // === HELPER: TABLE VIEW ===
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

    // === HELPER: SIMPLE LIST ===
    private static void showProductSimpleList(InventorySystem inv) {
        ArrayList<Product> list = inv.getAllProducts();
        StringBuilder sb = new StringBuilder("=== PRODUCT REFERENCE ===\n\n");
        for (Product p : list) {
            sb.append(String.format("[%s] %s - RM%.2f (Stock: %d)\n", 
                p.getProductID(), p.getName(), p.getPrice(), p.getQuantity()));
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(null, new JScrollPane(textArea), "Product List", JOptionPane.INFORMATION_MESSAGE);
    }
}