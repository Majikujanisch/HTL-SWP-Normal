package Stockdatasave;


import com.mysql.cj.protocol.Resultset;
import org.json.JSONException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class testingSuite {
    static Connection connection;
    static String port;
    static String url;
    static String usernameDB;
    static String passwordDB;
    static String ticker = "tsla";
    final static String path = "FreedayCalc/src/Stockdatasave/";

    public static void main(String[] args) {
        //List<LocalDate> date = new ArrayList<LocalDate>();
        LocalDate currentday = LocalDate.of(2010,1,1);  // tempdate or second variable
        setUserdata();
        int anzahl = 0;
        boolean bought = false;
        double money = 100000;
        try{
            connectToMysql();
            createTableMysql();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try{
            //JSONObject json = new JSONObject((IOUtils.toString(new URL("https://date.nager.at/api/v2/publicholidays/2017/US"), Charset.forName("UTF-8"))));
            //if(!json.isEmpty()) {
            //    date.add(LocalDate.parse((json.get("date")).toString()));

            //}
            //System.out.println(date);



            while (currentday.isAfter(LocalDate.now())) {
                if (currentday.getDayOfWeek() != DayOfWeek.SATURDAY || currentday.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    ResultSet res;
                    double close, _200er, divident;
                    res = ReadDataFromDB(currentday, ticker.toUpperCase());  //in stockdatasafe bei tableerstellung auch!
                    if (res.next()) {
                        close = res.getDouble(1);
                        _200er = res.getDouble(2);
                        divident = res.getDouble(3);
                        if (divident != 1) {
                            anzahl *= divident;
                        }
                        if (close > _200er && !bought) {
                            for (int i = 0; money > close; i++) {
                                anzahl = i;
                                bought = true;
                                money = money - close;

                            }
                            insertDataInDB(currentday, ticker, bought, anzahl, money);
                            System.out.println("bought");
                            System.out.println(anzahl);
                        }
                        if (close < _200er && bought) {
                            money = close * anzahl;
                            anzahl = 0;
                            bought = false;
                            insertDataInDB(currentday, ticker, bought, anzahl, money);
                            System.out.println("sold");
                            System.out.println(money);
                        }

                    }

                }
                currentday = currentday.plusDays(1);
                System.out.println(currentday);

            }

        }
        catch (Exception e){
            e.printStackTrace();
        }



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

    public static void createTableMysql(){
        try{
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            connection.createStatement().executeUpdate("create table if not exists testing200(" +
                    " Day date DEFAULT (CURRENT_DATE + INTERVAL 1 YEAR)," +
                    " ticker varchar(10)," +
                    " flag bool," +
                    " stücke int," +
                    " depot double," +
                    " primary key(Day, ticker));");

        }catch(Exception e){

            System.out.println("false1");
        }
    }
    public static ResultSet ReadDataFromDB(LocalDate date, String ticker){
        try{
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            return connection.createStatement().executeQuery("select close, zweihundert, divident from "+ticker+" where Day = '"+date+"';");
        }catch(Exception e){
            System.out.println("No Select");
        }
        return null;
    }
    public static void insertDataInDB(LocalDate date, String ticker, boolean flag, int anzahl, double money){
        int flagint;
        if(flag){
            flagint = 1;
        }
        else{
            flagint = 0;
        }
        try{
            connection = DriverManager.getConnection(url, usernameDB, passwordDB);
            connection.createStatement().executeUpdate("insert into testing200 values ('" + date + "','"+ticker+"','"+flagint+"','"+anzahl+"','"+money+"') on Duplicate key update depot='"+money+"';");
        }catch(Exception e){
            System.out.println("NOinsert");
            e.printStackTrace();
        }

    }

    public static void setUserdata() {
        List<String> userdates = new ArrayList<String>();
        userdates = loadTxt("UserDates");
        port = userdates.get(0);
        url = "jdbc:mysql://localhost:"+port+"/Aktienkurse?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        usernameDB = userdates.get(1);
        passwordDB = userdates.get(2);

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



}
