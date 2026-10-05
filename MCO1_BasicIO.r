current_balance <- 1000.0
currency <- "PHP"

cat("Select Transaction:\n")
cat("[1] Register Account Name\n")
cat("[2] Deposit Amount\n")
cat("[3] Withdraw Amount\n")
cat("[4] Currency Exchange\n")
cat("[5] Record Exchange Rates\n")
cat("[6] Show Interest Amount\n")
choice <- as.numeric(readline(prompt = "Choice: "))
cat("\n***\nChoice = ", choice, "\n\n")

switch(choice,
    "1" = {
        account_name <- readline(prompt = "Account Name: ")
        cat("\n***\nAccount Name = ", account_name)
    },
    "2" = {
        cat("Deposit Amount\n")
        account_name <- readline(prompt = "Account Name: ")
        cat("Current Balance: ", current_balance, "\n")
        cat("Currency: ", currency, "\n\n")
        deposit_amount <- as.numeric(readline(prompt = "Deposit Amount: "))
        cat("\n***\nAccount Name = ", account_name, "\nDeposit Amount = ", deposit_amount)
    },
    "3" = {
        cat("Deposit Amount\n")
        account_name <- readline(prompt = "Account Name: ")
        cat("Current Balance: ", current_balance, "\n")
        cat("Currency: ", currency, "\n\n")
        withdraw_amount <- as.numeric(readline(prompt = "Widthraw Amount: "))
        cat("\n***\nAccount Name = ", account_name, "\nWidthraw Amount = ", withdraw_amount)
    },
    "4" = {
        cat("Foreign Currency Exchange\n")
        source_amount <- as.numeric(readline(prompt = "Source Amount (PHP): "))
        cat("\nExchanged Currency\n")
        cat("[1] Philippine Peso (PHP) = ", source_amount, "\n")
        cat("[2] United States Dollar (USD) = ", source_amount * 62.00, "\n")
        cat("[3] Japanese Yen (JPY) = ", source_amount * 0.40, "\n")
        cat("[4] British Pound Sterling (GBP) = ", source_amount * 84.00, "\n")
        cat("[5] Euro (EUR) = ", source_amount * 72.00, "\n")
        cat("[6] Chinese Yuan Renminni (CNY) = ", source_amount * 9.00, "\n")
        cat("\n***\nSource Currency = Philippine Peso (PHP)\nSource Amount = ", source_amount)
    },
    "5" = {
        cat("Record Exchange Rate\n\n")
        cat("[1] Philippine Peso (PHP)\n")
        cat("[2] United States Dollar (USD)\n")
        cat("[3] Japanese Yen (JPY)\n")
        cat("[4] British Pound Sterling (GBP)\n")
        cat("[5] Euro (EUR)\n")
        cat("[6] Chinese Yuan Renminni (CNY)\n\n")
        foreign_currency <- readline(prompt = "Select Foreign Currency: ")
        exchange_rate <- readline(prompt = "Exchange Rate: ")
        cat("\n***\nSelect Foreign Currency = ", foreign_currency, "\nExchange Rate = ", exchange_rate)
    },
    "6" = {
        cat("Show Interest Amount\n")
        cat("Coming Soon!")
    }    
)
