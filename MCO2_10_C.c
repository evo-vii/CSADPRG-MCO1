/*
 * ********************
 * Last names: Bobadilla, Calvez, Casao, Sanico
 * Language: C
 * Paradigm(s): Procedural
 * ********************
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

#define MAX_ACCOUNTS 1000
#define MAX_ACCOUNT_NUMBER 2147483647
#define MAX_TRANSACTIONS 1000000
#define ADMIN_NAME "admin"
#define ADMIN_PIN "2580"
#define NUM_CURRENCIES 5
#define NUM_WALLETS 6
#define MAX_BALANCE 999999999999.99
#define MIN_RATE 0.0001
#define MIN_CONVERTED 0.0099999
#define INTEREST_RATE 0.05
#define DAYS_IN_YEAR 365
#define MAX_DAYS 3650
#define ACCOUNTS_FILE "accounts.txt"
#define RATES_FILE "rates.txt"
#define TRANSACTIONS_FILE "transactions.txt"
#define CSV_FILE "bank_transactions.csv"
#define LOG_FILE "validation_log.txt"
#define MAX_BANK_ROWS 20000
#define CSV_COLUMNS 8
#define CSV_FIELD_SIZE 100
#define CSV_LINE_SIZE 512
#define ACCOUNT_DIGITS 10
#define FIRST_ACCOUNT_NUMBER 1000000001
#define MIN_INITIAL_DEPOSIT 5000.00
#define MIN_ADB 100000.00
#define SILVER_LIMIT 500000.00
#define GOLD_LIMIT 1000000.00
#define MIN_INTEREST_BALANCE 10000.00
#define INTEREST_DAYS 30
#define SUSPICIOUS_WITHDRAWAL 500000.00
#define SUSPICIOUS_VELOCITY 90.00
#define MAX_MONTHS 240
#define REPORT1_FILE "report1_banking_privilege_tiers.csv"
#define REPORT2_FILE "report2_interest_expense_projection.csv"
#define REPORT3_FILE "report3_suspicious_client_behavior.csv"
#define REPORT4_FILE "report4_monthly_cash_flow_summary.csv"
#define SUMMARY_FILE "summary.json"

struct Account {
    int accountNumber;
    char name[100];
    char pin[5];
    char contactNumber[16];
    double wallets[NUM_WALLETS];
};

struct ExchangeRate {
    char code[4];
    char name[40];
    double rate;
};

struct Transaction {
    int accountNumber;
    char type[16];
    char currency[4];
    double amount;
    double balanceAfter;
    int otherAccount;
};

struct Date {
    int day;
    int month;
    int year;
};

struct BankRow {
    char accountName[100];
    long long accountNumber;
    struct Date date;
    int hour;
    int minute;
    int second;
    char currency[4];
    double amount;
    char type[16];
    double balance;
    int dateNumber;
    long long stamp;
};

struct AccountSummary {
    long long accountNumber;
    int firstRow;
    int lastRow;
};

struct DailyRecord {
    char name[100];
    long long accountNumber;
    struct Date date;
    int dateNumber;
    double totalWithdrawal;
    int numDeposits;
    double totalDeposit;
    double velocity;
    int suspicious;
};

struct MonthRow {
    int year;
    int month;
    int count;
    double deposits;
    double withdrawals;
};

struct Account accounts[MAX_ACCOUNTS];
struct ExchangeRate rates[NUM_WALLETS];
struct Transaction transactions[MAX_TRANSACTIONS];
int numAccounts = 0;
int numTransactions = 0;
int nextAccountNumber = 1;
int dataOk = 1;

struct BankRow bankRows[MAX_BANK_ROWS];
int numBankRows = 0;
int csvTotalRows = 0;
int csvErrorRows = 0;
int csvNonPhpRows = 0;
int csvLoaded = 0;
char csvError[200];
char columnNames[CSV_COLUMNS][20] = {"account_name", "account_number", "date", "time", "currency", "amount", "type", "current_balance"};
struct AccountSummary summaries[MAX_ACCOUNTS];
int numSummaries = 0;
int asOfRow = 0;
int lastMonth = 0;
int lastYear = 0;
struct DailyRecord dailyRecords[MAX_BANK_ROWS];
int numDaily = 0;
struct MonthRow monthRows[MAX_MONTHS];
int numMonths = 0;
double totalInterestExpense = 0;

int readLine(char buf[], int size);
int textToNumber(char text[]);
int readMenuChoice(char prompt[], int low, int high);
int readYesNo();
int askBackToMenu();
int isPlainNumber(char text[]);
double readAmount(char prompt[]);
double readRate(char prompt[]);
double roundToCentavos(double value);
double roundToRate(double value);
void trimSpaces(char text[]);
void readName(char prompt[], char name[]);
void readPin(char prompt[], char pin[]);
void readContact(char prompt[], char contact[]);

void showLoginMenu();
void showMainMenu();
void showAdminMenu();
void loginUser();
void runSession(int accountIndex);
void runAdmin();

int isValidPin(char pin[]);
int isValidContact(char contact[]);
int findAccount(int number, char pin[]);
int findAccountByNumber(int number);
void registerAccount();

void initializeExchangeRates();
int selectWallet(int accountIndex, char title[]);
int selectCurrency(char title[], char prompt[]);

int historyHasRoom(int needed);
void recordTransaction(int accountNumber, char type[], char currency[], double amount, double balanceAfter, int otherAccount);
void printTransactionHeader();
void printTransaction(int number, int transactionIndex);

void depositAmount(int accountIndex);
void withdrawAmount(int accountIndex);
void checkBalance(int accountIndex);
void quoteCurrency();
void checkExchangeRates();
void exchangeCurrency(int accountIndex);
void showInterest(int accountIndex);
void sendMoney(int accountIndex);
int closeAccount(int accountIndex);
void showTransactionHistory(int accountIndex);

void recordExchangeRate();
void viewUpdateProfile();
void viewAllTransactions();

void removeNewline(char text[]);
void loadAccounts();
void loadRates();
void loadTransactions();
void loadData();
void saveAccounts();
void saveRates();
void saveData();

int isLeapYear(int year);
int daysInMonth(int month, int year);
int isValidDate(int day, int month, int year);
int splitCsvLine(char line[], char fields[][CSV_FIELD_SIZE]);
long long parseAccountNumber(char text[]);
int parseDate(char text[], int row);
int parseTime(char text[], int row);
int isValidMoney(char text[]);
void toLowerText(char text[]);
void toUpperText(char text[]);
int convertRow(char fields[][CSV_FIELD_SIZE], int row);
void logCsvError(FILE *logFile, int lineNumber);
void loadCsvFile();
void showReportsMenu();
void runReports();
int dateToNumber(struct Date date);
int dateToDayCount(struct Date date);
void setRowStamp(int row);
void addCommas(char plain[], char result[]);
void formatMoney(double value, char result[]);
void formatWhole(int value, char result[]);
void getCurrentDateTime(char dateText[], char timeText[]);
void saveTransactionToCsv(int accountNumber, char currency[], double amount, double balanceAfter);
long long findHighestAccountNumber();
int getNextAccountNumber();
int findSummary(long long accountNumber);
void prepareReports();
double endOfDayBalance(long long accountNumber, int dateNumber);
double averageDailyBalance(long long accountNumber);
void sortIndexesDescending(int list[], int count, double keys[]);
void generateReport1();
void generateReport2();
int findDailyRecord(long long accountNumber, int dateNumber);
void buildDailyRecords();
int report3Before(int a, int b);
void generateReport3();
int findMonthRow(int year, int month);
void generateReport4();
double computeCompoundInterest(double balance, int days);
void generateSummary();
void generateReports();

/*
 * Starts the banking program and keeps showing the login menu until the user exits
 * @param none
 * @return 0 when the program ends
 * @pre none
 */
int main() {
    int choice;

    initializeExchangeRates();
    loadData();

    while (1) {
        showLoginMenu();
        choice = readMenuChoice("Enter choice: ", 1, 3);

        if (choice == 1) {
            loginUser();
        } else if (choice == 2) {
            do {
                registerAccount();
            } while (askBackToMenu() == 0);
        } else {
            printf("\nThank you for using the Banking System.\n");
            return 0;
        }
    }

    return 0;
}

/*
 * Asks for account number and PIN then sends the user to the admin menu or the customer menu
 * @param none
 * @return N/A
 * @pre accounts must already be loaded
 */
void loginUser() {
    char id[100];
    char pin[100];
    int accountIndex;

    printf("\nAccount number: ");
    readLine(id, sizeof(id));
    printf("PIN: ");
    readLine(pin, sizeof(pin));

    if (strcmp(id, ADMIN_NAME) == 0 && strcmp(pin, ADMIN_PIN) == 0) {
        runAdmin();
        return;
    }

    accountIndex = findAccount(textToNumber(id), pin);

    if (accountIndex == -1) {
        printf("\nInvalid account number or PIN.\n");
        return;
    }

    runSession(accountIndex);
}

/*
 * Runs the main menu
 * @param accountIndex position of the logged in account in the accounts array
 * @return N/A
 * @pre accountIndex must be a valid index of a registered account
 */
void runSession(int accountIndex) {
    int choice;
    int again;
    int loggedIn;

    loggedIn = 1;
    printf("\nWelcome, %s (Account %05d)!\n", accounts[accountIndex].name, accounts[accountIndex].accountNumber);

    while (loggedIn == 1) {
        showMainMenu();
        choice = readMenuChoice("Enter choice: ", 1, 10);

        if (choice == 1) {
            do {
                depositAmount(accountIndex);
            } while (askBackToMenu() == 0);
        } else if (choice == 2) {
            do {
                withdrawAmount(accountIndex);
            } while (askBackToMenu() == 0);
        } else if (choice == 3) {
            checkBalance(accountIndex);
        } else if (choice == 4) {
            checkExchangeRates();
        } else if (choice == 5) {
            do {
                exchangeCurrency(accountIndex);
                printf("\nConvert another currency (Y/N)? ");
                again = readYesNo();
            } while (again == 1);
        } else if (choice == 6) {
            do {
                showInterest(accountIndex);
            } while (askBackToMenu() == 0);
        } else if (choice == 7) {
            do {
                sendMoney(accountIndex);
            } while (askBackToMenu() == 0);
        } else if (choice == 8) {
            if (closeAccount(accountIndex) == 1) {
                loggedIn = 0;
            }
        } else if (choice == 9) {
            showTransactionHistory(accountIndex);
        } else {
            printf("\nLogging out %s. See you again!\n", accounts[accountIndex].name);
            loggedIn = 0;
        }
    }
}

/*
 * Runs the admin menu
 * @param none
 * @return N/A
 * @pre none
 */
void runAdmin() {
    int choice;
    int loggedIn;

    loggedIn = 1;
    printf("\nWelcome Admin!\n");

    while (loggedIn == 1) {
        showAdminMenu();
        choice = readMenuChoice("Enter choice: ", 1, 5);

        if (choice == 1) {
            do {
                recordExchangeRate();
            } while (askBackToMenu() == 0);
        } else if (choice == 2) {
            do {
                viewUpdateProfile();
            } while (askBackToMenu() == 0);
        } else if (choice == 3) {
            viewAllTransactions();
        } else if (choice == 4) {
            runReports();
        } else {
            printf("\nLogging out Administrator.\n");
            loggedIn = 0;
        }
    }
}

/*
 * Reads one line from the user and removes the newline at the end
 * @param buf array where the typed text is saved
 * @param size most characters buf can hold including the end character
 * @return 1 if the input fit or 0 if it was too long
 * @pre buf must not be null and size must be more than 0
 */
int readLine(char buf[], int size) {
    int length;
    int ch;
    int tooLong;

    tooLong = 0;
    fflush(stdout);

    if (fgets(buf, size, stdin) == NULL) {
        printf("\nInput ended\n");
        exit(0);
    }

    length = strlen(buf);

    if (length > 0 && buf[length - 1] == '\n') {
        buf[length - 1] = '\0';
        length--;
    } else {
        ch = getchar();

        while (ch != '\n' && ch != EOF) {
            tooLong = 1;
            ch = getchar();
        }
    }

    if (length > 0 && buf[length - 1] == '\r') {
        buf[length - 1] = '\0';
    }

    if (tooLong == 1) {
        return 0;
    }

    return 1;
}

/*
 * Changes text made of digits into a whole number
 * @param text the string to change
 * @return the number or -1 if the text is empty or too long or has a non digit
 * @pre text must be a valid string
 */
int textToNumber(char text[]) {
    int i;
    int length;
    long long value;

    length = strlen(text);

    if (length < 1 || length > 10) {
        return -1;
    }

    value = 0;

    for (i = 0; i < length; i++) {
        if (text[i] < '0' || text[i] > '9') {
            return -1;
        }

        value = value * 10 + (text[i] - '0');
    }

    if (value > MAX_ACCOUNT_NUMBER) {
        return -1;
    }

    return value;
}

/*
 * Keeps asking until the user types a number inside the given range
 * @param prompt message shown before the user types
 * @param low smallest allowed number
 * @param high biggest allowed number
 * @return the valid number typed by the user
 * @pre low must not be more than high
 */
int readMenuChoice(char prompt[], int low, int high) {
    char line[32];
    int value;
    int fit;

    while (1) {
        printf("%s", prompt);
        fit = readLine(line, sizeof(line));
        value = textToNumber(line);

        if (fit == 1 && value >= low && value <= high) {
            return value;
        }

        printf("\nInvalid input. Please enter a number from %d to %d.\n", low, high);
    }
}

/*
 * Keeps asking until the user types Y or N
 * @param none
 * @return 1 for yes or 0 for no
 * @pre none
 */
int readYesNo() {
    char line[16];
    int fit;

    while (1) {
        fit = readLine(line, sizeof(line));

        if (fit == 1 && (line[0] == 'Y' || line[0] == 'y') && line[1] == '\0') {
            return 1;
        }

        if (fit == 1 && (line[0] == 'N' || line[0] == 'n') && line[1] == '\0') {
            return 0;
        }

        printf("Invalid input. Please enter Y or N: ");
    }
}

/*
 * Asks the user if they want to go back to the main menu
 * @param none
 * @return 1 for yes or 0 for no
 * @pre none
 */
int askBackToMenu() {
    printf("\nBack to the Main Menu (Y/N): ");

    return readYesNo();
}

/*
 * Checks if the text has only digits and at most one dot
 * @param text the string to check
 * @return 1 if the text is a plain number or 0 if not
 * @pre text must be a valid string
 */
int isPlainNumber(char text[]) {
    int i;
    int dots;
    int digits;

    dots = 0;
    digits = 0;

    for (i = 0; text[i] != '\0'; i++) {
        if (text[i] == '.') {
            dots++;
        } else if (text[i] >= '0' && text[i] <= '9') {
            digits++;
        } else {
            return 0;
        }
    }

    if (dots > 1 || digits == 0) {
        return 0;
    }

    return 1;
}

/*
 * Keeps asking for a money amount until it is valid then rounds it to 2 decimal places
 * @param prompt message shown before the user types
 * @return the valid amount from 0.01 up to MAX_BALANCE
 * @pre prompt must not be null
 */
double readAmount(char prompt[]) {
    char line[64];
    double value;
    int fit;

    while (1) {
        printf("%s", prompt);
        fit = readLine(line, sizeof(line));

        if (fit == 1 && isPlainNumber(line) == 1) {
            sscanf(line, "%lf", &value);

            if (value <= MAX_BALANCE) {
                value = roundToCentavos(value);

                if (value >= 0.01) {
                    return value;
                }
            }
        }

        printf("\nInvalid amount. Enter a plain number from 0.01 to %.2f.\n", MAX_BALANCE);
    }
}

/*
 * Keeps asking for an exchange rate until it is valid then rounds it to 4 decimal places
 * @param prompt message shown before the user types
 * @return the valid rate from MIN_RATE up to MAX_BALANCE
 * @pre prompt must not be null
 */
double readRate(char prompt[]) {
    char line[64];
    double value;
    int fit;

    while (1) {
        printf("%s", prompt);
        fit = readLine(line, sizeof(line));

        if (fit == 1 && isPlainNumber(line) == 1) {
            sscanf(line, "%lf", &value);

            if (value <= MAX_BALANCE) {
                value = roundToRate(value);

                if (value >= MIN_RATE) {
                    return value;
                }
            }
        }

        printf("\nInvalid exchange rate. Enter a plain number from %.4f to %.2f.\n", MIN_RATE, MAX_BALANCE);
    }
}

/*
 * Rounds a number to 2 decimal places
 * @param value the number to round
 * @return the rounded number
 * @pre none
 */
double roundToCentavos(double value) {
    char text[64];

    sprintf(text, "%.2f", value);
    sscanf(text, "%lf", &value);

    return value;
}

/*
 * Rounds a number to 4 decimal places
 * @param value the number to round
 * @return the rounded number
 * @pre none
 */
double roundToRate(double value) {
    char text[64];

    sprintf(text, "%.4f", value);
    sscanf(text, "%lf", &value);

    return value;
}

/*
 * Removes the spaces at the start and end of the text
 * @param text the string to trim and it is changed directly
 * @return N/A
 * @pre text must be a valid string that can be changed
 */
void trimSpaces(char text[]) {
    int start;
    int end;
    int i;

    end = strlen(text);

    while (end > 0 && text[end - 1] == ' ') {
        end--;
    }

    text[end] = '\0';
    start = 0;

    while (text[start] == ' ') {
        start++;
    }

    if (start > 0) {
        i = 0;

        while (text[start + i] != '\0') {
            text[i] = text[start + i];
            i++;
        }

        text[i] = '\0';
    }
}

/*
 * Keeps asking for a name until it is not empty and not too long
 * @param prompt message shown before the user types
 * @param name array where the valid name is saved
 * @return N/A
 * @pre name must have room for 100 characters
 */
void readName(char prompt[], char name[]) {
    char line[100];
    int fit;

    while (1) {
        printf("%s", prompt);
        fit = readLine(line, sizeof(line));
        trimSpaces(line);

        if (fit == 0) {
            printf("\nName is too long. 99 characters only.\n");
        } else if (line[0] == '\0') {
            printf("\nName cannot be empty.\n");
        } else {
            strcpy(name, line);
            return;
        }
    }
}

/*
 * Keeps asking for a PIN until it is exactly 4 digits
 * @param prompt message shown before the user types
 * @param pin array where the valid PIN is saved
 * @return N/A
 * @pre pin must have room for at least 5 characters
 */
void readPin(char prompt[], char pin[]) {
    char line[100];
    int fit;

    while (1) {
        printf("%s", prompt);
        fit = readLine(line, sizeof(line));

        if (fit == 1 && isValidPin(line) == 1) {
            strcpy(pin, line);
            return;
        }

        printf("\nInvalid PIN. It must be exactly 4 digits.\n");
    }
}

/*
 * Keeps asking for a contact number until it is 10 to 15 digits
 * @param prompt message shown before the user types
 * @param contact array where the valid contact number is saved
 * @return N/A
 * @pre contact must have room for at least 16 characters
 */
void readContact(char prompt[], char contact[]) {
    char line[100];
    int fit;

    while (1) {
        printf("%s", prompt);
        fit = readLine(line, sizeof(line));

        if (fit == 1 && isValidContact(line) == 1) {
            strcpy(contact, line);
            return;
        }

        printf("\nInvalid contact number. Use 10 to 15 digits only.\n");
    }
}

/*
 * Prints the login menu
 * @param none
 * @return N/A
 * @pre none
 */
void showLoginMenu() {
    printf("\n========================================\n");
    printf("        BANKING SYSTEM - LOGIN\n");
    printf("========================================\n");
    printf("[1] Login\n");
    printf("[2] Register New Account\n");
    printf("[3] Exit Program\n");
    printf("========================================\n");
}

/*
 * Prints the main menu for customers
 * @param none
 * @return N/A
 * @pre none
 */
void showMainMenu() {
    printf("\n========================================\n");
    printf("Select Transaction:\n");
    printf("[1] Deposit Amount\n");
    printf("[2] Withdraw Amount\n");
    printf("[3] Check Balance\n");
    printf("[4] Check Currency Exchange\n");
    printf("[5] Exchange Currency\n");
    printf("[6] Show Interest Amount\n");
    printf("[7] Send Money\n");
    printf("[8] Close Account\n");
    printf("[9] Transaction History\n");
    printf("[10] Logout\n");
    printf("========================================\n");
}

/*
 * Prints the admin menu
 * @param none
 * @return N/A
 * @pre none
 */
void showAdminMenu() {
    printf("\n========================================\n");
    printf("        ADMIN MENU\n");
    printf("========================================\n");
    printf("[1] Record Exchange Rates\n");
    printf("[2] View/Update Customer Profile\n");
    printf("[3] View Transaction History\n");
    printf("[4] Generate Reports\n");
    printf("[5] Logout\n");
    printf("========================================\n");
}

/*
 * Checks if the PIN is exactly 4 digits
 * @param pin the PIN to check
 * @return 1 if the PIN is valid or 0 if not
 * @pre pin must be a valid string
 */
int isValidPin(char pin[]) {
    int i;

    if (strlen(pin) != 4) {
        return 0;
    }

    for (i = 0; i < 4; i++) {
        if (pin[i] < '0' || pin[i] > '9') {
            return 0;
        }
    }

    return 1;
}

/*
 * Checks if the contact number is 10 to 15 digits only
 * @param contact the contact number to check
 * @return 1 if the contact number is valid or 0 if not
 * @pre contact must be a valid string
 */
int isValidContact(char contact[]) {
    int i;
    int length;

    length = strlen(contact);

    if (length < 10 || length > 15) {
        return 0;
    }

    for (i = 0; i < length; i++) {
        if (contact[i] < '0' || contact[i] > '9') {
            return 0;
        }
    }

    return 1;
}

/*
 * Looks for an account that matches both the account number and the PIN
 * @param number account number to look for
 * @param pin PIN to match
 * @return the index of the account or -1 if there is no match
 * @pre pin must be a valid string
 */
int findAccount(int number, char pin[]) {
    int i;

    for (i = 0; i < numAccounts; i++) {
        if (accounts[i].accountNumber == number && strcmp(accounts[i].pin, pin) == 0) {
            return i;
        }
    }

    return -1;
}

/*
 * Looks for an account using only the account number
 * @param number account number to look for
 * @return the index of the account or -1 if not found
 * @pre none
 */
int findAccountByNumber(int number) {
    int i;

    for (i = 0; i < numAccounts; i++) {
        if (accounts[i].accountNumber == number) {
            return i;
        }
    }

    return -1;
}

/*
 * Makes a new account with a name and PIN and contact number then saves it
 * @param none
 * @return N/A
 * @pre none
 */
void registerAccount() {
    char name[100];
    char pin[100];
    char contact[100];
    int w;
    int newNumber;
    double initialDeposit;

    newNumber = getNextAccountNumber();

    if (numAccounts >= MAX_ACCOUNTS || newNumber == -1) {
        printf("\nThe system is full. Cannot register a new account.\n");
        return;
    }

    if (historyHasRoom(1) == 0) {
        return;
    }

    printf("\nRegister Account Name\n");
    readName("Account Name: ", name);
    readPin("Create a 4-digit PIN: ", pin);
    readContact("Contact Number: ", contact);

    do {
        initialDeposit = readAmount("Initial Deposit: ");

        if (initialDeposit < MIN_INITIAL_DEPOSIT) {
            printf("\nThe minimum initial deposit is %.2f.\n", MIN_INITIAL_DEPOSIT);
        }
    } while (initialDeposit < MIN_INITIAL_DEPOSIT);

    nextAccountNumber = newNumber;
    accounts[numAccounts].accountNumber = nextAccountNumber;
    strcpy(accounts[numAccounts].name, name);
    strcpy(accounts[numAccounts].pin, pin);
    strcpy(accounts[numAccounts].contactNumber, contact);

    for (w = 0; w < NUM_WALLETS; w++) {
        accounts[numAccounts].wallets[w] = 0.00;
    }

    accounts[numAccounts].wallets[0] = initialDeposit;

    printf("\nAccount \"%s\" registered successfully!\n", name);
    printf("Your account number is: %05d\n", accounts[numAccounts].accountNumber);
    printf("Use your account number to log in.\n");

    nextAccountNumber++;
    numAccounts++;
    recordTransaction(accounts[numAccounts - 1].accountNumber, "Deposit", rates[0].code, initialDeposit, initialDeposit, 0);
    saveData();
}

/*
 * Sets the starting names and rates of all the currencies
 * @param none
 * @return N/A
 * @pre none
 */
void initializeExchangeRates() {
    strcpy(rates[0].code, "PHP");
    strcpy(rates[0].name, "Philippine Peso (PHP)");
    rates[0].rate = 1.00;

    strcpy(rates[1].code, "USD");
    strcpy(rates[1].name, "United States Dollar (USD)");
    rates[1].rate = 62.00;

    strcpy(rates[2].code, "JPY");
    strcpy(rates[2].name, "Japanese Yen (JPY)");
    rates[2].rate = 0.40;

    strcpy(rates[3].code, "GBP");
    strcpy(rates[3].name, "British Pound Sterling (GBP)");
    rates[3].rate = 84.00;

    strcpy(rates[4].code, "EUR");
    strcpy(rates[4].name, "Euro (EUR)");
    rates[4].rate = 72.00;

    strcpy(rates[5].code, "CNY");
    strcpy(rates[5].name, "Chinese Yuan Renminbi (CNY)");
    rates[5].rate = 9.00;
}

/*
 * Shows the wallets with their balances and asks the user to pick one
 * @param accountIndex position of the account in the accounts array
 * @param title heading shown above the wallet list
 * @return the chosen wallet number starting from 0
 * @pre accountIndex must be a valid index of a registered account
 */
int selectWallet(int accountIndex, char title[]) {
    int w;
    int choice;

    printf("\n%s\n", title);

    for (w = 0; w < NUM_WALLETS; w++) {
        printf("[%d] %-30s Balance: %.2f\n", w + 1, rates[w].name, accounts[accountIndex].wallets[w]);
    }

    printf("\n");
    choice = readMenuChoice("Currency: ", 1, NUM_WALLETS);

    return choice - 1;
}

/*
 * Shows the currencies and asks the user to pick one
 * @param title heading shown above the currency list
 * @param prompt message shown before the user types
 * @return the chosen currency number starting from 0
 * @pre none
 */
int selectCurrency(char title[], char prompt[]) {
    int i;
    int choice;

    printf("\n%s\n", title);

    for (i = 0; i < NUM_WALLETS; i++) {
        printf("[%d] %s\n", i + 1, rates[i].name);
    }

    printf("\n");
    choice = readMenuChoice(prompt, 1, NUM_WALLETS);

    return choice - 1;
}

/*
 * Checks if the transaction list can still hold more records
 * @param needed how many new records will be added
 * @return 1 if there is room or 0 if the list is full
 * @pre needed must be 0 or more
 */
int historyHasRoom(int needed) {
    if (numTransactions + needed > MAX_TRANSACTIONS) {
        printf("\nThe transaction log is full.\n");
        return 0;
    }

    return 1;
}

/*
 * Saves a transaction in memory and adds it to the transactions file
 * @param accountNumber account number that owns the transaction
 * @param type kind of transaction like Deposit or Sent
 * @param currency 3 letter currency code
 * @param amount amount of the transaction and it is negative when money goes out
 * @param balanceAfter wallet balance after the transaction
 * @param otherAccount other account number involved or 0 if none
 * @return N/A
 * @pre there must be room in the transaction list and type and currency must not be null
 */
void recordTransaction(int accountNumber, char type[], char currency[], double amount, double balanceAfter, int otherAccount) {
    FILE *fp;

    transactions[numTransactions].accountNumber = accountNumber;
    strcpy(transactions[numTransactions].type, type);
    strcpy(transactions[numTransactions].currency, currency);
    transactions[numTransactions].amount = amount;
    transactions[numTransactions].balanceAfter = balanceAfter;
    transactions[numTransactions].otherAccount = otherAccount;

    if (dataOk == 1) {
        fp = fopen(TRANSACTIONS_FILE, "a");

        if (fp == NULL) {
            printf("\nWarning: could not save %s.\n", TRANSACTIONS_FILE);
        } else {
            fprintf(fp, "%d %s %.2f %.2f %d\n%s\n", accountNumber, currency, amount, balanceAfter, otherAccount, type);
            fclose(fp);
        }

        saveTransactionToCsv(accountNumber, currency, amount, balanceAfter);
    }

    numTransactions++;
}

/*
 * Prints the column titles of the transaction table
 * @param none
 * @return N/A
 * @pre none
 */
void printTransactionHeader() {
    printf("%-7s | %-7s | %-11s | %-3s | %15s | %15s | %s\n", "No.", "Account", "Type", "Cur", "Amount", "Balance After", "Other");
    printf("-------------------------------------------------------------------------------------------\n");
}

/*
 * Prints one row of the transaction table
 * @param number row number to show
 * @param transactionIndex position of the transaction in the transactions array
 * @return N/A
 * @pre transactionIndex must be a valid index of a saved transaction
 */
void printTransaction(int number, int transactionIndex) {
    printf("%-7d | %05d   | %-11s | %-3s | %15.2f | %15.2f | ",
           number,
           transactions[transactionIndex].accountNumber,
           transactions[transactionIndex].type,
           transactions[transactionIndex].currency,
           transactions[transactionIndex].amount,
           transactions[transactionIndex].balanceAfter);

    if (transactions[transactionIndex].otherAccount == 0) {
        printf("-\n");
    } else {
        printf("%05d\n", transactions[transactionIndex].otherAccount);
    }
}

/*
 * Adds money to a wallet of the customer and records it
 * @param accountIndex position of the account in the accounts array
 * @return N/A
 * @pre accountIndex must be a valid index of a registered account
 */
void depositAmount(int accountIndex) {
    int wallet;
    double amount;

    if (historyHasRoom(1) == 0) {
        return;
    }

    printf("\nDeposit Amount\n");
    printf("Account Number: %05d\n", accounts[accountIndex].accountNumber);
    printf("Account Name: %s\n", accounts[accountIndex].name);

    wallet = selectWallet(accountIndex, "Select Currency Wallet:");

    printf("\nWallet: %s\n", rates[wallet].name);
    printf("Current Balance: %.2f %s\n", accounts[accountIndex].wallets[wallet], rates[wallet].code);

    printf("\n");
    amount = readAmount("Deposit Amount: ");

    if (accounts[accountIndex].wallets[wallet] + amount > MAX_BALANCE) {
        printf("\nDeposit not allowed: a wallet cannot hold more than %.2f.\n", MAX_BALANCE);
        return;
    }

    accounts[accountIndex].wallets[wallet] = roundToCentavos(accounts[accountIndex].wallets[wallet] + amount);
    recordTransaction(accounts[accountIndex].accountNumber, "Deposit", rates[wallet].code, amount, accounts[accountIndex].wallets[wallet], 0);
    saveData();

    printf("\nDeposit successful!\n");
    printf("Updated Balance: %.2f %s\n", accounts[accountIndex].wallets[wallet], rates[wallet].code);
}

/*
 * Takes money out of a wallet of the customer and records it
 * @param accountIndex position of the account in the accounts array
 * @return N/A
 * @pre accountIndex must be a valid index of a registered account
 */
void withdrawAmount(int accountIndex) {
    int wallet;
    double amount;

    if (historyHasRoom(1) == 0) {
        return;
    }

    printf("\nWithdraw Amount\n");
    printf("Account Number: %05d\n", accounts[accountIndex].accountNumber);
    printf("Account Name: %s\n", accounts[accountIndex].name);

    wallet = selectWallet(accountIndex, "Select Currency Wallet:");

    printf("\nWallet: %s\n", rates[wallet].name);
    printf("Current Balance: %.2f %s\n", accounts[accountIndex].wallets[wallet], rates[wallet].code);

    printf("\n");
    amount = readAmount("Withdraw Amount: ");

    if (amount > accounts[accountIndex].wallets[wallet]) {
        printf("\nInsufficient balance!\n");
        return;
    }

    accounts[accountIndex].wallets[wallet] = roundToCentavos(accounts[accountIndex].wallets[wallet] - amount);
    recordTransaction(accounts[accountIndex].accountNumber, "Withdraw", rates[wallet].code, -amount, accounts[accountIndex].wallets[wallet], 0);
    saveData();

    printf("\nWithdrawal successful!\n");
    printf("Updated Balance: %.2f %s\n", accounts[accountIndex].wallets[wallet], rates[wallet].code);
}

/*
 * Shows the balance of every wallet of the customer
 * @param accountIndex position of the account in the accounts array
 * @return N/A
 * @pre accountIndex must be a valid index of a registered account
 */
void checkBalance(int accountIndex) {
    int w;

    printf("\nCheck Balance\n");
    printf("Account Number: %05d\n", accounts[accountIndex].accountNumber);
    printf("Account Name: %s\n\n", accounts[accountIndex].name);

    for (w = 0; w < NUM_WALLETS; w++) {
        printf("%-30s Balance: %.2f %s\n", rates[w].name, accounts[accountIndex].wallets[w], rates[w].code);
    }
}

/*
 * Shows how much an amount is worth in another currency without changing any balance
 * @param none
 * @return N/A
 * @pre none
 */
void quoteCurrency() {
    int source;
    int target;
    double amount;
    double result;

    printf("\nForeign Currency Exchange\n");

    source = selectCurrency("Source Currency Option:", "Source Currency: ");

    printf("\n");
    amount = readAmount("Source Amount: ");

    target = selectCurrency("Exchanged Currency Options:", "Exchange Currency: ");

    while (target == source) {
        printf("\nThe source and exchange currencies must be different.\n");
        target = selectCurrency("Exchanged Currency Options:", "Exchange Currency: ");
    }

    result = amount * rates[source].rate / rates[target].rate;

    if (result > MAX_BALANCE) {
        printf("\nThe converted amount is too large to display.\n");
        return;
    }

    if (result < MIN_CONVERTED) {
        printf("\nThe converted amount is too small (less than 0.01 %s).\n", rates[target].code);
        return;
    }

    result = roundToCentavos(result);

    printf("\nExchange Rate: 1 %s = %.6f %s\n", rates[source].code, rates[source].rate / rates[target].rate, rates[target].code);
    printf("Exchange Amount: %.2f %s\n", result, rates[target].code);
}

/*
 * Shows all the exchange rates against PHP and lets the user try conversions
 * @param none
 * @return N/A
 * @pre none
 */
void checkExchangeRates() {
    int i;
    int again;

    printf("\nCurrency Exchange Rates\n");
    printf("Base Currency: Philippine Peso (PHP)\n\n");

    for (i = 1; i < NUM_WALLETS; i++) {
        printf("%-30s 1 %s = %.4f PHP\n", rates[i].name, rates[i].code, rates[i].rate);
    }

    again = 1;

    while (again == 1) {
        quoteCurrency();
        printf("\nConvert another currency (Y/N)? ");
        again = readYesNo();
    }
}

/*
 * Moves money from one wallet to another wallet using the current rates
 * @param accountIndex position of the account in the accounts array
 * @return N/A
 * @pre accountIndex must be a valid index of a registered account
 */
void exchangeCurrency(int accountIndex) {
    int source;
    int target;
    double amount;
    double result;

    if (historyHasRoom(2) == 0) {
        return;
    }

    printf("\nExchange Currency\n");
    printf("Account Number: %05d\n", accounts[accountIndex].accountNumber);
    printf("Account Name: %s\n", accounts[accountIndex].name);

    source = selectWallet(accountIndex, "Exchange From:");

    if (accounts[accountIndex].wallets[source] <= 0) {
        printf("\nThe %s wallet has no balance to exchange.\n", rates[source].code);
        return;
    }

    target = selectWallet(accountIndex, "Exchange To:");

    while (target == source) {
        printf("\nThe source and target currencies must be different.\n");
        target = selectWallet(accountIndex, "Exchange To:");
    }

    printf("\nExchange Rate: 1 %s = %.6f %s\n", rates[source].code, rates[source].rate / rates[target].rate, rates[target].code);
    printf("Available Balance: %.2f %s\n", accounts[accountIndex].wallets[source], rates[source].code);

    printf("\n");
    amount = readAmount("Amount to Exchange: ");

    if (amount > accounts[accountIndex].wallets[source]) {
        printf("\nInsufficient balance!\n");
        return;
    }

    result = amount * rates[source].rate / rates[target].rate;

    if (result > MAX_BALANCE) {
        printf("\nThe converted amount is too large.\n");
        return;
    }

    if (result < MIN_CONVERTED) {
        printf("\nThe converted amount is too small (less than 0.01 %s). No exchange was made.\n", rates[target].code);
        return;
    }

    result = roundToCentavos(result);

    if (accounts[accountIndex].wallets[target] + result > MAX_BALANCE) {
        printf("\nExchange not allowed: the %s wallet cannot hold more than %.2f.\n", rates[target].code, MAX_BALANCE);
        return;
    }

    printf("\nYou will exchange: %.2f %s\n", amount, rates[source].code);
    printf("You will receive: %.2f %s\n", result, rates[target].code);
    printf("\nProceed with the exchange (Y/N)? ");

    if (readYesNo() == 0) {
        printf("\nExchange cancelled.\n");
        return;
    }

    accounts[accountIndex].wallets[source] = roundToCentavos(accounts[accountIndex].wallets[source] - amount);
    accounts[accountIndex].wallets[target] = roundToCentavos(accounts[accountIndex].wallets[target] + result);

    recordTransaction(accounts[accountIndex].accountNumber, "Convert Out", rates[source].code, -amount, accounts[accountIndex].wallets[source], 0);
    recordTransaction(accounts[accountIndex].accountNumber, "Convert In", rates[target].code, result, accounts[accountIndex].wallets[target], 0);
    saveData();

    printf("\nExchange successful!\n");
    printf("Exchanged %.2f %s to %.2f %s.\n", amount, rates[source].code, result, rates[target].code);
    printf("%s Balance: %.2f\n", rates[source].code, accounts[accountIndex].wallets[source]);
    printf("%s Balance: %.2f\n", rates[target].code, accounts[accountIndex].wallets[target]);
}

/*
 * Shows the daily interest and growing balance of a wallet for a number of days
 * @param accountIndex position of the account in the accounts array
 * @return N/A
 * @pre accountIndex must be a valid index of a registered account
 */
void showInterest(int accountIndex) {
    int wallet;
    int days;
    int day;
    double balance;
    double interest;

    printf("\nShow Interest Amount\n");
    printf("Account Name: %s\n", accounts[accountIndex].name);

    wallet = selectWallet(accountIndex, "Select Currency Wallet:");
    balance = accounts[accountIndex].wallets[wallet];

    printf("\nCurrent Balance: %.2f\n", balance);
    printf("Currency: %s\n", rates[wallet].code);
    printf("Interest Rate: %.0f%%\n", INTEREST_RATE * 100);

    printf("\n");
    days = readMenuChoice("Total Number of Days (1 to 3650): ", 1, MAX_DAYS);

    if (balance < 0.01) {
        printf("\nThe balance is 0.00 so no interest will be earned.\n");
    } else if (roundToCentavos(balance * INTEREST_RATE / DAYS_IN_YEAR) < 0.01) {
        printf("\nInterest is rounded to the nearest centavo each day so a balance this small earns 0.00.\n");
    }

    printf("\n%-5s | %-14s | %-16s |\n", "Day", "Interest", "Balance");

    for (day = 1; day <= days; day++) {
        interest = roundToCentavos(balance * INTEREST_RATE / DAYS_IN_YEAR);

        if (balance + interest > MAX_BALANCE) {
            printf("\nProjection stopped on day %d: the balance would exceed %.2f.\n", day, MAX_BALANCE);
            return;
        }

        balance = roundToCentavos(balance + interest);
        printf("%-5d | %-14.2f | %-16.2f |\n", day, interest, balance);
    }
}

/*
 * Sends money from a wallet of the customer to another account and records both sides
 * @param accountIndex position of the sender account in the accounts array
 * @return N/A
 * @pre accountIndex must be a valid index of a registered account
 */
void sendMoney(int accountIndex) {
    int wallet;
    int number;
    int recipientIndex;
    double amount;

    if (historyHasRoom(2) == 0) {
        return;
    }

    printf("\nSend Money\n");
    printf("Account Number: %05d\n", accounts[accountIndex].accountNumber);
    printf("Account Name: %s\n", accounts[accountIndex].name);

    wallet = selectWallet(accountIndex, "Select Currency Wallet:");

    printf("\nWallet: %s\n", rates[wallet].name);
    printf("Current Balance: %.2f %s\n", accounts[accountIndex].wallets[wallet], rates[wallet].code);

    printf("\n");
    number = readMenuChoice("Recipient Account Number: ", 1, MAX_ACCOUNT_NUMBER);
    recipientIndex = findAccountByNumber(number);

    if (recipientIndex == -1) {
        printf("\nRecipient account not found.\n");
        return;
    }

    if (recipientIndex == accountIndex) {
        printf("\nYou cannot send money to your own account.\n");
        return;
    }

    printf("Recipient Name: %s\n", accounts[recipientIndex].name);

    printf("\n");
    amount = readAmount("Send Amount: ");

    if (amount > accounts[accountIndex].wallets[wallet]) {
        printf("\nInsufficient balance!\n");
        return;
    }

    if (accounts[recipientIndex].wallets[wallet] + amount > MAX_BALANCE) {
        printf("\nTransfer not allowed: the recipient's wallet cannot hold more than %.2f.\n", MAX_BALANCE);
        return;
    }

    printf("\nSend %.2f %s to %s (%05d) (Y/N)? ", amount, rates[wallet].code, accounts[recipientIndex].name, accounts[recipientIndex].accountNumber);

    if (readYesNo() == 0) {
        printf("\nTransfer cancelled.\n");
        return;
    }

    accounts[accountIndex].wallets[wallet] = roundToCentavos(accounts[accountIndex].wallets[wallet] - amount);
    accounts[recipientIndex].wallets[wallet] = roundToCentavos(accounts[recipientIndex].wallets[wallet] + amount);

    recordTransaction(accounts[accountIndex].accountNumber, "Sent", rates[wallet].code, -amount, accounts[accountIndex].wallets[wallet], accounts[recipientIndex].accountNumber);
    recordTransaction(accounts[recipientIndex].accountNumber, "Received", rates[wallet].code, amount, accounts[recipientIndex].wallets[wallet], accounts[accountIndex].accountNumber);
    saveData();

    printf("\nTransfer successful!\n");
    printf("Sent %.2f %s to %s (%05d).\n", amount, rates[wallet].code, accounts[recipientIndex].name, accounts[recipientIndex].accountNumber);
    printf("Updated Balance: %.2f %s\n", accounts[accountIndex].wallets[wallet], rates[wallet].code);
}

/*
 * Closes the account after the PIN is checked and only if all wallets are empty
 * @param accountIndex position of the account in the accounts array
 * @return 1 if the account was closed or 0 if not
 * @pre accountIndex must be a valid index of a registered account
 */
int closeAccount(int accountIndex) {
    char pin[100];
    int w;
    int i;
    int hasBalance;

    printf("\nClose Account\n");
    printf("Account Number: %05d\n", accounts[accountIndex].accountNumber);
    printf("Account Name: %s\n", accounts[accountIndex].name);

    printf("\nEnter your PIN to continue: ");
    readLine(pin, sizeof(pin));

    if (strcmp(pin, accounts[accountIndex].pin) != 0) {
        printf("\nIncorrect PIN. The account was not closed.\n");
        return 0;
    }

    hasBalance = 0;

    for (w = 0; w < NUM_WALLETS; w++) {
        if (accounts[accountIndex].wallets[w] > 0) {
            hasBalance = 1;
        }
    }

    if (hasBalance == 1) {
        printf("\nThe account cannot be closed while it still has money.\n");
        printf("Please withdraw all balances first:\n\n");

        for (w = 0; w < NUM_WALLETS; w++) {
            if (accounts[accountIndex].wallets[w] > 0) {
                printf("%-30s %.2f %s\n", rates[w].name, accounts[accountIndex].wallets[w], rates[w].code);
            }
        }

        return 0;
    }

    printf("\nThis action cannot be undone. Close this account (Y/N)? ");

    if (readYesNo() == 0) {
        printf("\nAccount closure cancelled.\n");
        return 0;
    }

    for (i = accountIndex; i < numAccounts - 1; i++) {
        accounts[i] = accounts[i + 1];
    }

    numAccounts--;
    saveData();

    printf("\nYour account has been closed. Thank you for banking with us.\n");

    return 1;
}

/*
 * Shows all the transactions of one account
 * @param accountIndex position of the account in the accounts array
 * @return N/A
 * @pre accountIndex must be a valid index of a registered account
 */
void showTransactionHistory(int accountIndex) {
    int i;
    int count;

    printf("\nTransaction History\n");
    printf("Account Number: %05d\n", accounts[accountIndex].accountNumber);
    printf("Account Name: %s\n\n", accounts[accountIndex].name);

    count = 0;

    for (i = 0; i < numTransactions; i++) {
        if (transactions[i].accountNumber == accounts[accountIndex].accountNumber) {
            if (count == 0) {
                printTransactionHeader();
            }

            count++;
            printTransaction(count, i);
        }
    }

    if (count == 0) {
        printf("No transactions yet.\n");
    }
}

/*
 * Lets the admin pick a foreign currency and set its new rate against PHP
 * @param none
 * @return N/A
 * @pre none
 */
void recordExchangeRate() {
    int i;
    int choice;
    double rate;
    char prompt[80];

    printf("\nRecord Exchange Rate\n\n");

    for (i = 1; i <= NUM_CURRENCIES; i++) {
        printf("[%d] %-30s Current: 1 %s = %.4f PHP\n", i, rates[i].name, rates[i].code, rates[i].rate);
    }

    printf("\n");
    choice = readMenuChoice("Select Foreign Currency: ", 1, NUM_CURRENCIES);

    sprintf(prompt, "Exchange Rate (1 %s = ? PHP): ", rates[choice].code);
    rate = readRate(prompt);

    rates[choice].rate = rate;
    saveData();

    printf("\n%s exchange rate recorded: 1 %s = %.4f PHP\n", rates[choice].name, rates[choice].code, rate);
}

/*
 * Lets the admin view a customer profile and change the name or PIN or contact number
 * @param none
 * @return N/A
 * @pre none
 */
void viewUpdateProfile() {
    int number;
    int index;
    int choice;
    int w;
    char input[100];

    printf("\nView/Update Customer Profile\n");

    if (numAccounts == 0) {
        printf("\nThere are no registered accounts.\n");
        return;
    }

    number = readMenuChoice("Account Number: ", 1, MAX_ACCOUNT_NUMBER);
    index = findAccountByNumber(number);

    if (index == -1) {
        printf("\nAccount not found.\n");
        return;
    }

    printf("\nAccount Number: %05d\n", accounts[index].accountNumber);
    printf("Account Name: %s\n", accounts[index].name);
    printf("Contact Number: %s\n", accounts[index].contactNumber);
    printf("\nWallet Balances:\n");

    for (w = 0; w < NUM_WALLETS; w++) {
        printf("%-30s %.2f %s\n", rates[w].name, accounts[index].wallets[w], rates[w].code);
    }

    printf("\n[1] Update Account Name\n");
    printf("[2] Reset PIN\n");
    printf("[3] Update Contact Number\n");
    printf("[4] Back\n");
    choice = readMenuChoice("Enter choice: ", 1, 4);

    if (choice == 1) {
        readName("New Account Name: ", input);
        strcpy(accounts[index].name, input);
        saveData();
        printf("\nAccount name updated.\n");
    } else if (choice == 2) {
        readPin("New 4-digit PIN: ", input);
        strcpy(accounts[index].pin, input);
        saveData();
        printf("\nPIN updated.\n");
    } else if (choice == 3) {
        readContact("New Contact Number: ", input);
        strcpy(accounts[index].contactNumber, input);
        saveData();
        printf("\nContact number updated.\n");
    }
}

/*
 * Shows the transactions of all accounts
 * @param none
 * @return N/A
 * @pre none
 */
void viewAllTransactions() {
    int i;

    printf("\nAll Transaction History\n\n");

    if (numTransactions == 0) {
        printf("No transactions recorded.\n");
        return;
    }

    printTransactionHeader();

    for (i = 0; i < numTransactions; i++) {
        printTransaction(i + 1, i);
    }
}

/*
 * Removes the newline and carriage return characters at the end of the text
 * @param text the string to clean and it is changed directly
 * @return N/A
 * @pre text must be a valid string that can be changed
 */
void removeNewline(char text[]) {
    int length;

    length = strlen(text);

    while (length > 0 && (text[length - 1] == '\n' || text[length - 1] == '\r')) {
        text[length - 1] = '\0';
        length--;
    }
}

/*
 * Reads the accounts file and fills the accounts array
 * @param none
 * @return N/A
 * @pre none
 */
void loadAccounts() {
    FILE *fp;
    int i;
    int ch;
    int result;

    fp = fopen(ACCOUNTS_FILE, "r");

    if (fp == NULL) {
        return;
    }

    result = fscanf(fp, "%d", &nextAccountNumber);

    if (result != 1) {
        nextAccountNumber = 1;

        if (result != EOF) {
            dataOk = 0;
        }

        fclose(fp);
        return;
    }

    while (numAccounts < MAX_ACCOUNTS) {
        result = fscanf(fp, "%d %4s %15s %lf %lf %lf %lf %lf %lf",
                        &accounts[numAccounts].accountNumber,
                        accounts[numAccounts].pin,
                        accounts[numAccounts].contactNumber,
                        &accounts[numAccounts].wallets[0],
                        &accounts[numAccounts].wallets[1],
                        &accounts[numAccounts].wallets[2],
                        &accounts[numAccounts].wallets[3],
                        &accounts[numAccounts].wallets[4],
                        &accounts[numAccounts].wallets[5]);

        if (result == EOF) {
            break;
        }

        if (result != 9) {
            dataOk = 0;
            break;
        }

        ch = fgetc(fp);

        while (ch != '\n' && ch != EOF) {
            ch = fgetc(fp);
        }

        if (fgets(accounts[numAccounts].name, sizeof(accounts[numAccounts].name), fp) == NULL) {
            dataOk = 0;
            break;
        }

        removeNewline(accounts[numAccounts].name);
        numAccounts++;
    }

    fclose(fp);

    for (i = 0; i < numAccounts; i++) {
        if (accounts[i].accountNumber >= nextAccountNumber) {
            nextAccountNumber = accounts[i].accountNumber + 1;
        }
    }
}

/*
 * Reads the rates file and updates the exchange rates
 * @param none
 * @return N/A
 * @pre initializeExchangeRates must be called first
 */
void loadRates() {
    FILE *fp;
    int i;
    char code[4];
    double rate;

    fp = fopen(RATES_FILE, "r");

    if (fp == NULL) {
        return;
    }

    for (i = 0; i < NUM_WALLETS; i++) {
        if (fscanf(fp, "%3s %lf", code, &rate) != 2) {
            break;
        }

        if (i > 0 && strcmp(code, rates[i].code) == 0 && rate >= MIN_RATE && rate <= MAX_BALANCE) {
            rates[i].rate = rate;
        }
    }

    fclose(fp);
}

/*
 * Reads the transactions file and fills the transactions array
 * @param none
 * @return N/A
 * @pre none
 */
void loadTransactions() {
    FILE *fp;
    int ch;
    int result;

    fp = fopen(TRANSACTIONS_FILE, "r");

    if (fp == NULL) {
        return;
    }

    while (numTransactions < MAX_TRANSACTIONS) {
        result = fscanf(fp, "%d %3s %lf %lf %d",
                        &transactions[numTransactions].accountNumber,
                        transactions[numTransactions].currency,
                        &transactions[numTransactions].amount,
                        &transactions[numTransactions].balanceAfter,
                        &transactions[numTransactions].otherAccount);

        if (result == EOF) {
            break;
        }

        if (result != 5) {
            dataOk = 0;
            break;
        }

        ch = fgetc(fp);

        while (ch != '\n' && ch != EOF) {
            ch = fgetc(fp);
        }

        if (fgets(transactions[numTransactions].type, sizeof(transactions[numTransactions].type), fp) == NULL) {
            dataOk = 0;
            break;
        }

        removeNewline(transactions[numTransactions].type);
        numTransactions++;
    }

    fclose(fp);
}

/**
 * Loads the accounts and rates and transactions and warns if a file could not be read
 * @param none
 * @return N/A
 * @pre initializeExchangeRates must be called first
 */
void loadData() {
    loadAccounts();
    loadRates();
    loadTransactions();

    if (dataOk == 0) {
        printf("\nWarning: a data file could not be read correctly.\n");
        printf("Changes will NOT be saved so your existing files are protected.\n");
        printf("Fix or delete the file, then run the program again.\n");
    }
}

/**
 * Writes all the accounts to the accounts file
 * @param none
 * @return N/A
 * @pre none
 */
void saveAccounts() {
    FILE *fp;
    int i;
    int w;

    fp = fopen(ACCOUNTS_FILE, "w");

    if (fp == NULL) {
        printf("\nWarning: could not save %s.\n", ACCOUNTS_FILE);
        return;
    }

    fprintf(fp, "%d\n", nextAccountNumber);

    for (i = 0; i < numAccounts; i++) {
        fprintf(fp, "%d %s %s", accounts[i].accountNumber, accounts[i].pin, accounts[i].contactNumber);

        for (w = 0; w < NUM_WALLETS; w++) {
            fprintf(fp, " %.2f", accounts[i].wallets[w]);
        }

        fprintf(fp, "\n%s\n", accounts[i].name);
    }

    fclose(fp);
}

/**
 * Writes all the exchange rates to the rates file
 * @param none
 * @return N/A
 * @pre none
 */
void saveRates() {
    FILE *fp;
    int i;

    fp = fopen(RATES_FILE, "w");

    if (fp == NULL) {
        printf("\nWarning: could not save %s.\n", RATES_FILE);
        return;
    }

    for (i = 0; i < NUM_WALLETS; i++) {
        fprintf(fp, "%s %.4f\n", rates[i].code, rates[i].rate);
    }

    fclose(fp);
}

/**
 * Saves the accounts and rates unless a data file had a problem when loading
 * @param none
 * @return N/A
 * @pre none
 */
void saveData() {
    if (dataOk == 0) {
        return;
    }

    saveAccounts();
    saveRates();
}

/*
 * [MCO2] Checks if a year is a leap year
 * @param year year to check
 * @return 1 if the year is a leap year or 0 if not
 * @pre none
 */
int isLeapYear(int year) {
    if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
        return 1;
    }

    return 0;
}

/*
 * [MCO2] Gets the number of days in a month
 * @param month month number from 1 to 12
 * @param year year of the month which is used to check for leap years
 * @return number of days in the month
 * @pre month must be from 1 to 12
 */
int daysInMonth(int month, int year) {
    if (month == 2) {
        if (isLeapYear(year) == 1) {
            return 29;
        }

        return 28;
    }

    if (month == 4 || month == 6 || month == 9 || month == 11) {
        return 30;
    }

    return 31;
}

/*
 * [MCO2] Checks if a day, month, and year make a real calendar date from year 1900 to 2100
 * @param day day of the month
 * @param month month number from 1 to 12
 * @param year year of the date
 * @return 1 if the date is valid or 0 if not
 * @pre none
 */
int isValidDate(int day, int month, int year) {
    if (year < 1900 || year > 2100) {
        return 0;
    }

    if (month < 1 || month > 12) {
        return 0;
    }

    if (day < 1 || day > daysInMonth(month, year)) {
        return 0;
    }

    return 1;
}

/*
 * [MCO2] Splits one line of the CSV file into its fields using the commas
 * @param line text of one CSV row without the newline
 * @param fields array where the text of each column is saved
 * @return number of columns found in the line
 * @pre line must end with the end character and fields must have room for CSV_COLUMNS fields
 */
int splitCsvLine(char line[], char fields[][CSV_FIELD_SIZE]) {
    int i;
    int count;
    int length;

    count = 0;
    length = 0;
    fields[0][0] = '\0';

    for (i = 0; line[i] != '\0'; i++) {
        if (line[i] == ',') {
            if (count < CSV_COLUMNS) {
                fields[count][length] = '\0';
            }

            count++;
            length = 0;

            if (count < CSV_COLUMNS) {
                fields[count][0] = '\0';
            }
        } else if (count < CSV_COLUMNS && length < CSV_FIELD_SIZE - 1) {
            fields[count][length] = line[i];
            length++;
        }
    }

    if (count < CSV_COLUMNS) {
        fields[count][length] = '\0';
    }

    count++;

    return count;
}

/*
 * [MCO2] Converts a 10 digit account number in text form into a number
 * @param text account number as text
 * @return the account number or -1 if it is not exactly 10 digits
 * @pre text must not be null
 */
long long parseAccountNumber(char text[]) {
    int i;
    long long value;

    if (strlen(text) != ACCOUNT_DIGITS) {
        return -1;
    }

    value = 0;

    for (i = 0; i < ACCOUNT_DIGITS; i++) {
        if (text[i] < '0' || text[i] > '9') {
            return -1;
        }

        value = value * 10 + (text[i] - '0');
    }

    return value;
}

/*
 * [MCO2] Checks a dd/mm/yyyy date text and saves it in the given row of the bank rows
 * @param text date as text
 * @param row position in the bankRows array where the date is saved
 * @return 1 if the date is valid or 0 if not (the reason is saved in csvError)
 * @pre row must be a valid index of bankRows
 */
int parseDate(char text[], int row) {
    int i;
    int slashes;
    int day;
    int month;
    int year;

    slashes = 0;

    if (strlen(text) > 10) {
        sprintf(csvError, "invalid date format '%s'", text);
        return 0;
    }

    for (i = 0; text[i] != '\0'; i++) {
        if (text[i] == '/') {
            slashes++;
        } else if (text[i] < '0' || text[i] > '9') {
            sprintf(csvError, "invalid date format '%s'", text);
            return 0;
        }
    }

    if (slashes != 2 || sscanf(text, "%d/%d/%d", &day, &month, &year) != 3) {
        sprintf(csvError, "invalid date format '%s'", text);
        return 0;
    }

    if (isValidDate(day, month, year) == 0) {
        sprintf(csvError, "invalid date '%s'", text);
        return 0;
    }

    bankRows[row].date.day = day;
    bankRows[row].date.month = month;
    bankRows[row].date.year = year;

    return 1;
}

/*
 * [MCO2] Checks a hh:mm:ss time text in 24-hour format and saves it in the given row of the bank rows
 * @param text time as text
 * @param row position in the bankRows array where the time is saved
 * @return 1 if the time is valid or 0 if not (the reason is saved in csvError)
 * @pre row must be a valid index of bankRows
 */
int parseTime(char text[], int row) {
    int i;
    int colons;
    int hour;
    int minute;
    int second;

    colons = 0;

    if (strlen(text) > 8) {
        sprintf(csvError, "invalid time format '%s'", text);
        return 0;
    }

    for (i = 0; text[i] != '\0'; i++) {
        if (text[i] == ':') {
            colons++;
        } else if (text[i] < '0' || text[i] > '9') {
            sprintf(csvError, "invalid time format '%s'", text);
            return 0;
        }
    }

    if (colons != 2 || sscanf(text, "%d:%d:%d", &hour, &minute, &second) != 3) {
        sprintf(csvError, "invalid time format '%s'", text);
        return 0;
    }

    if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
        sprintf(csvError, "invalid time '%s'", text);
        return 0;
    }

    bankRows[row].hour = hour;
    bankRows[row].minute = minute;
    bankRows[row].second = second;

    return 1;
}

/*
 * [MCO2] Checks if a text is a plain money number that is not longer than 15 characters
 * @param text amount as text
 * @return 1 if the text is a valid money value or 0 if not
 * @pre text must not be null
 */
int isValidMoney(char text[]) {
    if (strlen(text) > 15) {
        return 0;
    }

    return isPlainNumber(text);
}

/*
 * [MCO2] Changes all the letters of a text to lowercase
 * @param text text to be changed
 * @return N/A
 * @pre text must not be null
 */
void toLowerText(char text[]) {
    int i;

    for (i = 0; text[i] != '\0'; i++) {
        if (text[i] >= 'A' && text[i] <= 'Z') {
            text[i] = text[i] + 32;
        }
    }
}

/*
 * [MCO2] Changes all the letters of a text to uppercase
 * @param text text to be changed
 * @return N/A
 * @pre text must not be null
 */
void toUpperText(char text[]) {
    int i;

    for (i = 0; text[i] != '\0'; i++) {
        if (text[i] >= 'a' && text[i] <= 'z') {
            text[i] = text[i] - 32;
        }
    }
}

/*
 * [MCO2] Validates the 8 fields of a CSV row (missing values, account number, date, time, currency, amount, type, balance) and converts them into the given row of the bank rows
 * @param fields text of each column of the CSV row
 * @param row position in the bankRows array where the converted row is saved
 * @return 1 if every field is valid or 0 if not (the reason is saved in csvError)
 * @pre fields must be filled by splitCsvLine and row must be a valid index of bankRows
 */
int convertRow(char fields[][CSV_FIELD_SIZE], int row) {
    int i;
    long long accountNumber;
    double amount;
    double balance;

    for (i = 0; i < CSV_COLUMNS; i++) {
        trimSpaces(fields[i]);

        if (fields[i][0] == '\0') {
            sprintf(csvError, "missing value in column %d (%s)", i + 1, columnNames[i]);
            return 0;
        }
    }

    accountNumber = parseAccountNumber(fields[1]);

    if (accountNumber == -1) {
        sprintf(csvError, "invalid account number '%s'", fields[1]);
        return 0;
    }

    if (parseDate(fields[2], row) == 0) {
        return 0;
    }

    if (parseTime(fields[3], row) == 0) {
        return 0;
    }

    setRowStamp(row);

    toUpperText(fields[4]);

    if (strlen(fields[4]) != 3) {
        sprintf(csvError, "invalid currency '%s'", fields[4]);
        return 0;
    }

    for (i = 0; i < 3; i++) {
        if (fields[4][i] < 'A' || fields[4][i] > 'Z') {
            sprintf(csvError, "invalid currency '%s'", fields[4]);
            return 0;
        }
    }

    if (isValidMoney(fields[5]) == 0) {
        sprintf(csvError, "invalid amount '%s'", fields[5]);
        return 0;
    }

    amount = roundToCentavos(atof(fields[5]));

    if (amount < 0.01 || amount > MAX_BALANCE) {
        sprintf(csvError, "amount out of range '%s'", fields[5]);
        return 0;
    }

    toLowerText(fields[6]);

    if (strcmp(fields[6], "deposit") != 0 && strcmp(fields[6], "withdrawal") != 0) {
        sprintf(csvError, "invalid transaction type '%s'", fields[6]);
        return 0;
    }

    if (isValidMoney(fields[7]) == 0) {
        sprintf(csvError, "invalid current balance '%s'", fields[7]);
        return 0;
    }

    balance = roundToCentavos(atof(fields[7]));

    if (balance > MAX_BALANCE) {
        sprintf(csvError, "current balance out of range '%s'", fields[7]);
        return 0;
    }

    strcpy(bankRows[row].accountName, fields[0]);
    bankRows[row].accountNumber = accountNumber;
    strcpy(bankRows[row].currency, fields[4]);
    bankRows[row].amount = amount;
    strcpy(bankRows[row].type, fields[6]);
    bankRows[row].balance = balance;

    return 1;
}

/*
 * [MCO2] Counts a CSV row error and writes its reason to the validation log
 * @param logFile validation log file which can be NULL if the log could not be created
 * @param lineNumber line number of the bad row in the CSV file
 * @return N/A
 * @pre csvError must already hold the reason of the error
 */
void logCsvError(FILE *logFile, int lineNumber) {
    csvErrorRows++;

    if (logFile != NULL) {
        fprintf(logFile, "Line %d: %s\n", lineNumber, csvError);
    }
}

/*
 * [MCO2] Reads the bank transactions CSV file, logs the total rows and the errors, removes bad rows and non-PHP rows, and keeps the PHP rows in memory
 * @return N/A
 * @pre none
 */
void loadCsvFile() {
    FILE *fp;
    FILE *logFile;
    char line[CSV_LINE_SIZE];
    char fields[CSV_COLUMNS][CSV_FIELD_SIZE];
    int lineNumber;

    numBankRows = 0;
    csvTotalRows = 0;
    csvErrorRows = 0;
    csvNonPhpRows = 0;
    csvLoaded = 0;

    fp = fopen(CSV_FILE, "r");

    if (fp == NULL) {
        printf("\nCould not open %s.\n", CSV_FILE);
        return;
    }

    logFile = fopen(LOG_FILE, "w");

    if (logFile == NULL) {
        printf("\nWarning: could not create %s.\n", LOG_FILE);
    }

    if (fgets(line, sizeof(line), fp) == NULL) {
        printf("\n%s is empty.\n", CSV_FILE);

        if (logFile != NULL) {
            fprintf(logFile, "%s is empty.\n", CSV_FILE);
            fclose(logFile);
        }

        fclose(fp);
        return;
    }

    lineNumber = 1;

    while (fgets(line, sizeof(line), fp) != NULL) {
        lineNumber++;
        removeNewline(line);

        if (line[0] == '\0') {
            continue;
        }

        csvTotalRows++;

        if (numBankRows >= MAX_BANK_ROWS) {
            strcpy(csvError, "row limit reached so the row was not stored");
            logCsvError(logFile, lineNumber);
        } else if (splitCsvLine(line, fields) != CSV_COLUMNS) {
            strcpy(csvError, "wrong number of columns");
            logCsvError(logFile, lineNumber);
        } else if (convertRow(fields, numBankRows) == 0) {
            logCsvError(logFile, lineNumber);
        } else if (strcmp(bankRows[numBankRows].currency, "PHP") == 0) {
            numBankRows++;
        } else {
            csvNonPhpRows++;
        }
    }

    fclose(fp);

    if (logFile != NULL) {
        fprintf(logFile, "\nTotal rows read: %d\n", csvTotalRows);
        fprintf(logFile, "Rows with errors (removed): %d\n", csvErrorRows);
        fprintf(logFile, "Non-PHP rows (removed): %d\n", csvNonPhpRows);
        fprintf(logFile, "PHP rows kept: %d\n", numBankRows);
        fclose(logFile);
    }

    csvLoaded = 1;

    printf("\nProcessing dataset... (%d rows loaded, %d filtered for PHP currency)\n", csvTotalRows, numBankRows);
    printf("Rows with errors removed: %d\n", csvErrorRows);
    printf("Non-PHP rows removed: %d\n", csvNonPhpRows);
    printf("Details saved to %s\n", LOG_FILE);
}

/*
 * [MCO2] Shows the generate reports menu
 * @return N/A
 * @pre none
 */
void showReportsMenu() {
    printf("\n========================================\n");
    printf("        GENERATE REPORTS\n");
    printf("========================================\n");
    printf("[1] Load the file\n");
    printf("[2] Generate Reports\n");
    printf("[3] Back\n");
    printf("========================================\n");
}

/*
 * [MCO2] Runs the generate reports menu where the file is loaded first before the reports can be generated
 * @return N/A
 * @pre none
 */
void runReports() {
    int choice;
    int back;

    back = 0;

    while (back == 0) {
        showReportsMenu();
        choice = readMenuChoice("Enter choice: ", 1, 3);

        if (choice == 1) {
            loadCsvFile();
        } else if (choice == 2) {
            if (csvLoaded == 0) {
                printf("\nPlease load the file first.\n");
            } else {
                generateReports();
            }
        } else {
            back = 1;
        }
    }
}

/*
 * [MCO2] Converts a date into a number in yyyymmdd form so dates can be compared
 * @param date date to convert
 * @return the date as a yyyymmdd number
 * @pre date must be a valid date
 */
int dateToNumber(struct Date date) {
    return date.year * 10000 + date.month * 100 + date.day;
}

/*
 * [MCO2] Counts the days from January 1, 1900 up to the given date, which is used to get the number of days between two dates
 * @param date date to convert
 * @return number of days counted until the date
 * @pre date must be a valid date with a year of 1900 or later
 */
int dateToDayCount(struct Date date) {
    int total;
    int year;
    int month;

    total = 0;

    for (year = 1900; year < date.year; year++) {
        total = total + 365;

        if (isLeapYear(year) == 1) {
            total++;
        }
    }

    for (month = 1; month < date.month; month++) {
        total = total + daysInMonth(month, date.year);
    }

    total = total + date.day;

    return total;
}

/*
 * [MCO2] Computes the date number and the date-and-time stamp of a bank row, which is used to order transactions
 * @param row position of the row in the bankRows array
 * @return N/A
 * @pre the date and time of the row must already be parsed
 */
void setRowStamp(int row) {
    int seconds;

    seconds = bankRows[row].hour * 3600 + bankRows[row].minute * 60 + bankRows[row].second;
    bankRows[row].dateNumber = dateToNumber(bankRows[row].date);
    bankRows[row].stamp = bankRows[row].dateNumber;
    bankRows[row].stamp = bankRows[row].stamp * 100000 + seconds;
}

/*
 * [MCO2] Adds thousands separators to a number in text form and keeps the negative sign and the decimals
 * @param plain number as text without commas
 * @param result array where the text with commas is saved
 * @return N/A
 * @pre result must be big enough to hold plain and its commas
 */
void addCommas(char plain[], char result[]) {
    int i;
    int j;
    int start;
    int wholeLength;
    int length;

    length = strlen(plain);
    start = 0;
    j = 0;

    if (plain[0] == '-') {
        result[j] = '-';
        j++;
        start = 1;
    }

    wholeLength = 0;

    while (plain[start + wholeLength] != '\0' && plain[start + wholeLength] != '.') {
        wholeLength++;
    }

    for (i = 0; i < wholeLength; i++) {
        result[j] = plain[start + i];
        j++;

        if (i < wholeLength - 1 && (wholeLength - i - 1) % 3 == 0) {
            result[j] = ',';
            j++;
        }
    }

    for (i = start + wholeLength; i < length; i++) {
        result[j] = plain[i];
        j++;
    }

    result[j] = '\0';
}

/*
 * [MCO2] Formats an amount with 2 decimal places and commas (and avoids showing -0.00)
 * @param value amount to format
 * @param result array where the formatted text is saved
 * @return N/A
 * @pre result must be big enough to hold the formatted text
 */
void formatMoney(double value, char result[]) {
    char plain[64];

    if (value > -0.005 && value < 0) {
        value = 0;
    }

    sprintf(plain, "%.2f", value);
    addCommas(plain, result);
}

/*
 * [MCO2] Formats a whole number with commas
 * @param value number to format
 * @param result array where the formatted text is saved
 * @return N/A
 * @pre result must be big enough to hold the formatted text
 */
void formatWhole(int value, char result[]) {
    char plain[32];

    sprintf(plain, "%d", value);
    addCommas(plain, result);
}

/*
 * [MCO2] Gets the current date as dd/mm/yyyy and the current time as hh:mm:ss in 24-hour format
 * @param dateText array where the date text is saved
 * @param timeText array where the time text is saved
 * @return N/A
 * @pre dateText and timeText must each hold at least 16 characters
 */
void getCurrentDateTime(char dateText[], char timeText[]) {
    time_t now;
    struct tm *timeInfo;

    now = time(NULL);
    timeInfo = localtime(&now);
    strftime(dateText, 16, "%d/%m/%Y", timeInfo);
    strftime(timeText, 16, "%H:%M:%S", timeInfo);
}

/*
 * [MCO2] Adds a transaction to the bank transactions CSV file with the current date and time and creates the file with its header if it does not exist yet (REQ-0001 and REQ-0002)
 * @param accountNumber account number that owns the transaction
 * @param currency 3 letter currency code
 * @param amount amount of the transaction and it is negative for a withdrawal
 * @param balanceAfter wallet balance after the transaction
 * @return N/A
 * @pre the account must be registered in the accounts array and currency must not be null
 */
void saveTransactionToCsv(int accountNumber, char currency[], double amount, double balanceAfter) {
    FILE *fp;
    int index;
    int i;
    char name[100];
    char dateText[16];
    char timeText[16];
    char type[16];
    double shown;

    if (accountNumber < FIRST_ACCOUNT_NUMBER) {
        return;
    }

    index = findAccountByNumber(accountNumber);

    if (index == -1) {
        return;
    }

    for (i = 0; accounts[index].name[i] != '\0'; i++) {
        name[i] = accounts[index].name[i];

        if (name[i] == ',') {
            name[i] = ' ';
        }
    }

    name[i] = '\0';

    if (amount < 0) {
        strcpy(type, "withdrawal");
        shown = -amount;
    } else {
        strcpy(type, "deposit");
        shown = amount;
    }

    fp = fopen(CSV_FILE, "r");

    if (fp == NULL) {
        fp = fopen(CSV_FILE, "w");

        if (fp == NULL) {
            printf("\nWarning: could not save %s.\n", CSV_FILE);
            return;
        }

        fprintf(fp, "account_name,account_number,date,time,currency,amount,type,current_balance\n");
    }

    fclose(fp);

    fp = fopen(CSV_FILE, "a");

    if (fp == NULL) {
        printf("\nWarning: could not save %s.\n", CSV_FILE);
        return;
    }

    getCurrentDateTime(dateText, timeText);
    fprintf(fp, "%s,%d,%s,%s,%s,%.2f,%s,%.2f\n", name, accountNumber, dateText, timeText, currency, shown, type, balanceAfter);
    fclose(fp);

    csvLoaded = 0;
}

/*
 * [MCO2] Looks through the bank transactions CSV file for the highest account number
 * @return the highest account number found or 0 if the file is missing or has none
 * @pre none
 */
long long findHighestAccountNumber() {
    FILE *fp;
    char line[CSV_LINE_SIZE];
    char fields[CSV_COLUMNS][CSV_FIELD_SIZE];
    long long highest;
    long long number;

    highest = 0;
    fp = fopen(CSV_FILE, "r");

    if (fp == NULL) {
        return 0;
    }

    while (fgets(line, sizeof(line), fp) != NULL) {
        removeNewline(line);

        if (splitCsvLine(line, fields) == CSV_COLUMNS) {
            trimSpaces(fields[1]);
            number = parseAccountNumber(fields[1]);

            if (number > highest) {
                highest = number;
            }
        }
    }

    fclose(fp);

    return highest;
}

/*
 * [MCO2] Gets the next 10-digit account number which is one more than the highest existing account number in the CSV file or in memory (REQ-0001)
 * @return the next account number or -1 if the account number limit was reached
 * @pre none
 */
int getNextAccountNumber() {
    long long highest;

    highest = findHighestAccountNumber();

    if (nextAccountNumber - 1 > highest) {
        highest = nextAccountNumber - 1;
    }

    if (highest < FIRST_ACCOUNT_NUMBER - 1) {
        highest = FIRST_ACCOUNT_NUMBER - 1;
    }

    if (highest >= MAX_ACCOUNT_NUMBER - 1) {
        return -1;
    }

    return highest + 1;
}

/*
 * [MCO2] Looks for the summary of an account in the summaries array
 * @param accountNumber 10-digit account number to look for
 * @return position of the summary or -1 if not found
 * @pre none
 */
int findSummary(long long accountNumber) {
    int i;

    for (i = 0; i < numSummaries; i++) {
        if (summaries[i].accountNumber == accountNumber) {
            return i;
        }
    }

    return -1;
}

/*
 * [MCO2] Builds the first and last row of every account, finds the latest transaction in the file, and works out the last month and year used by the reports
 * @return N/A
 * @pre the CSV file must be loaded and numBankRows must be more than 0
 */
void prepareReports() {
    int i;
    int s;

    numSummaries = 0;
    asOfRow = 0;

    for (i = 0; i < numBankRows; i++) {
        if (bankRows[i].stamp >= bankRows[asOfRow].stamp) {
            asOfRow = i;
        }

        s = findSummary(bankRows[i].accountNumber);

        if (s == -1) {
            if (numSummaries < MAX_ACCOUNTS) {
                summaries[numSummaries].accountNumber = bankRows[i].accountNumber;
                summaries[numSummaries].firstRow = i;
                summaries[numSummaries].lastRow = i;
                numSummaries++;
            }
        } else {
            if (bankRows[i].stamp < bankRows[summaries[s].firstRow].stamp) {
                summaries[s].firstRow = i;
            }

            if (bankRows[i].stamp >= bankRows[summaries[s].lastRow].stamp) {
                summaries[s].lastRow = i;
            }
        }
    }

    lastMonth = bankRows[asOfRow].date.month - 1;
    lastYear = bankRows[asOfRow].date.year;

    if (lastMonth == 0) {
        lastMonth = 12;
        lastYear--;
    }
}

/*
 * [MCO2] Gets the balance of an account at the end of a day using its latest transaction on or before that day
 * @param accountNumber 10-digit account number
 * @param dateNumber date in yyyymmdd form
 * @return the end-of-day balance or 0 if the account has no transaction yet
 * @pre the CSV file must be loaded
 */
double endOfDayBalance(long long accountNumber, int dateNumber) {
    int i;
    long long endStamp;
    long long bestStamp;
    double balance;

    endStamp = dateNumber;
    endStamp = endStamp * 100000 + 99999;
    bestStamp = -1;
    balance = 0;

    for (i = 0; i < numBankRows; i++) {
        if (bankRows[i].accountNumber == accountNumber && bankRows[i].stamp <= endStamp && bankRows[i].stamp >= bestStamp) {
            bestStamp = bankRows[i].stamp;
            balance = bankRows[i].balance;
        }
    }

    return balance;
}

/*
 * [MCO2] Computes the average daily balance of an account for the last month (sum of the end-of-day balances divided by the number of days in the month)
 * @param accountNumber 10-digit account number
 * @return the average daily balance
 * @pre prepareReports must have been called
 */
double averageDailyBalance(long long accountNumber) {
    int day;
    int days;
    double total;

    days = daysInMonth(lastMonth, lastYear);
    total = 0;

    for (day = 1; day <= days; day++) {
        total = total + endOfDayBalance(accountNumber, lastYear * 10000 + lastMonth * 100 + day);
    }

    return total / days;
}

/*
 * [MCO2] Sorts a list of summary positions from highest to lowest key using insertion sort and uses the lower account number to break ties
 * @param list positions in the summaries array to be sorted
 * @param count number of positions in list
 * @param keys value to sort by for each summary position
 * @return N/A
 * @pre every position in list must be a valid index of summaries and keys
 */
void sortIndexesDescending(int list[], int count, double keys[]) {
    int i;
    int j;
    int current;

    for (i = 1; i < count; i++) {
        current = list[i];
        j = i - 1;

        while (j >= 0 && (keys[list[j]] < keys[current] || (keys[list[j]] == keys[current] && summaries[list[j]].accountNumber > summaries[current].accountNumber))) {
            list[j + 1] = list[j];
            j--;
        }

        list[j + 1] = current;
    }
}

/*
 * [MCO2] Generates Report 1 (Banking Privilege Tiers) for accounts with an average daily balance of Php 100,000.00 or more, shows the first 2 rows, and saves the full table sorted by average daily balance in report1_banking_privilege_tiers.csv (REQ-0006)
 * @return N/A
 * @pre prepareReports must have been called
 */
void generateReport1() {
    FILE *fp;
    int list[MAX_ACCOUNTS];
    double adb[MAX_ACCOUNTS];
    int count;
    int s;
    int i;
    int age;
    int asOfDays;
    char tier[10];
    char ageText[20];
    char balanceText[40];
    char adbText[40];

    count = 0;

    for (s = 0; s < numSummaries; s++) {
        adb[s] = averageDailyBalance(summaries[s].accountNumber);

        if (adb[s] >= MIN_ADB) {
            list[count] = s;
            count++;
        }
    }

    sortIndexesDescending(list, count, adb);

    fp = fopen(REPORT1_FILE, "w");

    if (fp == NULL) {
        printf("\nCould not create %s.\n", REPORT1_FILE);
        return;
    }

    fprintf(fp, "AccountName,AccountAge,CurrentBalance,AverageDailyBalance,PrivilegeTier\n");
    printf("\nReport 1: Banking Privilege Tiers\n");
    printf("%-22s | %-10s | %-16s | %-19s | %s\n", "AccountName", "AccountAge", "CurrentBalance", "AverageDailyBalance", "PrivilegeTier");

    asOfDays = dateToDayCount(bankRows[asOfRow].date);

    for (i = 0; i < count; i++) {
        s = list[i];
        age = asOfDays - dateToDayCount(bankRows[summaries[s].firstRow].date);

        if (adb[s] < SILVER_LIMIT) {
            strcpy(tier, "Silver");
        } else if (adb[s] < GOLD_LIMIT) {
            strcpy(tier, "Gold");
        } else {
            strcpy(tier, "Platinum");
        }

        formatWhole(age, ageText);
        formatMoney(bankRows[summaries[s].lastRow].balance, balanceText);
        formatMoney(adb[s], adbText);

        fprintf(fp, "%s,\"%s\",\"%s\",\"%s\",%s\n", bankRows[summaries[s].firstRow].accountName, ageText, balanceText, adbText, tier);

        if (i < 2) {
            printf("%-22s | %-10s | %-16s | %-19s | %s\n", bankRows[summaries[s].firstRow].accountName, ageText, balanceText, adbText, tier);
        }
    }

    fclose(fp);
    printf("(Full table exported to %s)\n", REPORT1_FILE);
}

/*
 * [MCO2] Computes the interest earned by a balance when the daily interest is added to the balance every day
 * @param balance starting balance
 * @param days number of days the balance earns interest
 * @return the interest earned rounded to 2 decimals
 * @pre balance must not be negative and days must not be negative
 */
double computeCompoundInterest(double balance, int days) {
    int day;
    double current;

    current = balance;

    for (day = 1; day <= days; day++) {
        current = current + current * INTEREST_RATE / DAYS_IN_YEAR;
    }

    return roundToCentavos(current - balance);
}

/*
 * [MCO2] Generates Report 2 (Interest Expense Projection) for accounts with a current balance of Php 10,000.00 or more, shows the first 2 rows, saves the full table sorted by expected interest in report2_interest_expense_projection.csv, and totals the interest for the summary (REQ-0007)
 * @return N/A
 * @pre prepareReports must have been called
 */
void generateReport2() {
    FILE *fp;
    int list[MAX_ACCOUNTS];
    double interest[MAX_ACCOUNTS];
    int count;
    int s;
    int i;
    double balance;
    char balanceText[40];
    char interestText[40];

    count = 0;
    totalInterestExpense = 0;

    for (s = 0; s < numSummaries; s++) {
        balance = bankRows[summaries[s].lastRow].balance;

        if (balance >= MIN_INTEREST_BALANCE) {
            interest[s] = computeCompoundInterest(balance, INTEREST_DAYS);
            totalInterestExpense = roundToCentavos(totalInterestExpense + interest[s]);
            list[count] = s;
            count++;
        }
    }

    sortIndexesDescending(list, count, interest);

    fp = fopen(REPORT2_FILE, "w");

    if (fp == NULL) {
        printf("\nCould not create %s.\n", REPORT2_FILE);
        return;
    }

    fprintf(fp, "AccountName,CurrentBalance,ExpectedInterest\n");
    printf("\nReport 2: Interest Expense Projection\n");
    printf("%-22s | %-16s | %s\n", "AccountName", "CurrentBalance", "ExpectedInterest");

    for (i = 0; i < count; i++) {
        s = list[i];
        formatMoney(bankRows[summaries[s].lastRow].balance, balanceText);
        formatMoney(interest[s], interestText);

        fprintf(fp, "%s,\"%s\",\"%s\"\n", bankRows[summaries[s].firstRow].accountName, balanceText, interestText);

        if (i < 2) {
            printf("%-22s | %-16s | %s\n", bankRows[summaries[s].firstRow].accountName, balanceText, interestText);
        }
    }

    fclose(fp);
    printf("(Full table exported to %s)\n", REPORT2_FILE);
}

/*
 * [MCO2] Looks for the daily record of an account on a given date
 * @param accountNumber 10-digit account number
 * @param dateNumber date in yyyymmdd form
 * @return position of the daily record or -1 if not found
 * @pre none
 */
int findDailyRecord(long long accountNumber, int dateNumber) {
    int i;

    for (i = numDaily - 1; i >= 0; i--) {
        if (dailyRecords[i].accountNumber == accountNumber && dailyRecords[i].dateNumber == dateNumber) {
            return i;
        }
    }

    return -1;
}

/*
 * [MCO2] Groups the transactions by account and day, then computes the total withdrawals, number of deposits, total deposits, turnover velocity, and the suspicious flag of each day
 * @return N/A
 * @pre the CSV file must be loaded
 */
void buildDailyRecords() {
    int i;
    int d;

    numDaily = 0;

    for (i = 0; i < numBankRows; i++) {
        d = findDailyRecord(bankRows[i].accountNumber, bankRows[i].dateNumber);

        if (d == -1) {
            d = numDaily;
            strcpy(dailyRecords[d].name, bankRows[i].accountName);
            dailyRecords[d].accountNumber = bankRows[i].accountNumber;
            dailyRecords[d].date = bankRows[i].date;
            dailyRecords[d].dateNumber = bankRows[i].dateNumber;
            dailyRecords[d].totalWithdrawal = 0;
            dailyRecords[d].numDeposits = 0;
            dailyRecords[d].totalDeposit = 0;
            numDaily++;
        }

        if (strcmp(bankRows[i].type, "deposit") == 0) {
            dailyRecords[d].numDeposits++;
            dailyRecords[d].totalDeposit = roundToCentavos(dailyRecords[d].totalDeposit + bankRows[i].amount);
        } else {
            dailyRecords[d].totalWithdrawal = roundToCentavos(dailyRecords[d].totalWithdrawal + bankRows[i].amount);
        }
    }

    for (d = 0; d < numDaily; d++) {
        if (dailyRecords[d].totalDeposit > 0) {
            dailyRecords[d].velocity = dailyRecords[d].totalWithdrawal / dailyRecords[d].totalDeposit * 100;
        } else {
            dailyRecords[d].velocity = 0;
        }

        if (dailyRecords[d].totalWithdrawal >= SUSPICIOUS_WITHDRAWAL || dailyRecords[d].velocity >= SUSPICIOUS_VELOCITY) {
            dailyRecords[d].suspicious = 1;
        } else {
            dailyRecords[d].suspicious = 0;
        }
    }
}

/*
 * [MCO2] Compares two daily records to decide which one comes first in Report 3 (older date first, then Suspicious before Normal, then by name and account number)
 * @param a position of the first daily record
 * @param b position of the second daily record
 * @return 1 if record a must come before record b or 0 if not
 * @pre a and b must be valid indexes of dailyRecords
 */
int report3Before(int a, int b) {
    int compare;

    if (dailyRecords[a].dateNumber != dailyRecords[b].dateNumber) {
        return dailyRecords[a].dateNumber < dailyRecords[b].dateNumber;
    }

    if (dailyRecords[a].suspicious != dailyRecords[b].suspicious) {
        return dailyRecords[a].suspicious > dailyRecords[b].suspicious;
    }

    compare = strcmp(dailyRecords[a].name, dailyRecords[b].name);

    if (compare != 0) {
        return compare < 0;
    }

    return dailyRecords[a].accountNumber < dailyRecords[b].accountNumber;
}

/*
 * [MCO2] Generates Report 3 (Suspicious Client Behavior) with the daily withdrawals, deposits, turnover velocity, and risk flag of each account, shows the first 2 rows, and saves the full table sorted by date in report3_suspicious_client_behavior.csv (REQ-0008)
 * @return N/A
 * @pre the CSV file must be loaded
 */
void generateReport3() {
    FILE *fp;
    int order[MAX_BANK_ROWS];
    int i;
    int j;
    int d;
    int current;
    char dateText[16];
    char withdrawalText[40];
    char countText[20];
    char depositText[40];
    char velocityText[40];
    char flag[12];

    buildDailyRecords();

    for (i = 0; i < numDaily; i++) {
        order[i] = i;
    }

    for (i = 1; i < numDaily; i++) {
        current = order[i];
        j = i - 1;

        while (j >= 0 && report3Before(current, order[j]) == 1) {
            order[j + 1] = order[j];
            j--;
        }

        order[j + 1] = current;
    }

    fp = fopen(REPORT3_FILE, "w");

    if (fp == NULL) {
        printf("\nCould not create %s.\n", REPORT3_FILE);
        return;
    }

    fprintf(fp, "AccountName,Date,TotalWithdrawalAmounts,NumberOfDeposits,TotalDepositAmounts,TurnoverVelocity(%%),RiskFlagLevel\n");
    printf("\nReport 3: Suspicious Client Behavior\n");
    printf("%-22s | %-10s | %-20s | %-9s | %-18s | %-12s | %s\n", "AccountName", "Date", "TotalWithdrawal", "Deposits", "TotalDeposit", "Velocity(%)", "RiskFlagLevel");

    for (i = 0; i < numDaily; i++) {
        d = order[i];
        sprintf(dateText, "%02d/%02d/%04d", dailyRecords[d].date.day, dailyRecords[d].date.month, dailyRecords[d].date.year);
        formatMoney(dailyRecords[d].totalWithdrawal, withdrawalText);
        formatWhole(dailyRecords[d].numDeposits, countText);
        formatMoney(dailyRecords[d].totalDeposit, depositText);
        formatMoney(dailyRecords[d].velocity, velocityText);

        if (dailyRecords[d].suspicious == 1) {
            strcpy(flag, "Suspicious");
        } else {
            strcpy(flag, "Normal");
        }

        fprintf(fp, "%s,%s,\"%s\",\"%s\",\"%s\",\"%s\",%s\n", dailyRecords[d].name, dateText, withdrawalText, countText, depositText, velocityText, flag);

        if (i < 2) {
            printf("%-22s | %-10s | %-20s | %-9s | %-18s | %-12s | %s\n", dailyRecords[d].name, dateText, withdrawalText, countText, depositText, velocityText, flag);
        }
    }

    fclose(fp);
    printf("(Full table exported to %s)\n", REPORT3_FILE);
}

/*
 * [MCO2] Looks for the monthly totals of a given month and year
 * @param year month
 * @param month month number from 1 to 12
 * @return position of the month in the monthRows array or -1 if not found
 * @pre none
 */
int findMonthRow(int year, int month) {
    int i;

    for (i = 0; i < numMonths; i++) {
        if (monthRows[i].year == year && monthRows[i].month == month) {
            return i;
        }
    }

    return -1;
}

/*
 * [MCO2] Generates Report 4 (Monthly Cash Flow Summary) with the number of transactions, total deposits, total withdrawals, net cash flow, and cash flow status of each month, shows the first 2 rows, and saves the full table sorted by month in report4_monthly_cash_flow_summary.csv (REQ-0009)
 * @return N/A
 * @pre the CSV file must be loaded
 */
void generateReport4() {
    FILE *fp;
    struct MonthRow temp;
    int i;
    int j;
    int m;
    double net;
    char monthText[10];
    char countText[20];
    char depositText[40];
    char withdrawalText[40];
    char netText[40];
    char status[12];

    numMonths = 0;

    for (i = 0; i < numBankRows; i++) {
        m = findMonthRow(bankRows[i].date.year, bankRows[i].date.month);

        if (m == -1 && numMonths < MAX_MONTHS) {
            m = numMonths;
            monthRows[m].year = bankRows[i].date.year;
            monthRows[m].month = bankRows[i].date.month;
            monthRows[m].count = 0;
            monthRows[m].deposits = 0;
            monthRows[m].withdrawals = 0;
            numMonths++;
        }

        if (m != -1) {
            monthRows[m].count++;

            if (strcmp(bankRows[i].type, "deposit") == 0) {
                monthRows[m].deposits = roundToCentavos(monthRows[m].deposits + bankRows[i].amount);
            } else {
                monthRows[m].withdrawals = roundToCentavos(monthRows[m].withdrawals + bankRows[i].amount);
            }
        }
    }

    for (i = 1; i < numMonths; i++) {
        temp = monthRows[i];
        j = i - 1;

        while (j >= 0 && monthRows[j].year * 100 + monthRows[j].month > temp.year * 100 + temp.month) {
            monthRows[j + 1] = monthRows[j];
            j--;
        }

        monthRows[j + 1] = temp;
    }

    fp = fopen(REPORT4_FILE, "w");

    if (fp == NULL) {
        printf("\nCould not create %s.\n", REPORT4_FILE);
        return;
    }

    fprintf(fp, "Month,NumberOfTransactions,TotalDeposits,TotalWithdrawals,NetCashFlow,CashFlowStatus\n");
    printf("\nReport 4: Monthly Cash Flow Summary\n");
    printf("%-8s | %-12s | %-18s | %-18s | %-16s | %s\n", "Month", "Transactions", "TotalDeposits", "TotalWithdrawals", "NetCashFlow", "CashFlowStatus");

    for (i = 0; i < numMonths; i++) {
        net = roundToCentavos(monthRows[i].deposits - monthRows[i].withdrawals);
        sprintf(monthText, "%04d-%02d", monthRows[i].year, monthRows[i].month);
        formatWhole(monthRows[i].count, countText);
        formatMoney(monthRows[i].deposits, depositText);
        formatMoney(monthRows[i].withdrawals, withdrawalText);
        formatMoney(net, netText);

        if (net >= 0) {
            strcpy(status, "Net Inflow");
        } else {
            strcpy(status, "Net Outflow");
        }

        fprintf(fp, "%s,\"%s\",\"%s\",\"%s\",\"%s\",%s\n", monthText, countText, depositText, withdrawalText, netText, status);

        if (i < 2) {
            printf("%-8s | %-12s | %-18s | %-18s | %-16s | %s\n", monthText, countText, depositText, withdrawalText, netText, status);
        }
    }

    fclose(fp);
    printf("(Full table exported to %s)\n", REPORT4_FILE);
}

/*
 * [MCO2] Saves the key totals (accounts, deposits, withdrawals, balance, and interest expense) in summary.json and shows them (REQ-0010)
 * @return N/A
 * @pre prepareReports and generateReport2 must have been called first so the account summaries and total interest are ready
 */
void generateSummary() {
    FILE *fp;
    int i;
    int s;
    double totalDeposits;
    double totalWithdrawals;
    double totalBalance;

    totalDeposits = 0;
    totalWithdrawals = 0;
    totalBalance = 0;

    for (i = 0; i < numBankRows; i++) {
        if (strcmp(bankRows[i].type, "deposit") == 0) {
            totalDeposits = roundToCentavos(totalDeposits + bankRows[i].amount);
        } else {
            totalWithdrawals = roundToCentavos(totalWithdrawals + bankRows[i].amount);
        }
    }

    for (s = 0; s < numSummaries; s++) {
        totalBalance = roundToCentavos(totalBalance + bankRows[summaries[s].lastRow].balance);
    }

    fp = fopen(SUMMARY_FILE, "w");

    if (fp == NULL) {
        printf("\nCould not create %s.\n", SUMMARY_FILE);
        return;
    }

    fprintf(fp, "{\n");
    fprintf(fp, "  \"total_accounts\": %d,\n", numSummaries);
    fprintf(fp, "  \"total_deposits\": %.2f,\n", totalDeposits);
    fprintf(fp, "  \"total_withdrawals\": %.2f,\n", totalWithdrawals);
    fprintf(fp, "  \"total_balance\": %.2f,\n", totalBalance);
    fprintf(fp, "  \"total_interest_expense\": %.2f\n", totalInterestExpense);
    fprintf(fp, "}\n");
    fclose(fp);

    printf("\nSummary State (%s):\n", SUMMARY_FILE);
    printf("{\"total_accounts\": %d, \"total_deposits\": %.2f, \"total_withdrawals\": %.2f, \"total_balance\": %.2f, \"total_interest_expense\": %.2f}\n", numSummaries, totalDeposits, totalWithdrawals, totalBalance, totalInterestExpense);
}

/*
 * [MCO2] Runs all the report steps and creates Reports 1 to 4 and the summary file
 * @return N/A
 * @pre the CSV file must be loaded
 */
void generateReports() {
    if (numBankRows == 0) {
        printf("\nThere are no PHP transactions to report on.\n");
        return;
    }

    prepareReports();

    printf("\nGenerating reports...\n");
    printf("Outputs saved to individual files...\n");

    generateReport1();
    generateReport2();
    generateReport3();
    generateReport4();
    generateSummary();
}
