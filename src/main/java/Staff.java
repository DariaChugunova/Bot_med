public class Staff {
    static String[]id = null;
    static String[]names = null;
    static String []job = null;
    static String[]experience = null;
    static String[]photo = null;

    public static void input_Staff(){
        id = (String[]) Service.name_BD("SELECT id FROM medical_staff ORDER BY full_name ASC", "id");
        names = (String[]) Service.name_BD("SELECT full_name FROM medical_staff ORDER BY full_name ASC", "full_name");
        job = (String[]) Service.name_BD("SELECT job_title FROM medical_staff ORDER BY full_name ASC", "job_title");
        experience = (String[]) Service.name_BD("SELECT experience FROM medical_staff ORDER BY full_name ASC", "experience");
        photo = (String[]) Service.name_BD("SELECT photo FROM medical_staff ORDER BY full_name ASC", "photo");
    }
}
