public class Record {
    static String[]date= null;
    static String[]dateid= null;
    static String[]time = null;
    static String []policy = null;
    static String []serv = null;
    static String []servid = null;

    public  static void input_record(){
        date = (String[]) Service.name_BD("SELECT d.dt " +
                "FROM date2 d " +
                "JOIN record r ON d.id = r.date_id " +
                "ORDER BY date_id ASC, time_id ASC;", "dt");

        time = (String[]) Service.name_BD("SELECT time_id FROM record ORDER BY date_id, time_id ASC", "time_id");
        policy = (String[]) Service.name_BD("SELECT clients_policy FROM record ORDER BY date_id, time_id ASC", "clients_policy");


        serv = Service.name_BD("SELECT s.name " +
                "FROM service s " +
                "JOIN record r ON s.code = r.service_code " +
                "ORDER BY date_id ASC, time_id ASC;", "name");
    }

    public  static void input_record(String pol){
        Record.time = Service.name_BD("SELECT time_id " +
                "FROM public.record " +
                "WHERE clients_policy = '" + pol + "' " +
                "ORDER BY date_id ASC, time_id ASC;", "time_id");
        Record.dateid = Service.name_BD("SELECT date_id " +
                "FROM public.record " +
                "WHERE clients_policy = '" + pol + "' " +
                "ORDER BY date_id ASC, time_id ASC;", "date_id");
        Record.servid = Service.name_BD("SELECT service_code " +
                "FROM public.record " +
                "WHERE clients_policy = '" + pol + "' " +
                "ORDER BY date_id ASC, time_id ASC;", "service_code");

        Record.date = Service.name_BD("SELECT d.dt " +
                "FROM date2 d " +
                "JOIN record r ON d.id = r.date_id " +
                "WHERE clients_policy = '" + pol + "' " +
                "ORDER BY date_id ASC, time_id ASC;", "dt");

        Record.serv = Service.name_BD("SELECT s.name " +
                "FROM service s " +
                "JOIN record r ON s.code = r.service_code " +
                "WHERE clients_policy = '" + pol + "' " +
                "ORDER BY date_id ASC, time_id ASC;", "name");
    }
}
