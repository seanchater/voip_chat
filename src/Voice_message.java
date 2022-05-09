
import java.io.Serializable;

public class Voice_message implements Serializable {
    private static final long serialVersionUID = 1234L;
    private byte[] vn;
    private String toUsername;

    public Voice_message(String toUser, byte[] voice) {
        vn = voice;
        toUsername = toUser;
    }
    public String getToUsername() {
        return toUsername;
    }
    public byte[] getVoice() {
        return vn;
    }
    
}
