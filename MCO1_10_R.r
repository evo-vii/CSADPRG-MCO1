#   ********************
#   Last names: Bobadilla, Calvez, Casao, Sanico
#   Language: R
#   Paradigm(s): Multi-paradigm (Functional, Object-Oriented)
#   ********************

# Displays the list of available transactions in the main menu
# @param none
# @return none
# @pre none
display_options <- function() {
  cat("Select Transaction:\n")
  cat("[1] Register Account Name\n")
  cat("[2] Deposit Amount\n")
  cat("[3] Withdraw Amount\n")
  cat("[4] Currency Exchange\n")
  cat("[5] Record Exchange Rates\n")
  cat("[6] Show Interest Amount\n")
}

# Displays the list of supported foreign currencies
# @param none
# @return none
# @pre none
display_currencies <- function() {
  cat("[1] Philippine Peso (PHP)\n")
  cat("[2] United States Dollar (USD)\n")
  cat("[3] Japanese Yen (JPY)\n")
  cat("[4] British Pound Sterling (GBP)\n")
  cat("[5] Euro (EUR)\n")
  cat("[6] Chinese Yuan Renminni (CNY)\n\n")
}

# Prompts user repeatedly until an integer within the specified range is entered
# @param min Minimum integer choice allowed
# @param max Maximum integer choice allowed
# @param prompt String text displayed to the user
# @return Integer valid choice between min and max
# @pre min <= max
get_valid_choice <- function(min = 1, max = 6, prompt = "Choice: ") {
  repeat {
    choice <- suppressWarnings(as.integer(readline(prompt = prompt)))
    if (!is.na(choice) && choice >= min && choice <= max) {
      return(choice)
    }
    cat(sprintf("Invalid Input. Please enter a number between %d and %d!\n", min, max))
  }
}

# Prompts user repeatedly until a positive numeric amount is entered
# @param prompt String text displayed to the user
# @return Numeric valid positive number
# @pre none
get_valid_amount <- function(prompt = "Amount: ") {
  repeat {
    amount <- suppressWarnings(as.numeric(readline(prompt = prompt)))
    if (!is.na(amount) && amount > 0) {
      return(amount)
    }
    cat("Invalid Input. Please enter a valid positive number!\n")
  }
}

# Prompts user to confirm whether to return to the main menu or exit
# @param none
# @return Logical TRUE if user enters 'y', FALSE if 'n'
# @pre none
back_to_main_menu <- function() {
  repeat {
    choice <- tolower(readline(prompt = "Go back to main menu [y/n]: "))
    if (choice == "y") {
      return(TRUE)
    } else if (choice == "n") {
      cat("Exiting The Program!\n")
      return(FALSE)
    }
    cat("Invalid Input! Please Enter 'y' or 'n'.\n")
  }
}

convert_again <- function() {
  repeat {
    choice <- tolower(readline(prompt = "Convert another currency (Y/N)? "))
    if (choice == "y") {
      return(TRUE)
    } else if (choice == "n") {
      return(FALSE)
    }
    cat("Invalid Input! Please Enter 'y' or 'n'.\n")
  }
}

# Constructor for creating a new bank_account S3 object
# @param name Initial account holder name
# @param balance Initial balance amount
# @param currency Initial currency abbreviation
# @return S3 object of class 'bank_account'
# @pre balance >= 0
new_bank_account <- function(name = "Not Registered", balance = 0, currency = "PHP") {
  account <- list(
    name = name,
    balance = balance,
    currency = currency,
    currencies = c("PHP", "USD", "JPY", "GBP", "EUR", "CNY"),
    exchange_rates = c(
      "PHP" = 1.00,
      "USD" = 62.00,
      "JPY" = 0.40,
      "GBP" = 84.00,
      "EUR" = 72.00,
      "CNY" = 9.00
    )
  )
  class(account) <- "bank_account"
  return(account)
}

# Custom S3 print method to display account summary card
# @param x S3 object of class 'bank_account'
# @param ... Additional arguments passed to print
# @return none
# @pre x must be of class 'bank_account'
print.bank_account <- function(x, ...) {
  cat(sprintf("Account Name: %s\n", x$name))
  cat(sprintf("Current Balance: %.2f\n", x$balance))
  cat(sprintf("Currency: %s\n\n", x$currency))
}

# --- S3 Generic Declarations ---
register_name <- function(account, ...) UseMethod("register_name")
deposit       <- function(account, ...) UseMethod("deposit")
withdraw      <- function(account, ...) UseMethod("withdraw")
set_rate      <- function(account, ...) UseMethod("set_rate")
convert_curr  <- function(account, ...) UseMethod("convert_curr")
calc_interest <- function(account, ...) UseMethod("calc_interest")

# Updates the account holder name
# @param account S3 object of class 'bank_account'
# @param new_name String representing the new account holder name
# @return Updated S3 'bank_account' object with new name
# @pre new_name is not empty
register_name.bank_account <- function(account, new_name) {
  account$name <- new_name
  return(account)
}

# Adds a deposit amount to the account balance
# @param account S3 object of class 'bank_account'
# @param amount Numeric positive amount to deposit
# @return Updated S3 'bank_account' object with incremented balance
# @pre amount > 0
deposit.bank_account <- function(account, amount) {
  account$balance <- account$balance + amount
  return(account)
}

# Deducts a withdrawal amount if sufficient balance exists
# @param account S3 object of class 'bank_account'
# @param amount Numeric positive amount to withdraw
# @return List containing success status, updated account, and error message if failed
# @pre amount > 0
withdraw.bank_account <- function(account, amount) {
  if (amount > account$balance) {
    return(list(success = FALSE, account = account, error = "Insufficient funds"))
  }
  account$balance <- account$balance - amount
  return(list(success = TRUE, account = account, error = NULL))
}

# Updates the exchange rate of a specific currency
# @param account S3 object of class 'bank_account'
# @param currency_code String representing the currency key (e.g., 'USD')
# @param new_rate Numeric positive new exchange rate
# @return Updated S3 'bank_account' object with updated rate
# @pre currency_code exists in exchange_rates, new_rate > 0
set_rate.bank_account <- function(account, currency_code, new_rate) {
  account$exchange_rates[currency_code] <- new_rate
  return(account)
}

# Calculates currency conversion amount between two currencies
# @param account S3 object of class 'bank_account'
# @param src_idx Integer index of the source currency (1-6)
# @param dest_idx Integer index of the target currency (1-6)
# @param amount Numeric positive amount in source currency
# @return Numeric converted amount in destination currency
# @pre src_idx and dest_idx are within 1 to length(currencies), amount > 0
convert_curr.bank_account <- function(account, src_idx, dest_idx, amount) {
  src_currency <- account$currencies[src_idx]
  dest_currency <- account$currencies[dest_idx]

  src_rate <- account$exchange_rates[src_currency]
  dest_rate <- account$exchange_rates[dest_currency]

  return(amount * src_rate / dest_rate)
}

# Simulates daily compounding interest projection over a given number of days
# @param account S3 object of class 'bank_account'
# @param days Integer total number of days for simulation
# @param annual_rate Numeric annual interest rate (default: 0.05)
# @return Data frame containing columns Day, Interest, and Balance
# @pre days > 0, account$balance >= 0
calc_interest.bank_account <- function(account, days, annual_rate = 0.05) {
  day_col <- integer(days)
  interest_col <- numeric(days)
  balance_col <- numeric(days)
  temp_balance <- account$balance

  for (d in 1:days) {
    daily_interest <- round(temp_balance * (annual_rate / 365), 2)
    temp_balance <- temp_balance + daily_interest
    day_col[d] <- d
    interest_col[d] <- daily_interest
    balance_col[d] <- temp_balance
  }

  return(data.frame(
    Day = day_col,
    Interest = interest_col,
    Balance = balance_col
  ))
}

# UI handler for registering an account holder name
# @param account S3 object of class 'bank_account'
# @return Updated S3 'bank_account' object
# @pre none
register_account <- function(account) {
  cat("Register Account Name\n")
  name <- readline(prompt = "Account Name: ")

  account <- register_name(account, name)

  cat("\n***\n")
  cat(sprintf("Account Name = %s\n", account$name))
  return(account)
}

# UI handler for depositing funds and showing updated balance
# @param account S3 object of class 'bank_account'
# @return Updated S3 'bank_account' object
# @pre none
deposit_amount <- function(account) {
  cat("Deposit Amount\n")

  if (account$name == "Not Registered") {
    cat("Please register an account name first (option 1)!\n")
    return(account)
  }

  print(account)

  display_currencies()
  deposit_choice <- get_valid_choice(1, length(account$currencies), prompt = "Deposit Currency: ")
  deposit_currency <- account$currencies[deposit_choice]

  deposit_val <- get_valid_amount(prompt = "Deposit Amount: ")
  php_val <- convert_curr(account, deposit_choice, 1, deposit_val)

  account <- deposit(account, php_val)

  cat("\n***\n")
  cat(sprintf("Account Name = %s\n", account$name))
  cat(sprintf("Deposit Amount = %.2f %s\n", deposit_val, deposit_currency))
  cat(sprintf("Amount Credited = %.2f %s\n", php_val, account$currency))
  cat(sprintf("Updated Balance = %.2f\n", account$balance))
  return(account)
}

# UI handler for withdrawing funds with overdraft protection
# @param account S3 object of class 'bank_account'
# @return Updated S3 'bank_account' object (or unchanged if insufficient funds)
# @pre none
withdraw_amount <- function(account) {
  cat("Withdraw Amount\n")

  if (account$name == "Not Registered") {
    cat("Please register an account name first (option 1)!\n")
    return(account)
  }

  print(account)

  display_currencies()
  withdraw_choice <- get_valid_choice(1, length(account$currencies), prompt = "Withdraw Currency: ")
  withdraw_currency <- account$currencies[withdraw_choice]

  withdraw_val <- get_valid_amount(prompt = "Withdraw Amount: ")
  php_val <- convert_curr(account, withdraw_choice, 1, withdraw_val)

  res <- withdraw(account, php_val)

  if (!res$success) {
    cat("\n***\n")
    cat(sprintf("Transaction Failed: %s!\n", res$error))
    cat(sprintf("Current Balance remains = %.2f\n", account$balance))
    return(account)
  } else {
    account <- res$account
    cat("\n***\n")
    cat(sprintf("Account Name = %s\n", account$name))
    cat(sprintf("Withdraw Amount = %.2f %s\n", withdraw_val, withdraw_currency))
    cat(sprintf("Amount Debited = %.2f %s\n", php_val, account$currency))
    cat(sprintf("Updated Balance = %.2f\n", account$balance))
    return(account)
  }
}

# UI handler for performing foreign currency conversion
# @param account S3 object of class 'bank_account'
# @return S3 'bank_account' object unchanged
# @pre none
currency_exchange <- function(account) {
  repeat {
    cat("Foreign Currency Exchange\n")
    display_currencies()

    source_choice <- get_valid_choice(1, length(account$currencies), prompt = "Source Currency: ")
    source_amount <- get_valid_amount(prompt = "Source Amount: ")

    cat("\n\nExchanged Currency\n")
    display_currencies()

    exchange_choice <- get_valid_choice(1, length(account$currencies), prompt = "Exchange Currency: ")
    exchange_currency <- account$currencies[exchange_choice]

    exchange_amount <- convert_curr(account, source_choice, exchange_choice, source_amount)

    cat("\n***\n")
    cat(sprintf("Source Amount: %.2f\n", source_amount))
    cat(sprintf("Exchanged Currency: %s\n", exchange_currency))
    cat(sprintf("Exchanged Amount: %.2f\n\n", exchange_amount))

    if (!convert_again()) {
      break
    }
    cat("\n***\n")
  }
  return(account)
}

# UI handler for updating foreign exchange rates
# @param account S3 object of class 'bank_account'
# @return Updated S3 'bank_account' object with new exchange rate
# @pre none
record_exchange_rates <- function(account) {
  cat("Record Exchange Rate\n\n")
  display_currencies()

  foreign_choice <- get_valid_choice(1, length(account$currencies), prompt = "Select Foreign Currency (1-6): ")
  while (foreign_choice == 1) {
    cat("PHP is the base currency and its rate cannot be changed!\n")
    foreign_choice <- get_valid_choice(1, length(account$currencies), prompt = "Select Foreign Currency (1-6): ")
  }
  selected_currency <- account$currencies[foreign_choice]
  new_rate <- get_valid_amount(prompt = "Exchange Rate: ")

  account <- set_rate(account, selected_currency, new_rate)

  cat("\n***\n")
  cat(sprintf("Currency: %s\n", selected_currency))
  cat(sprintf("Updated Exchange Rate: %.2f\n\n", new_rate))
  return(account)
}

# UI handler for calculating and rendering the expected daily interest schedule
# @param account S3 object of class 'bank_account'
# @return S3 'bank_account' object unchanged
# @pre none
show_interest_amount <- function(account) {
  cat("Show Interest Amount\n")
  cat(sprintf("Account Name: %s\n", account$name))
  cat(sprintf("Current Balance: %.2f\n", account$balance))
  cat(sprintf("Currency: %s\n", account$currency))
  cat("Interest Rate: 5%\n\n")

  repeat {
    raw_days <- readline(prompt = "Total Number of Days: ")
    pattern <- "^\\s*([0-9]+)\\s*(days?)?\\s*$"
    if (grepl(pattern, raw_days, ignore.case = TRUE)) {
      days <- suppressWarnings(as.integer(sub(pattern, "\\1", raw_days, ignore.case = TRUE)))
      if (!is.na(days) && days > 0) {
        break
      }
    }
    cat("Invalid Input. Please enter a valid positive number of days!\n")
  }

  schedule <- calc_interest(account, days)

  cat("\nDay | Interest | Balance |\n")
  for (i in 1:nrow(schedule)) {
    cat(sprintf("%-4d| %-9.2f| %-8.2f|\n", schedule$Day[i], schedule$Interest[i], schedule$Balance[i]))
  }
  cat("\n")
  return(account)
}

# MAIN
account <- new_bank_account()
is_running <- TRUE

while (is_running) {
  display_options()
  choice <- get_valid_choice(1, 6)
  cat("\n***\n")
  
  account <- switch(choice,
    "1" = register_account(account),
    "2" = deposit_amount(account),
    "3" = withdraw_amount(account),
    "4" = currency_exchange(account),
    "5" = record_exchange_rates(account),
    "6" = show_interest_amount(account)
  )

  if (choice != 4 && !back_to_main_menu()) {
    is_running <- FALSE
  }
  cat("\n***\n")
}
