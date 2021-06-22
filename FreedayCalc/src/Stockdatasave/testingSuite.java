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
    static int multipercent = 0;
    static boolean percentTimerSet = false;
    static LocalTime starttime;
    final static String path = "FreedayCalc/src/Stockdatasave/";
//eingabe von nutzer, bei bestimmter parameter eingabe fehler ausgabe, auswertung der Strategien, 200er mit 3 Prozent
    public static void main(String[] args) {
        createTXT("testingSuite");
        switchMultOrSingle();

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
            connection.createStatement().executeUpdate("create table if not exists testing2003(" +
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
            connection.createStatement().executeUpdate("insert into testing200 values ('" + date + "','"+ticker+"','"+flagint+"','"+anzahl+"','"+money+"') on Duplicate key update depot='"+money+"';");
        }catch(Exception e){
            System.out.println("NOinsert");
            e.printStackTrace();
        }

    }
    public static void insertDataInDB2003(LocalDate date, String ticker, boolean flag, int anzahl, double money){
        int flagint;
        if(flag){
            flagint = 1;
        }
        else{
            flagint = 0;
        }
        try{
            connection.createStatement().executeUpdate("insert into testing2003 values ('" + date + "','"+ticker+"','"+flagint+"','"+anzahl+"','"+money+"') on Duplicate key update depot='"+money+"';");
        }catch(Exception e){
            System.out.println("NOinsert");
            e.printStackTrace();
        }

    }
    public static SimulationData buyComparison(SimulationData data, SimulationData dataBH, double splitcor, double _200, double close, LocalDate date, String ticker){
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
    public static SimulationData buyComparison3Percent(SimulationData data,double splitcor, double _200, double close, LocalDate date, String ticker){
        double temp200;
        temp200 = _200 * 1.03;
        if(splitcor > temp200 && !data.bought){
            data.buyStocks(splitcor);

            insertDataInDB2003(date, ticker, data.bought,  data.amount, data.money);
        }
        return data;
    }
    public static SimulationData sellComparison(SimulationData data, double splitcor, double _200, double close, LocalDate date, String ticker){
        if (splitcor < _200 && data.bought) {
            data.sellStocks(splitcor, _200);

            insertDataInDB(date, ticker, data.bought,  data.amount, data.money);
        }
        return data;
    }
    public static SimulationData sellComparison3Percent(SimulationData data, double splitcor, double _200, double close, LocalDate date, String ticker){
        double temp200;
        temp200 = _200*1.03;
        if (splitcor < temp200 && data.bought) {
            data.sellStocks(splitcor, _200);

            insertDataInDB2003(date, ticker, data.bought,  data.amount, data.money);
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
    public static void showPercentDone(LocalDate startdate, LocalDate currendate, int daysFromStartToNow, int waitamount, boolean multi, int tickercount){

        if(!percentTimerSet){
            starttime = LocalTime.now();
            percentTimerSet = true;
        }
        if(LocalTime.now().isAfter(starttime.plusSeconds(waitamount))){
            if(multi){
                System.out.println("["+ formateDouble((calcpercentdone(startdate, currendate, daysFromStartToNow)/tickercount) + (multipercent * 20))+"%] done");
            }
            else{
                System.out.println("["+ formateDouble(calcpercentdone(startdate, currendate, daysFromStartToNow))+"%] done");
            }
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
    public static String switchStartTicker(){
        boolean rightinput = false;
        char inputtype;
        String ticker = "TSLA";
        while (!rightinput){
            Scanner scan = new Scanner(System.in);
            System.out.println("Ticker selber eingeben [A] oder Standard-Ticker [B](TSLA)");
            inputtype = scan.next().toUpperCase().charAt(0);
            switch (inputtype){
                case('A'):
                    rightinput = true;
                    ticker = getStartTicker();
                    break;
                case('B'):
                    rightinput = true;
                    break;
                default:
                    System.out.println("Falsch Eingabe, Wrong Input!");
                    break;
            }
        }
        return ticker;
    }
    public static String getStartTicker(){
        Scanner user = new Scanner(System.in);
        System.out.println("geben sie den gewünschten Geldbetrag ein:");
        String ticker = user.next().toUpperCase();

        return ticker;

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
    public static void switchMultOrSingle(){
        boolean rightinput = false;
        char inputtype;
        while (!rightinput){
            Scanner scan = new Scanner(System.in);
            System.out.println("Einen Ticker [A] eingeben oder mehrere Ticker [B] aus Text Datei auslesen?");
            inputtype = scan.next().toUpperCase().charAt(0);


            LocalDate startdate = switchStartdate();
            LocalDate currentday = startdate ;
            int startmoney = switchStartMoney();
            int allDaysBetwStartNdToday = calcDaysFromPeriod(startdate.until(LocalDate.now())); //berechnungsmethode um alle tage zu erhalten
            double tempsplitcor = 0;
            if(inputtype == 'A'){
            }
            switch (inputtype){
                case('A'):
                    rightinput = true;


                    String ticker = switchStartTicker();
                    SimulationData data200 = new SimulationData(false, 0,startmoney, ticker);
                    SimulationData dataBuyHold = new SimulationData(false,  0,startmoney, ticker);
                    SimulationData data2003 = new SimulationData(false, 0, startmoney, ticker);
                    setUserdata();
                    try{
                        connectToMysql();
                        createTableMysql();
                        while (!currentday.isAfter(LocalDate.now())) {
                            if (currentday.getDayOfWeek() != DayOfWeek.SATURDAY || currentday.getDayOfWeek() != DayOfWeek.SUNDAY) {
                                ResultSet res;
                                double close, _200er, splitcor;
                                res = ReadDataFromDB(currentday, ticker.toUpperCase());  //in stockdatasafe bei tableerstellung auch!
                                if (res.next()) {
                                    close = res.getDouble("close");
                                    _200er = res.getDouble("zweihundert");
                                    splitcor = res.getDouble("splitcor");
                                    tempsplitcor = splitcor;
                                    buySellBlock(data200, dataBuyHold, data2003, splitcor, _200er, close, currentday,
                                            startdate, allDaysBetwStartNdToday,2, ticker, false, 1);
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
                    break;
                case('B'):
                    rightinput = true;;

                    List<String> TickerList = new ArrayList<String>();
                    TickerList = loadTxt("testingSuite");
                    List<SimulationData> dataList = new ArrayList<>();
                    String tic = "TSLA";


                    setUserdata();
                    try{
                        connectToMysql();
                        createTableMysql();
                        int tickerindex = 0;
                        for (String t : TickerList) {
                            System.out.println(t);
                            currentday = startdate ;
                            data200 = new SimulationData(false, 0,startmoney/TickerList.size(), t);
                            dataBuyHold = new SimulationData(false, 0,startmoney/TickerList.size(), t);
                            data2003 = new SimulationData(false, 0,startmoney/TickerList.size(), t);
                            while (!currentday.isAfter(LocalDate.now())) {
                                if (currentday.getDayOfWeek() != DayOfWeek.SATURDAY || currentday.getDayOfWeek() != DayOfWeek.SUNDAY) {
                                    ResultSet res;
                                    double close, _200er, splitcor;
                                    res = ReadDataFromDB(currentday, t.toUpperCase());

                                        //in stockdatasafe bei tableerstellung auch!
                                    if(res!=null) {
                                        if (res.next()) {
                                            close = res.getDouble("close");
                                            _200er = res.getDouble("zweihundert");
                                            splitcor = res.getDouble("splitcor");
                                            tempsplitcor = splitcor;
                                            buySellBlock(data200, dataBuyHold, data2003, splitcor, _200er, close, currentday,
                                                    startdate, allDaysBetwStartNdToday, 2, t, false, 1);
                                        }
                                    }

                                }
                                currentday = currentday.plusDays(1);
                            }
                            data200.lastsale(tempsplitcor);
                            data2003.lastsale(tempsplitcor);
                            dataBuyHold.lastsale(tempsplitcor);
                            dataList.add(tickerindex, data200);
                            dataList.add(tickerindex+1, data2003);
                            dataList.add(tickerindex + 2, dataBuyHold);
                            tickerindex =tickerindex+3;
                            multipercent++;
                        }
                        disconnectMysql();
                        showResultsmulti(dataList,TickerList, startmoney);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("Falsch Eingabe, Wrong Input!");
                    break;
            }
        }
    }
    public static void buySellBlock(SimulationData data200, SimulationData dataBuyHold, SimulationData data2003,
                                    double splitcor, double _200er, double close, LocalDate currentday,
                                    LocalDate startdate, int allDaysBetwStartNdToday, int waitamount, String ticker,
                                    boolean multi, int tickercount){
        buyComparison(data200, dataBuyHold, splitcor, _200er, close, currentday, ticker);
        buyComparison3Percent(data2003, splitcor, _200er, close, currentday, ticker);
        sellComparison(data200, splitcor, _200er, close, currentday, ticker);
        sellComparison3Percent(data2003, splitcor, _200er, close, currentday, ticker);
        if(multi){
            showPercentDone(startdate, currentday, allDaysBetwStartNdToday, waitamount, true, tickercount);
        }
        else{
            showPercentDone(startdate, currentday, allDaysBetwStartNdToday, waitamount, false, 1);
        }


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
    public static void showResultsmulti(List<SimulationData> dataList, List<String> ticker, double startmoney){
        System.out.println("[100%] done, completed Run");
        int i = 0;
        double endmoney200 = 0, endmoney2003 = 0, endmoneyBH = 0;
        for(String t: ticker){
            System.out.println(t);
            compareData(dataList.get(i), "200er");
            compareData(dataList.get(i+1), "200er mit 3%");
            compareData(dataList.get(i+2), "buy & hold");
            endmoney200 = endmoney200 + dataList.get(i).getMoney();
            endmoney2003 = endmoney2003 + dataList.get(i+1).getMoney();
            endmoneyBH = endmoneyBH + dataList.get(i+2).getMoney();
            System.out.println("Prozent mehr durch 200er Strategie: "+(endmoney200/startmoney)*100+"%");
            System.out.println("Prozent mehr durch 200er mit 3% Strategie: "+endmoney2003/startmoney * 100+"%");
            System.out.println("Prozent mehr durch 2buy & hold Strategie: "+endmoneyBH/startmoney * 100+"%");

            i = i + 3;
        }
        System.out.println("endgeld 200: " + endmoney200);
        System.out.println("endgeld 200 mit 3%: " + endmoney2003);
        System.out.println("endgeld Buy and hold: " + endmoneyBH);
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



}
