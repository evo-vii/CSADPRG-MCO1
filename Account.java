class Account {
    private final String name;
    private final String number;
    private double balance;
    private Currency currency;

    public Account(String name, String number, Currency currency) {
        this.name = name;
        this.number = number;
        this.currency = currency;
        this.balance = 0.0;
    }

    public String getName() { 
        return name; 
    }

    public String getNumber() { 
        return number; 
    }

    public double getBalance() { 
        return balance; 
    }

    public Currency getCurrency() { 
        return currency; 
    }

    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        balance = round(balance + amount);
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) return false;
        balance = round(balance - amount);
        return true;
    }

    public void exchangeTo(Currency target) {
        double inPhp = balance * currency.getRate();
        balance = round(inPhp / target.getRate());
        currency = target;
    }

    public double valueIn(Currency target) {
        return round(balance * currency.getRate() / target.getRate());
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}