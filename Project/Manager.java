package Project;

public class Manager extends User {
    
    public Manager(String userID, String password, String name, String role){
        super(userID, password, name);
        // Hardcode role jadi "Manager"
        this.name = name; 
    }
    
    // --- TAMBAHAN BARU: Format penyimpanan CSV ---
    @Override
    public String toString() {
        // Format: ID,Pass,Name,Role,Outlet
        // Manager kita anggap outletnya "HeadOffice" atau kosong
        return userID + "," + password + "," + name + ",Manager,HeadOffice";
    }
}