#   ********************
#   Last names: Bobadilla, Calvez, Casao, Sanico
#   Language: R
#   Paradigm(s): Multi-paradigm (Functional, Object-Oriented)
#   ********************

display_options <- function() {
  cat("Select Transaction:\n")
  cat("[1] Register Account Name\n")
  cat("[2] Deposit Amount\n")
  cat("[3] Withdraw Amount\n")
  cat("[4] Currency Exchange\n")
  cat("[5] Record Exchange Rates\n")
  cat("[6] Show Interest Amount\n")
  cat("[7] Exit The Program\n")
}

display_currencies <- function() {
  cat("[1] Philippine Peso (PHP)\n")
  cat("[2] United States Dollar (USD)\n")
  cat("[3] Japanese Yen (JPY)\n")
  cat("[4] British Pound Sterling (GBP)\n")
  cat("[5] Euro (EUR)\n")
  cat("[6] Chinese Yuan Renminbi (CNY)\n\n")
}

get_valid_choice <- function(min = 1, max = 7, prompt = "Choice: ") {
  repeat {
    choice <- suppressWarnings(as.integer(readline(prompt = prompt)))
    if (!is.na(choice) && choice >= min && choice <= max) {
      return(choice)
    }
    cat(sprintf("Invalid Input. Please enter a number between %d and %d!\n", min, max))
  }
}

get_valid_amount <- function(prompt = "Amount: ") {
  repeat {
    amount <- suppressWarnings(as.numeric(readline(prompt = prompt)))
    if (!is.na(amount) && amount > 0) {
      return(amount)
    }
    cat("Invalid Input. Please enter a valid positive number!\n")
  }
}

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

new_bank_account <- function(name = "Not Registered", balance = 1000.0, currency = "PHP") {
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

print.bank_account <- function(x, ...) {
  cat(sprintf("Account Name: %s\n", x$name))
  cat(sprintf("Current Balance: %.2f\n", x$balance))
  cat(sprintf("Currency: %s\n\n", x$currency))
}


register_name <- function(account, ...) UseMethod("register_name")
deposit       <- function(account, ...) UseMethod("deposit")
withdraw      <- function(account, ...) UseMethod("withdraw")
set_rate      <- function(account, ...) UseMethod("set_rate")
convert_curr  <- function(account, ...) UseMethod("convert_curr")
calc_interest <- function(account, ...) UseMethod("calc_interest")

register_name.bank_account <- function(account, new_name) {
  account$name <- new_name
  return(account)
}

deposit.bank_account <- function(account, amount) {
  account$balance <- account$balance + amount
  return(account)
}

withdraw.bank_account <- function(account, amount) {
  if (amount > account$balance) {
    return(list(success = FALSE, account = account, error = "Insufficient funds"))
  }
  account$balance <- account$balance - amount
  return(list(success = TRUE, account = account, error = NULL))
}

set_rate.bank_account <- function(account, currency_code, new_rate) {
  account$exchange_rates[currency_code] <- new_rate
  return(account)
}

convert_curr.bank_account <- function(account, src_idx, dest_idx, amount) {
  src_currency <- account$currencies[src_idx]
  dest_currency <- account$currencies[dest_idx]

  src_rate <- account$exchange_rates[src_currency]
  dest_rate <- account$exchange_rates[dest_currency]

  return(amount * src_rate / dest_rate)
}

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

register_account <- function(account) {
  cat("Register Account Name\n")
  name <- readline(prompt = "Account Name: ")
  
  account <- register_name(account, name)

  cat("\n***\n")
  cat(sprintf("Account Name = %s\n", account$name))
  return(account)
}

deposit_amount <- function(account) {
  cat("Deposit Amount\n")
  print(account)

  deposit_val <- get_valid_amount(prompt = "Deposit Amount: ")

  account <- deposit(account, deposit_val)

  cat("\n***\n")
  cat(sprintf("Account Name = %s\n", account$name))
  cat(sprintf("Deposit Amount = %.2f\n", deposit_val))
  cat(sprintf("Updated Balance = %.2f\n", account$balance))
  return(account)
}

withdraw_amount <- function(account) {
  cat("Withdraw Amount\n")
  print(account)

  withdraw_val <- get_valid_amount(prompt = "Withdraw Amount: ")

  res <- withdraw(account, withdraw_val)

  if (!res$success) {
    cat("\n***\n")
    cat(sprintf("Transaction Failed: %s!\n", res$error))
    cat(sprintf("Current Balance remains = %.2f\n", account$balance))
    return(account)
  } else {
    account <- res$account
    cat("\n***\n")
    cat(sprintf("Account Name = %s\n", account$name))
    cat(sprintf("Withdraw Amount = %.2f\n", withdraw_val))
    cat(sprintf("Updated Balance = %.2f\n", account$balance))
    return(account)
  }
}

currency_exchange <- function(account) {
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
  return(account)
}

record_exchange_rates <- function(account) {
  cat("Record Exchange Rate\n\n")
  display_currencies()

  foreign_choice <- get_valid_choice(1, length(account$currencies), prompt = "Select Foreign Currency (1-6): ")
  selected_currency <- account$currencies[foreign_choice]
  new_rate <- get_valid_amount(prompt = "Exchange Rate: ")

  account <- set_rate(account, selected_currency, new_rate)

  cat("\n***\n")
  cat(sprintf("Currency: %s\n", selected_currency))
  cat(sprintf("Updated Exchange Rate: %.2f\n\n", new_rate))
  return(account)
}

show_interest_amount <- function(account) {
  cat("Show Interest Amount\n")
  cat(sprintf("Account Name: %s\n", account$name))
  cat(sprintf("Current Balance: %.2f\n", account$balance))
  cat(sprintf("Currency: %s\n", account$currency))
  cat("Interest Rate: 5%\n\n")

  repeat {
    raw_days <- readline(prompt = "Total Number of Days: ")
    days <- suppressWarnings(as.integer(gsub("[^0-9]", "", raw_days)))
    if (!is.na(days) && days > 0) {
      break
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

account <- new_bank_account()
is_running <- TRUE

while (is_running) {
  display_options()
  choice <- get_valid_choice(1, 7)
  cat("\n***\n")
  cat(sprintf("Choice = %d\n\n", choice))

  account <- switch(choice,
    "1" = register_account(account),
    "2" = deposit_amount(account),
    "3" = withdraw_amount(account),
    "4" = currency_exchange(account),
    "5" = record_exchange_rates(account),
    "6" = show_interest_amount(account),
    "7" = {
      cat("Exiting The Program!\n")
      is_running <- FALSE
      account
    }
  )

  if (is_running && !back_to_main_menu()) {
    is_running <- FALSE
  }
}
