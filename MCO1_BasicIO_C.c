#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define MAX_ACCOUNTS 1000
#define MAX_ACCOUNT_NUMBER 999999
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

struct Account accounts[MAX_ACCOUNTS];
struct ExchangeRate rates[NUM_WALLETS];
struct Transaction transactions[MAX_TRANSACTIONS];
int numAccounts = 0;
int numTransactions = 0;
int nextAccountNumber = 1;
int dataOk = 1;

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
        choice = readMenuChoice("Enter choice: ", 1, 4);

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
    int value;

    length = strlen(text);

    if (length < 1 || length > 6) {
        return -1;
    }

    value = 0;

    for (i = 0; i < length; i++) {
        if (text[i] < '0' || text[i] > '9') {
            return -1;
        }

        value = value * 10 + (text[i] - '0');
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
    printf("[4] Logout\n");
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

    if (numAccounts >= MAX_ACCOUNTS || nextAccountNumber > MAX_ACCOUNT_NUMBER) {
        printf("\nThe system is full. Cannot register a new account.\n");
        return;
    }

    printf("\nRegister Account Name\n");
    readName("Account Name: ", name);
    readPin("Create a 4-digit PIN: ", pin);
    readContact("Contact Number: ", contact);

    accounts[numAccounts].accountNumber = nextAccountNumber;
    strcpy(accounts[numAccounts].name, name);
    strcpy(accounts[numAccounts].pin, pin);
    strcpy(accounts[numAccounts].contactNumber, contact);

    for (w = 0; w < NUM_WALLETS; w++) {
        accounts[numAccounts].wallets[w] = 0.00;
    }

    printf("\nAccount \"%s\" registered successfully!\n", name);
    printf("Your account number is: %05d\n", accounts[numAccounts].accountNumber);
    printf("Use your account number to log in.\n");

    nextAccountNumber++;
    numAccounts++;
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
