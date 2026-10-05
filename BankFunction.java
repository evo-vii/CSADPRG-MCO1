import java.util.Scanner;

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