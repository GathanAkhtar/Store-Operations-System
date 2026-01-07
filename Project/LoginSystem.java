package Project;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class LoginSystem {
    private HashMap<String, User> users;
    private User currentUser;
    private final String FILE_NAME = "users.csv";

    public LoginSystem() {
        users = new HashMap<>();
        loadUsersFromFile();
    }

    public boolean validateLogin(String inputId, String inputPass) {
        User user = users.get(inputId);
        if (user != null && user.getPassword().equals(inputPass)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void addUser(User newUser) {
        if (!users.containsKey(newUser.getUserID())) {
            users.put(newUser.getUserID(), newUser);
            saveUsersToFile();
        } else {
            System.out.println("Error: User ID already exists.");
        }
    }

    // --- FITUR BARU: AMBIL DAFTAR CABANG AKTIF ---
    public String[] getActiveOutlets() {
        Set<String> outletSet = new HashSet<>();
        for (User u : users.values()) {
            if (u instanceof Employee) {
                outletSet.add(((Employee) u).getOutletId());
            }
        }
        return outletSet.toArray(new String[0]);
    }

    public void logout() { currentUser = null; }
    public boolean isLoggedIn() { return currentUser != null; }
    public boolean isManager() { return currentUser instanceof Manager; }
    public User getCurrentUser() { return currentUser; }
    public HashMap<String, User> getUsers() { return users; }

    private void loadUsersFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("users.csv not found. Creating default Manager...");
            Manager defaultManager = new Manager("UM2025", "1234", "Ucup Manager", "Manager");
            users.put("UM2025", defaultManager);
            saveUsersToFile();
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(",");
                if (data.length >= 5) {
                    String id = data[0];
                    String pass = data[1];
                    String name = data[2];
                    String role = data[3];
                    String outlet = data[4];

                    if (role.equalsIgnoreCase("Manager")) {
                        users.put(id, new Manager(id, pass, name, role));
                    } else {
                        users.put(id, new Employee(id, pass, name, role, outlet));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading user file: " + e.getMessage());
        }
    }

    private void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, false))) {
            for (User u : users.values()) {
                writer.write(u.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving user file: " + e.getMessage());
        }
    }
}