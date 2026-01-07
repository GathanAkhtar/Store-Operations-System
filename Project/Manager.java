package Project;

public class Manager extends User {
    
    public Manager(String userID, String password, String name, String role){
        super(userID, password, name);
        // Hardcode the role to "Manager"
        this.name = name; // slight fix on super usage context if needed, but super handles it.
    }
    
    // Manager specific methods can be added here
}