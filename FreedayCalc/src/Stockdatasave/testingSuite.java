package Stockdatasave;


import com.mysql.cj.protocol.Resultset;
import org.json.JSONException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class testingSuite {
    static Connection connection;
    static String port;
    static String url;
    static String usernameDB;
    static String passwordDB;
    static String ticker = "ibm";
    static boolean percentTimerSet = false;
    static LocalTime starttime;
    final static String path = "FreedayCalc/src/Stockdatasave/";
//eingabe von nutzer, bei bestimmter parameter eingabe fehler ausgabe, auswertung der Strategien, 200er mit 3 Prozent
    public static void main(String[] args) {
        //List<LocalDate> date = new ArrayList<LocalDate>();
        LocalDate startdate = LocalDate.of(2010,1,1);
        LocalDate currentday = startdate ;
        int allDaysBetwStartNdToday = calcDaysFromPeriod(startdate.until(LocalDate.now())); //berechnungsmethode um alle tage zu erhalten
        double tempclose = 0;
        SimulationData data200 = new SimulationData(false, 0,100000.0);
        SimulationData dataBuyHold = new SimulationData(false,false,  0,100000.0);
        SimulationData data2003 = new SimulationData(false, 0, 100000.0);
        setUserdata();
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
            while (!currentday.isAfter(LocalDate.now())) {
                if (currentday.getDayOfWeek() != DayOfWeek.SATURDAY || currentday.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    ResultSet res;
                    double close, _200er, divident, splitcor;
                    res = ReadDataFromDB(currentday, ticker.toUpperCase());  //in stockdatasafe bei tableerstellung auch!
                    if (res.next()) {
                        close = res.getDouble("close");
                        _200er = res.getDouble("zweihundert");
                        divident = res.getDouble("divident");
                        splitcor = res.getDouble("splitcor");
                        tempclose = close;
                        if (divident != 1) {
                            data200.amount *= divident;
                        }
                        buyComparison(data200, dataBuyHold, splitcor, _200er, close, currentday);
                        buyComparison3Percent(data2003, splitcor, _200er, close, currentday);
                        sellComparison(data200, splitcor, _200er, close, currentday);
                        sellComparison3Percent(data2003, splitcor, _200er, close, currentday);
                        showPercentDone(startdate, currentday, allDaysBetwStartNdToday, 2);
                        System.out.println(data2003.amount);


                    }
                }
                currentday = currentday.plusDays(1);
            }
            System.out.println("[100%] done, completed Run");
            data200.lastsale(tempclose);
            data2003.lastsale(tempclose);
            dataBuyHold.lastsale(tempclose);
            disconnectMysql();
            compareData(data200, "200er");
            compareData(data2003, "200er mit 3%");
            compareData(dataBuyHold, "buy & hold");

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
            return connection.createStatement().executeQuery("select close, zweihundert, divident, splitcor from "+ticker+" where Day = '"+date+"';");
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
    public static SimulationData buyComparison(SimulationData data, SimulationData dataBH, double splitcor, double _200, double close, LocalDate date){
        if ((splitcor < _200 && !data.bought) || !dataBH.first){
            data.buyStocks(splitcor);
            dataBH.buyStocks(splitcor, dataBH.first);
            insertDataInDB(date, ticker, data.bought, data.amount, data.money);
        }
        return data;
    }
    public static SimulationData buyComparison3Percent(SimulationData data,double splitcor, double _200, double close, LocalDate date){
        double temp200;
        temp200 = _200 + ((_200/100)*3);
        if(splitcor < temp200 && data.bought){
            data.buyStocks(splitcor);
            insertDataInDB(date, ticker, data.bought, data.amount, data.money);
        }
        System.out.println(temp200);
        System.out.println(splitcor);
        return data;
    }
    public static SimulationData sellComparison(SimulationData data, double splitcor, double _200, double close, LocalDate date){
        if (splitcor > _200 && data.bought) {
            data.sellStocks(splitcor, _200);
            insertDataInDB(date, ticker, data.bought,  data.amount, data.money);
        }
        return data;
    }
    public static SimulationData sellComparison3Percent(SimulationData data, double splitcor, double _200, double close, LocalDate date){
        double temp200;
        temp200 = _200 - ((_200/100)*3);
        if (splitcor > temp200 && data.bought) {
            data.sellStocks(splitcor, _200);
            insertDataInDB(date, ticker, data.bought,  data.amount, data.money);
        }
        return data;
    }
    public static void compareData(SimulationData data, String strategy){
        double percent;
        percent = data.money/data.startmoney*100;
        System.out.println("Prozent mehr durch "+strategy+" Strategie: " + formateDouble(percent) + "%");
    }
    public static double calcpercentdone(LocalDate startdate, LocalDate currendate, double daysFromStartToNow){
        Period daysperiod;
        int daysuntil;
        daysperiod = startdate.until(currendate);
        daysuntil = calcDaysFromPeriod(daysperiod);
        return ((daysuntil/daysFromStartToNow)) * 100;
    }
    public static void showPercentDone(LocalDate startdate, LocalDate currendate, int daysFromStartToNow, int waitamount){

        if(!percentTimerSet){
            starttime = LocalTime.now();
            percentTimerSet = true;
        }
        if(LocalTime.now().isAfter(starttime.plusSeconds(waitamount))){
            System.out.println("["+ formateDouble(calcpercentdone(startdate, currendate, daysFromStartToNow))+"%] done");
            percentTimerSet = false;
        }

    }
    public static int calcDaysFromPeriod(Period duration){
        int allDays = 0;
        allDays = allDays + duration.getYears() * 356;
        allDays = allDays + duration.getMonths() * 30;
        allDays = allDays + duration.getDays();
        return allDays;

    }
    public static String formateDouble(double amount){
        DecimalFormat df2 = new DecimalFormat("#.##");
        return df2.format(amount);
    }
    public static LocalDate getStartDate(){
        Reader user = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("geben sie das gewünschte Datum ein JJJJ-MM-DD");

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
