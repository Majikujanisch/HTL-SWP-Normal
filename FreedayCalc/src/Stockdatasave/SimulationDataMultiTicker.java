package Stockdatasave;

import java.util.ArrayList;
import java.util.List;

public class SimulationDataMultiTicker extends SimulationData{
    String ticker;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }


    public SimulationDataMultiTicker(String ticker){
        this.ticker = ticker;
    }

    @Override
    public String toString() {
        return "SimulationDataMultiTicker{" +
                "ticker='" + ticker + '\'' +
                ", bought=" + bought +
                ", amount=" + amount +
                ", money=" + money +
                '}';
    }
}
