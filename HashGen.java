import org.mindrot.jbcrypt.BCrypt;
public class HashGen {
    public static void main(String[] args) {
        System.out.println(BCrypt.hashpw("admin123", BCrypt.gensalt(12)));
    }
}
