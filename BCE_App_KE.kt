
import java.util.Scanner


fun callMenu(nSelection: Int) {
    
    if (nSelection == 1) {
        println("In Menu 1")
    } else if (nSelection == 2) {
        println("In Menu 2")
    } else if (nSelection == 3) {
        println("In Menu 3")
    } else if (nSelection == 4) {
        println("In Menu 4")
    } else if (nSelection == 5) {
        println("In Menu 5")
    } else if (nSelection == 6) {
        println("In Menu 6")
    }
    
}

fun showMainMenu() : Int {

    val sc = Scanner(System.`in`)
    
    println("Select Transaction:")
    println("[1] Register Account Name")
    println("[2] Deposit Amount")
    println("[3] Withdraw Amount")
    println("[4] Currency Exchange")
    println("[5] Record Exchange Rates")
    println("[6] Show Interest Amount")
    print("\n\n>> ")
    
    var nChoice = sc.nextInt()
    
    if (nChoice != 1 && nChoice != 2 && nChoice != 3 && nChoice != 4 && nChoice != 5 && nChoice != 6) {
        nChoice = -1
    }
    
    return nChoice
    
}

fun main() {
    
    var bFlag = false
    var nChoice = 0
    
    while(bFlag == false) {
        
        nChoice = showMainMenu()
        
        if (nChoice != -1) {
            callMenu(nChoice)
            bFlag = true
        } else {
            println("\nInvalid input! Range 1 - 6 only!\n")
        }
    }
    
    
}