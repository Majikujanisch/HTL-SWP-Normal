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

public class Main extends Application{ // key IVB25ADTVUERPRXD
    static Connection connection;
    static String port;
    static String url;
    static String usernameDB;
    static String passwordDB;
    static String ticker;
    static String key;
    final static String path = "FreedayCalc/src/Stockdatasave/";
    static int test = 0;

    public static void main(String[] args) throws JSONException, MalformedURLException, IOException, ClassNotFoundException, SQLException {
        final String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=";
        //Scanner user = new Scanner(System.in); // deprecated
        List<String> tickerlist = new ArrayList<String>();

        final String txt = "ticker";
        createTXT(txt);
        createTXT("UserDates");
        tickerlist = loadTxt(txt);
        setUserdata();
        createDirec();
        for(String ticker1 : tickerlist) {
            ticker = ticker1;
            //URL Abfrage und zusammenbau
            /* System.out.println("Welchen Ticker wollen Sie abfragen?");
            ticker = user.next(); */
            String URL1 = buildURL(URL, ticker, key);
            System.out.println(URL1);

            //Json abspeichern
            JSONObject json = new JSONObject(IOUtils.toString(new URL(URL1), Charset.forName("UTF-8")));
            LocalDate date = LocalDate.parse((json.getJSONObject("Meta Data").get("3. Last Refreshed")).toString());
            json = json.getJSONObject("Time Series (Daily)");

            //MySQL
            connectToMysql();
            createTableMysql(ticker);


            int updatedays;
            if (daysDifference(ticker) >= 0) {
                updatedays = daysDifference(ticker);
            } else {
                updatedays = 7000;
            }

            double divi = 1.0;
            double divident = 1.0;
            for (int i = 0; i < updatedays; i++) {
                try {
                    boolean happened = false;
                    double coeffizient = Double.parseDouble(json.getJSONObject(date.toString()).get("8. split coefficient").toString());

                    if (coeffizient > 1 || coeffizient < 1) {
                        divident = divident * coeffizient;
                        happened  = true;
                    }

                    double close = Double.parseDouble(json.getJSONObject(date.toString()).get("4. close").toString());


                    double cor;
                    if(!happened){
                         cor = close / divident;
                    }
                    else{
                         cor = close / divi;
                    }
                    insertDataInDB(date, ticker, String.valueOf(close), String.valueOf(coeffizient),String.valueOf(cor));

                    divi=divident;


                } catch (JSONException e) {
                    insertDataInDB(date, ticker, "NULL", "NULL", "NULL");
                }

                date = date.minusDays(1);
            }

            calc200();
            insertDataInDB(LocalDate.now(), ticker);
            System.out.println("Wait for 12 sec");
            waitsec(12);

        }
        launch(args);
        disconnectMysql();
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
    public static boolean disconnectMysql() throws ClassNotFoundException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();

            connection.close();
            System.out.println("disconnected from database");
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
                    " Day date DEFAULT (CURRENT_DATE + INTERVAL 1 YEAR)," +
                    " close double(6,2)," +
                    " divident double(2,1)," +
                    " splitcor double(5,2)," +
                    " zweihundert double(5,2)," +
                    " primary key(Day));");

           /* String updateSql = String.format(
                    "CREATE TABLE IF NOT EXISTS %s (" +
                    "ticker VARCHAR(10)," +
                            "lastUpdate DATE," +
                            "PRIMARY KEY(ticker));",

                    //eig variable.. is eleganter
                    "UpdateDates"
            );*/
            connection.createStatement().executeUpdate("create table if not exists UpdateDates (ticker varchar(10) ,lastUpdate date, primary key(ticker));");

        }catch(Exception e){

            System.out.println("false1");
        }
    }
    public static void insertDataInDB(LocalDate date, String ticker, String close, String divident, String cor){


        try{
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);

            if(close != "NULL") {
                double avg = 0;
/*
                String updateSql = String.format(
                        "INSERT INTO %s VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE close=?;",

                );
*/
                connection.createStatement().executeUpdate("insert into " + ticker + " values('" + java.sql.Date.valueOf(date) + "','"
                        + Double.parseDouble(close)+ "','"
                        + Double.parseDouble(divident) +"','"
                        +cor+"','"
                        + avg +"')on Duplicate key update close=" + close + ";");

            }
        }catch(Exception e){
            System.out.println("noinsert");
            System.out.println(e);
        }



    }
    public static void insertDataInDB(LocalDate date, String ticker){
        try{
            System.out.println(date);
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            connection.createStatement().executeUpdate("insert into UpdateDates values ('" + ticker + "','"+date+"') on Duplicate key update lastupdate='"+date+"';");
        }catch(Exception e){
            System.out.println("NOinsert");
        }

    }
    public static void calc200(){

            //ResultSet resultavg=null;
            ResultSet resultday = null;


            try{
                connection = DriverManager.getConnection(url, usernameDB, passwordDB);
                resultday  = connection.createStatement().executeQuery("SELECT day from " + ticker +";");
                while(resultday.next()) {
                    LocalDate date = resultday.getDate(1).toLocalDate();
                    double avg = 0.00;

                    var resultavg = connection.createStatement().executeQuery("SELECT avg(splitcor) AS 'average' from " + ticker + " where day > '" + java.sql.Date.valueOf((date.minusDays(200))) + "' and day < '" + java.sql.Date.valueOf(date) + "';");
                    resultavg.next();
                    avg = resultavg.getDouble("average");


                    var sqlCmd = "insert into " + ticker + " (Day,zweihundert)values('"+java.sql.Date.valueOf(date)+"','"+avg+"')on Duplicate key update zweihundert='" + avg + "';";
                    System.out.println(sqlCmd);
                        connection.createStatement().executeUpdate(sqlCmd);



                    System.out.println(test);
                    test++;
                }
            }catch(Exception e){
                System.out.println("noinsert");
                e.printStackTrace();

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
        List<String> tickerlist = new ArrayList<String>();
        String txt = "ticker";
        createTXT(txt);
        tickerlist = loadTxt(txt);
        for(String ticker1 : tickerlist) {
            ticker = ticker1;

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
            final LineChart<String, Number> lineChart =
                    new LineChart<String, Number>(date, close);

            lineChart.setTitle("Stock Monitoring of " + ticker.toUpperCase());
            //defining a series
            XYChart.Series graph = new XYChart.Series();
            XYChart.Series mittelwert = new XYChart.Series();
            graph.setName("Aktien von " + ticker);
            mittelwert.setName("200'er Schnitt von " + ticker);
            //populating the series with data
            while (results.next()) {
                LocalDate date1 = results.getDate(1).toLocalDate();
                graph.getData().add(new XYChart.Data(results.getString(1), results.getDouble(4)));
                resultavg = connection.createStatement().executeQuery("SELECT zweihundert from " + ticker + " where day > '"+date1+ "';");
                while (resultavg.next()) {
                    mittelwert.getData().add(new XYChart.Data(results.getString(1), resultavg.getDouble(1)));
                }
            }

            lineChart.setCreateSymbols(false);

            Scene scene = new Scene(lineChart, 800, 450);
            lineChart.getData().add(graph);
            lineChart.getData().add(mittelwert);

            stage.setScene(scene);
            //stage.show();

            saveAsPNG(scene, path + "Bilder/" + LocalDate.now().toString()+"/" + ticker.toUpperCase() + ".png");
        }
        stage.show();
        stage.close();
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
        File dir = new File(path +"Bilder");
        File dir2 = new File(path + "Bilder/" +LocalDate.now());
        dir.mkdir();
        dir2.mkdir();
    }

    public static String buildURL(String url, String ticker, String key){
        return url = url + ticker.toUpperCase() + "&outputsize=full&apikey="+key;
    }
    public static ArrayList<String> loadTxt(String filename) {
        ArrayList<String> ticker = new ArrayList<String>();
        String path1 = path +filename + ".txt";
        File file = new File(path1);

        if (!file.canRead() || !file.isFile())
            System.exit(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(path1));
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
    public static void setUserdata(){
        List<String> userdates = new ArrayList<String>();
        userdates = loadTxt("UserDates");
        port = userdates.get(0);
        url = "jdbc:mysql://localhost:"+port+"/Aktienkurse?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        usernameDB = userdates.get(1);
        passwordDB = userdates.get(2);
        key = userdates.get(3);

    }

    public static boolean createTXT (String name){
        try {
            File myObj = new File(path +name + ".txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
                return true;
            } else {
                System.out.println("File already exists: "+name);
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
