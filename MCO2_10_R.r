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
  cat("[6] Chinese Yuan Renminbi (CNY)\n\n")
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

# Prompts user repeatedly for a yes/no confirmation
# @param prompt String question displayed to the user
# @return Logical TRUE if user enters 'y', FALSE if 'n'
# @pre none
ask_yes_no <- function(prompt) {
  repeat {
    ans <- tolower(trimws(readline(prompt = prompt)))
    if (ans == "y") return(TRUE)
    if (ans == "n") return(FALSE)
    cat("Invalid Input! Please enter 'y' or 'n'.\n")
  }
}

# Prompts user to confirm whether to return to the main menu or exit
# @param none
# @return Logical TRUE if user enters 'y', FALSE if 'n'
# @pre none
back_to_main_menu <- function() {
  repeat {
    choice <- tolower(trimws(readline(prompt = "Go back to main menu [y/n]: ")))
    if (choice == "y") {
      return(TRUE)
    } else if (choice == "n") {
      cat("Exiting The Program!\n")
      return(FALSE)
    }
    cat("Invalid Input! Please Enter 'y' or 'n'.\n")
  }
}

# Constructor for creating a new bank_account S3 object
# @param name Initial account holder name
# @param account_number Numeric 10-digit account number (NA if unassigned)
# @param balance Initial balance amount
# @param currency Initial currency abbreviation
# @return S3 object of class 'bank_account'
# @pre balance >= 0
new_bank_account <- function(name = "Not Registered", account_number = NA,
                             balance = 0, currency = "PHP") {
  account <- list(
    name = name,
    account_number = account_number,
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
  if (!is.na(x$account_number)) {
    cat(sprintf("Account Number: %.0f\n", x$account_number))
  }
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

# Reads, validates, cleans, and filters bank_transactions.csv for PHP rows
# @param file_path String path to the CSV file
# @return List containing success status, cleaned data frame, ingestion stats, and error
# @pre file_path is a non-empty string
load_transactions <- function(file_path = "bank_transactions.csv") {
  if (!file.exists(file_path)) {
    return(list(
      success = FALSE,
      data = NULL,
      stats = NULL,
      error = sprintf("File not found: %s", file_path)
    ))
  }

  raw_df <- read.csv(file_path, na.strings = c("", "NA"), strip.white = TRUE)
  total_rows <- nrow(raw_df)

  raw_df$date            <- as.Date(raw_df$date, format = "%d/%m/%Y")
  raw_df$amount          <- suppressWarnings(as.numeric(raw_df$amount))
  raw_df$current_balance <- suppressWarnings(as.numeric(raw_df$current_balance))
  raw_df$account_number  <- suppressWarnings(as.numeric(raw_df$account_number))
  raw_df$currency        <- toupper(raw_df$currency)
  raw_df$type            <- tolower(raw_df$type)

  is_valid_row <- !is.na(raw_df$account_name) &
                  !is.na(raw_df$account_number) &
                  !is.na(raw_df$date) &
                  !is.na(raw_df$time) &
                  !is.na(raw_df$currency) &
                  !is.na(raw_df$amount) & raw_df$amount > 0 &
                  !is.na(raw_df$type) & raw_df$type %in% c("deposit", "withdrawal") &
                  !is.na(raw_df$current_balance)

  invalid_rows <- sum(!is_valid_row)
  clean_df <- raw_df[is_valid_row, ]

  php_df <- clean_df[clean_df$currency == "PHP", ]
  php_rows <- nrow(php_df)

  return(list(
    success = TRUE,
    data = php_df,
    stats = list(
      total_rows = total_rows,
      invalid_rows = invalid_rows,
      php_rows = php_rows
    ),
    error = NULL
  ))
}

# Generates the next sequential 10-digit account number
# @param df Data frame of existing bank transactions
# @return Numeric next available 10-digit account number
# @pre none
get_next_account_number <- function(df) {
  if (is.null(df) || nrow(df) == 0) {
    return(1000000001)
  }
  return(max(df$account_number, na.rm = TRUE) + 1)
}

# Looks up an account by number or name and returns its latest S3 bank_account
# @param df Data frame of cleaned bank transactions
# @param query String account name or 10-digit account number entered by the user
# @return S3 'bank_account' object with latest balance, or NULL if not found
# @pre none
find_account <- function(df, query) {
  if (is.null(df) || nrow(df) == 0) return(NULL)

  q_clean <- tolower(trimws(query))
  num_query <- suppressWarnings(as.numeric(q_clean))

  if (!is.na(num_query)) {
    matches <- df[df$account_number == num_query, ]
  } else {
    matches <- df[tolower(trimws(df$account_name)) == q_clean, ]
  }

  if (nrow(matches) == 0) return(NULL)

  matches <- matches[order(matches$date, matches$time), ]
  latest <- matches[nrow(matches), ]

  return(new_bank_account(
    name = latest$account_name,
    account_number = latest$account_number,
    balance = latest$current_balance,
    currency = "PHP"
  ))
}

# Appends a new transaction row to the CSV file and updates the in-memory data frame
# @param df Data frame of current bank transactions
# @param account S3 'bank_account' object containing updated balance
# @param type String transaction type ('deposit' or 'withdrawal')
# @param amount Numeric positive transaction amount in PHP
# @param file_path String path to the CSV file
# @return Updated data frame containing the newly appended row
# @pre amount > 0, type %in% c('deposit', 'withdrawal')
save_transaction_to_csv <- function(df, account, type, amount, file_path = "bank_transactions.csv") {
  now_date <- Sys.Date()
  now_time <- format(Sys.time(), "%H:%M:%S")
  date_str <- format(now_date, "%d/%m/%Y")

  csv_line <- sprintf(
    "%s,%.0f,%s,%s,%s,%.2f,%s,%.2f\n",
    account$name,
    account$account_number,
    date_str,
    now_time,
    account$currency,
    amount,
    type,
    account$balance
  )

  cat(csv_line, file = file_path, append = TRUE)

  new_row <- data.frame(
    account_name = account$name,
    account_number = account$account_number,
    date = now_date,
    time = now_time,
    currency = account$currency,
    amount = amount,
    type = type,
    current_balance = account$balance
  )
  return(rbind(df, new_row))
}

# UI handler for registering a new bank account with a minimum Php 5,000.00 deposit
# @param transactions_df Data frame of loaded bank transactions
# @param file_path String path to the CSV file
# @return Updated transactions data frame including newly registered account(s)
# @pre none
register_account <- function(transactions_df, file_path = "bank_transactions.csv") {
  repeat {
    cat("Register Account\n")
    name <- trimws(readline(prompt = "Account Name: "))

    if (nchar(name) == 0) {
      cat("Account name cannot be empty!\n")
      next
    }

    repeat {
      init_dep <- get_valid_amount(prompt = "Initial Deposit: ")
      if (init_dep >= 5000.00) break
      cat("Minimum initial deposit is Php 5,000.00!\n")
    }

    new_acc_num <- get_next_account_number(transactions_df)
    new_acc <- new_bank_account(name = name, account_number = new_acc_num, balance = init_dep)

    transactions_df <- save_transaction_to_csv(transactions_df, new_acc, "deposit", init_dep, file_path)

    cat(sprintf("Account created successfully! (Account #: %.0f)\n", new_acc_num))
    if (!ask_yes_no("Register another account? (Y/N): ")) break
    cat("\n")
  }
  return(transactions_df)
}

# UI handler for depositing funds into an existing account and persisting to CSV
# @param transactions_df Data frame of loaded bank transactions
# @param file_path String path to the CSV file
# @return Updated transactions data frame including new deposit row(s)
# @pre none
deposit_amount <- function(transactions_df, file_path = "bank_transactions.csv") {
  repeat {
    cat("Deposit Amount\n")
    query <- readline(prompt = "Account Name (or Number): ")
    account <- find_account(transactions_df, query)

    if (is.null(account)) {
      cat("Account not found! Please register first (Option 1).\n")
    } else {
      print(account)
      display_currencies()
      dep_choice <- get_valid_choice(1, length(account$currencies), prompt = "Deposit Currency: ")
      dep_val <- get_valid_amount(prompt = "Deposit Amount: ")

      php_val <- convert_curr(account, dep_choice, 1, dep_val)
      account <- deposit(account, php_val)

      transactions_df <- save_transaction_to_csv(transactions_df, account, "deposit", php_val, file_path)
      cat(sprintf("Updated Balance: %.2f\n", account$balance))
    }

    if (!ask_yes_no("Deposit another amount? (Y/N): ")) break
    cat("\n")
  }
  return(transactions_df)
}

# UI handler for withdrawing funds with overdraft protection and CSV persistence
# @param transactions_df Data frame of loaded bank transactions
# @param file_path String path to the CSV file
# @return Updated transactions data frame including new withdrawal row(s)
# @pre none
withdraw_amount <- function(transactions_df, file_path = "bank_transactions.csv") {
  repeat {
    cat("Withdraw Amount\n")
    query <- readline(prompt = "Account Name (or Number): ")
    account <- find_account(transactions_df, query)

    if (is.null(account)) {
      cat("Account not found! Please register first (Option 1).\n")
    } else {
      print(account)
      display_currencies()
      wd_choice <- get_valid_choice(1, length(account$currencies), prompt = "Withdraw Currency: ")
      wd_val <- get_valid_amount(prompt = "Withdraw Amount: ")

      php_val <- convert_curr(account, wd_choice, 1, wd_val)
      res <- withdraw(account, php_val)

      if (!res$success) {
        cat(sprintf("Transaction Failed: %s!\n", res$error))
        cat(sprintf("Current Balance remains: %.2f\n", account$balance))
      } else {
        account <- res$account
        transactions_df <- save_transaction_to_csv(transactions_df, account, "withdrawal", php_val, file_path)
        cat(sprintf("Updated Balance: %.2f\n", account$balance))
      }
    }

    if (!ask_yes_no("Withdraw another amount? (Y/N): ")) break
    cat("\n")
  }
  return(transactions_df)
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

    if (!ask_yes_no("Convert another currency (Y/N)? ")) {
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
# @param transactions_df Data frame of loaded bank transactions
# @return Transactions data frame unchanged
# @pre none
show_interest_amount <- function(transactions_df) {
  cat("Show Interest Amount\n")
  query <- readline(prompt = "Account Name (or Number): ")
  account <- find_account(transactions_df, query)

  if (is.null(account)) {
    cat("Account not found! Please register first (Option 1).\n")
    return(transactions_df)
  }

  print(account)
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
  return(transactions_df)
}

# MAIN
csv_path <- "bank_transactions.csv"
load_res <- load_transactions(csv_path)

if (load_res$success) {
  transactions_df <- load_res$data
  cat(sprintf(
    "Processing dataset... (%s rows loaded, %s filtered for PHP currency, %d invalid rows skipped)\n\n",
    format(load_res$stats$total_rows, big.mark = ","),
    format(load_res$stats$php_rows, big.mark = ","),
    load_res$stats$invalid_rows
  ))
} else {
  cat(sprintf("Warning: %s\n\n", load_res$error))
  transactions_df <- NULL
}

account <- new_bank_account()
is_running <- TRUE

while (is_running) {
  display_options()
  choice <- get_valid_choice(1, 6)
  cat("\n***\n")

  switch(as.character(choice),
    "1" = { transactions_df <- register_account(transactions_df, csv_path) },
    "2" = { transactions_df <- deposit_amount(transactions_df, csv_path) },
    "3" = { transactions_df <- withdraw_amount(transactions_df, csv_path) },
    "4" = { account <- currency_exchange(account) },
    "5" = { account <- record_exchange_rates(account) },
    "6" = { transactions_df <- show_interest_amount(transactions_df) }
  )

  if (choice != 4 && !back_to_main_menu()) {
    is_running <- FALSE
  }
  cat("\n***\n")
}