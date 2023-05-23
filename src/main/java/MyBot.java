
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.io.File;
import java.sql.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Date;


public class MyBot extends TelegramLongPollingBot {
    String botName;
    String botToken;
    Boolean admin = false;

    static final String DB_URL = "jdbc:postgresql://localhost/Med";
    static final String DB_USERNAME = "postgres";
    static final String DB_PASSWORD = "Kolobok.3";

    static Map<Long, String> LoginMap = new HashMap<>();
    static Map<Long, String> RegMap = new HashMap<>();
    static Map<Long, String> AdmMap = new HashMap<>();


    public MyBot(String botName, String botToken) {
        this.botName = botName;
        this.botToken = botToken;

        List<BotCommand> listofCommand = new ArrayList<>();
        listofCommand.add(new BotCommand("/start", "Запуск/перезапуск бота"));
        listofCommand.add(new BotCommand("/info", "Информация о боте"));

        try {
            this.execute(new SetMyCommands(listofCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public String getBotUsername() {
        // геттер имени бота
        return this.botName;
    }

    @Override
    public String getBotToken() {
        // геттер токена бота
        return this.botToken;
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }


    @Override
    public void onUpdateReceived(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start":
                    sendMenu_start(chatId, 1, "Выберите действие", update);
                    break;
                case "/info":
                    sendTextMessage(chatId, "Бот - ИС для клиентов коммерческой медицинской организации. \n" +
                            "Создан в образовательных целях по курсу ПИ КФУ \n" +
                            "Студенткой группы 09-151 \n" +
                            "Чугуновой Дарьей");
                    break;
                case "/login":
                    sendMenu_start(chatId, 2, "Введите вашу почту:", update);
                    LoginMap.put(chatId, "");
                    break;
                case "/registration":
                    sendMenu_start(chatId, 3, "Регистрация в системе \n" +
                            " \n" +
                            "Комерческая медицинская организация \n" +
                            "Бот поможет Вам ознакомиться с услугами и специалистами, записаться на прием, а также покажет вам вашу медицинскую карточку \n" +
                            "  \n" +
                            "Для дальнейшей регистрации необходимо дать согласие на обработку данных \n" +
                            "\n" +
                            "Чтобы пользователь мог записать онлайн, системе необходимо знать его полис, ФИО, дату рождения и почту \n" +
                            "Данные будут надежно храниться в системе ", update);

                    break;
                default:
                    systema(chatId, messageText, update, callbackQuery);
                    break;
            }
        } else if (update.hasCallbackQuery()) {

            long chatId = callbackQuery.getMessage().getChatId();
            String callbackData = callbackQuery.getData();

            // Обработка обратных вызовов кнопок
            if (callbackData.equals("Yes")) {
                sendTextMessage(chatId, "-- Да");
                sendTextMessage(chatId, "Продолжаем регистрацию");
                sendTextMessage(chatId, "Введите персональные данные");
                sendTextMessage(chatId, "Медицинский полис");
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
                RegMap.put(chatId, "POLISE");


            } else if (callbackData.equals("No")) {
                sendTextMessage(chatId, "-- Нет");
                sendTextMessage(chatId, "Регистрацию завершается");
                // Пользователь выбрал "Нет", переходит на /start
                sendMenu_start(chatId, 1, "Выберите действие", null);
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
            } else if (callbackData.startsWith("У")) {
                Service.doctors.delete(0, Service.doctors.length());
                int index = Integer.parseInt(callbackData.substring(1)) - 1; // Получаем индекс элемента
                String serviceCode = Service.id[index]; // Получаем код услуги
                String sql = "SELECT ms.full_name " +
                        "FROM medical_staff ms " +
                        "JOIN service s ON ms.service_code = s.code " +
                        "WHERE s.code = ?";

                try (Connection connection = DriverManager.getConnection(MyBot.DB_URL, MyBot.DB_USERNAME, MyBot.DB_PASSWORD);
                     PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, serviceCode);
                    ResultSet resultSet = statement.executeQuery();


                    int k = 0;
                    while (resultSet.next()) {
                        String doctorName = resultSet.getString("full_name");
                        Service.doctors.append(doctorName).append("\n");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                outputDate(chatId, callbackQuery, "<b>Название</b>\n" +
                        Service.names[index] + "\n" +
                        "\n" +
                        "<b>Описание</b>\n" +
                        Service.discription[index] + "\n" +
                        "\n" +
                        "<b>Кабинет</b>\n" +
                        Service.cabinet[index] + "\n" +
                        "\n" +
                        "<b>Цена</b>\n" +
                        Service.price[index] + "\n" +
                        "\n" +
                        "<b>Врачи</b>\n" +
                        Service.doctors.toString());
            } else if (callbackData.equals("BackУ")) {
                Button_prog(chatId, Service.names, "Выберите услугу, чтобы узнать подробную информацию о ней", Service.id);
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
            } else if (callbackData.startsWith("М")) {

                int index = Integer.parseInt(callbackData.substring(1)) - 1; // Получаем индекс элемента
                sendPhoto(chatId, Staff.photo[index]);
                outputDate(chatId, callbackQuery, "<b>ФИО</b>\n" +
                        Staff.names[index] + "\n" +
                        "\n" +
                        "<b>Должность </b>\n" +
                        Staff.job[index] + "\n" +
                        "\n" +
                        "<b>Опыт работы</b>\n" +
                        Staff.experience[index]);
            } else if (callbackData.equals("BackМ")) {
                Button_prog(chatId, Staff.names, "Выберите работника, чтобы узнать  информацию о нем", Staff.id);
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
            } else if (callbackData.startsWith("И")) {
                System.out.println("И");
                System.out.println();
                String pol = "";


                if (admin){
                    pol = AdmMap.get(chatId);
                }
                else{
                    String value = LoginMap.get(chatId);
                     pol = value.substring(2, 18);
                }
                System.out.println(pol);

                if (Med_cart.date == null){
                    sendTextMessage(chatId,"Записи нет");

                }
                else {
                    String[] fn = Service.name_BD("SELECT full_name FROM clients WHERE policy = '" + pol + "'", "full_name");
                    int index = Integer.parseInt(callbackData.substring(1)); // Получаем индекс элемента
                    System.out.println(index);
                    outputDate(chatId, callbackQuery, "<b>Полис</b>\n" +
                            pol + "\n" +
                            "\n" +
                            "<b>ФИО</b>\n" +
                            fn[0] + "\n" +
                            "\n" +
                            "<b>Дата</b>\n" +
                            Med_cart.date[index] + "\n" +
                            "\n" +
                            "<b>Процедура</b>\n" +
                            Med_cart.serv[index] + "\n" +
                            "\n" +
                            "<b>Диагноз </b>\n" +
                            Med_cart.diagnosis[index] + "\n" +
                            "\n" +
                            "<b>Рекомендации</b>\n" +
                            Med_cart.recommend[index] + "\n" +
                            "\n" +
                            "<b>Специалист</b>\n" +
                            Med_cart.staff[index]);
                }
            } else if (callbackData.equals("BackИ")) {
                if (admin) {
                    String[] m = null;

                    m = Service.name_BD("SELECT date FROM medical_history WHERE clients_policy ='" + AdmMap.get(chatId) + "' ORDER BY date ASC", "date");
                    Button_prog2(chatId, m, "Выберите дату", "И");
                    editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);

                    InlineKeyboardButton deleteButton = new InlineKeyboardButton();
                    deleteButton.setText("Добавить");
                    deleteButton.setCallbackData("AddRec");


                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    List<InlineKeyboardButton> row = new ArrayList<>();
                    row.add(deleteButton);
                    markup.setKeyboard(Collections.singletonList(row));


                    SendMessage message = new SendMessage();
                    message.setChatId(String.valueOf(chatId));
                    message.setText("Хотите добавить сведения?");
                    message.setReplyMarkup(markup);

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }


                } else {
                    String[] m = null;
                    String value = LoginMap.get(chatId);
                    String pol = value.substring(2, 18);
                    m = Service.name_BD("SELECT date FROM medical_history WHERE clients_policy ='" + pol + "' ORDER BY date ASC", "date");
                    Button_prog2(chatId, m, "Выберите дату", "И");
                    editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
                }
            } else if (callbackData.startsWith("2У")) {
                String s = callbackData.substring(1);

                int index = Integer.parseInt(s.substring(1)) - 1; // Получаем индекс элемента
                Available_dates.s = Service.id[index];
                Available_dates.sname = Service.names[index];
                sendTextMessage(chatId, Service.names[index]);
                Available_dates.date = Service.name_BD("SELECT d.dt\n" +
                        "FROM date2 d\n" +
                        "JOIN available_dates ad ON d.id = ad.date_id\n" +
                        "JOIN service s ON ad.service_сode = s.code\n" +
                        "WHERE s.code = '" + Service.id[index] + "'", "dt");
                Available_dates.id2 = Service.name_BD("SELECT d.id\n" +
                        "FROM date2 d\n" +
                        "JOIN available_dates ad ON d.id = ad.date_id\n" +
                        "JOIN service s ON ad.service_сode = s.code\n" +
                        "WHERE s.code = '" + Service.id[index] + "'", "id");

                Available_dates.time = Service.name_BD("SELECT date_id, time_id\n" +
                        "FROM public.available_dates\n" +
                        "WHERE service_сode = '" + Service.id[index] + "'\n" +
                        "ORDER BY date_id ASC, time_id ASC;", "time_id");

                if (Available_dates.time == null) {

                    sendTextMessage(chatId, "Все занято");
                    editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
                    return;
                }
                Available_dates.id = new String[Available_dates.date.length];
                String date_time[] = new String[Available_dates.time.length];
                for (int i = 0; i < date_time.length; i++) {
                    date_time[i] = Available_dates.date[i];
                    date_time[i] += "  -  " + Available_dates.time[i];
                    Available_dates.id[i] = "Зап" + (i);
                }

                Button_prog(chatId, date_time, "Выберите дату и время", Available_dates.id);
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
            } else if (callbackData.startsWith("Зап")) {

                String s = callbackData.substring(3);

                String d = Available_dates.id2[Integer.parseInt(s)];

                String dd = Available_dates.date[Integer.parseInt(s)];

                String t = Available_dates.time[Integer.parseInt(s)];

                String se = Available_dates.s;


                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                    String sql = "DELETE FROM available_dates WHERE time_id = ? AND date_id = ? AND service_сode = ?";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, t);
                    statement.setString(2, d);
                    statement.setString(3, se);

                    int rowsDeleted = statement.executeUpdate();

                    String value = LoginMap.get(chatId);
                    String pol = value;
                    if (admin == false) {
                        pol = value.substring(2, 18);
                    }

                    String sql2 = "INSERT INTO record (date_id, time_id, clients_policy, service_code) VALUES (?, ?, ?, ?)";
                    PreparedStatement statement2 = connection.prepareStatement(sql2);
                    statement2.setString(1, d);
                    statement2.setString(2, t);
                    statement2.setString(3, pol);
                    statement2.setString(4, se);
                    statement2.executeUpdate();

                    sendTextMessage(chatId, "Запись прошла успешно! " +
                            "\n" +
                            "\n" +
                            "Услуга - " + Available_dates.sname +
                            "\nДата - " + dd +
                            "\nВремя - " + t);
                    editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else if (callbackData.startsWith("Delete")) {
                String[] a = new String[Record.date.length];
                for (int i = 0; i < Record.date.length; i++) {
                    a[i] = "№" + (i + 1);
                }
                Button_prog(chatId, a, "Выберите  запись для удаления", a);
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
            } else if (callbackData.startsWith("№")) {

                sendTextMessage(chatId, callbackData);
                String value = LoginMap.get(chatId);
                String pol = value;
                if (admin == false) {
                    pol = value.substring(2, 18);
                }
                String s = callbackData.substring(1);


                String t = Record.time[Integer.parseInt(s) - 1];

                String d = Record.dateid[Integer.parseInt(s) - 1];

                String c = pol;

                String se = Record.servid[Integer.parseInt(s) - 1];

                try (Connection connection = DriverManager.getConnection(MyBot.DB_URL, MyBot.DB_USERNAME, MyBot.DB_PASSWORD)) {

                    // Создаем SQL-запрос для удаления записи из таблицы record
                    String deleteQuery = "DELETE FROM record WHERE date_id = ? AND time_id = ? AND clients_policy = ? AND service_code = ?";
                    PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);

                    // Задаем параметры для удаления записи
                    deleteStatement.setString(1, d);
                    deleteStatement.setString(2, t);
                    deleteStatement.setString(3, c);
                    deleteStatement.setString(4, se);

                    // Выполняем SQL-запрос на удаление записи
                    int rowsDeleted = deleteStatement.executeUpdate();

                    if (rowsDeleted > 0) {

                    } else {

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                try (Connection connection = DriverManager.getConnection(MyBot.DB_URL, MyBot.DB_USERNAME, MyBot.DB_PASSWORD)) {
                    // Создаем SQL-запрос для вставки записи в таблицу available_dates
                    String insertQuery = "INSERT INTO available_dates (date_id, time_id, service_сode) VALUES (?, ?, ?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery);

                    // Задаем параметры для вставки записи
                    insertStatement.setString(1, d);
                    insertStatement.setString(2, t);
                    insertStatement.setString(3, se);

                    // Выполняем SQL-запрос на вставку записи
                    int rowsInserted = insertStatement.executeUpdate();

                    if (rowsInserted > 0) {

                    } else {

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                sendTextMessage(chatId, "Запись успешно отменена");
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
            } else if (callbackData.equals("AddRec")) {

                int id = Med_cart.id2.length + 1;

                String q = "И" + id;
                String clientsPolicy = AdmMap.get(chatId);

                Date currentDate = new Date();
                // Форматирование даты в нужном формате
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = dateFormat.format(currentDate);
                LocalDate date = LocalDate.parse(formattedDate);

                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                    String sql = "INSERT INTO medical_history (id, clients_policy, date) VALUES (?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, q);
                    statement.setString(2, clientsPolicy);
                    statement.setDate(3, java.sql.Date.valueOf(date));
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                sendTextMessage(chatId, "Введите id специалиста (М*)");
                AdmMap.put(chatId, "IdВрач");
                LoginMap.put(chatId,clientsPolicy);
            } else if (callbackData.equals("Ad_AllRecords")) {
                sendTextMessage(chatId, "Все записи");
                Record.input_record();
                rec(chatId);

                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
            } else if (callbackData.equals("Ad_SearchRecord")) {
                sendTextMessage(chatId, "Поиск записи");
                sendTextMessage(chatId, "Введите номер полиса");
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
                AdmMap.put(chatId, "Ad_SearchRecord");

            } else if (callbackData.equals("Ad_AddRecord")) {
                sendTextMessage(chatId, "Добавление записи");
                sendTextMessage(chatId, "Введите полис клиента");
                AdmMap.put(chatId, "Ad_AddRecord");
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);

            }
            else if (callbackData.equals("Ad_DeleteRecord")) {
                sendTextMessage(chatId, "Удаление");
                sendTextMessage(chatId, "Введите полис клиента");
                AdmMap.put(chatId, "Ad_DeleteRecord");
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
                System.out.println("КОнец");
                System.out.println(AdmMap.get(chatId));

            }
            else if (callbackData.equals("Ad_SearchDate")) {
                sendTextMessage(chatId, "Поиск клиента");
                sendTextMessage(chatId, "Введите номер полиса");
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
                AdmMap.put(chatId, "Ad_SearchDate");

            }
            else if (callbackData.equals("Ad_AdDate")) {
                sendTextMessage(chatId, "Добавление клиента");
                sendTextMessage(chatId, "Введите полис клиента");
                AdmMap.put(chatId, "Ad_AddDate");
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);

            }

        }

    }

    public void rec(long chatId) {
        String s = "";
        for (int i = 0; i < Record.serv.length; i++) {
            s += (i + 1) + ". " + Record.serv[i] + "\n";
            s += Record.date[i] + "\n";
            s += Record.time[i] + "\n";
            s += Record.policy[i] + "\n" + "\n";
        }
        sendTextMessage(chatId, s);
    }


    public void systema(long chatId, String messageText, Update update, CallbackQuery callbackQuery) {

        if (admin == true) {
            if (messageText.equals("/Medical_card")) {
                AdmMap.put(chatId, "");
                sendTextMessage(chatId, "Введите номер полиса клиента");
            } else if (messageText.equals("/Record")) {
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();


                List<InlineKeyboardButton> searchRow = new ArrayList<>();
                InlineKeyboardButton searchButton = new InlineKeyboardButton();
                searchButton.setText("Все записи");
                searchButton.setCallbackData("Ad_AllRecords");
                searchRow.add(searchButton);
                rows.add(searchRow);


                List<InlineKeyboardButton> allRecordsRow = new ArrayList<>();
                InlineKeyboardButton allRecordsButton = new InlineKeyboardButton();
                allRecordsButton.setText("Поиск записи");
                allRecordsButton.setCallbackData("Ad_SearchRecord");
                allRecordsRow.add(allRecordsButton);
                rows.add(allRecordsRow);


                List<InlineKeyboardButton> addRecordRow = new ArrayList<>();
                InlineKeyboardButton addRecordButton = new InlineKeyboardButton();
                addRecordButton.setText("Добавить запись");
                addRecordButton.setCallbackData("Ad_AddRecord");
                addRecordRow.add(addRecordButton);
                rows.add(addRecordRow);

                List<InlineKeyboardButton> deleteRecordRow = new ArrayList<>();
                InlineKeyboardButton deleteRecordButton = new InlineKeyboardButton();
                deleteRecordButton.setText("Удалить запись");
                deleteRecordButton.setCallbackData("Ad_DeleteRecord");
                deleteRecordRow.add(deleteRecordButton);
                rows.add(deleteRecordRow);

                markup.setKeyboard(rows);

                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("Выберите действие");
                message.setReplyMarkup(markup);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
            else if (messageText.equals("/Update_date")) {
                try {
                    // Установка соединения с базой данных
                    Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

                    // Получаем текущую дату
                    LocalDate currentDate = LocalDate.now();

                    // Добавляем 7 дней к текущей дате
                    LocalDate newDate = currentDate.plusDays(7);

                    // Форматируем новую дату в строку с помощью DateTimeFormatter
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String newDateStr = newDate.format(formatter);

                    // Заменяем значение даты на новую дату в SQL-запросе
                    String sql = "UPDATE date2 SET dt = CAST(? AS date) WHERE dt = CAST(CURRENT_DATE AS date)";

                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, newDateStr);
                    statement.executeUpdate();

                    // Закрываем соединение с базой данных
                    connection.close();
                    sendTextMessage(chatId,"Запись на "+newDateStr+" успешно открыта");
                } catch (SQLException e) {
                    sendTextMessage(chatId,"Запись на следующий день уже открыта");
                }
            }

            else if ((messageText.equals("/Date"))) {
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();

                List<InlineKeyboardButton> allRecordsRow = new ArrayList<>();
                InlineKeyboardButton allRecordsButton = new InlineKeyboardButton();
                allRecordsButton.setText("Поиск клиента");
                allRecordsButton.setCallbackData("Ad_SearchDate");
                allRecordsRow.add(allRecordsButton);
                rows.add(allRecordsRow);


                List<InlineKeyboardButton> addRecordRow = new ArrayList<>();
                InlineKeyboardButton addRecordButton = new InlineKeyboardButton();
                addRecordButton.setText("Добавить клиента");
                addRecordButton.setCallbackData("Ad_AdDate");
                addRecordRow.add(addRecordButton);
                rows.add(addRecordRow);

                markup.setKeyboard(rows);

                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("Выберите действие");
                message.setReplyMarkup(markup);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }



            } else if (AdmMap.containsKey(chatId) && AdmMap.get(chatId).equals("Ad_SearchRecord")) {
                System.out.println("add");
                System.out.println(Record.date.length);
                Record.input_record(messageText);
                if (Record.date == null){
                    sendTextMessage(chatId,"Полис не найден");
                    return;
                }
                else {
                    rec(chatId);
                }
            } else if (AdmMap.containsKey(chatId) && AdmMap.get(chatId).equals("Ad_SearchDate")) {

                User.input_User(messageText);
                if (User.policy == null){
                    sendTextMessage(chatId,"Полис не найден");
                    return;
                }
                else {
                    String s = "";
                    for (int i = 0; i < User.policy.length; i++) {
                        s += User.policy[i] + "\n";
                        s += User.full_name[i] + "\n";
                        s += User.date_of_birth[i] + "\n";
                        if (User.mail[i] != null) {
                            s += User.mail[i] + "\n" + "\n";
                        }
                    }
                    sendTextMessage(chatId, s);
                }

            } else if (AdmMap.containsKey(chatId) && AdmMap.get(chatId).equals("Ad_AddRecord")) {
                Service.input_Service();
                User.input_User();
                Long l = Long.valueOf(0);
                Boolean b = false;

                try {
                    if (messageText.length() == 16) {
                        l = Long.valueOf(messageText);
                        for (int i = 0; i < User.policy.length; i++) {
                            Long q = Long.valueOf(User.policy[i]);

                            if (l.equals(q)) {
                                b = true;
                            }

                        }
                        if (b) {
                            LoginMap.put(chatId, String.valueOf(l));
                            Button_prog(chatId, Service.names, "Выберите услугу для записи", Service.id, "2");
                            editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else {
                        throw new IllegalArgumentException();
                    }
                } catch (IllegalArgumentException e) {
                    sendTextMessage(chatId, "Неизвестный полис");
                }
            }  else if (AdmMap.containsKey(chatId) && AdmMap.get(chatId).equals("Ad_DeleteRecord")) {
                System.out.println("qweqwe");
                Record.input_record(messageText);
                rec(chatId);
                String[] a = new String[Record.date.length];
                for (int i = 0; i < Record.date.length; i++) {
                    a[i] = "№" + (i + 1);
                }
                Button_prog(chatId, a, "Выберите  запись для удаления", a);
                editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
            }

            else if (AdmMap.containsKey(chatId) && AdmMap.get(chatId).equals("Ad_AddDate")) {
                try {
                if (messageText.length() == 16) {
                    Long l = Long.valueOf(messageText);
                    User.input_User(messageText);
                    if (User.policy != null) {
                        sendTextMessage(chatId, "Полис уже есть в системе");
                        AdmMap.clear();
                    } else {
                        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                            String sql = "INSERT INTO clients (policy) VALUES (?)";
                            PreparedStatement statement = connection.prepareStatement(sql);
                            statement.setString(1, messageText);
                            statement.executeUpdate();
                            AdmMap.put(chatId,messageText + "FIO");
                            sendTextMessage(chatId, "ФИО");
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                        // Строка содержит символы, отличные от русских букв
                        throw new IllegalArgumentException();
                    }
                } catch (IllegalArgumentException e) {
                    sendTextMessage(chatId, "Полис должен содержать 16 цифр");
                }
            }
            else if (AdmMap.containsKey(chatId) && AdmMap.get(chatId).endsWith("FIO")) {
                try {
                    String value = AdmMap.get(chatId);
                    String s = value.substring(0, 16);
                    // Проверяем, что строка содержит только русские буквы
                    if (messageText.matches("[а-яА-ЯёЁ\\s-]+")) {
                        AdmMap.put(chatId,s + "DATE");
                        sendTextMessage(chatId, "Дата рождения");
                        String q = messageText;
                        Log_Reg_DB.Add_DataBase2(new ArrayList<>(Arrays.asList(q, s)), "UPDATE clients SET full_name = ? WHERE policy = ?");
                    } else {
                        // Строка содержит символы, отличные от русских букв
                        throw new IllegalArgumentException();
                    }
                } catch (IllegalArgumentException e) {

                    sendTextMessage(chatId, "Можно использовать только русские буквы и тире");
                }

            } else if (AdmMap.containsKey(chatId) && AdmMap.get(chatId).endsWith("DATE")) {
                String dateString = messageText;

                try {
                    String value = AdmMap.get(chatId);
                    // Проверяем, что строка содержит только русские буквы
                    String q = messageText;
                    String s = value.substring(0, 16);
                    // Пытаемся преобразовать строку в объект LocalDate
                    LocalDate date2 = LocalDate.parse(dateString);
                    AdmMap.put(chatId, s + "ADDRESS");
                    Log_Reg_DB.Add_DataBase2(new ArrayList<>(Arrays.asList(date2, s)), "UPDATE clients SET date_of_birth = ? WHERE policy = ?");
                    sendTextMessage(chatId, "Пользователь успешно зарегистрирован");
                } catch (DateTimeParseException e) {

                    sendTextMessage(chatId, "Введенная строка не является корректной датой, повторите попытку");
                }

            }



            else if (AdmMap.containsKey(chatId) && AdmMap.get(chatId).equals("")) {
                try {
                    if (messageText.length() == 16) {
                        Long l = Long.valueOf(messageText);
                        Med_cart.input_Cart(String.valueOf(l));
                        if (Med_cart.date == null){
                            sendTextMessage(chatId,"Нет данных о клиенте");
                        }
                        else {
                            Med_cart.date = Service.name_BD("SELECT date FROM medical_history WHERE clients_policy ='" + l + "' ORDER BY date ASC", "date");
                            Button_prog2(chatId, Med_cart.date, Med_cart.serv, "История посещения", "И");
                            AdmMap.put(chatId, String.valueOf(l));
                            LoginMap.put(chatId, String.valueOf(l));
                        }

                        InlineKeyboardButton deleteButton = new InlineKeyboardButton();
                        deleteButton.setText("Добавить");
                        deleteButton.setCallbackData("AddRec");


                        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                        List<InlineKeyboardButton> row = new ArrayList<>();
                        row.add(deleteButton);
                        markup.setKeyboard(Collections.singletonList(row));


                        SendMessage message = new SendMessage();
                        message.setChatId(String.valueOf(chatId));
                        message.setText("Хотите добавить сведения?");
                        message.setReplyMarkup(markup);

                        try {
                            execute(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Строка содержит символы, отличные от русских букв
                        throw new IllegalArgumentException();
                    }

                } catch (IllegalArgumentException e) {
                    sendTextMessage(chatId, "Полис не найден");
                }


            } else if (AdmMap.get(chatId).equals("IdВрач")) {
                Staff.input_Staff();

                try {
                    String w = String.valueOf(messageText.charAt(0));
                    String m = messageText.substring(1);
                    int mm = Integer.parseInt(m);
                    m = "М" + mm;
                    int len = Staff.id.length;
                    if (mm > len || mm < 1 || w.equals("М") == false) {
                        throw new IllegalArgumentException();
                    }
                    med_add("UPDATE medical_history SET medical_staff_id = ? WHERE clients_policy = ? AND medical_staff_id IS NULL", m, LoginMap.get(chatId));
                    AdmMap.put(chatId, "IdУслуги");
                    sendTextMessage(chatId, "Введите Id услуги (У*)");
                } catch (IllegalArgumentException e) {
                    sendTextMessage(chatId, "Ошибка введенного id");
                }


            } else if (AdmMap.get(chatId).equals("IdУслуги")) {
                Service.input_Service();

                try {
                    String w = String.valueOf(messageText.charAt(0));
                    String m = messageText.substring(1);
                    int mm = Integer.parseInt(m);
                    m = "У" + mm;
                    int len = Service.id.length;
                    if (mm > len || mm < 1 || w.equals("У") == false) {
                        throw new IllegalArgumentException();
                    }
                    med_add("UPDATE medical_history SET service_code = ? WHERE clients_policy = ? AND service_code IS NULL", m, LoginMap.get(chatId));
                    AdmMap.put(chatId, "Диагноз");
                    sendTextMessage(chatId, "Введите диагноз");
                } catch (IllegalArgumentException e) {
                    sendTextMessage(chatId, "Ошибка введенного id");
                }


            } else if (AdmMap.get(chatId).equals("Диагноз")) {
                Staff.input_Staff();

                try {
                    String m = messageText;
                    med_add("UPDATE medical_history SET recommendations = ? WHERE clients_policy = ? AND recommendations IS NULL", m, LoginMap.get(chatId));
                    AdmMap.put(chatId, "Рекомендации");
                    sendTextMessage(chatId, "Дайте рекомендации");
                } catch (IllegalArgumentException e) {
                    sendTextMessage(chatId, "Ошибка введенных данных");
                }

            } else if (AdmMap.get(chatId).equals("Рекомендации")) {
              //  Staff.input_Staff();

                try {
                    String m = messageText;
                    med_add("UPDATE medical_history SET diagnosis = ? WHERE clients_policy = ? AND diagnosis IS NULL", m, LoginMap.get(chatId));
                    sendTextMessage(chatId, "Данные успешно добавлены");
                    Med_cart.input_Cart(LoginMap.get(chatId));

                } catch (IllegalArgumentException e) {
                    sendTextMessage(chatId, "Ошибка введенных данных");

                }

            }


        } else {
            if ((LoginMap.containsKey(chatId)) && LoginMap.get(chatId).length() == 18) {
                String value = LoginMap.get(chatId);
                String s = value.substring(0, 2);
                if (s.equals("ДА")) {
                    if (messageText.equals("/Services")) {
                        Service.input_Service();
                        Button_prog(chatId, Service.names, "Выберите услугу, чтобы узнать подробную информацию о ней", Service.id);
                    } else if (messageText.equals("/Specialists")) {
                        Staff.input_Staff();
                        Button_prog(chatId, Staff.names, "Выберите работника, чтобы узнать  информацию о нем", Staff.id);
                    } else if (messageText.equals("/Medical_card")) {
                        System.out.println("Medical_card");
                        String[] m = null;
                        String pol = value.substring(2, 18);
                        Med_cart.input_Cart(pol);
                        m = Service.name_BD("SELECT date FROM medical_history WHERE clients_policy ='" + pol + "' ORDER BY date ASC", "date");
                        System.out.println(m.length);
                        if (m == null){
                            sendTextMessage(chatId,"Нет данных");
                        }
                        else {
                            Button_prog2(chatId, m, "Выберите дату", "И");
                            System.out.println("qweqwe");
                        }
                    } else if (messageText.equals("/Data")) {
                        User.input_User();
                        String pol = value.substring(2, 18);
                        for (int i = 0; i < User.policy.length; i++) {
                            if (User.policy[i].equals(pol)) {
                                sendTextMessage(chatId, "<b>ФИО</b>\n" +
                                        User.full_name[i] + "\n" +
                                        "\n" +
                                        "<b>Полис</b>\n" +
                                        User.policy[i] + "\n" +
                                        "\n" +
                                        "<b>Дата рождения</b>\n" +
                                        User.date_of_birth[i] + "\n" +
                                        "\n" +
                                        "<b>Почта</b>\n" +
                                        User.mail[i] + "\n" +
                                        "\n");
                            }
                        }
                    } else if (messageText.equals("/Record")) {
                        Service.input_Service();
                        Button_prog(chatId, Service.names, "Выберите услугу для записи", Service.id, "2");
                    } else if (messageText.equals("/Viewing_records")) {
                        String value3 = LoginMap.get(chatId);
                        String pol = value3.substring(2, 18);
                        String stext = "";
                        Record.input_record(pol);
                        if (Record.time == null) {
                            sendTextMessage(chatId, "Нет актуальных записей");
                            return;
                        }

                        for (int i = 0; i < Record.time.length; i++) {
                            stext += (i + 1) + ". " + Record.serv[i] + "\n";
                            stext += Record.date[i] + "\n";
                            stext += Record.time[i] + "\n" + "\n";
                        }
                        sendTextMessage(chatId, stext);
// Создание кнопки "Удалить"
                        InlineKeyboardButton deleteButton = new InlineKeyboardButton();
                        deleteButton.setText("Удалить");
                        deleteButton.setCallbackData("Delete");

// Создание клавиатуры с кнопкой "Удалить"
                        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                        List<InlineKeyboardButton> row = new ArrayList<>();
                        row.add(deleteButton);
                        markup.setKeyboard(Collections.singletonList(row));

// Отправка сообщения с клавиатурой в чат
                        SendMessage message = new SendMessage();
                        message.setChatId(String.valueOf(chatId));
                        message.setText("Хотите отменить запись?");
                        message.setReplyMarkup(markup);

                        try {
                            execute(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }


                    }
                } else {
                    loging(chatId, messageText, update);
                }
            } else {
                loging(chatId, messageText, update);
            }
        }
    }




    private void outputDate(long chatId, CallbackQuery callbackQuery, String text) {
        editMessageReplyMarkup(chatId, callbackQuery.getMessage().getMessageId(), null);
        String callbackData = callbackQuery.getData();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Назад");
        String qwe = callbackData.substring(0, 1);


        button.setCallbackData("Back" + qwe);
        buttons.add(button);
        keyboard2.add(buttons);
        markup.setKeyboard(keyboard2);
        SendMessage message2 = new SendMessage();
        message2.setChatId(String.valueOf(chatId));

        int index = Integer.parseInt(callbackData.substring(1)) - 1; // Получаем индекс элемента


        message2.setText(text);
        message2.enableHtml(true);
        message2.setReplyMarkup(markup);
        try {
            execute(message2); // Отправка сообщения с InlineKeyboardMarkup в чат
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private void Button_prog(long chatId, String[] s, String text, String[] id) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Создаем отдельный список кнопок для каждого столбца
        for (int i = 0; i < s.length; i++) {
            List<InlineKeyboardButton> columnButtons = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s[i]);
            button.setCallbackData(id[i]);
            columnButtons.add(button);
            keyboard.add(columnButtons);
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        try {
            execute(message); // Отправка сообщения с обычной клавиатурой в чат
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void Button_prog2(long chatId, String[] s, String text, String id) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        System.out.println("Button_prog2");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        System.out.println(s.length);
        // Создаем отдельный список кнопок для каждого столбца
        for (int i = 0; i < s.length; i++) {
            List<InlineKeyboardButton> columnButtons = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s[i]);
            button.setCallbackData(id+i);
            System.out.println(id+i);
            columnButtons.add(button);
            keyboard.add(columnButtons);
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        try {
            execute(message); // Отправка сообщения с обычной клавиатурой в чат
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void Button_prog(long chatId, String[] s, String[] s2, String text, String[] id) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Создаем отдельный список кнопок для каждого столбца
        for (int i = 0; i < s.length; i++) {
            List<InlineKeyboardButton> columnButtons = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s[i] + " - " + s2[i]);
            button.setCallbackData(id[i]);
            columnButtons.add(button);
            keyboard.add(columnButtons);
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        try {
            execute(message); // Отправка сообщения с обычной клавиатурой в чат
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void Button_prog2(long chatId, String[] s, String[] s2, String text, String id) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Создаем отдельный список кнопок для каждого столбца
        for (int i = 0; i < s.length; i++) {
            List<InlineKeyboardButton> columnButtons = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s[i] + " - " + s2[i]);
            button.setCallbackData(id + i);
            columnButtons.add(button);
            keyboard.add(columnButtons);
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        try {
            execute(message); // Отправка сообщения с обычной клавиатурой в чат
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void Button_prog(long chatId, String[] s, String text, String[] id, String w) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Создаем отдельный список кнопок для каждого столбца
        for (int i = 0; i < s.length; i++) {
            List<InlineKeyboardButton> columnButtons = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s[i]);
            button.setCallbackData(w + id[i]);
            columnButtons.add(button);
            keyboard.add(columnButtons);
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        try {
            execute(message); // Отправка сообщения с обычной клавиатурой в чат
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //////////////////////Ввод логина и пароля при входе//////////////////
    public void loging(long chatId, String messageText, Update update) {


        if (LoginMap.containsKey(chatId) == false && RegMap.containsKey(chatId) == false) {
            sendMenu_start(chatId, 1, "Ошибка введенных данных", update);
        }
        if (LoginMap.containsKey(chatId)) {
            if (LoginMap.get(chatId).equals("") == false) {

                //получаем сохраненную почту
                String email = LoginMap.get(chatId);
                //полученный текст - пароль
                String password = messageText;
                //  boolean loginSuccessful = loginUser(email, password);
                boolean loginSuccessful = Log_Reg_DB.Add_DataBase(new ArrayList<>(Arrays.asList(email, password)), "SELECT * FROM clients WHERE mail = ? AND password_2 = ?");
                if (loginSuccessful) {
                    sendTextMessage(chatId, "Вы вошли в систему");

                    main_menu(chatId, "Выберите предложенные действия");
                    Log_Reg_DB.search_DataBase2(chatId, email, "SELECT policy FROM clients WHERE mail = ?", "policy");
                } else if (email.equals("admin") && password.equals("admin")) {
                    admin = true;
                    menu_admin(chatId, "Вход в систему с правами админа");
                } else {
                    sendMenu_start(chatId, 1, "Ошибка входа. Проверьте почту и пароль.", update);
                }


                // Удаляем сохраненную почту

            } else {

                // полученное сообщение - почта
                String email = messageText;
                sendTextMessage(chatId, "Введите ваш пароль:");
                // Сохраняем введенную почту в контексте пользователя
                LoginMap.put(chatId, email);

            }

        } else {
            reg(chatId, messageText, update);
        }
    }


    ///////////регистрация клиента
    public void reg(long chatId, String messageText, Update update) {

        String q = "";
        String value = RegMap.get(chatId);
        if (RegMap.get(chatId).equals("POLISE")) {
            try {
                if (messageText.length() == 16) {

                    Long l = Long.valueOf(messageText);

                    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                        String sql = "SELECT COUNT(*) FROM clients WHERE policy = ? ";
                        PreparedStatement statement = connection.prepareStatement(sql);
                        statement.setString(1, String.valueOf(l));
                        ResultSet resultSet = statement.executeQuery();
                        resultSet.next();
                        int count = resultSet.getInt(1);
                        if (count > 0) {

                             sql = "SELECT COUNT(*) FROM clients WHERE policy = ? and  mail is null";
                             statement = connection.prepareStatement(sql);
                            statement.setString(1, String.valueOf(l));
                             resultSet = statement.executeQuery();
                            resultSet.next();
                            int count2 = resultSet.getInt(1);

                            if (count2==0){
                                throw new SQLException();
                            }
                            else {
                                sendTextMessage(chatId,"Ваш полис есть в системе, но не активирован аккаунт");
                                RegMap.put(chatId, l + "ADDRESS");
                                sendTextMessage(chatId, "Введите почту");
                            }
                        } else {
                            sendTextMessage(chatId,"Ваш полис есть в системе, но не активирован аккаунт");
                            RegMap.put(chatId, l + "FIO");
                            sendTextMessage(chatId, "ФИО");
                            Log_Reg_DB.Add_DataBase2(new ArrayList<>(Arrays.asList(l)), "INSERT INTO clients (policy) VALUES (?)");
                        }
                    } catch (SQLException e) {
                        sendTextMessage(chatId, "Полис уже зарегистрирован в системе");
                    }


                } else {
                    // Строка содержит символы, отличные от русских букв
                    throw new IllegalArgumentException();
                }

            } catch (IllegalArgumentException e) {
                sendTextMessage(chatId, "Полис должен содержать 16 цифр");
            }

        } else if (value != null && value.endsWith("FIO")) {
            try {
                String s = value.substring(0, 16);
                // Проверяем, что строка содержит только русские буквы
                if (messageText.matches("[а-яА-ЯёЁ\\s-]+")) {
                    RegMap.put(chatId, s + "DATE");
                    sendTextMessage(chatId, "Дата рождения");
                    q = messageText;
                    Log_Reg_DB.Add_DataBase2(new ArrayList<>(Arrays.asList(q, s)), "UPDATE clients SET full_name = ? WHERE policy = ?");
                } else {
                    // Строка содержит символы, отличные от русских букв
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {

                sendTextMessage(chatId, "Можно использовать только русские буквы и тире");
            }

        } else if (value != null && value.endsWith("DATE")) {
            String dateString = messageText;

            try {
                q = messageText;
                String s = value.substring(0, 16);
                // Пытаемся преобразовать строку в объект LocalDate
                LocalDate date2 = LocalDate.parse(dateString);
                RegMap.put(chatId, s + "ADDRESS");
                sendTextMessage(chatId, "Адрес почты");
                Log_Reg_DB.Add_DataBase2(new ArrayList<>(Arrays.asList(date2, s)), "UPDATE clients SET date_of_birth = ? WHERE policy = ?");
            } catch (DateTimeParseException e) {

                sendTextMessage(chatId, "Введенная строка не является корректной датой, повторите попытку");
            }

        } else if (value != null && value.endsWith("ADDRESS")) {
            q = messageText;
            try {
                if ((q.endsWith("@gmail.com") || q.endsWith("@mail.ru") || q.endsWith("@yandex.com")) && messageText.matches("[a-zA-z@._0-9]+")) {
                    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                        String sql = "SELECT COUNT(*) FROM clients WHERE mail = ?";
                        PreparedStatement statement = connection.prepareStatement(sql);
                        statement.setString(1, String.valueOf(q));
                        ResultSet resultSet = statement.executeQuery();
                        resultSet.next();
                        int count = resultSet.getInt(1);
                        if (count > 0) {
                            throw new SQLException();
                        } else {
                            String s = value.substring(0, 16);
                            RegMap.put(chatId, s + "PASSW");
                            sendTextMessage(chatId, "Придумайте пароль для входа в систему");
                            Log_Reg_DB.Add_DataBase2(new ArrayList<>(Arrays.asList(q, s)), "UPDATE clients SET mail = ? WHERE policy = ?");
                        }
                    } catch (SQLException e) {
                        sendTextMessage(chatId, "Почта уже зарегистрирована в системе");
                    }
                } else {
                    // Строка содержит символы, отличные от русских букв
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {

                sendTextMessage(chatId, "Недопустимый формат почты");
            }
        } else if (value != null && value.endsWith("PASSW")) {
            q = messageText;
            String s = value.substring(0, 16);
            Log_Reg_DB.Add_DataBase2(new ArrayList<>(Arrays.asList(q, s)), "UPDATE clients SET password_2 = ? WHERE policy = ?");
            main_menu(chatId, "Вы успешно зарегистрированы в системе");
            LoginMap.put(chatId, "ДА" + s);
            ;
        } else {
            sendMenu_start(chatId, 1, "Ошибка введенных данных", update);
        }
    }


    // меню при старте
    private void sendMenu_start(long chatId, int a, String s, Update update) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        // Создаем список строк клавиатуры
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("/login");
        keyboardFirstRow.add("/registration");
        keyboard.add(keyboardFirstRow);
        keyboardMarkup.setKeyboard(keyboard);
        // Добавляем кнопки в первую строчку клавиатуры
        if (a == 1) {
            AdmMap.remove(chatId);
            RegMap.remove(chatId);
            LoginMap.remove(chatId);
            admin = false;
        } else if (a == 2) {
            RegMap.remove(chatId);
        } else if (a == 3) {
            LoginMap.remove(chatId);
        }
        SendMessage message = new SendMessage();
        message.setReplyMarkup(keyboardMarkup);
        message.setText(s);
        message.setChatId(String.valueOf(chatId));
        try {
            execute(message); // Отправка сообщения с обычной клавиатурой в чат
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        if (a == 3) {
            Button_yes_no(chatId, update);
        }
    }

    private void main_menu(long chatId, String s) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("/Services");
        keyboardFirstRow.add("/Specialists");
        keyboard.add(keyboardFirstRow);

        KeyboardRow keyboard2Row = new KeyboardRow();
        keyboard2Row.add("/Medical_card");
        keyboard2Row.add("/Record");
        keyboard2Row.add("/Data");

        keyboard.add(keyboard2Row);

        KeyboardRow keyboard3Row = new KeyboardRow();
        keyboard3Row.add("/Viewing_records");
        keyboard.add(keyboard3Row);

        keyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setReplyMarkup(keyboardMarkup);
        message.setText(s);
        message.setChatId(String.valueOf(chatId));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void menu_admin(long chatId, String s) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("/Medical_card");
        keyboardFirstRow.add("/Date");
        keyboard.add(keyboardFirstRow);

        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add("/Record");
        keyboardSecondRow.add("/Update_date");
        keyboard.add(keyboardSecondRow);

        keyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setReplyMarkup(keyboardMarkup);
        message.setText(s);
        message.setChatId(String.valueOf(chatId));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void Button_yes_no(long chatId, Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Да");
        button.setCallbackData("Yes");
        buttons.add(button);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Нет");
        button2.setCallbackData("No");
        buttons.add(button2);

        keyboard2.add(buttons);
        markup.setKeyboard(keyboard2);

        SendMessage message2 = new SendMessage();
        message2.setChatId(String.valueOf(chatId));
        message2.setText("Хотите продолжить регистрацию?");
        message2.setReplyMarkup(markup);

        try {
            execute(message2); // Отправка сообщения с InlineKeyboardMarkup в чат
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


        // Обработка обратных вызовов кнопок
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callbackData = callbackQuery.getData();
        long userId = callbackQuery.getFrom().getId();

        if (callbackData.equals("Yes")) {
            // Пользователь выбрал "Да", предлагается заполнить персональные данные
            sendTextMessage(userId, "Продолжаем регистрацию");
        } else if (callbackData.equals("No")) {
            // Пользователь выбрал "Нет", переходит на /start
            sendMenu_start(userId, 1, "Выберите действие", update);
            return;
        }

    }


    ///////Отправка сообщений//////////
    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.enableHtml(true);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // редактирвоание кнопок
    private void editMessageReplyMarkup(long chatId, int messageId, InlineKeyboardMarkup replyMarkup) {
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(String.valueOf(chatId));
        editMarkup.setMessageId(messageId);
        editMarkup.setReplyMarkup(replyMarkup);
        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(long chatId, String photoPath) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(new InputFile(new File(photoPath)));

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void med_add(String s, String q, String ee) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = s;
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, q); // Замените "М1" на актуальное значение medical_staff_id
            statement.setString(2, ee); // Замените "1234567891234567" на актуальное значение clients_policy
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}