/*
 * ********************
 * Last names: Bobadilla, Calvez, Casao, Sanico
 * Language: Kotlin
 * Paradigm(s): Functional Programming
 * ********************
 */

import java.util.Scanner

    fun regAcc(sc: Scanner) {
        
        println("\nRegister Account Name")
        print("Account Name: ")
        
        var nInput = sc.nextLine()
        
        println("\n***")
        println("Account Name = " + nInput)

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

    fun callMenu(nSelection: Int, sc: Scanner) {
        
        if (nSelection == 1) {
            regAcc(sc)
        } else if (nSelection == 2) {
            deposit(sc)
        } else if (nSelection == 3) {
            withdraw(sc)
        } else if (nSelection == 4) {
            recordER(sc)
        } else if (nSelection == 5) {
            currEx(sc)
        } else if (nSelection == 6) {
        }
        
    }

    fun showMainMenu(sc: Scanner) : Int {
        
        println("Select Transaction:")
        println("[1] Register Account Name")
        println("[2] Deposit Amount")
        println("[3] Withdraw Amount")
        println("[4] Currency Exchange")
        println("[5] Record Exchange Rates")
        println("[6] Show Interest Amount")
        print("\nChoice: ")
        
        var nChoice = sc.nextInt()
        var temp = sc.nextLine()
        
        if (nChoice != 1 && nChoice != 2 && nChoice != 3 && nChoice != 4 && nChoice != 5 && nChoice != 6) {
            nChoice = -1
        }
        
        return nChoice
        
    }

    fun main() {
        
        var bFlag = false
        var nChoice = 0
        val sc = Scanner(System.`in`)
        
        while(bFlag == false) {
            
            nChoice = showMainMenu(sc)
            
            if (nChoice != -1) {
                // I/O Checks
                println("\n***")
                println("Choice = " + nChoice + "\n")
                
                callMenu(nChoice, sc)
                bFlag = true
            } else {
                println("\nInvalid input! Range 1 - 6 only!\n")
            }
        }
        
        
    }

