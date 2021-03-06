package Stockdatasave._25_05_21;

public class SimulationData250521 {
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

    public SimulationData250521(boolean bought, int amount, double money) {
        this.bought = bought;
        this.amount = amount;
        this.startmoney = money;
        this.money = this.startmoney;
        this.first = false;
    }

    public void buyStocks (double close){
       // for (int i = 0; money > close; i++) {
       //     this.amount = i;
       //     this.money -= close;
       // }

        this.amount = (int) (money / close);
        this.money = this.money - this.amount * close;
        this.bought = true;
        this.first = true;
    }
    public void sellStocks (double close, double _200){
        this.money = close * this.amount;
        this.amount = 0;
        this.bought = false;
    }
    public void lastsale(double splitcor){
        if(this.bought){
            this.money = splitcor * this.amount;
            this.amount=0;
            this.bought = false;
        }
    }
}
