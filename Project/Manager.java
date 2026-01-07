package Project;

public class Manager extends User {
    protected String role;

    public Manager(String userID, String password, String name, String role){
        super(userID, password, name);
        this.role = "Manager";
    }

    void registerEmployee(){

    }
}
