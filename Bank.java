import java.util.ArrayList;

class Bank {
    private final ArrayList<SavingsAccount> accounts = new ArrayList<>();
    private final Currency[] currencies = {
        new Currency("PHP", "Philippine Peso", 1.00),
        new Currency("USD", "United States Dollar", 62.00),
        new Currency("JPY", "Japanese Yen", 0.40),
        new Currency("GBP", "British Pound Sterling", 84.00),
        new Currency("EUR", "Euro", 72.00),
        new Currency("CNY", "Chinese Yuan Renminbi", 9.00)
    };

    public Currency[] getCurrencies() { 
        return currencies; 
    }

    public Currency getBaseCurrency() { 
        return currencies[0]; 
    } 

    public SavingsAccount findByName(String name) {
        for (SavingsAccount a : accounts) {
            if (a.getName().equalsIgnoreCase(name)) return a;
        }
        return null;
    }

    public boolean numberExists(String number) {
        for (SavingsAccount a : accounts) {
            if (a.getNumber().equals(number)) return true;
        }
        return false;
    }

    public SavingsAccount register(String name, String number) {
        SavingsAccount account = new SavingsAccount(name, number, getBaseCurrency());
        accounts.add(account);
        return account;
    }
}