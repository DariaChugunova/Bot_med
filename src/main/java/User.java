public class User {
    static String[]policy= null;
    static String[]full_name = null;
    static String []date_of_birth = null;
    static String[]mail = null;
    public static void input_User(){
        policy = (String[]) Service.name_BD("SELECT policy FROM clients ORDER BY full_name ASC", "policy");
        full_name = (String[]) Service.name_BD("SELECT full_name FROM clients ORDER BY full_name ASC", "full_name");
        date_of_birth = (String[]) Service.name_BD("SELECT date_of_birth FROM clients ORDER BY full_name ASC", "date_of_birth");
        mail = (String[]) Service.name_BD("SELECT mail FROM clients ORDER BY full_name ASC", "mail");

    }
    public static void input_User(String pol){
        policy = (String[]) Service.name_BD("SELECT policy FROM clients " +
                "WHERE policy = '" + pol + "' " +
                "ORDER BY full_name ASC", "policy");
        full_name = (String[]) Service.name_BD("SELECT full_name FROM clients " +
                "WHERE policy = '" + pol + "' " +
                "ORDER BY full_name ASC", "full_name");
        date_of_birth = (String[]) Service.name_BD("SELECT date_of_birth FROM clients " +
                "WHERE policy = '" + pol + "' " +
                "ORDER BY full_name ASC", "date_of_birth");
        mail = (String[]) Service.name_BD("SELECT mail FROM clients " +
                "WHERE policy = '" + pol + "' " +
                "ORDER BY full_name ASC", "mail");

    }
}
