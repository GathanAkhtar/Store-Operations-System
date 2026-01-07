package Project;

import javax.swing.JOptionPane;

public class Tester {
    public static void main(String[] args) {
        LoginSystem system = new LoginSystem();
        AttendanceLog attendanceLog = new AttendanceLog();

        while (true) {
            
            if (!system.isLoggedIn()) {
                String[] options = {"Login", "Exit"};
                int choice = JOptionPane.showOptionDialog(
                    null, 
                    "Welcome to GoldenHour System", 
                    "Main Menu", 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.INFORMATION_MESSAGE, 
                    null, options, options[0]
                );

                if (choice == 0) { 
                    String id = JOptionPane.showInputDialog("Enter User ID:");
                    if (id == null) continue;

                    String pass = JOptionPane.showInputDialog("Enter Password:");
                    if (pass == null) continue;

                    if (system.validateLogin(id, pass)) {
                        JOptionPane.showMessageDialog(null, "Login Successful!\nWelcome, " + system.getCurrentUser().getName());
                    } else {
                        JOptionPane.showMessageDialog(null, "Login Failed! Invalid ID or Password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Goodbye! Shutting down...");
                    System.exit(0);
                }

            } else {
                User currentUser = system.getCurrentUser();

                if (system.isManager()) {
                    String[] mgrOptions = {"Register New Employee", "Logout"};
                    int mgrChoice = JOptionPane.showOptionDialog(
                        null, 
                        "Manager Dashboard: " + currentUser.getName(), 
                        "Admin Menu", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, mgrOptions, mgrOptions[0]
                    );

                    if (mgrChoice == 0) {
                        String newId = JOptionPane.showInputDialog("Enter New Employee ID (Unique):");
                        if (newId == null || newId.isEmpty()) continue;

                        if (system.getUsers().containsKey(newId)) {
                            JOptionPane.showMessageDialog(null, "Error: User ID " + newId + " is already taken!", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                            continue;
                        }

                        String newPass = JOptionPane.showInputDialog("Create Password for " + newId + ":");
                        String newName = JOptionPane.showInputDialog("Enter Full Name:");
                        
                        String[] roles = {"Part-time", "Full-time"};
                        int roleOpt = JOptionPane.showOptionDialog(null, "Select Role:", "Role Selection", 
                                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);
                        String newRole = (roleOpt == 0) ? "Part-time" : "Full-time";

                        String newOutlet = JOptionPane.showInputDialog("Enter Outlet Location (e.g., C60):");

                        Employee newEmp = new Employee(newId, newPass, newName, newRole, newOutlet);
                        system.addUser(newEmp);

                        JOptionPane.showMessageDialog(null, "Success! User " + newName + " (" + newId + ") has been registered.");
                    
                    } else {
                        system.logout();
                    }

                } else {
                    Employee emp = (Employee) currentUser;
                    String attendanceStatus = attendanceLog.checkStatus(emp);
                    
                    String[] empOptions = {"Clock In", "Clock Out", "Logout"};
                    int empChoice = JOptionPane.showOptionDialog(
                        null, 
                        "Staff Dashboard: " + currentUser.getName() + 
                        "\nRole: " + emp.getRole() + 
                        "\nOutlet: " + emp.getOutletId() +
                        "\n\n" + attendanceStatus,
                        "Staff Menu", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, empOptions, empOptions[0]
                    );

                    if (empChoice == 0) {
                        String result = attendanceLog.clockIn(emp);
                        JOptionPane.showMessageDialog(null, result);
                        
                    } else if (empChoice == 1) {
                        String result = attendanceLog.clockOut(emp);
                        JOptionPane.showMessageDialog(null, result);
                        
                    } else {
                        system.logout();
                    }
                }
            }
        }
    }
}