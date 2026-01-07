package Project;

public class Manager extends User {
    private String role;

    public Manager(int userID, int password, String name, String role){
        super(userID, password, name);
        this.role = "Manager";
    }

    void registerEmployee(){

    }
}
