/*
 * ********************
 * Last names: Bobadilla, Calvez, Casao, Sanico
 * Language: Kotlin
 * Paradigm(s): Object-Oriented Programming
 * ********************
 */


// Pre-processor Directives
import java.util.Scanner


// Classes


/**
 * This is the account class which acts as a template to store the user's account details
 */
class Account (var accNum: Int, var name: String, var pin: String) {
    
    var currBal = 0.0
    
    companion object {
        var numAccs = 0
    }
    
    
    init {
        
        numAccs++
        
    }
    
    /**
     * Performs a deposit on the account's balance.
     * 
     * @param d is the amount deposited
     * @return the success of the operation
     */
    fun deposit(d: Double) {
        currBal += d
    }
    
    /**
     * Performs a withdrawal on the account's balance.
     * 
     * @param d is the amount of withdrawal
     * @return the success of the operation
     */
    fun withdraw(d: Double): Boolean {
        if (currBal < d) {
            return false
        } else {
            currBal -= d 
            return true
        }
    }
    
    /**
     * Retrieves the account number.
     * 
     * @return the account number
     */
    fun getAccNumb() : Int {
        return accNum
    }
    
    /**
     * Retrieves the account name.
     * 
     * @return the account name
     */
    fun getUsername() : String {
        return name
    }
    
    /**
     * Retrieves the account pin.
     * 
     * @return the account pin
     */
    fun getAccPin() : String {
        return pin
    }
    
    /**
     * Retrieves the balance of the account.
     * 
     * @return the account balance
     */
    fun getBal() : Double {
        return currBal
    }
    
    /**
     * Retrieves total number of accounts made.
     * 
     * @return the number of accounts
     */
    fun getNumOfAccs(): Int {
        return numAccs
    }
    
    
}

/**
 * This is the class that acts as a template for each object representation of
 * a currency's details.
 */
class ExcRate (val code: String, val name: String, var rate: Double) {
    
    /**
     * Sets the exchange rate
     * 
     * @param d is the value of the updated exchange rate
     * @return the success of the operation
     */
    fun setExRate(d: Double): Boolean {
        if (d < 0) {
            return false
        } else {
            rate = d
            return true
        }
        
    }
    
    /**
     * Retrieves the currency code
     * 
     * @return the currency code
     */
    fun getExCode(): String {
        return code
    }
    
    /**
     * Retrieves the name of the currency
     * 
     * @return name of the currency
     */
    fun getExName(): String {
        return name
    }
    
    /**
     * Retrieves the exchange rate
     * 
     * @return the exchange rate
     */
    fun getExRate(): Double {
        return rate
    }
    
}

/**
 * This is the class which handles and controls all the transactions of the active account
 */
class AccountCenter (var currAcc: Account) {
    
    
    companion object {
        // Initializing the exchange rates
        val rates = mutableListOf(ExcRate("PHP", "Philippine Peso", 1.00))

        /**
         * Initializes the currency objects to its default exchange rate, respective to the specifications
         * 
         * @return NONE
         */
        fun initializeRates() {
            rates.add(ExcRate("USD", "United States Dollar", 62.00))
            rates.add(ExcRate("JPY", "Japanese Yen", 0.40))
            rates.add(ExcRate("GBP", "British Pound Sterling", 84.00))
            rates.add(ExcRate("EUR", "Euro", 72.00))
            rates.add(ExcRate("CNY", "Chinese Yuan Renminbi", 9.00))
        }
    }
    
    val INTEREST = 0.05
    
    
    // Helper Functions
    
    /*
     * Validates if an input is not empty
     * 
     * @param input is the string input by the user
     * @return a boolean stating if the input was empty or not
     */
    fun validateStringInput(nInput: String): Boolean {
        
        try {
            
            // Trying to test and cause an exception if inputted string is empty.
            if (nInput[0].compareTo('\u0000') == 0) {
                println("Warning: Null Input!!") 
            }
            
        } catch (e: Exception) {
            
            println("\nWarning: Invalid input has caused an error!")
            println("Please retry your action.")
            return false
        }
        
        return true
    }
    
    /**
     * Checks if the current account is the admin
     * 
     * @return a boolean stating if the admin is logged on or otherwise
     */
    fun checkAdmin(): Boolean {
        if (currAcc.getAccNumb() != 1000) {
            return false
        }
        return true
    }
    
    /**
     * Handles user input when asked for a currency
     * 
     * @param input is the user's input of the currency code
     * @return the index of the currency being retrieved from the list
     */
    fun checkCurrency(input: String): Int {
        if (input == "PHP") {
            return 0
        } else if (input == "USD") {
            return 1
        } else if (input == "JPY") {
            return 2
        } else if (input == "GBP") {
            return 3
        } else if (input == "EUR") {
            return 4
        } else if (input == "CNY") {
            return 5
        } else {
            return -1
        }
    }
    
    // A function that returns the exchanged amount, given the base value in Peso, and the index of the target currency conversion.
    fun automaticConversion(base: Double, currIdx: Int): Double = base / rates.get(currIdx).getExRate()
    
    // A function that returns the exchanged amount, given the foreign value not in Peso, and the index of the amount being converted to Peso.
    fun reverseConversion(from: Double, currIdx: Int): Double = from*rates.get(currIdx).getExRate()
    
    /**
     * This is a helper function that manages the user's option to return to the main menu after a transaction
     * 
     * @param sc is the scanner object allowing for basic input
     * @return a boolean value that determines if the user wishes to return to the main menu
     */
    fun returnToMM(sc: Scanner): Boolean {
        
        print("\nBack to Main Menu? (Y/N): ")
        var exitChoice = sc.nextLine()
        
        if (validateStringInput(exitChoice) == false) {
            return false
        } else if (exitChoice.compareTo("N") == 0) {
            return true
        } else if (exitChoice.compareTo("Y") == 0) {
            println("\nReturning to main menu.")
            return false
        } else {
            println("\nWarning: Input unrecognized, defaulting to \"Y\", returning to main menu.")
            return false
        }
        
    }
    
    
    // Core Methods
    
    
    /**
     * This is the menu to deposit funds into an account
     * 
     * @param sc is the scanner object allowing for basic input
     */
    fun deposit(sc: Scanner) {
        
        var depositIn = 0.0
        
        println("\nDeposit Amount")
        println("Account Name: ${currAcc.getUsername()}")
        
        println("Current Balance: %.2f".format(currAcc.getBal()))
        print("Currency: ")
        var currChoice = sc.nextLine()
        
        if (checkCurrency(currChoice) == -1) {
            println("\nWarning: Unrecognized Currency! Returning to Main Menu")
            return
        }
        
        print("\nDeposit Amount: ")
        
        try {
            depositIn = sc.nextDouble()
        } catch (e: Exception) {
            println("\nWarning: Input may not be a number!")
            println("\nDeposit Failed! Please try again!")
            return
        }
        
        if (depositIn <= 0) {
            println("\nWarning: Invalid amount!")
            return
        }
        
        var convertedVal = 0.0
        
        if (currChoice.compareTo("PHP") == 0) {
            currAcc.deposit(depositIn)
            convertedVal = depositIn
        } else {
            convertedVal = reverseConversion(depositIn, checkCurrency(currChoice))
            currAcc.deposit(convertedVal)
        }
        
        
        
        println("\n***")
        println("Account Name: ${currAcc.getUsername()}")
        println("Current Balance (PHP): %.2f".format(currAcc.getBal()))
        println("Deposited Amount (Autoconverted to Target Currency) = " + "%.2f".format(convertedVal))
        
    }

    /*
     * This is the menu to withdraw funds from an account
     * 
     * @param sc is the scanner object allowing for basic input
     * @return NONE
     */
    fun withdraw(sc: Scanner) {
        
        var withdrawIn = 0.0
        
        println("\nWithdraw Amount")
        println("Account Name: ${currAcc.getUsername()}")
        
        println("Current Balance: %.2f".format(currAcc.getBal()))
        print("Currency: ")
        var currChoice = sc.nextLine()
        
        if (checkCurrency(currChoice) == -1) {
            println("\nWarning: Unrecognized Currency! Returning to Main Menu")
            return
        }
        
        print("\nWithdraw Amount: ")
        
        try {
            withdrawIn = sc.nextDouble()
        } catch (e: Exception) {
            println("\nWarning: Input may not be a number!")
            println("\nWithdraw Failed! Please try again!")
            return
        }
        
        if (withdrawIn <= 0) {
            println("\nWarning: Invalid amount!")
            return
        }
        
        var convertedVal = 0.0
        
        if (currChoice.compareTo("PHP") == 0) {
            if (currAcc.withdraw(withdrawIn) == false) {
                println("\nWithdraw Failed! Insufficient Funds!")
                return 
            }
            convertedVal = withdrawIn
        } else {
            convertedVal = reverseConversion(withdrawIn, checkCurrency(currChoice))
            
            if (currAcc.withdraw(convertedVal) == false) {
                println("Withdraw Failed! Insufficient Funds!")
                return 
            }
        }
        
        println("\n***")
        println("Account Name: ${currAcc.getUsername()}")
        println("Current Balance (PHP): %.2f".format(currAcc.getBal()))
        println("Withdrawal Amount = " + "%.2f".format(convertedVal))
        
        
    }

    /*
     * This is the menu to record the exchange rates of this application.
     * 
     * This menu has been programmed to only be accessed by the admin! Username: admin, Pin: 2580
     * 
     * @param sc is the scanner object allowing for basic input
     * @return NONE
     */
    fun recordER(sc: Scanner) {
        
        var inputVal = false
        var choice = 0
        
        if (checkAdmin() == false) {
            println("\nSorry, you do not have the clearance to access this function")
            return 
        }
        
        println("\nRecord Exchange Rate\n")
        
        println("[1] Philippine Peso (PHP)          | Current Rate: 1 PHP = %.2f".format(rates.get(0).getExRate()))
        println("[2] United States Dollar (USD)     | Current Rate: 1 USD = %.2f".format(rates.get(1).getExRate()))
        println("[3] Japanese Yen (JPY)             | Current Rate: 1 JPY = %.2f".format(rates.get(2).getExRate()))
        println("[4] British Pound Sterling (GBP)   | Current Rate: 1 GBP = %.2f".format(rates.get(3).getExRate()))
        println("[5] Euro (EUR)                     | Current Rate: 1 EUR = %.2f".format(rates.get(4).getExRate()))
        println("[6] Chinese Yuan Renminni (CNY)    | Current Rate: 1 CNY = %.2f".format(rates.get(5).getExRate()))
        
        while (inputVal == false) {
            print("\nSelect Foreign Currency: ")
            var currIn = sc.nextLine()
            
            if (currIn.indexOf('[') == -1 || currIn.indexOf(']')  == -1) {
                
                println("\nInvalid input! Encolse choice in \"[]\" (ex. [1]) ")
                
            } else if (currIn[0] != '[' && currIn[2] != ']') {
                println("\nInvalid input! Encolse choice in \"[]\" (ex. [1]) ")
            } else {
                choice = currIn[1].digitToInt()
                
                if (choice >= 1 && choice <= 6) {
                    inputVal = true
                } else {
                    println("\nInvalid input! Number is not found in range of options (1 - 6)!")
                }
            }
        }
        
        print("Set New Exchange Rate: ")
        var rateIn = 0.0
        var excepted = false
        
        try {
            rateIn = sc.nextDouble()
        } catch (e: Exception) {
            println("\nWarning: Input may not be a number!")
            excepted = true
        }
        
        var temp = sc.nextLine()
        
        if (excepted == true) {
            return
        }
        
        if (choice == 1) {
            println("\nWarning: PHP is the base currency! Cannot record any exchange rate!")
        }
        else if (rates.get(choice-1).setExRate(rateIn) == false) {
            println("\nWarning: Invalid input! Operation was not successful")
        } else {
            println("\n***")
            println("Recording of Rates Successful!")
            println("Set ${rates.get(choice-1).getExCode()}\'s rate to %.2f".format(rates.get(choice-1).getExRate()))
        }
        
        if (returnToMM(sc) == true) {
            recordER(sc)
        }
        
        
    }
    
    /*
     * This is the menu for performing currency exchange across different denominations
     * 
     * @param sc is the scanner object allowing for basic input
     * @return NONE
     */
    fun currEx(sc: Scanner) {
        
        println("\nForeign Currency Exchange")
        println("Source Currency Option:")
        println("[1] Philippine Peso (PHP)")
        println("[2] United States Dollar (USD)")
        println("[3] Japanese Yen (JPY)")
        println("[4] British Pound Sterling (GBP)")
        println("[5] Euro (EUR)")
        println("[6] Chinese Yuan Renminni (CNY)")
        
        print("\nSource Currency: ")
        var currChoice = 0
        var excepted = false
        
        try {
            currChoice = sc.nextInt()
        } catch (e: Exception) {
            println("\nWarning: Input may not be a number!")
            excepted = true
        }
        
        var temp = sc.nextLine()
        
        if (excepted == true) {
            return
        }
        
        if (currChoice < 1 || currChoice > 6) {
            println("\nWarning: Choice exceeds range of 1 - 6! Please try again!")
            return 
        }
        
        print("Source Amount: ")
        var amtIn = 0.0
        
        try {
            amtIn = sc.nextDouble()
        } catch (e: Exception) {
            println("\nWarning: Input may not be a number!")
            excepted = true
        }
        
        temp = sc.nextLine()
        
        if (excepted == true) {
            return
        }
        
        if (amtIn == 0.0) {
            println("\nWarning: Converting to rate 0 is erroneous! Please try again!")
            return 
        } else if (amtIn < 0) {
            println("\nWarnng: Invalid value! Please try again!")
            return
        }
        
        var pesoConverted = 0.0
        
        if (currChoice != 1) {
            pesoConverted = reverseConversion(amtIn, currChoice-1)
        } else {
            pesoConverted = amtIn
        }
         
        
        println("\nExchanged Currency Options")
        println("[1] Philippine Peso (PHP)")
        println("[2] United States Dollar (USD)")
        println("[3] Japanese Yen (JPY)")
        println("[4] British Pound Sterling (GBP)")
        println("[5] Euro (EUR)")
        println("[6] Chinese Yuan Renminni (CNY)")
        
        print("\nExchange Currency: ")
        var excCurr = 0
        
        try {
            excCurr = sc.nextInt()
        } catch (e: Exception) {
            println("\nWarning: Input may not be a number!")
            excepted = true
        }
        
        temp = sc.nextLine()
        
        if (excepted == true) {
            return
        }
        
        if (excCurr < 1 || excCurr > 6) {
            println("\nWarning: Choice exceeds range of 1 - 6! Please try again!")
            return 
        }
    
        pesoConverted = automaticConversion(pesoConverted, excCurr-1)
        
        println("\n***")
        println("\nExchanged Amount: %.2f".format(pesoConverted))
        
        if (returnToMM(sc) == true) {
            currEx(sc)
        }
        
    }
    
    /*
     * This is a display function which shows the table of interest per day
     * 
     * @param days is the number of days inputted by the user, currIdx is the index of the currency used, stored in a list
     * @return NONE
     */
    fun displayInterestTable(days: Int, currIdx: Int) {
        
        println("\n%-5s".format("Day") + " | %-14s".format("Interest") + " | %-16s".format("Balance") + " |")
        
        var sumBal = currAcc.getBal()
        var ctr = 1
        
        do {
            sumBal += sumBal*(INTEREST/365)
            println("%-5d".format(ctr) + " | %-14.2f".format(INTEREST) + " | %-16.2f".format(automaticConversion(sumBal, currIdx)) + " |")
            ctr++
        } while (ctr <= days)
        
    }
    
    /*
     * This is the menu for presenting the expected daily interest
     * 
     * @param sc is the scanner object allowing for basic input
     * @return NONE
     */
    fun presentInterest(sc: Scanner) {
        
        var inputDay = false
        var choice = 0
        
        println("\nShow Interest Amount")
        println("Account Name: ${currAcc.getUsername()}")
        println("Current Balance: %.2f".format(currAcc.getBal()))
        
        print("Currency: ")
        var currChoice = sc.nextLine()
        
        if (checkCurrency(currChoice) == -1) {
            println("\nWarning: Unrecognized Currency! Returning to Main Menu")
            return
        }
        
        println("Interest Rate: 5%")
        
        // Checking if input follows format of example: "5 days"
        while (inputDay == false) {
            print("\nTotal Number of Days: ")
            var dayIn = sc.nextLine()
            
            if (dayIn.indexOf("days") == -1) {
                
                println("\nInvalid input! Append \"days\" after numerical input (ex. 5 days) ")
                
            } else if (dayIn.substring(dayIn.indexOf(" ") + 1).compareTo("days") != 0 ) {
                
                println("\nInvalid input! Append \"days\" after numerical input (ex. 5 days) ")
                
            } else {
                
                try {
                    choice = dayIn.substringBefore(" ").toInt()
                } catch (e: Exception) {
                    println("\nWarning: No possible numerical value from input! / Unknown input!")
                    return
                }
                
                if (choice >= 1) {
                    inputDay = true
                    println(choice)
                } else {
                    println("\nInvalid input! At least input one (1) day!")
                }
            }
        }
        
        displayInterestTable(choice, checkCurrency(currChoice))
        
        if (returnToMM(sc) == true) {
            presentInterest(sc)
        }
        
    }
    
    /**
     * @param sysCtrl is the SysControl Object passed for account management
     */
    fun closeAccount(sysCtrl: SysControl, sc: Scanner): Boolean {
        
        print("\nProceed with account closing? This action is irreversible. (Y/N): ")
        
        var input = sc.nextLine()
        
        if (validateStringInput(input) == false) {
            return false
        } else if (input.compareTo("N") == 0 ) {
            println("Did not close account.")
            return false
        } else if (input.compareTo("Y") == 0 ){
            sysCtrl.removeAccount(currAcc.getUsername())
            return true
        } else {
            println("Warning: Input unrecognized, defaulting to \"N\". Did not close account")
            return false
        }
    }
    
    /*
     * Calls the respective menu given the user input
     * 
     * @param nSelection is the number of the menu displayed in showTransacMenu(), sc is the scanner object allowing for basic input
     * @param sysCtrl is the SysControl Object passed for account management
     * @return NONE
     */
    fun callMenu(nSelection: Int, sc: Scanner) {
        
        if (nSelection == 1) {
            deposit(sc)
        } else if (nSelection == 2) {
            withdraw(sc)
        } else if (nSelection == 3) {
            currEx(sc)
        } else if (nSelection == 4) {
            recordER(sc)
        } else if (nSelection == 5) {
            presentInterest(sc)
        }
        
    }
    
    /*
     * This is the "Main Menu" of the application and serves as the center to perform transactions
     * 
     * @param sc is the scanner object allowing for basic input
     * @param sysCtrl is the SysControl Object passed for account management
     * @return a boolean stating if the user has logged out
     */
    fun showTransacMenu(sc: Scanner, sysCtrl: SysControl) : Boolean {
        
        println("\n========================================\n")
        println("Select Transaction: \n")
        println("========================================\n")
        println("[1] Deposit Amount")
        println("\n[2] Withdraw Amount")
        println("\n[3] Currency Exchange")
        println("\n[4] Record Exchange Rates")
        println("\n[5] Show Interest Amount")
        println("\n[6] Close Account")
        println("\n[7] Log Out")
        println("\n========================================\n")
        
        print("\n>> ")
        var nChoice = 0
        var excepted = false
        
        try {
            nChoice = sc.nextInt()
        } catch (e: Exception) {
            excepted = true
            println("\nWarning: Input may not be a number!")
        }
        
        var temp = sc.nextLine()
        
        var logoutPass = false
        
        do {
            if (excepted == true) {
                break
            }
            
            if (nChoice == 1 || nChoice == 2 || nChoice == 3 || nChoice == 4 || nChoice == 5 || nChoice == 6) {
                callMenu(nChoice, sc)
            
                if (nChoice == 6) {
                    if (currAcc.getAccNumb() == 1000) {
                        println("\nWarning: Invalid action for administrator account.")
                    } else if (closeAccount(sysCtrl, sc) == true){
                        println("\nAccount ${currAcc.getUsername()} closed.")
                        logoutPass = true
                    }
                }
                
            } else if (nChoice == 7){
                println("\nLogging out of the system.")
                logoutPass = true
            }
        } while (nChoice != 1 && nChoice != 2 && nChoice != 3 && nChoice != 4 && nChoice != 5 && nChoice != 6 && nChoice != 7)
        
        return logoutPass
    }
    
    
    // Setter
    
    
    /*
     * Sets the currently logged in account within the application
     * 
     * @param a is the account being logged in to
     * @return NONE
     */
    fun setAccount(a: Account) {
        currAcc = a
    }
}

/*
 * This class which handles the main program flow and display methods of the application
 */
class SysControl {

    
    // Attributes
    
    
    var numAccounts = 0
    
    val MAX_ACC = 100       // Cap for maximum number of accounts in the program
    
    companion object {
        
        val ADMIN_NAME = "admin"
        val ADMIN_PASS = "2580"
    
        val ADMIN_ACC = Account(1000, ADMIN_NAME, ADMIN_PASS)
        var accounts = mutableListOf(ADMIN_ACC)
        
        // Retrieves an account within the list given an index (n)
        fun getAccList(n: Int): Account = accounts.get(n)
        
    }
    
    
    // Helper Methods
    
    
    /*
     * Validates if an input is not empty
     * 
     * @param input is the string input by the user
     * @return a boolean stating if the input was empty or not
     */
    fun validateInput(input: String): Boolean {
        
        try {
            
            // Trying to test and cause an exception if inputted string is empty.
            if (input[0].compareTo('\u0000') == 0) {
                println("\nWarning: Null Input!!") 
            }
            
        } catch (e: Exception) {
            
            println("\nWarning: Invalid input has caused an error!")
            println("Please retry your action.")
            return false
        }
        
        return true
    }
    
    /*
     * A pass by method used to enclose an input function to load into
     * try-catch clauses.
     * .
     * 
     * @param sc is the scanner object allowing for basic input
     * @return the integer input of the user
     */
    fun passInt(sc: Scanner): Int {
        
        
        var input = sc.nextInt()
        
        return input
    }
    
    /*
     * Searches if a user account exists within the list of accounts
     * 
     * @param input is the username of the account
     * @return the index of the account
     */
    fun searchUser(input: String): Int {
        
        var ctr = 0
        var flag = false
        
        do {
            
            if (accounts.get(ctr).getUsername() == input) {
                flag = true
            }
            
            ctr++
            
        } while (flag == false && ctr <= numAccounts);
        
        if (flag == false) {
            return -1
        }
        
        return ctr-1
        
    }
    
    /**
    * Removes an account from the accounts mutable list, representing the registered users on the application
    * 
    * @param user represents the username of the account to be closed
    * @return NONE
    */
    fun removeAccount(user: String) {
            
        accounts.remove(accounts.get(searchUser(user)))
        numAccounts--
            
    }
    
    /*
     * Takes a string input and the index of the account within the list and cross references the
     * inputted pin of the user versus the actual pin of the account
     * 
     * @param input is the inputted pin, accIdx is the index of the account in the list
     * @return a boolean stating if pin is correct or not
     */
    fun matchPin(input: String, accIdx: Int): Boolean {
        
        // Checks if the pin of an account matches the entered pin of the user
        if (accounts.get(accIdx).getAccPin() == input) {
            return true
        }
        
        return false
        
    }
    
        
    // Core Methods
        
    
    /*
     * Allows a user to register an account given a name, and pin
     * 
     * @param sc is the scanner object allowing for basic input
     * @return NONE
     */
    fun regAcc(sc: Scanner) {
        
        var bPass = true

        // Debug
        // numAccounts = MAX_ACC+1 
        
        // First Layer Checks (Account Capacity)
        if (numAccounts > MAX_ACC) {
            bPass = false
            println("\nWarning! Maximum Number of Accounts Exceeded!")
            return 
        }

        // Menu Header
        println("\nRegister Account Name")
        print("\nAccount Name: ")
        
        var nInput = sc.nextLine()
        
        if (validateInput(nInput) == false) {
            return
        }
        
        // Second Layer Checks (Validity of Inputted Name)
        
        var nAccName = nInput // Transfers the inputted name from the temporary variable
        
        // Checks for similar name
        // val iterator = accounts.iterator()
        
        for (elem in accounts) {
            if (elem.getUsername().compareTo(nAccName) == 0) {
                println("\nWarning: Account Name has already been taken!")
                return
            }
        }
        
        // Input for Pin
        print("\nEnter New 4-Digit Pin: ")
        var pinInput = sc.nextLine()
        
        if (validateInput(pinInput) == false) {
            return
        }
        
        if (pinInput.length > 4) {
            println("\nWarning: Pin is greater than 4 characters!")
            return
        }
        
        for (i in pinInput.indices) {
            if (pinInput[i] < '0' || pinInput[i] > '9') {
                println("\nWarning: Pin includes non-numeric characters!")
                return 
            }
        }
        
        var pin4 = pinInput
        
        accounts.add(Account(1000 + Account.numAccs, nAccName, pin4))
        
        var accIndex = 0
        
        for (j in accounts) {
            if (j.getUsername().compareTo(nAccName) == 0) {
                accIndex = accounts.indexOf(j)
            }
        }
        
        println("\n***")
        println("Account under \"$nAccName\" successfully created!")
        
        numAccounts++

    }

    
    // Control Methods
    

    /*
     * Shows the main menu upon execution of the application
     * 
     * @param sc is the scanner object allowing for basic input
     * @return an integer representing the choice of the user, or the failure status recognizing the input
     */
    fun showMainMenu(sc: Scanner) : Int {
        
        println("\n========================================\n")
        println("        BANKING SYSTEM - LOGIN\n");
        println("========================================\n")
        println("[1] Login\n")
        println("[2] Register New Account\n")
        println("[3] Exit Program\n")
        println("========================================\n")
        
        print(">> ")
        
        var nChoice = 0
        
        try {
            
            nChoice = passInt(sc)
            
            
        }
        catch (e: Exception) {
            
            println("\nWarning: Input may not be a number!")
            return -1
            
        }
            
        if (nChoice != 1 && nChoice != 2 && nChoice != 3) {
             nChoice = -1
        }
            
        
        
        return nChoice
        
    }
    
    /*
     * Presents the login screen to enter the credentials of the user
     * 
     * @param sc is the scanner object allowing for basic input
     * @return an integer representing the success or failure of login
     */
    fun showLogin(sc: Scanner) : Int {
     
        print("\nEnter Account Name: ")
        var userIn = sc.nextLine()
        
        var userIdx = searchUser(userIn)
        
        if (validateInput(userIn) == false) {
            return -1
        } else if (userIdx == -1) {
            println("\nWarning: Login Failed. User not found")
            return -1
        }
        
        var pinPass = false
        var attempts = 3
        
        do {
            
            print("\nEnter Pin: ")
            var pin = sc.nextLine()
            
            if (matchPin(pin, userIdx) == true) {
                pinPass = true
            } else {
                println("Warning: Pin is incorrect! You have $attempts more attempt/s!")
                attempts--
            }
            
        } while (attempts >= 0 && pinPass == false)
        
        if (pinPass == false) {
            return -1
        }
        
        println("\nLogin Successful! Welcome: ${accounts.get(userIdx).getUsername()}")
        
        return userIdx
           
    }
    
}
    

// Main Function


/*
 * This is the main function which drives the whole program flow of the application
 * 
 * @return NONE
 */
fun main() {
        
    // Control Variables
    var bFlag = false
    var nChoice = 0
    var nMainChoice = 0
    
    // Reference Types
    val sc = Scanner(System.`in`)
    val main = SysControl()
    val center = AccountCenter(SysControl.getAccList(0))
        
    AccountCenter.initializeRates()
    
    // Control flow for login screen
    while(bFlag == false) {
            
        nChoice = main.showMainMenu(sc)
        var temp = sc.nextLine()
            
        if (nChoice == 1) {         // Login Flow
            
            var activeIdx = main.showLogin(sc)
            
            // Account Validation
            if (activeIdx != -1) {
                println("Active Account: ${SysControl.getAccList(activeIdx).getUsername()}")
                
                center.setAccount(SysControl.getAccList(activeIdx))
                
                var logout = false
                
                while (logout == false) {
                    logout = center.showTransacMenu(sc, main)
                }
                
            } else {
                println("\nUser attempt to Login Failed! Please try again")
            }
            
        } else if (nChoice == 2) {  // Register Flow
            
            main.regAcc(sc)
            
        } else if (nChoice == 3) {  // Exit Flow
            
            println("\nThank you for using the Banking System!\n")    
        
            bFlag = true
            
        } else {
            
            println("\nInvalid input! Range 1 - 6 only!\n") // Error Message
            
        }
    }
             
}

