import java.sql.*;
import java.util.ArrayList;

public class Log_Reg_DB {



    static boolean  Add_DataBase(ArrayList s, String sql) {
        //подключение к базе данных
        try (Connection connection = DriverManager.getConnection(MyBot.DB_URL, MyBot.DB_USERNAME, MyBot.DB_PASSWORD);
             //запрос
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // значения параметров
            for (int i = 0; i < s.size(); i++) {
                statement.setObject(i+1, s.get(i));
            }
            // возвращаем объект с результатом запроса
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // Если найдена хотя бы одна запись, вход успешен
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    static void Add_DataBase2(ArrayList s, String sql) {
        //подключение к базе данных
        try (Connection connection = DriverManager.getConnection(MyBot.DB_URL, MyBot.DB_USERNAME, MyBot.DB_PASSWORD);
             //запрос
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // значения параметров
            for (int i = 0; i < s.size(); i++) {
                statement.setObject(i+1, s.get(i));
            }
            // возвращаем объект с результатом запроса
            statement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void search_DataBase2(long chatId, String s, String sql, String q) {
        //подключение к базе данных

        try (Connection connection = DriverManager.getConnection(MyBot.DB_URL, MyBot.DB_USERNAME, MyBot.DB_PASSWORD);
             //запрос
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // значения параметров
            statement.setString(1,s);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String policy = resultSet.getString(q);
                MyBot.LoginMap.put(chatId, "ДА"+policy);
                System.out.println(MyBot.LoginMap.toString());
            } else {
                throw new SQLException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
