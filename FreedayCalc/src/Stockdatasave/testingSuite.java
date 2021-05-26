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
import java.util.Scanner;

public class testingSuite {
    static Connection connection;
    static String port;
    static String url;
    static String usernameDB;
    static String passwordDB;
    static String ticker = "aapl";
    static boolean percentTimerSet = false;
    static LocalTime starttime;
    final static String path = "FreedayCalc/src/Stockdatasave/";
//eingabe von nutzer, bei bestimmter parameter eingabe fehler ausgabe, auswertung der Strategien, 200er mit 3 Prozent
    public static void main(String[] args) {
        LocalDate startdate = switchStartdate();
        LocalDate currentday = startdate ;
        int startmoney = switchStartMoney();
        int allDaysBetwStartNdToday = calcDaysFromPeriod(startdate.until(LocalDate.now())); //berechnungsmethode um alle tage zu erhalten
        double tempsplitcor = 0;
        SimulationData data200 = new SimulationData(false, 0,startmoney);
        SimulationData dataBuyHold = new SimulationData(false,  0,startmoney);
        SimulationData data2003 = new SimulationData(false, 0, startmoney);
        setUserdata();
        try{
            connectToMysql();
            createTableMysql();
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
                        tempsplitcor = splitcor;
                        if (divident != 1) {
                            dividendAnwenden(data200, divident);
                            dividendAnwenden(data2003, divident);
                            dividendAnwenden(dataBuyHold, divident);
                        }
                        buySellBlock(data200, dataBuyHold, data2003, splitcor, _200er, close, currentday,
                                startdate, allDaysBetwStartNdToday,2);
                    }
                }
                currentday = currentday.plusDays(1);
            }
            disconnectMysql();
            showResults(data200, data2003, dataBuyHold, tempsplitcor);
            System.out.println(data200.money);
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
        if (((splitcor > _200 && !data.bought) || !dataBH.first)){
            if((splitcor > _200 && !data.bought)){
                 data.buyStocks(splitcor);

                insertDataInDB(date, ticker, data.bought, data.amount, data.money);
            }
            if(!dataBH.first)  {
               dataBH.buyStocks(splitcor);
            }


        }
        return data;
    }
    public static SimulationData buyComparison3Percent(SimulationData data,double splitcor, double _200, double close, LocalDate date){
        double temp200;
        temp200 = _200 * 1.03;
        if(splitcor > temp200 && !data.bought){
            data.buyStocks(splitcor);
        }
        return data;
    }
    public static SimulationData sellComparison(SimulationData data, double splitcor, double _200, double close, LocalDate date){
        if (splitcor < _200 && data.bought) {
            data.sellStocks(splitcor, _200);

            insertDataInDB(date, ticker, data.bought,  data.amount, data.money);
        }
        return data;
    }
    public static SimulationData sellComparison3Percent(SimulationData data, double splitcor, double _200, double close, LocalDate date){
        double temp200;
        temp200 = _200*1.03;
        if (splitcor < temp200 && data.bought) {
            data.sellStocks(splitcor, _200);
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
       Scanner user = new Scanner(System.in);
       String date;
       String year = "", month = "", day = "";
       System.out.println("geben sie das gewünschte Datum ein JJJJ-MM-DD, keine Freizeichen!");
       date = user.next();
       for(int i = 0; date.length() > i; i++){
           if(i < 4){
               year = year + date.charAt(i);
           }
           if(i > 4 && i< 7){
               month = month + date.charAt(i);
           }
           if(i > 7 && i<10){
               day = day + date.charAt(i);
           }

       }
       LocalDate startdate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month),Integer.parseInt(day));
       System.out.println(startdate);

       return startdate ;

    }
    public static int getStartMoney(){
        Scanner user = new Scanner(System.in);
        System.out.println("geben sie den gewünschten Geldbetrag ein:");
        int money = user.nextInt();

        return money;

    }
    public static int switchStartMoney(){
        boolean rightinput = false;
        char inputtype;
        int startmoney = 100000;
        while (!rightinput){
            Scanner scan = new Scanner(System.in);
            System.out.println("Geld selber eingeben [A] oder Standard-Betrag [B](100.000)");
            inputtype = scan.next().toUpperCase().charAt(0);
            switch (inputtype){
                case('A'):
                    rightinput = true;
                    startmoney = getStartMoney();
                    break;
                case('B'):
                    rightinput = true;
                    break;
                default:
                    System.out.println("Falsch Eingabe, Wrong Input!");
                    break;
            }
        }
        return startmoney;
    }
    public static void buySellBlock(SimulationData data200, SimulationData dataBuyHold, SimulationData data2003,
                                    double splitcor, double _200er, double close, LocalDate currentday,
                                    LocalDate startdate, int allDaysBetwStartNdToday, int waitamount){
        buyComparison(data200, dataBuyHold, splitcor, _200er, close, currentday);
        buyComparison3Percent(data2003, splitcor, _200er, close, currentday);
        sellComparison(data200, splitcor, _200er, close, currentday);
        sellComparison3Percent(data2003, splitcor, _200er, close, currentday);
        showPercentDone(startdate, currentday, allDaysBetwStartNdToday, waitamount);
    }
    public static LocalDate switchStartdate(){
        boolean rightinput = false;
        char inputtype;
        LocalDate startdate = LocalDate.of(2010, 1, 1);;
        while (!rightinput){
            Scanner scan = new Scanner(System.in);
            System.out.println("Eigenes Datum eingeben [A] oder Standard-Datum [B](2010-01-01)");
            inputtype = scan.next().toUpperCase().charAt(0);
            switch (inputtype){
                case('A'):
                    rightinput = true;
                    startdate = getStartDate();
                break;
                case('B'):
                    rightinput = true;
                    break;
                default:
                    System.out.println("Falsch Eingabe, Wrong Input!");
                    break;
            }
        }
        return startdate;
    }
    public static SimulationData dividendAnwenden(SimulationData data, double dividend){
        data.amount *= dividend;
        return data;
    }
    public static void showResults(SimulationData data200, SimulationData data2003, SimulationData dataBH,
                                   double tempclose){
        System.out.println("[100%] done, completed Run");
        data200.lastsale(tempclose);
        data2003.lastsale(tempclose);
        dataBH.lastsale(tempclose);
        compareData(data200, "200er");
        compareData(data2003, "200er mit 3%");
        compareData(dataBH, "buy & hold");
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
