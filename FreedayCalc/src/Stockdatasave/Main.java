package Stockdatasave;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

import javax.imageio.ImageIO;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class Main /*extends Application*/{ // key IVB25ADTVUERPRXD
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
        List<String> tickerlist = new ArrayList<String>();
        String txt = "ticker";
        String URL1 = null;
        createTXT(txt);
        tickerlist = loadTicker(txt);
        int updatedays;
        createDirec();
        for(String ticker1 : tickerlist) {
            ticker = ticker1;
            //URL Abfrage und zusammenbau
            /* System.out.println("Welchen Ticker wollen Sie abfragen?");
            ticker = user.next(); */
            URL1 = buildURL(URL, ticker, key);
            System.out.println(URL1);



            //Json abspeichern
            JSONObject json = new JSONObject(IOUtils.toString(new URL(URL1), Charset.forName("UTF-8")));
            LocalDate date = LocalDate.parse((json.getJSONObject("Meta Data").get("3. Last Refreshed")).toString());
            json = json.getJSONObject("Time Series (Daily)");

            //MySQL
            connectToMysql();
            createTableMysql(ticker);



            if (daysDifference(ticker) >= 0) {
                updatedays = daysDifference(ticker);
            } else {
                updatedays = 2000;
            }
            for (int i = 0; i < updatedays; i++) {
                try {
                    double coeffizient = Double.parseDouble(json.getJSONObject(date.toString()).get("8. split coefficient").toString());

                    if (Double.parseDouble(json.getJSONObject(date.toString()).get("8. split coefficient").toString()) > 1 || Double.parseDouble(json.getJSONObject(date.toString()).get("8. split coefficient").toString()) < 1) {
                        divident = divident * Double.parseDouble(json.getJSONObject(date.toString()).get("8. split coefficient").toString());
                    }
                    double close = Double.parseDouble(json.getJSONObject(date.toString()).get("4. close").toString()) / divident;

                    insertDataInDB(date, ticker, String.valueOf(close), String.valueOf(coeffizient));



                } catch (JSONException e) {
                    insertDataInDB(date, ticker, "NULL", "NULL");
                }

                date = date.minusDays(1);
            }
            insertDataInDB(LocalDate.now(), ticker);
            System.out.println("Wait for 12 sec");
            waitsec(12);

        }
        //launch(args);
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

        System.out.println("insert");

    }
    public static void insertDataInDB(LocalDate date, String ticker){
        try{
            System.out.println(date);
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            connection.createStatement().executeUpdate("insert into UpdateDates values ('" + ticker + "','"+date+"') on Duplicate key update lastupdate='"+date+"';");
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
        int differenz = -1;
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
            if(lastday == LocalDate.now()){
                differenz = -1;
            }
            else{

                differenz = (int) ChronoUnit.DAYS.between(lastday, LocalDate.now());
            }
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
        //stage.show();
        saveAsPNG(scene, "FreedayCalc/src/Stockdatasave/Bilder/" + LocalDate.now() + ticker + ".png" );
    }
    public static void saveAsPNG(Scene scene, String filename){
        WritableImage image = scene.snapshot(null);
        File file = new File(filename);
        try{
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        }catch(IOException e){
            e.printStackTrace();
        }
     }
    public static void createDirec(){
        File dir = new File("FreedayCalc/src/Stockdatasave/Bilder");
        if (dir.mkdir()){
            System.out.println("Ordner Erstellt!");
        }
    }

    public static ArrayList<String> loadTicker(String filename) {
        ArrayList<String> ticker = new ArrayList<String>();
        String path = "FreedayCalc/src/Stockdatasave/"+filename + ".txt";
        File file = new File(path);

        if (!file.canRead() || !file.isFile())
            System.exit(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(path));
            String zeile = null;
            while ((zeile = in.readLine()) != null) {
                ticker.add(zeile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                }
        }
        return ticker;
    }
    public static boolean createTXT (String name){
        try {
            File myObj = new File("FreedayCalc/src/Stockdatasave/"+name + ".txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
                return true;
            } else {
                System.out.println("File already exists.");
                return false;
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false;
    }
    public static void waitsec(int time){
        try {
            TimeUnit.SECONDS.sleep(12);
        }
        catch(Exception e){
            System.out.println("schlafen nicht möglich!");
        }
    }

}
