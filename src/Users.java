import java.io.Serializable;

public class Users implements Serializable {
    private static final long serialVersionUID = 1234L;
    private String username;
    private boolean available;
    
    public Users(String username,boolean available) {
        this.username = username;
        this.available = available;
    }
    public String get_Username(){
        return username;
    }
    public boolean get_Available() {
        return available;
    }
    public void set_available(boolean available) {
        this.available = available;
    }
    
}
