package sample;
import java.sql.*;
public class DBManager {
    Connection connection;

    public DBManager() {

    }

    public boolean connectToMysql(String host, String database, String user, String passwd){
        try{
            Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
            String connectionCommand = "jdbc:mysql://"+host+"/"+database+"?user="+user+"&password="+passwd;
            connection = DriverManager.getConnection(connectionCommand);
            return true;

        }catch (Exception ex){
            System.out.println("false");
            return false;
        }
    }
}


