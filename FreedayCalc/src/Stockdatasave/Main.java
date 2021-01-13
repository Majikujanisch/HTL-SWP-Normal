package Stockdatasave;

import com.mysql.cj.protocol.Resultset;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

public class Main extends Application{ // key IVB25ADTVUERPRXD
    static Connection connection;
    static String url = "jdbc:mysql://localhost/Aktienkurse?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    static String usernameDB="root";
    static String passwordDB="";
    static String ticker;

    public static void main(String[] args) throws JSONException, MalformedURLException, IOException, ClassNotFoundException, SQLException {
        String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=", key = "IVB25ADTVUERPRXD";
        Scanner user = new Scanner(System.in);


        //URL Abfrage und zusammenbau
        System.out.println("Welchen Ticker wollen Sie abfragen?");
        ticker = user.next();
        URL = buildURL(URL, ticker, key);
        System.out.println(URL);

        //Json abspeichern
        JSONObject json = new JSONObject(IOUtils.toString(new URL(URL), Charset.forName("UTF-8")));
        LocalDate date = LocalDate.parse((json.getJSONObject("Meta Data").get("3. Last Refreshed")).toString());
        json = json.getJSONObject("Time Series (Daily)");

        //MySQL
        connectToMysql();
        createTableMysql(ticker);

        for(int i = 0; i < 200; i++){
            try {
                createDataMysql(date,i, ticker, json.getJSONObject(date.toString()).get("4. close").toString());
            }
            catch(JSONException e){
                createDataMysql(date,i, ticker, "NULL");
            }

            date = date.minusDays(1);
        }
        showMysql(ticker);
        launch(args);
    }


    public static String buildURL(String url, String ticker, String key){
        return url = url + ticker.toUpperCase() + "&outputsize=full&apikey="+key;
    }
    public static boolean connectToMysql() throws ClassNotFoundException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();

            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
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
                    "Day varchar(10), inde int(4), close varchar(10), primary key(Day));");

        }catch(Exception e){

            System.out.println("false1");
        }
    }
    public static void createDataMysql(LocalDate date, int index, String ticker,String close){
        try{
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            connection.createStatement().executeUpdate("insert into "+ ticker +" values('" + date + "','" + index  + "','" + close+ "')on Duplicate key update inde="+ index +";");

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("false2");
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
        System.out.printf("%10s   %10s   %10s %n", "Datum", "Index", "Close");
        while (results.next()) {
            System.out.printf("%10s   %10s   %10s%n",
                    results.getString(1),
                    results.getInt(2),
                    results.getString(3)
            );
        }
    }

    public void start(Stage stage) throws Exception {
        ResultSet results = null;
        int avg = 0;

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
        mittelwert.setName("xxx'er Schnitt von " + ticker);
        //populating the series with data
        while (results.next()) {
            graph.getData().add(new XYChart.Data(results.getString(1), results.getString(3)));
        }
        lineChart.setCreateSymbols(false);

        Scene scene  = new Scene(lineChart,1500,900);
        lineChart.getData().add(graph);

        stage.setScene(scene);
        stage.show();
    }
}
