import java.sql.*;
import java.util.ArrayList;

public class Service {
    static String[]id = null;
    static String[]names = null;
    static String []discription = null;
    static String[]cabinet = null;
    static String[]price = null;
    static StringBuilder doctors = new StringBuilder();
    public static String[] name_BD(String s, String q) {
        String[] names = null;
        try (Connection connection = DriverManager.getConnection(MyBot.DB_URL, MyBot.DB_USERNAME, MyBot.DB_PASSWORD)) {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String sql = s;
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.last()) {
                int rowCount = resultSet.getRow();
                resultSet.beforeFirst(); // Возвращаем указатель на начало набора данных

                names = new String[rowCount];
                int index = 0;
                while (resultSet.next()) {
                    String name = resultSet.getString(q);
                    names[index++] = name;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    public static void input_Service(){
        id = Service.name_BD("SELECT code FROM service ORDER BY name ASC", "code");
        names = Service.name_BD("SELECT name FROM service ORDER BY name ASC", "name");
        discription = Service.name_BD("SELECT description FROM service ORDER BY name ASC", "description");
        cabinet = Service.name_BD("SELECT cabinet FROM service ORDER BY name ASC", "cabinet");
        price = Service.name_BD("SELECT price FROM service ORDER BY name ASC", "price");
    }


}
