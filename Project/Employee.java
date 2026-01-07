package Project;

public class Employee extends User {
    protected String role;     // Part-time / Full-time
    protected String outletId; // Workplace location

    public Employee(String userID, String password, String name, String role, String outletId) {
        super(userID, password, name);
        this.role = role;
        this.outletId = outletId;
    }

    public String getRole() { return role; }
    public String getOutletId() { return outletId; }

    @Override
    public String toString() {
        return userID + "," + password + "," + name + "," + role + "," + outletId;
    }
}