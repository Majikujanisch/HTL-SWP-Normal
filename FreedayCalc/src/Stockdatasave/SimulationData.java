package Stockdatasave;

public class SimulationData {
    boolean bought;
    int amount;
    double money;

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
        this.money = money;
    }

    public void buyStocks (double close, double _200){
        for (int i = 0; money > close; i++) {
            this.amount = i;
            this.bought = true;
            this.money = this.money - close;

        }
    }
    public void sellStocks (double close, double _200){
        this.money = close * this.amount;
        this.amount = 0;
        this.bought = false;
    }
}
