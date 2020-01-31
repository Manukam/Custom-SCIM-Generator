import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;

public class CustomSCIMGenerator {

    private static String LDAP_URL;
    private static String LDAP_USER;
    private static String LDAP_PASSWORD;
    private static String LDAP_SEARCH_BASE;
    private static String SEARCH_FILTER;
    private static String ATTRIBUTE_TO_PRINT;
    private static String LDAP_REFERRAL;
    private static String KEYSTORE;
    private static String KEYSTORE_PASSWORD;
    private static String UPDATE_ENTRY;

    public static void main(String[] args) throws NamingException, IOException {

        setProperties();
        System.out.println("LDAP URL: " + LDAP_URL);
        System.out.println("LDAP User: " + LDAP_USER);
        System.out.println("LDAP Search Base: " + LDAP_SEARCH_BASE);
        System.out.println("LDAP Search Filter: " + SEARCH_FILTER);
        System.out.println("LDAP Referral: " + LDAP_REFERRAL);
        System.out.println("LDAP Attribute: " + ATTRIBUTE_TO_PRINT);
        System.out.println("Trust store location: " + KEYSTORE);

        System.setProperty("javax.net.ssl.trustStore", KEYSTORE);
        System.setProperty("javax.net.ssl.trustStorePassword", KEYSTORE_PASSWORD);

        Hashtable<String, String> environment = new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.REFERRAL, LDAP_REFERRAL);
        environment.put(Context.PROVIDER_URL, LDAP_URL);
        environment.put(Context.SECURITY_PRINCIPAL, LDAP_USER);
        environment.put(Context.SECURITY_CREDENTIALS, LDAP_PASSWORD);

        DirContext ctx = null;
        ctx = new InitialDirContext(environment);

        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration answer = ctx.search(LDAP_SEARCH_BASE, SEARCH_FILTER, ctls);
        answer.next();
        System.out.println("-------Users with missing SCIM ID ------");
        while (answer.hasMore() == true) {     //Iterating over the users who does not have SCIM_IDs
            SearchResult sr = (SearchResult) answer.next();
            Attributes attrs = sr.getAttributes();
            String uid = attrs.get("uid").toString().split(":")[1].trim();
            String objectClass = String.valueOf(attrs.get("objectClass")).split(":")[1].trim();  //Extracting the object class of the user
            if(objectClass.contains("identityPerson")){
                System.out.println(uid);
                System.out.println("Object Class: "+ objectClass);
            }
            if (objectClass.contains("identityPerson")) {   //Only updating the users with identityPerson object class
                if(UPDATE_ENTRY.equalsIgnoreCase("true")){   //Checking if updating is configured to be true.
                    updateScimEntry(ctx, uid);
                }
            }
        }
        System.out.println("-----END-------");
    }

    public static boolean updateScimEntry(DirContext ctx, String uid) {
        try {
            System.out.println("Attempting to update the scim id entry of user: "+ uid);
            Attributes attributes = new BasicAttributes();
            Attribute atb = new BasicAttribute("scimId", UUID.randomUUID().toString());
            attributes.put(atb);
            ctx.modifyAttributes("uid=" + uid + ","+ LDAP_SEARCH_BASE, DirContext.ADD_ATTRIBUTE, attributes);
            System.out.println("update successful for user: "+ uid);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static void setProperties() throws IOException {
        Properties catalogProps = new Properties();
        catalogProps.load(new FileInputStream("config.properties"));
        LDAP_URL = catalogProps.getProperty("LDAP_URL");
        LDAP_USER = catalogProps.getProperty("LDAP_USER");
        LDAP_PASSWORD = catalogProps.getProperty("LDAP_PASSWORD");
        LDAP_SEARCH_BASE = catalogProps.getProperty("LDAP_SEARCH_BASE");
        SEARCH_FILTER = catalogProps.getProperty("SEARCH_FILTER");
        ATTRIBUTE_TO_PRINT = catalogProps.getProperty("ATTRIBUTE_TO_PRINT");
        LDAP_REFERRAL = catalogProps.getProperty("LDAP_REFERRAL");
        KEYSTORE = catalogProps.getProperty("KEYSTORE");
        KEYSTORE_PASSWORD = catalogProps.getProperty("KEYSTORE_PASSWORD");
        UPDATE_ENTRY = catalogProps.getProperty("UPDATE_ENTRY");
    }

}
