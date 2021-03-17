package Stockdatasave;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import java.time.temporal.ChronoUnit;

public class Main extends Application{ // key IVB25ADTVUERPRXD
    static Connection connection;
    /*
    String.format("jdbc:mysql://%s/%s?user=%s&password=%s&serverTimezone=UTC",
						m_hostname, m_database, m_user, m_password));
     */
    static String url = "jdbc:mysql://localhost:3305/Aktienkurse?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    static String usernameDB="Majikujanisch";
    static String passwordDB="C4QvI3PRllLGen82eV4odV";
    static String ticker;
    static double divident = 1;

    public static void main(String[] args) throws JSONException, MalformedURLException, IOException, ClassNotFoundException, SQLException {
        String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=", key = "IVB25ADTVUERPRXD";
        Scanner user = new Scanner(System.in);
        int updatedays;


        //URL Abfrage und zusammenbau
        System.out.println("Welchen Ticker wollen Sie abfragen?");
        ticker = user.next();
        URL = buildURL(URL, ticker, key);
        System.out.println(URL);


        //Json abspeichern
        JSONObject json = new JSONObject(IOUtils.toString(new URL(URL), Charset.forName("UTF-8")));
        LocalDate date = LocalDate.parse((json.getJSONObject("Meta Data").get("3. Last Refreshed")).toString());
        LocalDate now = LocalDate.now();
        json = json.getJSONObject("Time Series (Daily)");

        //MySQL
        connectToMysql();
        createTableMysql(ticker);
        insertDataInDB(LocalDate.now(), ticker);
        if(daysDifference(ticker) > 0){
            updatedays = daysDifference(ticker);
        }
        else{
            updatedays = 1000;
        }
        for(int i = 0; i < updatedays; i++){
            try {
                    double coeffizient=Double.parseDouble(json.getJSONObject(date.toString()).get("8. split coefficient").toString());

                    if(Double.parseDouble(json.getJSONObject(date.toString()).get("8. split coefficient").toString()) > 1 || Double.parseDouble(json.getJSONObject(date.toString()).get("8. split coefficient").toString()) < 1){
                        divident = divident * Double.parseDouble(json.getJSONObject(date.toString()).get("8. split coefficient").toString());
                    }
                    double close = Double.parseDouble(json.getJSONObject(date.toString()).get("4. close").toString())/divident;

                insertDataInDB(date, ticker, String.valueOf(close), String.valueOf(coeffizient));


            }
            catch(JSONException e){
                insertDataInDB(date, ticker, "NULL", "NULL");
            }

            date = date.minusDays(1);
        }
        launch(args);
    }


    public static String buildURL(String url, String ticker, String key){
        return url = url + ticker.toUpperCase() + "&outputsize=full&apikey="+key;
    }
    public static boolean connectToMysql() throws ClassNotFoundException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();

            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            System.out.println("Connected to database");
            return true;


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void createTableMysql(String ticker){
        try{
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            connection.createStatement().executeUpdate("create table if not exists "+ ticker +"(" +
                    "Day date, close double(5,2), divident double(2,1), primary key(Day));");
            connection.createStatement().executeUpdate("create table if not exists UpdateDates (ticker varchar(10) ,lastUpdate date, primary key(ticker));");

        }catch(Exception e){

            System.out.println("false1");
        }
    }
    public static void insertDataInDB(LocalDate date, String ticker, String close, String divident){
        try{
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            if(close != "NULL") {
                connection.createStatement().executeUpdate("insert into " + ticker + " values('" + date + "','" + Double.parseDouble(close)+ "','" + Double.parseDouble(divident) +"')on Duplicate key update close=" + close + ";");
            }
        }catch(Exception e){
        }
    }
    public static void insertDataInDB(LocalDate date, String ticker){
        try{
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            connection.createStatement().executeUpdate("insert into UpdateDates values ('" + ticker + "','"+date+"');");
        }catch(Exception e){

        }

    }
    public static void showMysql(String ticker) throws SQLException {
        ResultSet results = null;
        try {
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            try {
                results = connection.createStatement().executeQuery("SELECT * from " + ticker + ";");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("false4");
            }
        } catch (Exception e) {

            e.printStackTrace();
            System.out.println("false3");
        }
        //System.out.printf("%10s   %10s", "Datum", "Close");
        while (results.next()) {
            System.out.printf(results.getString(1));
            System.out.printf("   ");
            System.out.printf(results.getString(2));
            System.out.printf("  ");
            System.out.println(results.getString(4));

        }
    }
    public static int daysDifference(String ticker) throws SQLException {
        LocalDate lastday = null;
        int differenz = 0;
        ResultSet results = null;
        try {
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            try {

                results = connection.createStatement().executeQuery("SELECT lastUpdate from UpdateDates where ticker = '"+ticker+"';");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("false4");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("false3");
        }
        while(results.next()) {
            lastday = (results.getDate("lastUpdate")).toLocalDate();
            differenz = (int) ChronoUnit.DAYS.between(lastday, LocalDate.now());
        }
        return differenz;
    }

    public void start(Stage stage) throws Exception {
        ResultSet results = null;
        ResultSet resultavg=null;
        int avg = 0;
        int index1 = 1, index2 = 200;


        try {
            try {
                results = connection.createStatement().executeQuery("SELECT * from " + ticker + ";");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("false4");
            }
        } catch (Exception e) {

            e.printStackTrace();
            System.out.println("false3");
        }

        stage.setTitle("Line Chart Sample");
        //defining the axes
        final CategoryAxis date = new CategoryAxis();
        final NumberAxis close = new NumberAxis();
        date.setLabel("Number of Month");
        //creating the chart
        final LineChart<String,Number> lineChart =
                new LineChart<String,Number>(date,close);

        lineChart.setTitle("Stock Monitoring of "+ ticker);
        //defining a series
        XYChart.Series graph = new XYChart.Series();
        XYChart.Series mittelwert = new XYChart.Series();
        graph.setName("Aktien von " + ticker);
        mittelwert.setName("200'er Schnitt von " + ticker);
        //populating the series with data
        while (results.next()) {
            LocalDate date1 = results.getDate(1).toLocalDate();
            graph.getData().add(new XYChart.Data(results.getString(1), results.getDouble(2)));
            resultavg = connection.createStatement().executeQuery("SELECT avg(close) from " + ticker + " where day > "+ java.sql.Date.valueOf( (date1.minusDays(200) )) +" and day <"+ java.sql.Date.valueOf(date1)+";");
            while(resultavg.next()){
                mittelwert.getData().add(new XYChart.Data(results.getString(1), resultavg.getDouble(1)));
            }
            index1++;
            index2++;
        }

        lineChart.setCreateSymbols(false);

        Scene scene  = new Scene(lineChart,800,450);
        lineChart.getData().add(graph);
        lineChart.getData().add(mittelwert);

        stage.setScene(scene);
        stage.show();
    }
}
