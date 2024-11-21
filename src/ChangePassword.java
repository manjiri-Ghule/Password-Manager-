import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.BasicAttribute;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Scanner;

public class ChangePassword
{
    // Method to generate a random alphanumeric password
    public static String generateRandomPassword(int length) 
    {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        for(int i = 0; i < length; i++) 
        {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    // Method to format password for Active Directory (UTF-16LE encoding)
    public static byte[] encodePasswordForAD(String password)
    {
        String quotedPassword = "\"" + password + "\""; // Password must be in quotes
        try 
        {
            return quotedPassword.getBytes("UTF-16LE");
        } 
        catch (Exception e) 
        {
            throw new RuntimeException("Error encoding password to UTF-16LE", e);
        }
    }

    // Main method
    public static void main(String[] args) 
    {
        Scanner scanner = new Scanner(System.in);
        // Taking input for username
        System.out.print("Enter the username (cn) to update the password: ");
        String username = scanner.nextLine(); // Get username input

        // LDAP server connection details
        String ldapUrl = "ldaps://app010w001.minjtech.xyz:636"; // LDAP URL
        String userDN = "CN=" + username + ",CN=Users,DC=minjtech,DC=xyz"; // Construct User DN from input
        String adminDN = "CN=Administrator,CN=Users,DC=minjtech,DC=xyz"; // Admin DN
        String adminPassword = "Login%12345"; // Admin Password

        // Generate a new random password
        String newPassword = generateRandomPassword(10);
        System.out.println("Generated new password: " + newPassword);

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, adminDN); // Administrator DN
        env.put(Context.SECURITY_CREDENTIALS, adminPassword); // Admin password

        try 
        {
            DirContext ctx = new InitialDirContext(env);

            // Prepare the modification
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", encodePasswordForAD(newPassword)));

            // Change the password
            ctx.modifyAttributes(userDN, mods);
            System.out.println("Password changed successfully for user: " + username);

            ctx.close();
        } 
        catch (NamingException e) 
        {
            e.printStackTrace();
        } 
        finally 
        {
            scanner.close(); // Close the scanner resource
        }
    }
}
