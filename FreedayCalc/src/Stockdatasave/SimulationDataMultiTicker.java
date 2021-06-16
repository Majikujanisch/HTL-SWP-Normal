package Stockdatasave;

import java.util.ArrayList;
import java.util.List;

public class SimulationDataMultiTicker extends SimulationData{
    String ticker;
    SimulationData _200er, _2003er, BuyHold;


    public SimulationData get_200er() {
        return _200er;
    }

    public void set_200er(SimulationData _200er) {
        this._200er = _200er;
    }

    public SimulationData get_2003er() {
        return _2003er;
    }

    public void set_2003er(SimulationData _2003er) {
        this._2003er = _2003er;
    }

    public SimulationData getBuyHold() {
        return BuyHold;
    }

    public void setBuyHold(SimulationData buyHold) {
        BuyHold = buyHold;
    }

    public SimulationDataMultiTicker(String ticker, List<SimulationData> datas){
        this.ticker = ticker;
        this._200er = datas.get(0);
        this._2003er = datas.get(1);
        this.BuyHold = datas.get(2);
    }
}
