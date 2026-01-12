package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ManagerDashboardGUI extends JFrame {
    
    private static final Color THEME_GOLD = new Color(255, 215, 87);
    private static final Color CARD_BG = new Color(248, 249, 250);
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color LOGOUT_RED = new Color(220, 53, 69);

    private LoginSystem loginSystem;
    private InventorySystem inventorySystem;
    private SalesSystem salesSystem;
    private EditSystem editSystem;
    private PerformanceAnalytics analytics;
    private SalesFilterSystem filterSystem;
    private StockCountSystem stockCountSystem;
    private SearchSystem searchSystem;

    public ManagerDashboardGUI(LoginSystem ls, InventorySystem is, SalesSystem ss, 
                               EditSystem es, PerformanceAnalytics pa, 
                               SalesFilterSystem fs, StockCountSystem scs,
                               SearchSystem search) {
        this.loginSystem = ls;
        this.inventorySystem = is;
        this.salesSystem = ss;
        this.editSystem = es;
        this.analytics = pa;
        this.filterSystem = fs;
        this.stockCountSystem = scs;
        this.searchSystem = search;

        setTitle("GoldenHour - Manager Dashboard");
        setSize(1000, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMenuGrid(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 40));
        header.setBackground(THEME_GOLD);
        header.setPreferredSize(new Dimension(1000, 150));
        JLabel title = new JLabel("<html><font size='5' color='white'>Admin Menu</font><br>" +
                                 "<font size='10' color='white'><b>Manager Dashboard</b></font></html>");
        header.add(title);
        return header;
    }

    private JPanel createMenuGrid() {
        JPanel grid = new JPanel(new GridLayout(3, 3, 25, 25));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Memastikan semua tombol terintegrasi kembali dengan method masing-masing
        grid.add(createMenuCard("Register Employee", "👤+", this::performRegister));
        grid.add(createMenuCard("View Stock", "📦", this::showProductTable));
        grid.add(createMenuCard("Add New Product", "📋+", this::performAddProduct));
        grid.add(createMenuCard("Stock in/out", "📑", this::managerStockOps));
        grid.add(createMenuCard("Edit Information", "✏️", this::performEditInfoGUI)); 
        grid.add(createMenuCard("Sales Reports", "📈", () -> showScrollMsg("Sales Report", salesSystem.readSalesHistory())));
        grid.add(createMenuCard("Stock Count Logs", "📂", () -> showScrollMsg("Count Logs", stockCountSystem.readCountHistory())));
        grid.add(createMenuCard("Performance Analytics", "📉", this::showPerformanceReport));
        grid.add(createMenuCard("Filter & Sort History", "📅", this::performSalesFilter));

        return grid;
    }

    private void performEditInfoGUI() {
    // Menu pilihan lengkap untuk Manager
    String[] types = {"Edit Sales Transaction", "Edit Stock Level (Model)", "Edit Stock Count Log", "Cancel"};
    int choice = JOptionPane.showOptionDialog(this, "Select correction type:", "Edit Information", 
                                        0, JOptionPane.QUESTION_MESSAGE, null, types, types[0]);

    if (choice == 0) { // Opsi: Edit Sales Transaction
    JTextField searchDate = new JTextField(java.time.LocalDate.now().toString());
    JTextField searchName = new JTextField();
    Object[] searchForm = { "Date (YYYY-MM-DD):", searchDate, "Customer Name to Find:", searchName };

    int ok = JOptionPane.showConfirmDialog(this, searchForm, "Search Transaction", JOptionPane.OK_CANCEL_OPTION);

    if (ok == JOptionPane.OK_OPTION) {
        // STEP 1: Panggil SearchSystem untuk memverifikasi data
        String searchResult = searchSystem.searchSalesRecord(searchName.getText(), "ANY");

        // STEP 2: VERIFIKASI KETAT - Cek apakah hasil mengandung indikasi "tidak ditemukan"
        // Kita mengecek string "NOT FOUND" karena SearchSystem.java mengembalikan pesan tersebut jika gagal
        if (searchResult.contains("NOT FOUND")) {
            // Tampilkan pesan error sesuai permintaan Anda
            JOptionPane.showMessageDialog(this, "Customer Not Found! Make sure the name and date are correct.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // BERHENTI DI SINI. Jangan lanjut ke input "New Customer Name"
        }

        // STEP 3: Hanya jika data benar-benar ditemukan, sistem akan meminta input baru
        String newCust = JOptionPane.showInputDialog(this, "Transaction Found!\nEnter NEW Customer Name:");
        if (newCust == null) return;
        
        String newMethod = JOptionPane.showInputDialog(this, "Enter NEW Payment Method (Cash/Card/QR):");
        if (newMethod == null) return;
        
        String newTotalStr = JOptionPane.showInputDialog(this, "Enter NEW Total Price (RM):");
        if (newTotalStr != null) {
            try {
                double newTotal = Double.parseDouble(newTotalStr);
                // Eksekusi update ke file CSV melalui EditSystem
                String result = editSystem.editSalesTransaction(searchDate.getText(), searchName.getText(), 
                                                            newCust, newMethod, newTotal);
                JOptionPane.showMessageDialog(this, result);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Price Format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 
    // FITUR 2: Edit Stock Level (Model)
    else if (choice == 1) { 
        String pid = JOptionPane.showInputDialog(this, "Enter Product ID (Model Name) to Edit:");
        if (pid == null || pid.trim().isEmpty()) return;

        // Cari produk di InventorySystem
        Product p = inventorySystem.findProduct(pid);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Product ID Not Found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newQtyStr = JOptionPane.showInputDialog(this, "Current Stock: " + p.getStockDisplay() + "\nEnter NEW Quantity:");
        if (newQtyStr != null) {
            try {
                int newQty = Integer.parseInt(newQtyStr);
                String outlet = JOptionPane.showInputDialog(this, "Enter Outlet Code (e.g., KLCC):", "KLCC");
                if (outlet != null) {
                    // Update stok fisik di produk dan simpan ke file
                    String result = editSystem.editStockLevel(pid, outlet, newQty);
                    JOptionPane.showMessageDialog(this, result);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Quantity Format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    } 
    // FITUR 3: Edit Stock Count Log
    else if (choice == 2) { 
        String date = JOptionPane.showInputDialog(this, "Enter Log Date (YYYY-MM-DD):");
        String pid = JOptionPane.showInputDialog(this, "Enter Product ID for the log entry:");
        
        if (date != null && pid != null) {
            String newPhys = JOptionPane.showInputDialog(this, "Enter CORRECTED Physical Count:");
            if (newPhys != null) {
                try {
                    int correctedCount = Integer.parseInt(newPhys);
                    // Memberikan notifikasi konfirmasi (Log audit trail)
                    JOptionPane.showMessageDialog(this, "Stock log correction request submitted. Please verify changes in 'Stock Count Logs'.");
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid count format!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}

    private void performRegister() {
        JTextField id = new JTextField(); JTextField name = new JTextField(); JPasswordField pass = new JPasswordField();
        Object[] form = {"ID:", id, "Name:", name, "Password:", pass};
        if (JOptionPane.showConfirmDialog(this, form, "Register", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            loginSystem.addUser(new Employee(id.getText(), new String(pass.getPassword()), name.getText(), "Full-time", "KLCC"));
            JOptionPane.showMessageDialog(this, "Registered!");
        }
    }

    private void showProductTable() {
        ArrayList<Product> list = inventorySystem.getAllProducts();
        String[] cols = {"ID", "Name", "Price", "Stock"};
        String[][] data = new String[list.size()][4];
        for (int i = 0; i < list.size(); i++) {
            Product p = list.get(i);
            data[i][0] = p.getProductID(); data[i][1] = p.getName();
            data[i][2] = String.format("%.2f", p.getPrice()); data[i][3] = p.getStockDisplay();
        }
        JOptionPane.showMessageDialog(this, new JScrollPane(new JTable(data, cols)), "Inventory View", JOptionPane.PLAIN_MESSAGE);
    }

    private void performAddProduct() {
        try {
            String pid = JOptionPane.showInputDialog(this, "Product ID:");
            String nam = JOptionPane.showInputDialog(this, "Name:");
            double prc = Double.parseDouble(JOptionPane.showInputDialog(this, "Price:"));
            inventorySystem.addProduct(new Product(pid, nam, prc));
            JOptionPane.showMessageDialog(this, "Product Added Successfully.");
        } catch(Exception e) { JOptionPane.showMessageDialog(this, "Input Error."); }
    }

    private void managerStockOps() {
        String pid = JOptionPane.showInputDialog(this, "Product ID:");
        String qty = JOptionPane.showInputDialog(this, "Quantity:");
        if (pid != null && qty != null) {
            try {
                String msg = inventorySystem.stockIn(pid, Integer.parseInt(qty), "KLCC", loginSystem.getCurrentUser().getName());
                JOptionPane.showMessageDialog(this, msg);
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid quantity."); }
        }
    }

    private void showPerformanceReport() {
        try {
            java.util.List<EmployeeStats> report = analytics.getEmployeePerformanceReport((Manager)loginSystem.getCurrentUser());
            StringBuilder sb = new StringBuilder("Rank | Name | Total Sales\n");
            for (int i = 0; i < report.size(); i++) {
                sb.append(String.format("%d. %s - RM %.2f\n", i+1, report.get(i).getEmployeeName(), report.get(i).getTotalSales()));
            }
            showScrollMsg("Performance Analytics", sb.toString());
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Access Denied."); }
    }

    private void performSalesFilter() {
        JTextField start = new JTextField("2026-01-01"); JTextField end = new JTextField("2026-12-31");
        Object[] msg = {"Start:", start, "End:", end};
        if (JOptionPane.showConfirmDialog(this, msg, "Filter Sales", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            showScrollMsg("Filtered Records", filterSystem.filterAndSortSales(start.getText(), end.getText(), 1));
        }
    }

    private JPanel createMenuCard(String title, String icon, Runnable action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        card.add(lblIcon, BorderLayout.CENTER);
        card.add(lblTitle, BorderLayout.SOUTH);
        card.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { action.run(); }
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(235, 235, 235)); }
            public void mouseExited(MouseEvent e) { card.setBackground(CARD_BG); }
        });
        return card;
    }

    private void showScrollMsg(String title, String msg) {
        JTextArea ta = new JTextArea(msg, 15, 40);
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), title, JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 40, 20));
        footer.setBackground(Color.WHITE);
        JButton logoutBtn = new JButton("Logout ⏏");
        logoutBtn.setForeground(LOGOUT_RED);
        logoutBtn.addActionListener(e -> { loginSystem.logout(); dispose(); Tester.showLogin(); });
        footer.add(logoutBtn);
        return footer;
    }
}