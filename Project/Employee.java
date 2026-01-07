package Project;

public class Employee extends User {
    private String role;     // Part-time / Full-time
    private String outletId; // Lokasi kerja (misal: C60)

    public Employee(String userID, String password, String name, String role, String outletId) {
        // "super" memanggil constructor dari class User (Parent)
        super(userID, password, name); 
        this.role = role;
        this.outletId = outletId;
    }

    // Getter khusus Employee
    public String getRole() { return role; }
    public String getOutletId() { return outletId; }

    // Override toString untuk memudahkan print data karyawan nanti
    @Override
    public String toString() {
        return userID + "," + password + "," + name + "," + role + "," + outletId;
    }
}