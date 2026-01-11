package Project;

public abstract class User {
    protected String userID;
    protected String password;
    protected String name;

    public User(String userID, String password, String name) {
        this.userID = userID;
        this.password = password;
        this.name = name;
    }

    public String getUserID() { return userID; }
    public String getPassword() { return password; }
    public String getName() { return name; }

    // Setiap anak (Manager/Employee) wajib punya format CSV sendiri
    @Override
    public abstract String toString();
}