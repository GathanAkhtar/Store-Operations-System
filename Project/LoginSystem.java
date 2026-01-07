package Project;

import java.util.HashMap;
import java.util.Scanner;

public class LoginSystem {
    // Data untuk menyimpan semua user
    private HashMap<String, User> users;
    
    // User yang sedang login
    private User currentUser;
    
    // Scanner untuk input
    private Scanner scanner;
    
    // Constructor - dijalankan saat objek dibuat
    public LoginSystem() {
        users = new HashMap<>();
        scanner = new Scanner(System.in);
        
        // Buat akun manager default untuk pertama kali
        Manager defaultManager = new Manager("MGR001", "admin123", "Admin Manager", "Manager");
        users.put("MGR001", defaultManager);
    }
    
    // Method untuk LOGIN
    public boolean login() {
        System.out.print("\nEnter Employee ID: ");
        String empId = scanner.nextLine();
        
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        
        // Cari user berdasarkan Employee ID
        User user = users.get(empId);
        
        // Cek apakah user ada dan password benar
        if (user == null || !user.getPassword().equals(password)) {
            // Login gagal
            System.out.println("\n*** Login Failed ***");
            System.out.println("Employee ID or Password is incorrect. Please try again.");
            return false;
        } else {
            // Login berhasil
            currentUser = user;
            System.out.println("\n*** Login Successful ***");
            System.out.println("Welcome, " + currentUser.getName());
            
            // Tampilkan role user
            if (currentUser instanceof Manager) {
                System.out.println("Role: Manager");
            } else if (currentUser instanceof Employee) {
                Employee emp = (Employee) currentUser;
                System.out.println("Role: " + emp.getRole());
            }
            
            return true;
        }
    }
    
    // Method untuk LOGOUT
    public void logout() {
        if (currentUser != null) {
            System.out.println("\n*** Logout ***");
            System.out.println("Goodbye, " + currentUser.getName());
            currentUser = null;  // Hapus user yang login
        }
    }
    
    // Cek apakah ada user yang sedang login
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    // Cek apakah user yang login adalah Manager
    public boolean isManager() {
        return currentUser instanceof Manager;
    }
    
    // Ambil user yang sedang login
    public User getCurrentUser() {
        return currentUser;
    }
    
    // Ambil semua data users (untuk keperluan lain seperti registrasi)
    public HashMap<String, User> getUsers() {
        return users;
    }
    
    // Ambil scanner (untuk keperluan lain)
    public Scanner getScanner() {
        return scanner;
    }
}