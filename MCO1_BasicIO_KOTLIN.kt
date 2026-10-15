/*
 * ********************
 * Last names: Bobadilla, Calvez, Casao, Sanico
 * Language: Kotlin
 * Paradigm(s): Functional Programming, Object-Oriented Programming
 * ********************
 */


// Pre-processor Directives
import java.util.Scanner


// Classes


class Account (var accNum: Int, var name: String, var pin: String, var contactNum: String) {
    
    var currBal = 0.0
    
    companion object {
        var numAccs = 0
    }
    
    
    init {
        
        numAccs++
        
    }
    
    fun deposit(f: Float) {
        currBal += f
    }
    
    fun withdraw(f: Float): Boolean {
        if (currBal < f) {
            return false
        } else {
            currBal -= f 
            return true
        }
    }
    
    fun getAccNumb() : Int {
        return accNum
    }
    
    fun getUsername() : String {
        return name
    }
    
    fun getAccPin() : String {
        return pin
    }
    
    fun getBal() : Double {
        return currBal
    }
    
    fun getNumOfAccs(): Int {
        return numAccs
    }
    
    
}

class ExcRate (var code: String, var name: String, var rate: Double){
    
    
    
}

class AccountCenter (var currAcc: Account) {

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
    
    fun deposit(sc: Scanner) {
        
        var depositIn = 0.0.toFloat()
        
        println("\nDeposit Amount")
        println("Account Name: ${currAcc.getUsername()}")
        
        println("Current Balance: ${currAcc.getBal()}")
        println("Currency: PHP")
        
        print("\nDeposit Amount: ")
        
        try {
            depositIn = sc.nextFloat()
        } catch (e: Exception) {
            println("\nWarning: Input may not be a number!")
            println("\nDeposit Failed! Please try again!")
            return
        }
        
        currAcc.deposit(depositIn)
        
        println("\n***")
        println("Account Name: ${currAcc.getUsername()}")
        println("Current Balance: ${currAcc.getBal()}")
        println("Deposited Amount = " + "%.2f".format(depositIn))
        
    }

    fun withdraw(sc: Scanner) {
        
        var withdrawIn = 0.0.toFloat()
        
        println("\nWithdraw Amount")
        println("Account Name: ${currAcc.getUsername()}")
        
        println("Current Balance: ${currAcc.getBal()}")
        println("Currency: PHP")
        
        print("\nWithdraw Amount: ")
        
        try {
            withdrawIn = sc.nextFloat()
        } catch (e: Exception) {
            println("\nWarning: Input may not be a number!")
            println("\nWithdraw Failed! Please try again!")
            return
        }
        
        if (currAcc.withdraw(withdrawIn) == false) {
            println("Withdraw Failed! Insufficient Funds!")
            return 
        }
        
        println("\n***")
        println("Account Name: ${currAcc.getUsername()}")
        println("Current Balance: ${currAcc.getBal()}")
        println("Withdrawal Amount = " + "%.2f".format(withdrawIn))
        
        
    }

    fun recordER(sc: Scanner) {
        
        var inputVal = false
        var choice = 0
        
        println("\nRecord Exchange Rate\n")
        
        println("[1] Philippine Peso (PHP)")
        println("[2] United States Dollar (USD)")
        println("[3] Japanese Yen (JPY)")
        println("[4] British Pound Sterling (GBP)")
        println("[5] Euro (EUR)")
        println("[6] Chinese Yuan Renminni (CNY)")
        
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
        
        print("Exchange Rate: ")
        
        var rateIn = sc.nextFloat()
        
        println("\n***")
        println("Select Foreign Currency = [" + choice + "]")
        println("Exchange Rate = " + "%.2f".format(rateIn)) 
        
    }
    
    fun currEx(sc: Scanner) {
        
        println("\nForeign Currency Exchange")
        print("Source Amount (PHP): ")
        
        var currIn = sc.nextFloat()
        
        println("\nExchanged Currency")
        println("[1] Philippine Peso (PHP) = " + "%.2f".format(currIn * 1))
        println("[2] United States Dollar (USD) = " + "%.2f".format(currIn * 62.00))
        println("[3] Japanese Yen (JPY) = " + "%.2f".format(currIn * 0.40))
        println("[4] British Pound Sterling (GBP) = " + "%.2f".format(currIn * 84.00))
        println("[5] Euro (EUR) = " + "%.2f".format(currIn * 72.00))
        println("[6] Chinese Yuan Renminni (CNY) = " + "%.2f".format(currIn * 9.00))
        
        println("\n***")
        println("Source Currency = Philippine Peso (PHP)")
        println("Source Amount (PHP) = " + "%.2f".format(currIn))
        
    }
    
    fun callMenu(nSelection: Int, sc: Scanner) {
        
        if (nSelection == 1) {
            deposit(sc)
        } else if (nSelection == 2) {
            withdraw(sc)
        } else if (nSelection == 3) {
            recordER(sc)
        } else if (nSelection == 4) {
            currEx(sc)
        } else if (nSelection == 5) {
        }
        
    }
    
    fun showTransacMenu(sc: Scanner) : Boolean {
        
        println("Select Transaction:")
        println("[1] Deposit Amount")
        println("[2] Withdraw Amount")
        println("[3] Currency Exchange")
        println("[4] Record Exchange Rates")
        println("[5] Show Interest Amount")
        println("[6] Log Out")
        print("\nChoice: ")
        
        print(">> ")
        var nChoice = sc.nextInt()
        var temp = sc.nextLine()
        
        var logoutPass = false
        
        do {
            if (nChoice == 1 || nChoice == 2 || nChoice == 3 || nChoice == 4 || nChoice == 5) {
                callMenu(nChoice, sc)
            } else if (nChoice == 6){
                println("\nLogging out of the system.")
                logoutPass = true
            }
        } while (nChoice != 1 && nChoice != 2 && nChoice != 3 && nChoice != 4 && nChoice != 5 && nChoice != 6)
        
        return logoutPass
    }
    
    fun setAccount(a: Account) {
        currAcc = a
    }
}

class SysControl {

    
    // Attributes
    
    
    var numAccounts = 0
    
    val MAX_ACC = 100
    
    companion object {
        
        val ADMIN_NAME = "admin"
        val ADMIN_PASS = "2580"
    
        val ADMIN_ACC = Account(1000, ADMIN_NAME, ADMIN_PASS, "639999999999")
        var accounts = mutableListOf(ADMIN_ACC)
        
        fun getAccList(n: Int): Account = accounts.get(n)
    }
    
    // Helper Methods
    
    
    fun validateInput(nInput: String): Boolean {
        
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
    
    fun matchPin(input: String, accIdx: Int): Boolean {
        
        if (accounts.get(accIdx).getAccPin() == input) {
            return true
        }
        
        return false
        
    }
    
        
    // Core Methods
        
        
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
        print("Account Name: ")
        
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
        print("Enter New Pin: ")
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
        
        // Input for Contact Number
        print("\nEnter Contact Number: ")
        var CNInput = sc.nextLine()
        
        if (validateInput(CNInput) == false) {
            return
        }
        
        if (CNInput.length != 11) {
            println("\nWarning: Invalid length of mobile number!")
            return
        }
        
        for (i in pinInput.indices) {
            if (pinInput[i] < '0' || pinInput[i] > '9') {
                println("\nWarning: Pin includes non-numeric characters!")
                return 
            }
        }
        
        var contact = CNInput
        
        accounts.add(Account(1000 + Account.numAccs, nAccName, pin4, contact))
        
        var accIndex = 0
        
        for (j in accounts) {
            if (j.getUsername().compareTo(nAccName) == 0) {
                accIndex = accounts.indexOf(j)
            }
        }
        
        numAccounts++
        
        // FOR DEBUG - INPUT CHECK
        println("\n***")
        println("Inputted Account Name = " + accounts.get(accIndex).getUsername())
        println(numAccounts)

    }

    
    // Control Methods
    

    fun showMainMenu(sc: Scanner) : Int {
        
        println("\n========================================\n");
        println("        BANKING SYSTEM - LOGIN\n");
        println("========================================\n");
        println("[1] Login\n");
        println("[2] Register New Account\n");
        println("[3] Exit Program\n");
        println("========================================\n");
        
        print(">> ")
        var nChoice = sc.nextInt()
        var temp = sc.nextLine()
        
        if (nChoice != 1 && nChoice != 2 && nChoice != 3) {
            nChoice = -1
        }
        
        return nChoice
        
    }
    
    fun showLogin(sc: Scanner) : Int {
     
        print("Enter Account Name: ")
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

    
    // Setters

    
    
    
    
    // Getters

    
    
    
}
    

// Main Function

fun main() {
        
    // Control Variables
    var bFlag = false
    var nChoice = 0
    var nMainChoice = 0
    
    // Reference Types
    val sc = Scanner(System.`in`)
    val main = SysControl()
    val center = AccountCenter(SysControl.getAccList(0))
        
    // Control flow for login screen
    while(bFlag == false) {
            
        nChoice = main.showMainMenu(sc)
            
        if (nChoice == 1) {         // Login Flow
            
            // I/O Checks
            println("\n***")
            println("Choice = " + nChoice + "\n")
            
            var activeIdx = main.showLogin(sc)
            
            // Account Validation
            if (activeIdx != -1) {
                println("Active Account: ${SysControl.getAccList(activeIdx).getUsername()}")
                
                center.setAccount(SysControl.getAccList(activeIdx))
                
                var logout = false
                
                while (logout == false) {
                    logout = center.showTransacMenu(sc)
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

