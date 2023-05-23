import java.util.Arrays;

public class Med_cart {
    static String[]id = null;
    static String[]id2 = null;
    static String[]staff = null;
    static String []clients = null;
    static String[]date = null;
    static String[]recommend = null;
    static String[]diagnosis = null;
    static String[]serv = null;

    public static void input_Cart(String pol ){
        id = Service.name_BD("SELECT id FROM medical_history " +
                "WHERE clients_policy = '" + pol + "' " +
                "ORDER BY date ASC ", "id");
        staff = Service.name_BD("SELECT medical_staff_id FROM medical_history " +
                "WHERE clients_policy = '" + pol + "' " +
                "ORDER BY date ASC", "medical_staff_id");
        clients = Service.name_BD("SELECT clients_policy FROM medical_history " +
                "WHERE clients_policy = '" + pol + "' " +
                "ORDER BY date ASC", "clients_policy");
        date = Service.name_BD("SELECT date FROM medical_history WHERE clients_policy ='" + pol + "' ORDER BY date ASC", "date");
        recommend = Service.name_BD("SELECT recommendations FROM medical_history " +
                "WHERE clients_policy = '" + pol + "' " +
                "ORDER BY date ASC", "recommendations");

        diagnosis = Service.name_BD("SELECT diagnosis FROM medical_history " +
                "WHERE clients_policy = '" + pol + "' " +
                "ORDER BY date ASC", "diagnosis");
        serv = Service.name_BD("SELECT mh.service_code, s.name " +
                "FROM medical_history mh " +
                "JOIN service s ON mh.service_code = s.code " +
                "WHERE clients_policy = '" + pol + "'", "name");
//
        staff = Service.name_BD("SELECT mh.medical_staff_id, ms.full_name " +
                "FROM medical_history mh " +
                "JOIN medical_staff ms ON mh.medical_staff_id = ms.id " +
                "WHERE clients_policy = '" + pol + "'", "full_name");
        id2 = Service.name_BD("SELECT id FROM medical_history " +
                "ORDER BY date ASC ", "id");
        System.out.println(Arrays.toString(date));

    }
    public static void input_Cart( ){
        id = Service.name_BD("SELECT id FROM medical_history " +
                "ORDER BY date ASC ", "id");
        staff = Service.name_BD("SELECT medical_staff_id FROM medical_history " +

                "ORDER BY date ASC", "medical_staff_id");
        clients = Service.name_BD("SELECT clients_policy FROM medical_history " +

                "ORDER BY date ASC", "clients_policy");
        date = Service.name_BD("SELECT date FROM medical_history " +

                "ORDER BY date ASC", "date");
        recommend = Service.name_BD("SELECT recommendations FROM medical_history " +

                "ORDER BY date ASC", "recommendations");

        diagnosis = Service.name_BD("SELECT diagnosis FROM medical_history " +

                "ORDER BY date ASC", "diagnosis");
        serv = Service.name_BD("SELECT mh.service_code, s.name " +
                "FROM medical_history mh " +
                "JOIN service s ON mh.service_code = s.code " , "name");
//
        staff = Service.name_BD("SELECT mh.medical_staff_id, ms.full_name " +
                "FROM medical_history mh " +
                "JOIN medical_staff ms ON mh.medical_staff_id = ms.id " , "full_name");

        System.out.println(Arrays.toString(date));

    }
}
