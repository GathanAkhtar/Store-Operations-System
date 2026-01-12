package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StaffDashboardGUI extends JFrame {
    
    private static final Color THEME_GOLD = new Color(255, 215, 87); 
    private static final Color CARD_BG = new Color(248, 249, 250);   
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color LOGOUT_RED = new Color(220, 53, 69);

    private LoginSystem loginSystem;
    private Employee currentStaff;
    private InventorySystem inventorySystem;
    private AttendanceLog attendanceLog;
    private StockCountSystem stockCountSystem;
    private SearchSystem searchSystem;
    private EditSystem editSystem;
    private SalesSystem salesSystem;

    public StaffDashboardGUI(LoginSystem ls, InventorySystem is, AttendanceLog al, 
                             StockCountSystem scs, SearchSystem ss, EditSystem es, SalesSystem sls) {
        this.loginSystem = ls;
        this.currentStaff = (Employee) ls.getCurrentUser();
        this.inventorySystem = is;
        this.attendanceLog = al;
        this.stockCountSystem = scs;
        this.searchSystem = ss;
        this.editSystem = es;
        this.salesSystem = sls;

        setTitle("GoldenHour - Staff Dashboard");
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

        JPanel textGroup = new JPanel();
        textGroup.setOpaque(false);
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));

        JLabel staffLbl = new JLabel("Staff Menu");
        staffLbl.setFont(new Font("Arial", Font.PLAIN, 18));
        staffLbl.setForeground(Color.WHITE);

        JLabel dashLbl = new JLabel("Staff Dashboard");
        dashLbl.setFont(new Font("Arial", Font.BOLD, 32));
        dashLbl.setForeground(Color.WHITE);

        textGroup.add(staffLbl);
        textGroup.add(Box.createRigidArea(new Dimension(0, 5)));
        textGroup.add(dashLbl);

        header.add(textGroup);
        return header;
    }

    private JPanel createMenuGrid() {
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 25, 25));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        gridPanel.add(createMenuCard("Attendance", "🕒", this::performAttendance));
        gridPanel.add(createMenuCard("Daily Stock Count", "📋", this::performStaffStockCount));
        gridPanel.add(createMenuCard("Search Info", "🔍", this::performSearch));
        gridPanel.add(createMenuCard("Edit Information", "✏️", this::performEditInfoGUI));
        gridPanel.add(createMenuCard("Stock Ops", "📦", this::employeeStockOps));
        gridPanel.add(createMenuCard("Record Sale", "💰", this::recordSale));

        return gridPanel;
    }

    // --- FITUR EDIT INFORMATION (IDENTIK DENGAN MANAGER DASHBOARD) ---
private void performEditInfoGUI() {
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
    } else if (choice == 1) { // Opsi 2: Edit Stock Level (Model)
        String pid = JOptionPane.showInputDialog(this, "Enter Product ID to Edit:");
        if (pid == null || pid.trim().isEmpty()) return;

        Product p = inventorySystem.findProduct(pid);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Product ID Not Found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newQtyStr = JOptionPane.showInputDialog(this, "Current Stock: " + p.getStockDisplay() + "\nEnter NEW Quantity:");
        if (newQtyStr != null) {
            try {
                int newQty = Integer.parseInt(newQtyStr);
                String result = editSystem.editStockLevel(pid, currentStaff.getOutletId(), newQty);
                JOptionPane.showMessageDialog(this, result);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Quantity Format!");
            }
        }
    } else if (choice == 2) { // Opsi 3: Edit Stock Count Log (DISAMAKAN DENGAN MANAGER)
        String date = JOptionPane.showInputDialog(this, "Enter Log Date (YYYY-MM-DD):");
        String pid = JOptionPane.showInputDialog(this, "Enter Product ID for the log entry:");
        
        if (date != null && pid != null) {
            String newPhys = JOptionPane.showInputDialog(this, "Enter CORRECTED Physical Count:");
            if (newPhys != null) {
                try {
                    int correctedCount = Integer.parseInt(newPhys);
                    // Menampilkan pesan konfirmasi bahwa permintaan koreksi telah diproses
                    JOptionPane.showMessageDialog(this, "Stock log correction request submitted. Please verify changes in 'Stock Count Logs'.");
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid count format!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}

    private JPanel createMenuCard(String title, String icon, Runnable action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40)); 
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_DARK);

        card.add(lblIcon, BorderLayout.CENTER);
        card.add(lblTitle, BorderLayout.SOUTH);
        card.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { action.run(); }
            @Override
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(235, 235, 235)); }
            @Override
            public void mouseExited(MouseEvent e) { card.setBackground(CARD_BG); }
        });
        return card;
    }

    private void showScrollMsg(String title, String msg) {
        JTextArea textArea = new JTextArea(msg, 15, 45);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), title, JOptionPane.INFORMATION_MESSAGE);
    }

    // --- INTEGRASI LOGIKA FITUR LAINNYA ---
    private void performAttendance() {
        String[] opts = {"Clock In", "Clock Out", "Check Status"};
        int choice = JOptionPane.showOptionDialog(this, "Attendance Menu", "Time Clock", 0, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (choice == 0) JOptionPane.showMessageDialog(this, attendanceLog.clockIn(currentStaff));
        else if (choice == 1) JOptionPane.showMessageDialog(this, attendanceLog.clockOut(currentStaff));
        else if (choice == 2) JOptionPane.showMessageDialog(this, attendanceLog.checkStatus(currentStaff));
    }

    private void performStaffStockCount() {
        String[] sessions = {"Morning Count", "Night Count"};
        int choice = JOptionPane.showOptionDialog(this, "Select Session", "Stock Count", 0, JOptionPane.PLAIN_MESSAGE, null, sessions, sessions[0]);
        if (choice != -1) {
            StringBuilder report = new StringBuilder("=== COUNT SUMMARY ===\n");
            for(Product p : inventorySystem.getAllProducts()) {
                String input = JOptionPane.showInputDialog(this, "Item: " + p.getName() + "\nEnter PHYSICAL Qty:");
                if (input == null) break;
                try {
                    int physQty = Integer.parseInt(input);
                    stockCountSystem.logCount(sessions[choice], currentStaff.getOutletId(), currentStaff.getName(), p.getProductID(), p.getName(), p.getQuantity(currentStaff.getOutletId()), physQty);
                    report.append(p.getName()).append(": ").append(physQty).append("\n");
                } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid number."); }
            }
            showScrollMsg("Count Complete", report.toString());
        }
    }

    private void performSearch() {
        String query = JOptionPane.showInputDialog(this, "Enter Model Name or Date (YYYY-MM-DD):");
        if (query != null) showScrollMsg("Search Results", searchSystem.searchStockByModel(query) + "\n" + searchSystem.searchSalesRecord(query, "ANY"));
    }

    private void employeeStockOps() {
        String pid = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (pid == null) return;
        String qtyStr = JOptionPane.showInputDialog(this, "Enter Quantity:");
        if (qtyStr == null) return;
        try {
            int qty = Integer.parseInt(qtyStr);
            JOptionPane.showMessageDialog(this, inventorySystem.stockIn(pid, qty, currentStaff.getOutletId(), currentStaff.getName()));
        } catch(Exception e) { JOptionPane.showMessageDialog(this, "Invalid numeric input."); }
    }

    private void recordSale() {
        String customer = JOptionPane.showInputDialog(this, "Customer Name (or 'Walk-in'):");
        if (customer == null) return;
        
        SalesTransaction transaction = salesSystem.createNewTransaction(customer, currentStaff);
        
        while (true) {
            String productID = JOptionPane.showInputDialog(this, "Current Total: RM " + 
                               String.format("%.2f", transaction.getTotalPrice()) + 
                               "\n\nEnter Product ID (Cancel to Finish):");
            if (productID == null) break; // Berhenti jika user menekan Cancel
            
            try {
                // Menambahkan 1 unit barang ke keranjang
                String addResult = salesSystem.addItemToTransaction(transaction, productID, 1);
                JOptionPane.showMessageDialog(this, addResult);
            } catch (Exception e) { 
                JOptionPane.showMessageDialog(this, "Error adding product."); 
            }
        }
        
        // Setelah selesai input barang, pilih metode pembayaran
        if (!transaction.getItems().isEmpty()) {
            String[] paymentMethods = {"Cash", "Card", "QR Pay"};
            int methodChoice = JOptionPane.showOptionDialog(this, 
                    "Total: RM " + String.format("%.2f", transaction.getTotalPrice()) + "\nSelect Payment Method:", 
                    "Checkout", 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, 
                    paymentMethods, 
                    paymentMethods[0]);
            
            if (methodChoice != -1) {
                // Finalisasi transaksi dan tampilkan struk (Receipt)
                String receipt = salesSystem.finalizeTransaction(transaction, transaction.getTotalPrice(), paymentMethods[methodChoice]);
                showScrollMsg("Receipt", receipt);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Transaction cancelled (No items).");
        }
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