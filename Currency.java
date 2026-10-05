class Currency {
    private final String code;
    private final String name;
    private double rate; 

    public Currency(String code, String name, double rate) {
        this.code = code;
        this.name = name;
        this.rate = rate;
    }

    public String getCode() { 
        return code; 
    }

    public String getName() { 
        return name; 
    }

    public double getRate() { 
        return rate; 
    }

    public boolean setRate(double newRate) {
        if (newRate <= 0) return false;
        this.rate = newRate;
        return true;
    }
}