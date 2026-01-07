package Project;

public class Manager extends User {
    
    public Manager(String userID, String password, String name, String role){
        super(userID, password, name);
        this.name = name; 
    }
    
    @Override
    public String toString() {
        // Manager dianggap outletnya "HeadOffice"
        return userID + "," + password + "," + name + ",Manager,HeadOffice";
    }
}