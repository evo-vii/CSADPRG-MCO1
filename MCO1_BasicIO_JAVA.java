import java.util.Scanner;
import java.util.ArrayList;

public class MCO1_BasicIO_JAVA {
    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            Display.showMainMenu();
            int choice = BankFunction.readInt("Choose Option: ", 1, 7);

            System.out.println();
            System.out.println("=================================");

            switch (choice) {
                case 1:
                    System.out.println("Going to REGISTER...\n");
                    BankFunction.registerAccount();
                    break;
                case 2:
                    System.out.println("Going to DEPOSIT...\n");
                    BankFunction.depositAmount();
                    break;
                case 3:
                    System.out.println("Going to WITHDRAW...\n");
                    BankFunction.withdrawAmount();
                    break;
                case 4:
                    System.out.println("Going to CURRENCY...\n");
                    BankFunction.currencyExchange();
                    break;
                case 5:
                    System.out.println("Going to RECORD...\n");
                    BankFunction.recordExchangeRate();
                    break;
                case 6:
                    System.out.println("Going to INTEREST...\n");
                    BankFunction.computeInterest();
                    break;
                case 7:
                    System.out.println("Going to EXIT...\n");
                    System.out.println("Thank you for using Banking and Currency Exchange!");
                    running = false;
                    break;
            }
        }
        BankFunction.closeInput();
    }
}

public class BankFunction {
    private static final Bank bank = new Bank();
    private static final Scanner input = new Scanner(System.in);

    public static void closeInput() {
        input.close();
    }

    public static void registerAccount() {
        Display.printHeader("REGISTER ACCOUNT DETAILS");
        System.out.println();

        String name = readAccountName("Account Name: ");
        String number = readAccountNumber("Account Number: ");

        System.out.println();
        Display.printFooter();
        System.out.println();

        SavingsAccount account = bank.register(name, number);

        System.out.println("ACCOUNT REGISTERED ....");
        System.out.println("Account Name: " + account.getName());
        System.out.println("Account Number: " + account.getNumber());

        pressEnter();
    }

    public static void depositAmount() {
        Display.printHeader("DEPOSIT AMOUNT");
        System.out.println();

        SavingsAccount account = askForExistingAccount();
        if (account == null) return;

        showAccountSummary(account);

        double amount = readAmount("Deposit Amount: ", -1);
        account.deposit(amount);

        System.out.println();
        Display.printFooter();
        System.out.println();
        System.out.println("AMOUNT DEPOSITED ....");
        System.out.println("Account Name: " + account.getName());
        System.out.println("Account Number: " + account.getNumber());
        System.out.println("Current Balance: " + Display.money(account.getBalance())
                + " " + account.getCurrency().getCode());

        pressEnter();
    }

    public static void withdrawAmount() {
        Display.printHeader("WITHDRAW AMOUNT");
        System.out.println();

        SavingsAccount account = askForExistingAccount();
        if (account == null) return;

        showAccountSummary(account);

        double amount = readAmount("Withdraw Amount: ", account.getBalance());
        account.withdraw(amount);

        System.out.println();
        Display.printFooter();
        System.out.println();
        System.out.println("AMOUNT WITHDRAWN ....");
        System.out.println("Account Name: " + account.getName());
        System.out.println("Account Number: " + account.getNumber());
        System.out.println("Current Balance: " + Display.money(account.getBalance())
                + " " + account.getCurrency().getCode());

        pressEnter();
    }

    public static void currencyExchange() {
        Display.printHeader("CURRENCY EXCHANGE");
        System.out.println();

        SavingsAccount account = askForExistingAccount();
        if (account == null) return;

        Currency[] currencies = bank.getCurrencies();
        Currency source = account.getCurrency();
        double sourceAmount = account.getBalance();

        System.out.println("Current Balance (" + source.getCode() + "): " + Display.money(sourceAmount));
        System.out.println();
        System.out.println("Exchanged Currency");
        for (int i = 0; i < currencies.length; i++) {
            System.out.println("[" + (i + 1) + "] " + currencies[i].getName()
                    + " (" + currencies[i].getCode() + ") = "
                    + Display.money(account.valueIn(currencies[i])));
        }

        System.out.println();
        Display.printFooter();
        System.out.println();

        int option = readInt("Select Currency to Exchange To: ", 1, currencies.length);
        Currency target = currencies[option - 1];

        if (target == source) {
            System.out.println();
            System.out.println("Your balance is already in " + target.getName()
                    + " (" + target.getCode() + "). Nothing changed.");
            pressEnter();
            return;
        }

        account.exchangeTo(target);

        System.out.println();
        System.out.println("Source Currency = " + source.getName() + " (" + source.getCode() + ")");
        System.out.println("Source Amount = " + Display.money(sourceAmount));
        System.out.println("Target Currency = " + target.getName() + " (" + target.getCode() + ")");
        System.out.println("Target Amount = " + Display.money(account.getBalance()));
        System.out.println();
        System.out.println("CURRENCY EXCHANGED ....");
        System.out.println("Current Balance: " + Display.money(account.getBalance())
                + " " + account.getCurrency().getCode());

        pressEnter();
    }
 
    public static void recordExchangeRate() {
        Display.printHeader("RECORD EXCHANGE RATE");

        Currency[] currencies = bank.getCurrencies();
        for (int i = 0; i < currencies.length; i++) {
            System.out.println("[" + (i + 1) + "] " + currencies[i].getName()
                    + " (" + currencies[i].getCode() + ")");
        }
        System.out.println();

        int option = readInt("Select Currency: ", 1, currencies.length);
        Currency selected = currencies[option - 1];

        if (selected == bank.getBaseCurrency()) {
            System.out.println();
            System.out.println("PHP is the base currency, its rate is always 1.00.");
            pressEnter();
            return;
        }

        System.out.println("(Rate = how many PHP equal 1 " + selected.getCode() + ")");
        double rate = readRate("Exchange Rate: ");
        selected.setRate(rate);

        System.out.println();
        Display.printFooter();
        System.out.println();
        System.out.println("CURRENCY RATE ...");
        System.out.println("(" + selected.getCode() + ") Exchange Rate: " + Display.money(selected.getRate()));

        pressEnter();
    }
 
    public static void computeInterest() {
        Display.printHeader("INTEREST");
        System.out.println();

        SavingsAccount account = askForExistingAccount();
        if (account == null) {
            return;
        }

        System.out.println("\n[1] ANNUAL INTEREST");
        System.out.println("[2] DAILY INTEREST");
        System.out.println();

        int option = readInt("Choose Option: ", 1, 2);

        System.out.println();
        System.out.println("=========================================");
        System.out.println();

        if (option == 1) {
            System.out.println("Annual Interest Rate: " + (account.getAnnualRate() * 100) + "% per annum");
            int years = readInt("Number of Years: ", 1, 100);
            System.out.println();
            Display.printHeader("ANNUAL INTEREST");
            System.out.println();
            Display.printInterestTable("YEAR", account.projectAnnual(years));
        } else {
            System.out.println("Annual Interest Rate: " + (account.getAnnualRate() * 100) + "% per annum");
            int days = readInt("Number of Days: ", 1, 3650);
            System.out.println();
            Display.printHeader("DAILY INTEREST");
            System.out.println();
            Display.printInterestTable("DAY", account.projectDaily(days));
        }

        pressEnter();
    }
    private static void showAccountSummary(Account account) {
        System.out.println("\nAccount Name: " + account.getName());
        System.out.println("Current Balance: " + Display.money(account.getBalance()));
        System.out.println("Currency: " + account.getCurrency().getCode());
        System.out.println();
    }

    
    private static void pressEnter() {
        System.out.println();
        System.out.print("Press ENTER to go back to the main menu...");
        input.nextLine();
    }
 
    private static String readLine(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }

    private static String readAccountName(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value.matches("[A-Za-z. ]+") && value.matches(".*[A-Za-z].*")) {
                if (bank.findByName(value) == null) return value;
                System.out.println("That account name is already registered. Try again.");
            } else {
                System.out.println("Invalid name. Use letters, spaces and periods only.");
            }
        }
    }

    private static String readAccountNumber(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value.matches("\\d{7}")) {
                if (!bank.numberExists(value)) return value;
                System.out.println("That account number is already in use. Try again.");
            } else {
                System.out.println("Invalid number. Enter exactly 7 digits.");
            }
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            if (value.matches("\\d{1,9}")) {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) return number;
            }
            System.out.println("Invalid input. Enter a number from " + min + " to " + max + ".");
        }
    }
 
    private static double readAmount(String prompt, double max) {
        while (true) {
            String value = readLine(prompt);
            if (value.matches("\\d{1,12}(\\.\\d{1,2})?")) {
                double amount = Double.parseDouble(value);
                if (amount <= 0) {
                    System.out.println("Amount must be greater than zero.");
                } else if (max >= 0 && amount > max) {
                    System.out.println("Insufficient balance. You can withdraw up to " + Display.money(max) + ".");
                } else {
                    return amount;
                }
            } else {
                System.out.println("Invalid input. Enter numbers only (e.g. 1000 or 1000.50).");
            }
        }
    }

    private static double readRate(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value.matches("\\d{1,9}(\\.\\d{1,6})?") && Double.parseDouble(value) > 0) {
                return Double.parseDouble(value);
            }
            System.out.println("Invalid input. Enter a number greater than zero.");
        }
    }

    private static SavingsAccount askForExistingAccount() {
        String name = readLine("Account Name: ");
        SavingsAccount account = bank.findByName(name);

        if (account != null) {
            return account;
        }
        
        System.out.println();
        System.out.println("Account is not yet registered.");
        System.out.println("[1] Register an account");
        System.out.println("[2] Go Back to Main Menu");
        int choice = readInt("Choose Option: ", 1, 2);

        if (choice == 1) {
            System.out.println();
            System.out.println("=================================");
            System.out.println("Going to REGISTER...");
            registerAccount();
        }
        return null; 
    }
}

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

public class Display {
    
    public static void showMainMenu() {
        System.out.println();
        System.out.println("|-------------------------------|");
        System.out.println("| BANKING AND CURRENCY EXCHANGE |");
        System.out.println("|-------------------------------|");
        System.out.println("| [1] REGISTER ACCOUNT          |");
        System.out.println("| [2] DEPOSIT AMOUNT            |");
        System.out.println("| [3] WITHDRAW AMOUNT           |");
        System.out.println("| [4] CURRENCY EXCHANGE         |");
        System.out.println("| [5] RECORD RATE               |");
        System.out.println("| [6] INTEREST                  |");
        System.out.println("| [7] EXIT                      |");
        System.out.println("|-------------------------------|");
        System.out.println();
    }

    public static void printHeader(String title) {
        int width = 40;
        int left = (width - title.length()) / 2;
        int right = width - title.length() - left;
        String bar = "|" + "-".repeat(width) + "|";
        System.out.println(bar);
        System.out.println("|" + " ".repeat(left) + title + " ".repeat(right) + "|");
        System.out.println(bar);
    }

    public static void printFooter() {
        System.out.println("|" + "-".repeat(40) + "|");
    }

    public static void printInterestTable(String periodLabel, double[][] rows) {
        String[] heads = { periodLabel, "INTEREST", "BALANCE" };
        int[] widths = { heads[0].length(), heads[1].length(), heads[2].length() };
        String[][] cells = new String[rows.length][3];

        for (int i = 0; i < rows.length; i++) {
            cells[i][0] = String.valueOf((int) rows[i][0]);
            cells[i][1] = money(rows[i][1]);
            cells[i][2] = money(rows[i][2]);
            for (int c = 0; c < 3; c++) {
                widths[c] = Math.max(widths[c], cells[i][c].length());
            }
        }

        printTableRow(heads, widths);
        for (String[] row : cells) {
            printTableRow(row, widths);
        }

    }

    private static void printTableRow(String[] values, int[] widths) {
        StringBuilder line = new StringBuilder("|");
        for (int c = 0; c < values.length; c++) {
            line.append(" ").append(String.format("%-" + widths[c] + "s", values[c])).append(" |");
        }
        System.out.println(line);
    }

    public static String money(double value) {
        return String.format("%.2f", value);
    }
}
