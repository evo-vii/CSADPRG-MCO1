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


class Account (var accNum: Int, var name: String, var pin: String, var contactNum: Int) {
    
    init {
        
        
        
    }
    
}

class ExcRate (var code: String, var name: String, var rate: Double){
    
    
    
}

class SysControl {

    
    // Helpers
    
    
    fun logUser() : Boolean {
        
        
        
        
        
        return bPass
        
    }
        
    
    // Core Methods
    
    
    fun regAcc(sc: Scanner) {
        
        var bPass = true
        
        // Menu Header
        println("\nRegister Account Name")
        print("Account Name: ")
        
        var nInput = sc.nextLine()
        
        // FOR DEBUG - INPUT CHECK
        println("\n***")
        println("Inputted Account Name = " + nInput)
        
        // First Layer Checks
        if (numAccounts > MAX_ACC) {
            bPass = false
            println("Warning! Maximum Number of Accounts Exceeded!")
        }

        sc.close()

    }

    fun deposit(sc: Scanner) {
        
        println("\nDeposit Amount")
        print("Account Name: ")
        
        var nameIn = sc.nextLine()
        
        println("Current Balance: 1000.00")
        println("Currency: PHP")
        
        print("\nDeposit Amount: ")
        
        var depositIn = sc.nextFloat()
        
        println("\n***")
        println("Account Name = " + nameIn)
        println("Deposit Amount = " + "%.2f".format(depositIn))
        
        sc.close()
    }

    fun withdraw(sc: Scanner) {
        
        println("\nWithdraw Amount")
        print("Account Name: ")
        
        var nameIn = sc.nextLine()
        
        println("Current Balance: 1000.00")
        println("Currency: PHP")
        
        print("\nWithdraw Amount: ")
        
        var withdrawIn = sc.nextFloat()
        
        println("\n***")
        println("Account Name = " + nameIn)
        println("Withdraw Amount = " + "%.2f".format(withdrawIn))
        
        sc.close()
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
        
        sc.close()
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
        
        sc.close()
    }

    
    // Control Methods
    
    
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

    fun showLogin(sc: Scanner) : Int {
        
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
    
    fun showMainMenu(sc: Scanner) : Int {
        
        println("Select Transaction:")
        println("[1] Deposit Amount")
        println("[2] Withdraw Amount")
        println("[3] Currency Exchange")
        println("[4] Record Exchange Rates")
        println("[5] Show Interest Amount")
        print("\nChoice: ")
        
        print(">> ")
        var nChoice = sc.nextInt()
        var temp = sc.nextLine()
        
        if (nChoice != 1 && nChoice != 2 && nChoice != 3 && nChoice != 4 && nChoice != 5) {
            nChoice = -1
        }
        
        return nChoice
        
    }

    
    // Attributes
    
    
    var numAccounts = 0
    
    val MAX_ACC = 100
    val ADMIN_NAME = "admin"
    val ADMIN_PASS = "2580"
    
    
}

// Main Function

fun main() {
        
    var bFlag = false
    var nChoice = 0
    var nMainChoice = 0
    val sc = Scanner(System.`in`)
    val main = SysControl()
        
    // Control flow for login screen
    while(bFlag == false) {
            
        nChoice = main.showLogin(sc)
            
        if (nChoice == 1) {         // Login Flow
            
            // I/O Checks
            println("\n***")
            println("Choice = " + nChoice + "\n")
            
            nMainChoice = main.showMainMenu(sc)
            main.callMenu(nChoice, sc)
            bFlag = true
            
        } else if (nChoice == 2) {  // Register Flow
            
            main.regAcc(sc)
            
        } else if (nChoice == 3) {  // Exit Flow
            
            println("Thank you for using the Banking System!\n")    
        
            bFlag = true
        } else {
            
            println("\nInvalid input! Range 1 - 6 only!\n") // Error Message
            
        }
    }
        
        
}

