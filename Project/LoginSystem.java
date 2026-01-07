package Project;

import java.io.*; // Import Wajib untuk File
import java.util.HashMap;
import java.util.Scanner;

public class LoginSystem {
    // Database in memory (HashMap)
    private HashMap<String, User> users;
    private User currentUser;
    
    // --- TAMBAHAN BARU: Lokasi File CSV ---
    // File ini akan dicari di folder utama (di luar folder Project)
    private final String FILE_NAME = "users.csv";

    public LoginSystem() {
        users = new HashMap<>();
        loadUsersFromFile(); // <--- BACA FILE SAAT PROGRAM NYALA
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

    // Method to add a new user to the database
    public void addUser(User newUser) {
        if (!users.containsKey(newUser.getUserID())) {
            users.put(newUser.getUserID(), newUser);
            saveUsersToFile(); // <--- SIMPAN KE FILE SETELAH NAMBAH USER
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
    
    public HashMap<String, User> getUsers() {
        return users;
    }

    // ========================================================
    // === TAMBAHAN BARU: FILE INPUT / OUTPUT (BACA TULIS) ===
    // ========================================================

    private void loadUsersFromFile() {
        File file = new File(FILE_NAME);
        
        // Jika file belum ada, buat Manager Default
        if (!file.exists()) {
            System.out.println("Database users.csv tidak ditemukan. Membuat data default...");
            Manager defaultManager = new Manager("UM2025", "1234", "Ucup Manager", "Manager");
            users.put("UM2025", defaultManager);
            saveUsersToFile(); // Buat filenya sekarang
            return;
        }

        // Jika file ada, baca isinya baris per baris
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // Format CSV: ID,Pass,Name,Role,Outlet
                String[] data = line.split(",");
                
                if (data.length >= 5) {
                    String id = data[0];
                    String pass = data[1];
                    String name = data[2];
                    String role = data[3];
                    String outlet = data[4];

                    // Bedakan mana Manager mana Employee
                    if (role.equalsIgnoreCase("Manager")) {
                        users.put(id, new Manager(id, pass, name, role));
                    } else {
                        users.put(id, new Employee(id, pass, name, role, outlet));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Gagal membaca file: " + e.getMessage());
        }
    }

    private void saveUsersToFile() {
        // 'false' artinya kita timpa isi file lama dengan data terbaru dari HashMap
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, false))) {
            for (User u : users.values()) {
                writer.write(u.toString()); // Panggil toString() dari Employee/Manager
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Gagal menyimpan file: " + e.getMessage());
        }
    }
}