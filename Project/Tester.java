package Project;
import javax.swing.JOptionPane;
public class Tester {
    public static void main(String[] args) {

        LoginSystem system = new LoginSystem();
        
        // Loop utama aplikasi
        while (true) {
            // Cek apakah sudah login
            if (!system.isLoggedIn()) {
                // POP UP MENU AWAL
                String[] options = {"Login", "Exit"};
                int choice = JOptionPane.showOptionDialog(
                    null, 
                    "Welcome to GoldenHour System", 
                    "Main page", 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.INFORMATION_MESSAGE, 
                    null, options, options[0]
                );

                if (choice == 0) { // Jika pilih Login
                    // POP UP INPUT ID
                    String id = JOptionPane.showInputDialog("Enter User ID:");
                    if (id == null) continue; // Kalau user klik cancel

                    // POP UP INPUT PASSWORD
                    String pass = JOptionPane.showInputDialog("Enter Password:");
                    if (pass == null) continue;

                    // Panggil method baru yang kita buat tadi
                    if (system.validateLogin(id, pass)) {
                        JOptionPane.showMessageDialog(null, "Login Succesful! \nHalo, " + system.getCurrentUser().getName());
                    } else {
                        JOptionPane.showMessageDialog(null, "Login Failed! ID or Password is Incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } else {
                    // Jika pilih Exit
                    JOptionPane.showMessageDialog(null, "Good bye!");
                    System.exit(0);
                }

            } else {
                // === AREA SETELAH LOGIN ===
                User currentUser = system.getCurrentUser();
                
                // Cek Role untuk menentukan menu
                if (system.isManager()) {
                    // MENU MANAGER
                    String[] mgrOptions = {"Register Employee", "Logout"};
                    int mgrChoice = JOptionPane.showOptionDialog(
                        null, 
                        "Dashboard Manager: " + currentUser.getName(), 
                        "Menu Manager", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, mgrOptions, mgrOptions[0]
                    );

                    if (mgrChoice == 0) {
                        // Fitur Register
                        String newName = JOptionPane.showInputDialog("New Employee's Name:");
                        String newId = JOptionPane.showInputDialog("New ID:");
                        // ... lanjut minta input lain ...
                        JOptionPane.showMessageDialog(null, "User " + newName + " Registered Succesfully");
                    } else {
                        system.logout();
                    }

                } else {
                    // MENU EMPLOYEE
                    String[] empOptions = {"Clock In/Out", "Logout"};
                    int empChoice = JOptionPane.showOptionDialog(
                        null, 
                        "Dashboard Employee: " + currentUser.getName(), 
                        "Staff's Menu", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, empOptions, empOptions[0]
                    );

                    if (empChoice == 0) {
                        JOptionPane.showMessageDialog(null, "Not Available");
                    } else {
                        system.logout();
                    }
                }
            }
        }
    }
}