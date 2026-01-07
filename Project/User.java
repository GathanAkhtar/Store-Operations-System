package Project;

public class User {
    private int userID;
    private int password;
    private String name;
    private String role;

    public User(int userID, int password, String name, String role){
        this.userID = userID;
        this.password = password;
        this.name = name;
        this.role = role;
    }
}