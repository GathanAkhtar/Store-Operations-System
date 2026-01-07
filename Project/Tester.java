package Project;

import javax.swing.JOptionPane;

public class Tester {
    public static void main(String[] args) {
        // 1. Initialize System
        LoginSystem system = new LoginSystem();

        // 2. Main Loop (Keeps the program running)
        while (true) {
            
            // --- STATE: NOT LOGGED IN ---
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
                    // LOGIN PROCESS
                    String id = JOptionPane.showInputDialog("Enter User ID:");
                    if (id == null) continue; // Handle cancel

                    String pass = JOptionPane.showInputDialog("Enter Password:");
                    if (pass == null) continue;

                    if (system.validateLogin(id, pass)) {
                        JOptionPane.showMessageDialog(null, "Login Successful!\nWelcome, " + system.getCurrentUser().getName());
                    } else {
                        JOptionPane.showMessageDialog(null, "Login Failed! Invalid ID or Password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // EXIT PROGRAM
                    JOptionPane.showMessageDialog(null, "Goodbye! Shutting down...");
                    System.exit(0);
                }

            } else {
                // --- STATE: LOGGED IN ---
                User currentUser = system.getCurrentUser();

                if (system.isManager()) {
                    // === MANAGER MENU ===
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
                        // --- REGISTER EMPLOYEE LOGIC ---
                        String newId = JOptionPane.showInputDialog("Enter New Employee ID (Unique):");
                        if (newId == null || newId.isEmpty()) continue;

                        // Check for Duplicate ID
                        if (system.getUsers().containsKey(newId)) {
                            JOptionPane.showMessageDialog(null, "Error: User ID " + newId + " is already taken!", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                            continue;
                        }

                        String newPass = JOptionPane.showInputDialog("Create Password for " + newId + ":");
                        String newName = JOptionPane.showInputDialog("Enter Full Name:");
                        
                        // Select Role
                        String[] roles = {"Part-time", "Full-time"};
                        int roleOpt = JOptionPane.showOptionDialog(null, "Select Role:", "Role Selection", 
                                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);
                        String newRole = (roleOpt == 0) ? "Part-time" : "Full-time";

                        String newOutlet = JOptionPane.showInputDialog("Enter Outlet Location (e.g., C60):");

                        // SAVE TO SYSTEM
                        Employee newEmp = new Employee(newId, newPass, newName, newRole, newOutlet);
                        system.addUser(newEmp); // <--- Adds user to memory so they can login immediately

                        JOptionPane.showMessageDialog(null, "Success! User " + newName + " (" + newId + ") has been registered.\nYou can now login with this ID.");
                    
                    } else {
                        system.logout();
                    }

                } else {
                    // === EMPLOYEE MENU ===
                    // Casting to Employee to access specific methods like getRole()
                    Employee emp = (Employee) currentUser;
                    
                    String[] empOptions = {"Clock In/Out (Demo)", "Logout"};
                    int empChoice = JOptionPane.showOptionDialog(
                        null, 
                        "Staff Dashboard: " + currentUser.getName() + "\nRole: " + emp.getRole() + "\nOutlet: " + emp.getOutletId(), 
                        "Staff Menu", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, empOptions, empOptions[0]
                    );

                    if (empChoice == 0) {
                        JOptionPane.showMessageDialog(null, "Attendance feature is under construction.");
                    } else {
                        system.logout();
                    }
                }
            }
        }
    }
}