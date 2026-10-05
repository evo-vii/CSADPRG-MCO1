class SavingsAccount extends Account {
    private static final double annualRate = 0.05; 

    public SavingsAccount(String name, String number, Currency currency) {
        super(name, number, currency);
    }

    public double getAnnualRate() { 
        return annualRate; 
    }

    public double getDailyRate() { 
        return annualRate / 365; 
    }

    public double[][] projectDaily(int days) {
        double[][] rows = new double[days][3];
        double balance = getBalance();
        for (int day = 1; day <= days; day++) {
            double interest = balance * getDailyRate();
            balance += interest;
            rows[day - 1] = new double[] { day, interest, balance };
        }
        return rows;
    }

    public double[][] projectAnnual(int years) {
        double[][] rows = new double[years][3];
        double balance = getBalance();
        for (int year = 1; year <= years; year++) {
            double interest = balance * annualRate;
            balance += interest;
            rows[year - 1] = new double[] { year, interest, balance };
        }
        return rows;
    }
}