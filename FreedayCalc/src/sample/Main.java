package sample;


import java.io.File;
import java.io.FileNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.sql.*;

public class Main extends Application{

    static int monday = 0, thusday = 0, wednesday = 0, thursday = 0, friday = 0, saturday = 0, sunday = 0;
    static Connection connection;
    public static void main(String[] args){
        //Variablen
        int year = 2000, durationYears, fixedyear, alldays;

        ArrayList<LocalDate> freedays = new ArrayList<>();
        Scanner User = new Scanner(System.in);

        //eingabe durch den User
        System.out.println("Startjahr angeben: ");
        year = User.nextInt();
        fixedyear = year;
        System.out.println("Dauer in Jahren angeben: ");
        durationYears = User.nextInt();

        //Definierung der Feiertage
        do {
            try (Scanner scanner = new Scanner(new File("C:\\Users\\Anna\\IdeaProjects\\HTL-SWP-Normal\\FreedayCalc\\src\\sample\\FreeDays.csv"))) {
                while (scanner.hasNextLine()) {
                    freedays.add(getRecordFromLine(scanner.nextLine(), year));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            year++;
        }while(year <= fixedyear + durationYears);

        //überprüfung welche Tage frei sind
        for(LocalDate c:freedays){
            DayOfWeek dayS = c.getDayOfWeek();
            switch (dayS) {
                case MONDAY -> monday++;
                case TUESDAY -> thusday++;
                case WEDNESDAY -> wednesday++;
                case THURSDAY -> thursday++;
                case FRIDAY -> friday++;
                case SATURDAY -> saturday++;
                case SUNDAY -> sunday++;

            }

        }
        monday += 2*durationYears;
        thursday += 2*durationYears;
        sunday += 2*durationYears;
        alldays  = monday + thusday+wednesday+thursday+friday+saturday+sunday;


        //Ausgabe der Daten
        writeDays(monday,thusday,wednesday,thursday,friday,saturday,sunday);
        //Mysql
        connectToMysql("127.0.0.1","root",""); // Password 
        createTableMysql("127.0.0.1","root",""); // Password
        createDataMysql("127.0.0.1","root","",year,durationYears,alldays, 
                monday,thursday,wednesday,thusday,friday,saturday,sunday); //Password
        showMysql("127.0.0.1","root",""); // Password
        //JavaFX
        launch(args);


    }

    public static LocalDate getRecordFromLine(String line, int year) {
        LocalDate values = LocalDate.MIN;
        int month, day;
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter("#");
            while (rowScanner.hasNextInt()) {
                month = rowScanner.nextInt();
                day = rowScanner.nextInt();

                values = LocalDate.of(year, month, day);
            }
        }
        return values;
    }
    public static int getDaysofMonth(int month){

        int days = switch (month) {
            case 2 -> 28;   //Februar
            case 4, 6, 9, 11 -> 30;     //Monate mit 30 tagen
            default -> 31;      //Monate mit 31 Tagen
        };
        return days;
    }
    public static void writeDays(int mon, int thus, int wed, int thur, int fri, int sat, int sun){
        System.out.print(mon);
        System.out.println(" mondays");
        System.out.print(thus);
        System.out.println(" thuesdays");
        System.out.print(wed);
        System.out.println(" wednesdays");
        System.out.print(thur);
        System.out.println(" thursdays");
        System.out.print(fri);
        System.out.println(" fridays");
        System.out.print(sat);
        System.out.println(" saturdays");
        System.out.print(sun);
        System.out.println(" sundays");
    }
    public static boolean connectToMysql(String host, String user, String passwd){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            String connectionCommand = "jdbc:mysql://"+host+":3306/"+"?user="+user+"&password="+passwd;
            connection = DriverManager.getConnection(connectionCommand);
            connection.createStatement().executeUpdate("create database if not exists FreeDays");
            connectionCommand = "jdbc:mysql://"+host+":3306/"+"FreeDays?user="+user+"&password="+passwd;
            connection = DriverManager.getConnection(connectionCommand);
            return true;

        }catch (Exception ex){
            System.out.println("false");
            return false;
        }
    }
    public static void createTableMysql(String host, String user, String passwd){
        try{
            String connectionCommand = "jdbc:mysql://"+host+":3306/"+"FreeDays?user="+user+"&password="+passwd;
            connection.createStatement().executeUpdate("create table if not exists data(" +
                    "Jahr int(4) PRIMARY KEY, Dauer int(4), AlleTage int(5), Montage int (5),Dienstage int (5)," +
                    "Mittwoche int (5),Donnerstage int (5),Freitage int (5),Samstage int (5),Sonntage int (5))");
        }catch(Exception e){
            System.out.println("false");
        }
    }
    public static void createDataMysql(String host, String user, String passwd, int jahr, int dauer, int alldays, int mon
            ,int thur, int wed, int thus, int fri, int sat, int sun){
        int jahrSql, dauerSql;

        try{
            String connectionCommand = "jdbc:mysql://"+host+":3306/"+"FreeDays?user="+user+"&password="+passwd;
            jahrSql = connection.createStatement().executeUpdate("select Jahr from data where Jahr =" + jahr + ";");
            dauerSql = connection.createStatement().executeUpdate("select Dauer from data where Dauer =" + dauer + ";");
            if(jahr != jahrSql && dauer != dauerSql) {
                connection.createStatement().executeUpdate("insert into data values('" + jahr + "','" + dauer + "','" + alldays + "','" + mon
                        + "','" + thur + "','" + wed + "','" + thus + "','" + fri + "','" + sat + "','" + sun + "')");
            }
        }catch(Exception e){
            System.out.println("false");
        }
    }
    public static void showMysql(String host, String user, String passwd){
        try{
            String connectionCommand = "jdbc:mysql://"+host+":3306/"+"FreeDays?user="+user+"&password="+passwd; //Port überprüfen
            connection.createStatement().executeUpdate("select * from data");
        }catch(Exception e){
            System.out.println("false");
        }
    }

    @Override
    public void start(Stage primaryStage)throws Exception {
        try{
            // Angeben wie die Achsen sein sollen
            final NumberAxis xAxis = new NumberAxis();
            final CategoryAxis yAxis = new CategoryAxis();

            // Anlegen der BarChart und angabe wie die Anordnung
            final BarChart<String, Number> barChart = new BarChart<String, Number>(yAxis, xAxis);
            barChart.setTitle("Freie Tage im Vergleich");
            xAxis.setLabel("Tage");
            yAxis.setLabel("Wochentage");

            // Anlegen einer Serie mit den jeweiligen Werten und dem Item
            // dem sie zugeordnet werden.
            XYChart.Series<String, Number> series1 = new XYChart.Series<String,Number>();
            series1.setName("Days of week");
            series1.getData().add(new XYChart.Data("Mondays",monday));
            series1.getData().add(new XYChart.Data("tuesdays",thusday));
            series1.getData().add(new XYChart.Data("wednesday",wednesday));
            series1.getData().add(new XYChart.Data("thursdays",thursday ));
            series1.getData().add(new XYChart.Data("fridays",friday));


            barChart.getData().add(series1);

            primaryStage.setTitle("Freie Tage im Vergleich");
            Scene scene = new Scene(barChart, 400,200);

            primaryStage.setScene(scene);
            primaryStage.setHeight(300);
            primaryStage.setWidth(400);

            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();

        }
    }

}
