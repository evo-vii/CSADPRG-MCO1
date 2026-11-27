/*
 * ********************
 * Last names: Bobadilla, Calvez, Casao, Sanico
 * Language: Kotlin
 * Paradigm(s): Object-Oriented Programming
 * ********************
 */


// Pre-processor Directives
import java.util.Scanner
import java.io.*
import java.util.*
import java.time.*
import java.time.format.*
import java.time.temporal.ChronoUnit
    
// Classes


/**
 * This is the account class which acts as a template to store the user's account details
 */
class Account (var accNum: Int, var name: String, var pin: String, var creaTime: LocalDateTime) {
    
    var currBal = 0.0
    var numOfOverseasTrans: Int = 0
    
    var totalDeposits = 0
    var totalWithdrawals = 0
    
    var accumDeposits = 0
    var accumWithdrawal = 0
    
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
        totalDeposits++
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
            totalWithdrawals++
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
    
    fun getCreationTime(): LocalDateTime {
        return creaTime
    }
}

/**
 * This is a tuple data type simulated via a class and is specially modified to accept values
 * from the bank_transactions.csv file.
 */
class recordTuple (var accName: String, var accNumber: Int, var trnscDate: LocalDateTime, var curr: String, var diff: Double, var tType: String, var currBal: Double) {
    
    /**
     * Retrieves the account name
     * 
     * @return the account name
     */
    fun getUsername(): String {
        return accName
    }
    
    /**
     * Retrieves the account number
     * 
     * @return the account number
     */
    fun getAccNumb(): Int {
        return accNumber
    }
    
    /**
     * Retrieves the date of transaction
     * 
     * @return the date of transaction
     */
    fun getTimeDate(): LocalDateTime {
        return trnscDate
    }
    
    /**
     * Retrieves the currency code
     * 
     * @return the currency code
     */
    fun getCurrCode(): String {
        return curr
    }
    
    /**
     * Retrieves the transacted amount
     * 
     * @return the transacted amount
     */
    fun getMoneyDiff(): Double {
        return diff
    }
    
    /**
     * Retrieves the transaction type
     * 
     * @return the transaction type
     */
    fun getType(): String {
        return tType
    }
    
    /**
     * Retrieves the current balance
     * 
     * @return the current balance
     */
    fun getBall(): Double {
        return currBal
    }
}

/*
 * A dedicated private class for row objects
 * NOTE: AI-ASSISTED
 * 
 */
private data class Report1Row(
    val accountName: String,
    val accountAgeDays: Long,
    val currentBalance: Double,
    val adb: Double,
    val privilegeTier: String
)

/*
 * A dedicated private class for row objects
 * NOTE: AI-ASSISTED
 * 
 */
private data class Report2Row(
    val accountName: String,
    val currentBalance: Double,
    val expectedInterest: Double
)

/*
 * A dedicated private class for row objects
 * NOTE: AI-ASSISTED
 * 
 */
private data class Report3Row(
        val accountName: String,
        val date: LocalDate,
        val totalWithdrawals: Double,
        val numDeposits: Int,
        val totalDeposits: Double,
        val riskFlagLevel: String
    )

/**
 * This is the class that acts as a primary key for all unique transactions to users.
 * NOTE: AI-REFINED AND ASSISTED
 */
data class DM_PrimaryKey (val name: String, val num: Int) {
    
    /**
     * Retrieves the account name
     * 
     * @return the account name
     */
    fun getAName(): String {
        return name
    }
    
    /**
     * Retrieves the account number
     * 
     * @return the account number
     */
    fun getANum(): Int {
        return num
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
        if (d <= 0 || d.isNaN() || d.isInfinite()) {
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
 * This is the class which handles all data management, extraction and filtering functions
 */
class DataManagement() {
    
    companion object {
        val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formatterDay = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        
        val formatterAbs = DateTimeFormatter.ofPattern("dd/MM/yyyy H:mm:ss")
        
        var listTuple = mutableListOf<recordTuple>()        
        var records = mutableListOf<String>()
        
        var rowDiscrepancies = 0
        var totalRows = 0
        var nonPHP = 0
        
        /**
         * Attempts to gather the distinct transactions together
         * NOTE: AI-REFINED AND ASSISTED
         * 
         * @return NONE
         */
        fun findDistinct(): Map<DM_PrimaryKey, List<recordTuple>> {
            return listTuple.filter{ it.getType() != "InvalidRow"}.groupBy({row -> DM_PrimaryKey(row.getUsername(), row.getAccNumb())})
        }
        
        /**
         * Parses each row and loads elements to a tuple class
         * 
         * @param r - the string form of the csv row
         * @return NONE
         */
        fun parseRow(r: String) {
            
            var recSplit = r.split(",")
            
            if (recSplit.size < 8) {
                listTuple.add(recordTuple("InvalidRow", 1050000000, LocalDateTime.now(), "PHP", 0.0, "InvalidRow", 0.0))
                rowDiscrepancies++
                totalRows++
                return
            }
            
            var name = recSplit[0]
            
            var accNum = recSplit[1].toInt()
            
            var dateABS = LocalDateTime.parse(recSplit[2] + " " + recSplit[3], formatterAbs)
            
            if (recSplit[4] != "PHP") {
                listTuple.add(recordTuple("InvalidRow", 1050000000, LocalDateTime.now(), "PHP", 0.0, "InvalidRow", 0.0))
                nonPHP++
                totalRows++
            }
            var curr = recSplit[4]
            
            var diff = recSplit[5].toDouble()
            
            var type = recSplit[6]
            
            var bal = recSplit[7].toDouble()
            
            totalRows++
            listTuple.add(recordTuple(name, accNum, dateABS, curr, diff, type, bal))
        }
        
        /**
         * Reads the csv file of transactions
         * 
         * @return tells whether reading the file was successful or not
         */
        fun readfile(): Boolean {
            
            var nullFlag = false
            
            try {
            
                val br: BufferedReader = BufferedReader(FileReader("bank_transactions.csv"))
                
                while(nullFlag == false) {
                    var temp = br.readLine()
                    
                    if (temp == null) {
                        nullFlag = false
                        break
                    }
                    
                    records.add(temp)
                }
                br.close()
           
                return true
           
            } catch (e: Exception) {

                e.printStackTrace()
                System.out.println("Warning: File cannot be found!")
                System.out.println("Logging of Transaction Unsuccesful")
                
                return false
            }
            
        }
        
    }
    
}

/**
 * This is the class which handles and controls all the transactions of the active account
 */
class AccountCenter (var currAcc: Account) {
    
    
    companion object {
        // Initializing the exchange rates
        val rates = mutableListOf(ExcRate("PHP", "Philippine Peso", 1.00))
        val holding = mutableListOf<Account>()
        
        var isFileLoaded = false
        
        /**
         * Formats a number with thousands separators and 2 decimals (ex. 1,234,567.89)
         * 
         * @param d is the amount to format
         * @return the formatted string
         */
        fun money(d: Double): String = "%,.2f".format(Locale.US, d)
        
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
        
        /**
        * Writes to the csv file containing all logged transactions done within the system
        * 
        * @param update pertains to the addition or deduction to the balance, type is the transaction type, bal is the balance of the account
        * @return a boolean value stating whether logging the transaction was successful or not
        */
        fun writeToFile(acc: Account, update: Double, type: String, bal: Double, currency: String): Boolean {
            
            val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
            val formatterDay = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            
            try {
                
                val fw: FileWriter = FileWriter("bank_transactions.csv", true)
                fw.write(acc.getUsername() + "," + acc.getAccNumb() + "," + LocalDateTime.now().format(formatterDay) + "," + LocalTime.now().format(formatterTime) + "," + currency + "," + "%.2f".format(update) + "," + type + "," + "%.2f".format(bal))
                fw.write("\n")
            
                fw.close()
            
                return true
            
            } catch (e: Exception) {
                
                System.out.println("Warning: File cannot be found!")
                System.out.println("Logging of Transaction Unsuccesful")
                    
                return false
            }
            
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
        
        if (nInput.isBlank()) {
            
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
        if (currAcc.getAccNumb() != 1000000000) {
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
        var code = input.uppercase()
        
        if (code == "PHP") {
            return 0
        } else if (code == "USD") {
            return 1
        } else if (code == "JPY") {
            return 2
        } else if (code == "GBP") {
            return 3
        } else if (code == "EUR") {
            return 4
        } else if (code == "CNY") {
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
        var exitChoice = sc.nextLine().trim().uppercase()

        if (validateStringInput(exitChoice) == false) {
            return true
        } else if (exitChoice.compareTo("N") == 0) {
            return false
        } else if (exitChoice.compareTo("Y") == 0) {
            println("\nReturning to main menu.")
            return true
        } else {
            println("\nWarning: Input unrecognized, defaulting to \"Y\", returning to main menu.")
            return true
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
        var overseasTag = false
        
        println("\nDeposit Amount")
        println("Account Name: ${currAcc.getUsername()}")
        
        println("Current Balance (PHP): %.2f".format(currAcc.getBal()))
        System.out.printf("\nPHP - PESO\nUSD - US DOLLAR\nJPY - JAPANESE YEN\nGBP - BRITISH POUND STERLING\nEUR - EURO\nCNY - CHINESE YUAN RENMINBI\n\n")
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
            sc.nextLine()
            return
        }
        
        sc.nextLine()
        
        if (depositIn <= 0 || depositIn.isNaN() || depositIn.isInfinite()) {
            println("\nWarning: Invalid amount!")
            return
        }
        
        var convertedVal = 0.0
        
        if (checkCurrency(currChoice) == 0) {
            currAcc.deposit(depositIn)
            convertedVal = depositIn
        } else {
            convertedVal = reverseConversion(depositIn, checkCurrency(currChoice))
            currAcc.deposit(convertedVal)
            overseasTag = true
        }
        
        writeToFile(currAcc, convertedVal, "deposit", currAcc.getBal(), "PHP")
        
        if (overseasTag == true) {
            currAcc.numOfOverseasTrans++
        }
        
        println("\n***")
        println("Account Name: ${currAcc.getUsername()}")
        println("Updated Balance (PHP): %.2f".format(currAcc.getBal()))
        println("Deposited Amount (PHP) = " + "%.2f".format(convertedVal))

        if (returnToMM(sc) == false) {
            deposit(sc)
        }
        
    }

    /*
     * This is the menu to withdraw funds from an account
     * 
     * @param sc is the scanner object allowing for basic input
     * @return NONE
     */
    fun withdraw(sc: Scanner) {
        
        var withdrawIn = 0.0
        var overseasTag = false
        
        println("\nWithdraw Amount")
        println("Account Name: ${currAcc.getUsername()}")
        
        println("Current Balance (PHP): %.2f".format(currAcc.getBal()))
        System.out.printf("\nPHP - PESO\nUSD - US DOLLAR\nJPY - JAPANESE YEN\nGBP - BRITISH POUND STERLING\nEUR - EURO\nCNY - CHINESE YUAN RENMINBI\n\n")
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
            sc.nextLine()
            return
        }
        
        sc.nextLine()
        
        if (withdrawIn <= 0 || withdrawIn.isNaN() || withdrawIn.isInfinite()) {
            println("\nWarning: Invalid amount!")
            return
        }
        
        var convertedVal = 0.0
        
        if (checkCurrency(currChoice) == 0) {
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
            overseasTag = true
        }
        
        writeToFile(currAcc, convertedVal, "withdrawal", currAcc.getBal(), "PHP")
        
        if (overseasTag == true) {
            currAcc.numOfOverseasTrans++
        }
        
        println("\n***")
        println("Account Name: ${currAcc.getUsername()}")
        println("Updated Balance (PHP): %.2f".format(currAcc.getBal()))
        println("Withdrawal Amount (PHP) = " + "%.2f".format(convertedVal))

        if (returnToMM(sc) == false) {
            withdraw(sc)
        }
        
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
                
            } else if (currIn.length != 3 || currIn[0] != '[' || currIn[2] != ']') {
                println("\nInvalid input! Encolse choice in \"[]\" (ex. [1]) ")
            } else if (currIn[1] < '0' || currIn[1] > '9') {
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
        
        if (returnToMM(sc) == false) {
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
        } else if (amtIn < 0 || amtIn.isNaN() || amtIn.isInfinite()) {
            println("\nWarnng: Invalid value! Please try again!")
            return
        }
        
        var pesoConverted = 0.0
        
        if (currChoice != 1) {
            pesoConverted = reverseConversion(amtIn, currChoice-1)
        } else {
            pesoConverted = amtIn
        }
         
        
        println("\nExchanged Currency Options:")
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
        
        println("Exchange Amount: %.2f".format(pesoConverted))
        
        print("\nConvert another currency (Y/N)? ")
        var convertAgain = sc.nextLine()
        
        if (validateStringInput(convertAgain) == false) {
            return
        } else if (convertAgain.compareTo("Y") == 0) {
            currEx(sc)
        } else if (convertAgain.compareTo("N") == 0) {
            println("\nReturning to main menu.")
        } else {
            println("\nWarning: Input unrecognized, defaulting to \"N\", returning to main menu.")
        }
        
    }
    
    /*
     * This is a display function which shows the table of interest per day
     * 
     * @param days is the number of days inputted by the user, principal is the amount in PHP entered by the user
     * @return NONE
     */
    fun displayInterestTable(days: Int, principal: Double) {
        
        println("\n%-5s".format("Day") + " | %-14s".format("Interest (PHP)") + " | %-16s".format("Balance (PHP)") + " |")
        
        var sumBal = Math.round(principal*100)/100.0
        var dailyInterest = 0.0
        var ctr = 1

        do {
            dailyInterest = Math.round(sumBal*(INTEREST/365)*100)/100.0
            sumBal = Math.round((sumBal + dailyInterest)*100)/100.0
            println("%-5d".format(ctr) + " | %-14.2f".format(dailyInterest) + " | %-16.2f".format(sumBal) + " |")
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
        println("Current Balance (PHP): %.2f".format(currAcc.getBal()))
        println("Currency: PHP")
        println("Interest Rate: 5%")

        while (inputDay == false) {
            print("\nTotal Number of Days: ")
            var dayIn = sc.nextLine()
            
            try {
                choice = dayIn.trim().toInt()
            } catch (e: Exception) {
                println("\nWarning: No possible numerical value from input! / Unknown input!")
                return
            }
            
            if (choice >= 1) {
                inputDay = true
            } else {
                println("\nInvalid input! At least input one (1) day!")
            }
        }
        
        displayInterestTable(choice, currAcc.getBal())
        
        if (returnToMM(sc) == false) {
            presentInterest(sc)
        }
        
    }
    
    /*
     * This is the menu for retrieving the account balance
     * 
     * @return NONE
     */
    fun checkBalance() {

        println("\nCheck Balance")
        println("Account Name: ${currAcc.getUsername()}")
        println("Current Balance (PHP): %.2f".format(currAcc.getBal()))

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
            sysCtrl.removeAccount(currAcc.getAccNumb())
            return true
        } else {
            println("Warning: Input unrecognized, defaulting to \"N\". Did not close account")
            return false
        }
    }
    
    
    // Report Handling
    
    
    /*
     * Calculates average daily balance
     * NOTE: AI-REFINED AND ASSISTED
     * 
     * @return NONE
     */
    fun calcADB(tuples: List<recordTuple>): Double {
        
        if (tuples.isEmpty()) {
            return 0.0
        }
        
        // Sorted by date
        val sorted = tuples.sortedBy { it.getTimeDate() }
        
        // Returns just the balance due to no additional balances to average.
        if (sorted.size == 1) {
            return sorted.first().getBall()
        }

        var weightedBalanceSum = 0.0
        var totalDays = 0L

        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]

            val startDay = current.getTimeDate().toLocalDate()
            val endDay = next.getTimeDate().toLocalDate()
            val daysActive = ChronoUnit.DAYS.between(startDay, endDay).coerceAtLeast(0L)

            weightedBalanceSum += current.getBall() * daysActive
            totalDays += daysActive
        }

        // Include the final transaction's balance for at least 1 day (or through the end of its month)
        val lastTuple = sorted.last()
        val lastDate = lastTuple.getTimeDate().toLocalDate()
        val endOfLastMonth = YearMonth.from(lastDate).atEndOfMonth()
        val remainingDaysInMonth = ChronoUnit.DAYS.between(lastDate, endOfLastMonth).coerceAtLeast(1L)

        weightedBalanceSum += lastTuple.getBall() * remainingDaysInMonth
        totalDays += remainingDaysInMonth

        return if (totalDays > 0L) weightedBalanceSum / totalDays else lastTuple.getBall()
    }

    /*
     * Handles the generation of report 1
     * NOTE: AI-REFINED AND ASSISTED
     * 
     * @return NONE
     */
    fun report1() {
        val groupedUsers: Map<DM_PrimaryKey, List<recordTuple>> = DataManagement.findDistinct()
        var excpPass = false

        try {
            val rows = mutableListOf<Report1Row>()

            for ((userKey, userTuples) in groupedUsers) {
                if (userTuples.isEmpty()) continue

                val sortedTuples = userTuples.sortedBy { it.getTimeDate() }
                val firstDate = sortedTuples.first().getTimeDate()
                val lastDate = sortedTuples.last().getTimeDate()
                val accountAgeDays = ChronoUnit.DAYS.between(firstDate, lastDate)

                val currentBal = sortedTuples.last().getBall()
                val adb = calcADB(sortedTuples)

                val privTier = when {
                    adb < 500000.0 -> "Silver"
                    adb < 1000000.0 -> "Gold"
                    else -> "Platinum"
                }

                rows.add(
                    Report1Row(
                        accountName = userKey.getAName(),
                        accountAgeDays = accountAgeDays,
                        currentBalance = currentBal,
                        adb = adb,
                        privilegeTier = privTier
                    )
                )
            }

            val sortedRows = rows.sortedByDescending { it.adb }

            FileWriter("report1_banking_privilege_tiers.csv").use { fw ->
                fw.write("AccountName,AccountAge,CurrentBalance,AverageDailyBalance,PrivilegeTier\n")
                for (r in sortedRows) {
                    fw.write(
                        "${r.accountName},${r.accountAgeDays},\"${money(r.currentBalance)}\",\"${money(r.adb)}\",${r.privilegeTier}\n"
                    )
                }
            }

            excpPass = true
        } catch (e: Exception) {
            e.printStackTrace()
            println("Warning: File cannot be written!")
            println("Generation of Report Unsuccesful")
        }

        if (excpPass == false) {
            return
        }
    }   

    /*
     * Handles the generation of report 2
     * NOTE: AI-REFINED AND ASSISTED
     * 
     * @return NONE
     */
    fun report2() {
    val groupedUsers: Map<DM_PrimaryKey, List<recordTuple>> = DataManagement.findDistinct()
        var excpPass = false

        try {
            val rows = mutableListOf<Report2Row>()

            for ((userKey, userTuples) in groupedUsers) {
                if (userTuples.isEmpty()) continue

                val sortedTuples = userTuples.sortedBy { it.getTimeDate() }
                val currentBal = sortedTuples.last().getBall()

                val startBal = Math.round(currentBal * 100) / 100.0
                var sumBal = startBal
                var dailyInterest: Double
                var ctr = 1

                do {
                    dailyInterest = Math.round(sumBal * (INTEREST / 365.0) * 100) / 100.0
                    sumBal = Math.round((sumBal + dailyInterest) * 100) / 100.0
                    ctr++
                } while (ctr <= 30)

                val interestEarned30Days = sumBal - startBal

                rows.add(
                    Report2Row(
                        accountName = userKey.getAName(),
                        currentBalance = currentBal,
                        expectedInterest = interestEarned30Days
                    )
                )
            }

            val sortedRows = rows.sortedByDescending { it.expectedInterest }

            FileWriter("report2_interest_expense_projection.csv").use { fw ->
                fw.write("AccountName,CurrentBalance,ExpectedInterest\n")
                for (r in sortedRows) {
                    fw.write(
                        "${r.accountName},\"${money(r.currentBalance)}\",\"${money(r.expectedInterest)}\"\n"
                    )
                }
            }

            excpPass = true
        } catch (e: Exception) {
            e.printStackTrace()
            println("Warning: File cannot be written!")
            println("Generation of Report Unsuccesful")
        }

        if (excpPass == false) {
            return
        }
    }
    
    /*
     * Handles the generation of report 3
     * 
     * @return NONE
     */
    fun report3() {
        val groupedUsers: Map<DM_PrimaryKey, List<recordTuple>> = DataManagement.findDistinct()
        val formatterDay = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        var excpPass = false

        try {
            val rows = mutableListOf<Report3Row>()

            for ((userKey, userTuples) in groupedUsers) {
                if (userTuples.isEmpty()) continue

                val byDay = userTuples
                    .sortedBy { it.getTimeDate() }
                    .groupBy { it.getTimeDate().toLocalDate() }

                for ((day, dayTuples) in byDay) {
                    val totalWithdrawals = dayTuples
                        .filter { it.getType().equals("withdrawal", ignoreCase = true) }
                        .sumOf { it.getMoneyDiff() }

                    val dayDeposits = dayTuples
                        .filter { it.getType().equals("deposit", ignoreCase = true) }

                    val numDeposits = dayDeposits.size
                    val totalDeposits = dayDeposits.sumOf { it.getMoneyDiff() }

                    val turnoverVelocity = if (totalDeposits > 0.0) {
                        (totalWithdrawals / totalDeposits) * 100.0
                    } else {
                        0.0
                    }

                    val riskFlagLevel = if (totalWithdrawals >= 500000.0 || turnoverVelocity >= 90.0) {
                        "Suspicious"
                    } else {
                        "Normal"
                    }

                    rows.add(
                        Report3Row(
                            accountName = userKey.getAName(),
                            date = day,
                            totalWithdrawals = totalWithdrawals,
                            numDeposits = numDeposits,
                            totalDeposits = totalDeposits,
                            riskFlagLevel = riskFlagLevel
                        )
                    )
                }
            }

            val sortedRows = rows.sortedWith(
                compareBy<Report3Row> { it.date }.thenBy { it.riskFlagLevel }
            )

            FileWriter("report3_suspicious_client_behavior.csv").use { fw ->
                fw.write("AccountName,Date,TotalWithdrawalAmounts,NumberOfDeposits,TotalDepositAmounts,RiskFlagLevel\n")
                for (r in sortedRows) {
                    val formattedDate = r.date.format(formatterDay)
                    fw.write(
                        "${r.accountName},$formattedDate,\"${money(r.totalWithdrawals)}\",${r.numDeposits},\"${money(r.totalDeposits)}\",${r.riskFlagLevel}\n"
                    )
                }
            }

            excpPass = true
        } catch (e: Exception) {
            e.printStackTrace()
            println("Warning: File cannot be written!")
            println("Generation of Report Unsuccesful")
        }

        if (excpPass == false) {
            return
        }
    }
    
    /*
     * Handles the generation of report 4
     * 
     * @return NONE
     */
    fun report4() {
        
        val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formatterDay = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        var excpPass = false
        
        try {
            
            val fw: FileWriter = FileWriter("report4_overseas_transaction_record.csv")
            fw.write("AccountName" + "," + "NumberOfOverseasTransactions")
            fw.write("\n")
           
            excpPass = true
            
            fw.close()
           
        } catch (e: Exception) {
            
            System.out.println("Warning: File cannot be written!")
            System.out.println("Generation of Report Unsuccesful")
        }
        
        if (excpPass == false) {
            return
        }
        
        var i = 0
        val fw: FileWriter = FileWriter("report4_overseas_transaction_record.csv", true)
        
        while (i < SysControl.accounts.size) {
            fw.write(SysControl.accounts.get(i).getUsername() + "," + SysControl.accounts.get(i).numOfOverseasTrans)
            fw.write("\n")
            i++
        }
        
        fw.close()
        
    }
    
    /**
     * A helper data class that consolidates to a json file
     * NOTE: AI-Assisted
     * 
     * @return NONE
     */
    data class SummaryMetrics(
        val totalUniqueAccounts: Int,
        val absTotalDeposited: Double,
        val absTotalWithdrawn: Double,
        val absTotalBalance: Double,
        val absTotalInterestExpense: Double
    )
    
    /**
     * This consolidates all banking app metrics to be ready for output
     * NOTE: AI-Assisted
     * 
     * @return NONE
     */
    fun summary(): AccountCenter.SummaryMetrics {
        
        val groupedUsers: Map<DM_PrimaryKey, List<recordTuple>> = DataManagement.findDistinct()

        val totalUniqueAccounts = groupedUsers.size // pseudo-groupby
        var absTotalDeposited = 0.0
        var absTotalWithdrawn = 0.0
        var absTotalBalance = 0.0
        var absTotalInterestExpense = 0.0

        // Harvests total amounts of deposits and withdrawals
        for ((_, userTuples) in groupedUsers) {
            if (userTuples.isEmpty()) continue

            val sortedTuples = userTuples.sortedBy { it.getTimeDate() }

            for (tuple in sortedTuples) {
                when {
                    tuple.getType().equals("deposit", ignoreCase = true) -> {
                        absTotalDeposited += kotlin.math.abs(tuple.getMoneyDiff())
                    }
                    tuple.getType().equals("withdrawal", ignoreCase = true) -> {
                        absTotalWithdrawn += kotlin.math.abs(tuple.getMoneyDiff())
                    }
                }
            }

            // Gets sum of latest account balances 
            val latestBal = sortedTuples.last().getBall()
            absTotalBalance += kotlin.math.abs(latestBal)

            val startBal = Math.round(latestBal * 100) / 100.0
            var sumBal = startBal
            var dailyInterest: Double
            var ctr = 1

            do {
                dailyInterest = Math.round(sumBal * (INTEREST / 365.0) * 100) / 100.0
                sumBal = Math.round((sumBal + dailyInterest) * 100) / 100.0
                ctr++
            } while (ctr <= 30)

            absTotalInterestExpense += kotlin.math.abs(sumBal - startBal)
        }

        return SummaryMetrics(
            totalUniqueAccounts = totalUniqueAccounts,
            absTotalDeposited = absTotalDeposited,
            absTotalWithdrawn = absTotalWithdrawn,
            absTotalBalance = absTotalBalance,
            absTotalInterestExpense = absTotalInterestExpense
        )
        
    }
    
    /**
     * This consolidates all banking app metrics to be ready for output
     * NOTE: AI-Assisted
     * 
     * @return NONE
     */
    fun writeSummaryJson(filePath: String = "summary.json"): Boolean {
        val metrics = summary()

        val jsonContent = """
            {
            "totalUniqueAccounts": ${metrics.totalUniqueAccounts},
            "absoluteTotalDeposited": "${money(metrics.absTotalDeposited)}",
            "absoluteTotalWithdrawn": "${money(metrics.absTotalWithdrawn)}",
            "absoluteTotalBalance": "${money(metrics.absTotalBalance)}",
            "absoluteTotalInterestExpense": "${money(metrics.absTotalInterestExpense)}"
            }
        """.trimIndent()

        return try {
            FileWriter(filePath).use { fw ->
                fw.write(jsonContent)
                fw.write("\n")
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            println("Warning: File cannot be written!")
            println("Generation of summary.json Unsuccessful")
            false
        }
    }
    
    /*
     * A helper method facilitating the outputs after generating the reports
     * 
     * @return NONE
     */
    fun reportFlow() {
        
        // Generate Reports first
        
        report1()
        report2()
        report3()
        report4()
        
        println("\nGenerating reports...")
        println("Outputs saved to individual files...")
        
        println("\nReport 1: Banking Privilege Tiers\n")
        
        // Try segment is AI-Assisted
        try {
            
            BufferedReader(FileReader("report1_banking_privilege_tiers.csv")).use { br ->
            val headerLine = br.readLine()
            if (headerLine == null) {
                println("(Empty file)")
                return
            }

            fun formatRow(line: String): String {
                return line.split(Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
                    .joinToString(" | ") { cell ->
                        cell.trim().trim('"').padEnd(18)
                    }
            }

            val formattedHeader = formatRow(headerLine)
            println(formattedHeader)
            println("-".repeat(formattedHeader.length))

            var printed = 0
            while (printed < 2) {
                val rowLine = br.readLine() ?: break
                if (rowLine.isBlank()) continue
                println(formatRow(rowLine))
                printed++
            }

            if (printed == 0) {
                println("(No data rows available)")
            }
        }
           
        } catch (e: Exception) {
            
            System.out.println("Warning: File cannot be read!")
            System.out.println("File Reading Unsuccesful")
        }
        
        
        println("\n(Full table exported to report1_banking_privilege_tiers.csv)")
        
        println("\nReport 2: Interest Expense Projection\n")
        
        // Try segment is AI-Assisted
        try {
            
            BufferedReader(FileReader("report2_interest_expense_projection.csv")).use { br ->
            val headerLine = br.readLine()
            if (headerLine == null) {
                println("(Empty file)")
                return
            }

            fun formatRow(line: String): String {
                return line.split(Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
                    .joinToString(" | ") { cell ->
                        cell.trim().trim('"').padEnd(18)
                    }
            }

            val formattedHeader = formatRow(headerLine)
            println(formattedHeader)
            println("-".repeat(formattedHeader.length))

            var printed = 0
            while (printed < 2) {
                val rowLine = br.readLine() ?: break
                if (rowLine.isBlank()) continue
                println(formatRow(rowLine))
                printed++
            }

            if (printed == 0) {
                println("(No data rows available)")
            }
        }
           
        } catch (e: Exception) {
            
            System.out.println("Warning: File cannot be read!")
            System.out.println("File Reading Unsuccesful")
        }
            
        println("\n(Full table exported to report2_interest_expense_projection.csv)")
        
        println("\nReport 3: Suspicious Client Behavior\n")
        
        // Try segment is AI-Assisted
        try {
            
            BufferedReader(FileReader("report3_suspicious_client_behavior.csv")).use { br ->
            val headerLine = br.readLine()
            if (headerLine == null) {
                println("(Empty file)")
                return
            }

            fun formatRow(line: String): String {
                return line.split(Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
                    .joinToString(" | ") { cell ->
                        cell.trim().trim('"').padEnd(15)
                    }
            }

            val formattedHeader = formatRow(headerLine)
            println(formattedHeader)
            println("-".repeat(formattedHeader.length))

            var printed = 0
            while (printed < 2) {
                val rowLine = br.readLine() ?: break
                if (rowLine.isBlank()) continue
                println(formatRow(rowLine))
                printed++
            }

            if (printed == 0) {
                println("(No data rows available)")
            }
        }
           
        } catch (e: Exception) {
            
            System.out.println("Warning: File cannot be read!")
            System.out.println("File Reading Unsuccesful")
        }
        
        println("\n(Full table exported to report3_suspicious_client_behavior.csv)")
        
        println("\nReport 4: Total Number of Overseas Transactions\n")
        
        // Try segment is AI-Assisted
        try {
            
            BufferedReader(FileReader("report4_overseas_transaction_record.csv")).use { br ->
            val headerLine = br.readLine()
            if (headerLine == null) {
                println("(Empty file)")
                return
            }

            fun formatRow(line: String): String {
                return line.split(Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
                    .joinToString(" | ") { cell ->
                        cell.trim().trim('"').padEnd(18)
                    }
            }

            val formattedHeader = formatRow(headerLine)
            println(formattedHeader)
            println("-".repeat(formattedHeader.length))

            var printed = 0
            while (printed < 2) {
                val rowLine = br.readLine() ?: break
                if (rowLine.isBlank()) continue
                println(formatRow(rowLine))
                printed++
            }

            if (printed == 0) {
                println("(No data rows available)")
            }
        }
           
        } catch (e: Exception) {
            
            System.out.println("Warning: File cannot be read!")
            System.out.println("File Reading Unsuccesful")
        }
        
        println("\n(Full table exported to report4_custom_report.csv)")
        
        println("\nSummary State (summary.json):")
        if (writeSummaryJson("summary.json")) {
            try {
                val br = BufferedReader(FileReader("summary.json"))
                
                var a = br.readLine()
                var b = br.readLine()
                var c = br.readLine()
                var d = br.readLine()
                var e = br.readLine()
                
                println(a + " " + b + " " + c + " " + d + " " + e + "}")
                
            } catch (e: Exception) {
                
                System.out.println("Warning: File cannot be read!")
                System.out.println("File Reading Unsuccesful")
                
            }
        } else {
            println("Warning: Could not write summary.json!")
        }
        
    }
    
    /*
     * This is the menu for generating the system reports of the banking application
     * 
     * @param sc is the scanner object allowing for basic input
     * @return NONE
     */
    fun genReports(sc: Scanner) {
        
        var currChoice = 0
        
        println("\nGenerate Reports")
        println("\n[1] Load the file")
        println("[2] Generate Reports")
        
        print("\nEnter choice: ")
        
        try {
            currChoice = sc.nextInt()
            
            if (currChoice != 1 && currChoice != 2) {
                println("Warning: Input unrecognized, returning to main menu!")
                return
            }
            
        } catch (e: Exception) {
            println("Warning: Input unrecognized, returning to main menu!")
        }
        
        if (currChoice == 1) {
            
            if (isFileLoaded == true) {
                println("\nWarning: Do not attempt to read the file again!")
                return
            }
            
            DataManagement.readfile()
            
            var i = 0
            
            do {
                i++
                DataManagement.parseRow(DataManagement.records[i])
            } while (i < DataManagement.records.size - 1);
            
            isFileLoaded = true
            
            println("\nFile successfully loaded!")
            println("\nMetrics:")
            println("Total Rows: ${DataManagement.totalRows}")
            println("Erroneous Rows: ${DataManagement.rowDiscrepancies}")
            println("Non-PHP Transaction Logs: ${DataManagement.nonPHP}")
            
        } else if (currChoice == 2) {
            if (isFileLoaded == true) {
                reportFlow()
            } else {
                println("\nWarning: File has not yet been loaded")
            }
            
        }
    }
    
    
    // Menu Management
    
    
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
        } else if (nSelection == 6) {
            checkBalance()
        } else if (nSelection == 7) {
            genReports(sc)
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
        println("\n[6] Check Balance")
        println("\n[7] Generate Reports")
        println("\n[8] Close Account")
        println("\n[9] Log Out")
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
        
        if (excepted == false) {
            
            if (nChoice == 1 || nChoice == 2 || nChoice == 3 || nChoice == 4 || nChoice == 5 || nChoice == 6 || nChoice == 7) {
                callMenu(nChoice, sc)

            } else if (nChoice == 8) {
                if (currAcc.getAccNumb() == 1000000000) {
                    println("\nWarning: Invalid action for administrator account.")
                } else if (closeAccount(sysCtrl, sc) == true){
                    println("\nAccount ${currAcc.getUsername()} closed.")
                    logoutPass = true
                }
            } else if (nChoice == 9){
                println("\nLogging out of the system.")
                logoutPass = true
            } else {
                println("\nInvalid input! Range 1 - 8 only!")
            }
            
        }
        
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
    
        val ADMIN_ACC = Account(1000000000, ADMIN_NAME, ADMIN_PASS, LocalDateTime.now())
        var accounts = mutableListOf(ADMIN_ACC)
        
        // Retrieves an account within the list given an index (n)
        fun getAccList(n: Int): Account = accounts.get(n)
        
        /*
        * Searches if a user account exists within the list of accounts
        *
        * @param input is the account number of the account
        * @return the index of the account
        */
        fun searchUser(input: Int): Int {

            return accounts.indexOfFirst { it.getAccNumb() == input }

        }
        
    }
    
    
    // Helper Methods
    
    
    /*
     * Validates if an input is not empty
     * 
     * @param input is the string input by the user
     * @return a boolean stating if the input was empty or not
     */
    fun validateInput(input: String): Boolean {
        
        if (input.isBlank()) {
            
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


        var input = sc.nextLine().trim().toInt()

        return input
    }

    

    /**
    * Removes an account from the accounts mutable list, representing the registered users on the application
    *
    * @param user represents the account number of the account to be closed
    * @return NONE
    */
    fun removeAccount(user: Int) {

        val idx = searchUser(user)

        if (idx != -1) {
            accounts.removeAt(idx)
            numAccounts--
            Account.numAccs--
        }

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
    
    /*
     * Retrieves account number for newly made account
     * 
     * @return NONE
     */
    fun getNewAccNumber(): Int {
        
       var i = 0
       var numList = mutableListOf<Int>()
       
       do {
          numList.add(DataManagement.listTuple[i].getAccNumb())
          i++
       }
       while(i < DataManagement.listTuple.size) 
    
       var retVal = numList.max() + 1
       
       return retVal
    }
    
        
    // Core Methods
    
    
    /*
     * Allows a user to register an account given a name, and pin
     * 
     * @param sc is the scanner object allowing for basic input
     * @return NONE
     */
    fun regAcc(sc: Scanner) {
        
        var depositIn = 0.0 
        
        // First Layer Checks (Account Capacity)
        if (numAccounts >= MAX_ACC) {
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
        
        if (pinInput.length != 4) {
            println("\nWarning: Pin must be exactly 4 digits!")
            return
        }
        
        for (i in pinInput.indices) {
            if (pinInput[i] < '0' || pinInput[i] > '9') {
                println("\nWarning: Pin includes non-numeric characters!")
                return 
            }
        }
        
        var pin4 = pinInput
        
        print("\nInitial Deposit (min. Php 5000, This is a required field): ")
        
        try {
            depositIn = sc.nextDouble()
            
        } catch (e: Exception) {
            println("\nWarning: Input may not be a number!")
            println("\nDeposit Failed! Please try again!")
            sc.nextLine()
            return
        } 
        
        var temp = sc.nextLine()
        
        if (depositIn < 5000.0) {
            println("Warning: Initial deposit is lower than minimum!")
            return
        } 
        
        var newAcc: Account
        
        if (DataManagement.listTuple.size == 0) {
            newAcc = Account(1000000000 + Account.numAccs, nAccName, pin4, LocalDateTime.now())
        } else {
            newAcc = Account(getNewAccNumber(), nAccName, pin4, LocalDateTime.now())
        }
        
        accounts.add(newAcc)
        
        newAcc.deposit(depositIn)
        AccountCenter.writeToFile(newAcc, depositIn, "deposit", newAcc.getBal(), "PHP")
        println("\nSuccessfully made an initial deposit!")

        println("\n***")
        println("Account under \"$nAccName\" successfully created!")
        println("Your Account Number: ${newAcc.getAccNumb()}")
        
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
     
        print("\nEnter Account Number: ")
        var userIn = 0

        try {
            userIn = passInt(sc)
        } catch (e: Exception) {
            println("\nWarning: Input may not be a number!")
            return -1
        }

        var userIdx = searchUser(userIn)

        if (userIdx == -1) {
            println("\nWarning: Login Failed. Account not found")
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
                attempts--
                println("Warning: Pin is incorrect! You have $attempts more attempt/s!")
            }

        } while (attempts > 0 && pinPass == false)
        
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
    
    // Reference Types
    val sc = Scanner(System.`in`)
    val main = SysControl()
    val center = AccountCenter(SysControl.getAccList(0))
        
    AccountCenter.initializeRates()
    
    // Control flow for login screen
    while(bFlag == false) {
            
        nChoice = main.showMainMenu(sc)

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

                center.setAccount(SysControl.getAccList(0))

            } else {
                println("\nUser attempt to Login Failed! Please try again")
            }
            
        } else if (nChoice == 2) {  // Register Flow
            
            main.regAcc(sc)
            
            while (center.returnToMM(sc) == false) {
                main.regAcc(sc)
            }
            
        } else if (nChoice == 3) {  // Exit Flow
            
            println("\nThank you for using the Banking System!\n")    
        
            bFlag = true
            
        } else {
            
            println("\nInvalid input! Range 1 - 3 only!\n") // Error Message
            
        }
    }
             
}