package Stockdatasave;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.*;
import java.util.*;

public class Main { // key IVB25ADTVUERPRXD
    public static void main(String[] args) throws JSONException, MalformedURLException, IOException {
        String URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=", key = "IVB25ADTVUERPRXD";
        Scanner user = new Scanner(System.in);


        //URL Abfrage und zusammenbau
        System.out.println("Welchen Ticker wollen Sie abfragen?");
        String ticker = user.next();
        URL = buildURL(URL, ticker, key);
        System.out.println(URL);

        //Json abspeichern
        JSONObject json = new JSONObject(IOUtils.toString(new URL(URL), Charset.forName("UTF-8")));
        LocalDate date = LocalDate.parse((json.getJSONObject("Meta Data").get("3. Last Refreshed")).toString());
        json = json.getJSONObject("Time Series (Daily)");

        for(int i = 0; i < 200; i++){
            try {
                System.out.println(json.getJSONObject(date.toString()).get("4. close").toString());
            }
            catch(JSONException e){
                System.out.println("Nicht vorhanden");
            }

            date = date.minusDays(1);
        }
    }


    public static String buildURL(String url, String ticker, String key){
        return url = url + ticker.toUpperCase() + "&outputsize=full&apikey="+key;
    }
}
