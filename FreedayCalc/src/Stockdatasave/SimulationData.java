package Stockdatasave;

public class SimulationData {
    boolean bought, first;
    int amount;
    double money, startmoney;

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public SimulationData(boolean bought, int amount, double money) {
        this.bought = bought;
        this.amount = amount;
        this.startmoney = money;
        this.money = this.startmoney;
        this.first = true;
    }
    public SimulationData(boolean bought,boolean first, int amount, double money) {
        this.bought = bought;
        this.amount = amount;
        this.startmoney = money;
        this.money = this.startmoney;
        this.first = false;
    }

    public void buyStocks (double close){
        for (int i = 0; money > close; i++) {
            this.amount = i;
            this.money = this.money - close;
        }
        this.bought = true;
    }
    public void buyStocks (double close, boolean bought){
        for (int i = 0; money > close; i++) {
            this.amount = i;
            this.money = this.money - close;
        }
        this.bought = true;
        this.first = true;
    }
    public void sellStocks (double close, double _200){
        this.money = close * this.amount;
        this.amount = 0;
        this.bought = false;
    }
    public void lastsale(double close){
        if(this.bought){
            this.money = close * this.amount;
            this.amount=0;
            this.bought = false;
        }
    }
}
