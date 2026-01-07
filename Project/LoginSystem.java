package Project;

import java.util.HashMap;

public class LoginSystem {
    // Database in memory (HashMap)
    private HashMap<String, User> users;
    private User currentUser;

    public LoginSystem() {
        users = new HashMap<>();
        
        // --- DUMMY DATA (For initial login) ---
        // ID: MGR001, Pass: admin123
        Manager defaultManager = new Manager("UM2025", "1234", "Ucup", "Manager");
        users.put("UM2025", defaultManager);
        
        // Add 1 example employee
        Employee emp1 = new Employee("UMC123", "123", "Binti", "Full-time", "KLCC");
        users.put("UMC123", emp1);
    }

    // Method to validate login credentials
    public boolean validateLogin(String inputId, String inputPass) {
        User user = users.get(inputId);
        
        // Check if user exists AND password matches
        if (user != null && user.getPassword().equals(inputPass)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    // Method to add a new user to the database (Immediate Login capability)
    public void addUser(User newUser) {
        if (!users.containsKey(newUser.getUserID())) {
            users.put(newUser.getUserID(), newUser);
        } else {
            System.out.println("Error: User ID already exists in database.");
        }
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isManager() {
        return currentUser instanceof Manager;
    }

    public User getCurrentUser() {
        return currentUser;
    }
    
    // Getter to access the user list
    public HashMap<String, User> getUsers() {
        return users;
    }
}