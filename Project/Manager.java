package Project;

public class Manager extends User {
    protected String role;

    public Manager(String userID, String password, String name, String role){
        super(userID, password, name);
        this.role = "Manager";
    }

// Method untuk mendaftarkan karyawan baru 
    public void registerEmployee(String id, String pass, String name, String role, String outlet) {
        // Logika pendaftaran akan ditulis di sini
        System.out.println("Employee " + name + " registered successfully!");
    }
}
