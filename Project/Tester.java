package Project;

import javax.swing.*;

public class Tester {
    static LoginSystem loginSystem = new LoginSystem();
    static InventorySystem inventorySystem = new InventorySystem();
    static AttendanceLog attendanceLog = new AttendanceLog();
    static SalesSystem salesSystem = new SalesSystem(inventorySystem);
    static StockCountSystem stockCountSystem = new StockCountSystem(inventorySystem);
    static SearchSystem searchSystem = new SearchSystem(inventorySystem);
    static EditSystem editSystem = new EditSystem(inventorySystem);
    static PerformanceAnalytics analytics = new PerformanceAnalytics(inventorySystem);
    static SalesFilterSystem filterSystem = new SalesFilterSystem();

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        showLogin();
    }

    public static void showLogin() {
        SwingUtilities.invokeLater(() -> {
            new LoginGUI(loginSystem, () -> {
                showDashboard();
            });
        });
    }

    private static void showDashboard() {
        if (!loginSystem.isLoggedIn()) return;

        User user = loginSystem.getCurrentUser();

        if (loginSystem.isManager()) {
            new ManagerDashboardGUI(loginSystem, inventorySystem, salesSystem, 
                                   editSystem, analytics, filterSystem, stockCountSystem, 
                                   searchSystem); 
        } else {
            new StaffDashboardGUI(loginSystem, inventorySystem, attendanceLog, 
                                 stockCountSystem, searchSystem, editSystem, salesSystem);
        }
    }
}