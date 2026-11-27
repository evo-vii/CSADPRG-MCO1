#   ********************
#   Last names: Bobadilla, Calvez, Casao, Sanico
#   Language: R
#   Paradigm(s): Multi-paradigm (Functional, Object-Oriented)
#   ********************

CSV_FILE <- "bank_transactions.csv"
STDIN_CON <- if (interactive()) NULL else file("stdin", open = "r")

# Displays the list of available transactions in the main menu
# @param none
# @return none
# @pre none
display_options <- function() {
  cat("Select Transaction:\n")
  cat("[1] Register Account\n")
  cat("[2] Deposit Amount\n")
  cat("[3] Withdraw Amount\n")
  cat("[4] Currency Exchange\n")
  cat("[5] Record Exchange Rates\n")
  cat("[6] Show Interest Amount\n")
  cat("[7] Generate Reports\n")
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

# Reads a single line from interactive console or persistent stdin connection
# @param prompt String text displayed to the user
# @return String line entered by the user
# @pre none
read_line_prompt <- function(prompt = "") {
  if (interactive()) {
    return(readline(prompt = prompt))
  }
  cat(prompt)
  flush.console()
  line <- readLines(con = STDIN_CON, n = 1, warn = FALSE)
  if (length(line) == 0) quit(save = "no", status = 0)
  return(line)
}

# Prompts user repeatedly until an integer within the specified range is entered
# @param min Minimum integer choice allowed
# @param max Maximum integer choice allowed
# @param prompt String text displayed to the user
# @return Integer valid choice between min and max
# @pre min <= max
get_valid_choice <- function(min = 1, max = 7, prompt = "Choice: ") {
  repeat {
    line <- read_line_prompt(prompt)
    choice <- suppressWarnings(as.integer(trimws(line)))
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
    line <- read_line_prompt(prompt)
    amount <- suppressWarnings(as.numeric(trimws(line)))
    if (!is.na(amount) && amount > 0) {
      return(amount)
    }
    cat("Invalid Input. Please enter a valid positive number!\n")
  }
}

# Prompts user repeatedly until 'y' or 'n' is entered
# @param prompt String question displayed to the user
# @return Logical TRUE if user enters 'y', FALSE if 'n'
# @pre none
ask_yes_no <- function(prompt) {
  repeat {
    ans <- tolower(trimws(read_line_prompt(prompt)))
    if (ans == "y") return(TRUE)
    if (ans == "n") return(FALSE)
    cat("Invalid Input! Please enter 'y' or 'n'.\n")
  }
}

# Constructor for creating a new bank_account S3 object
# @param name Initial account holder name
# @param account_number 10-digit account number (or NA)
# @param balance Initial balance amount
# @param currency Initial currency abbreviation
# @param exchange_rates Named numeric vector of currency rates (optional)
# @return S3 object of class 'bank_account'
# @pre balance >= 0
new_bank_account <- function(name = "Not Registered", account_number = NA,
                             balance = 0, currency = "PHP",
                             exchange_rates = NULL) {
  if (is.null(exchange_rates)) {
    exchange_rates <- c(
      "PHP" = 1.00, "USD" = 62.00, "JPY" = 0.40,
      "GBP" = 84.00, "EUR" = 72.00, "CNY" = 9.00
    )
  }
  account <- list(
    name = name,
    account_number = account_number,
    balance = balance,
    currency = currency,
    currencies = c("PHP", "USD", "JPY", "GBP", "EUR", "CNY"),
    exchange_rates = exchange_rates
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
deposit       <- function(account, ...) UseMethod("deposit")
withdraw      <- function(account, ...) UseMethod("withdraw")
set_rate      <- function(account, ...) UseMethod("set_rate")
convert_curr  <- function(account, ...) UseMethod("convert_curr")
calc_interest <- function(account, ...) UseMethod("calc_interest")

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

  for (d in seq_len(days)) {
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
load_transactions <- function(file_path = CSV_FILE) {
  if (!file.exists(file_path)) {
    return(list(success = FALSE, data = NULL, stats = NULL,
                error = sprintf("File not found: %s", file_path)))
  }

  raw_df <- read.csv(file_path, stringsAsFactors = FALSE, na.strings = c("", "NA"))
  total_rows <- nrow(raw_df)

  raw_df$date            <- as.Date(trimws(raw_df$date), format = "%d/%m/%Y")
  raw_df$amount          <- suppressWarnings(as.numeric(raw_df$amount))
  raw_df$current_balance <- suppressWarnings(as.numeric(raw_df$current_balance))
  raw_df$account_number  <- suppressWarnings(as.numeric(raw_df$account_number))
  raw_df$currency        <- toupper(trimws(raw_df$currency))
  raw_df$type            <- tolower(trimws(raw_df$type))

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

  return(list(
    success = TRUE,
    data = php_df,
    stats = list(total_rows = total_rows, invalid_rows = invalid_rows, php_rows = nrow(php_df)),
    error = NULL
  ))
}

# Generates the next 10-digit account number after the highest existing number
# @param df Cleaned transactions data frame (or NULL)
# @return Numeric 10-digit account number
# @pre none
get_next_account_number <- function(df) {
  if (is.null(df) || nrow(df) == 0) return(1000000001)
  return(max(df$account_number, na.rm = TRUE) + 1)
}

# Looks up an account by name or 10-digit number and returns its latest S3 object
# @param df Cleaned transactions data frame
# @param query String account name or account number to search
# @param exchange_rates Named numeric vector of current exchange rates
# @return S3 'bank_account' object with latest balance, or NULL if not found
# @pre query is a string
find_account <- function(df, query, exchange_rates) {
  if (is.null(df) || nrow(df) == 0) return(NULL)
  q_clean <- tolower(trimws(query))
  matches <- df[tolower(trimws(df$account_name)) == q_clean |
                sprintf("%.0f", df$account_number) == q_clean, ]
  if (nrow(matches) == 0) return(NULL)

  matches <- matches[order(matches$date, matches$time), ]
  latest <- matches[nrow(matches), ]

  return(new_bank_account(
    name = latest$account_name,
    account_number = latest$account_number,
    balance = latest$current_balance,
    currency = "PHP",
    exchange_rates = exchange_rates
  ))
}

# Appends a new transaction row to the CSV file and updates the in-memory data frame
# @param df Cleaned transactions data frame (or NULL)
# @param account S3 object of class 'bank_account'
# @param type String transaction type ('deposit' or 'withdrawal')
# @param amount Numeric transaction amount in PHP
# @param file_path String path to the CSV file
# @return Updated transactions data frame including the new row
# @pre amount > 0, type is 'deposit' or 'withdrawal'
save_transaction_to_csv <- function(df, account, type, amount, file_path = CSV_FILE) {
  now_date <- Sys.Date()
  now_time <- format(Sys.time(), "%H:%M:%S")
  date_str <- format(now_date, "%d/%m/%Y")

  csv_line <- sprintf(
    "%s,%.0f,%s,%s,%s,%.2f,%s,%.2f\n",
    account$name, account$account_number, date_str, now_time,
    account$currency, amount, type, account$balance
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
    current_balance = account$balance,
    stringsAsFactors = FALSE
  )
  if (is.null(df)) return(new_row)
  return(rbind(df, new_row))
}

# Generates Report 1: Banking Privilege Tiers based on last month's EOD balances (REQ-0006)
# @param df Cleaned transactions data frame (PHP only)
# @return Sorted data frame of qualifying accounts (AverageDailyBalance >= 100,000)
# @pre df contains valid date, time, account_number, account_name, current_balance columns
generate_report1 <- function(df) {
  ref_date <- max(df$date, na.rm = TRUE)
  month_start <- as.Date(format(ref_date, "%Y-%m-01"))
  month_end <- seq(month_start, by = "1 month", length.out = 2)[2] - 1
  days_in_month <- seq(month_start, month_end, by = "day")
  num_days <- length(days_in_month)

  acc_nums <- unique(df$account_number)
  rows <- list()

  for (acc_num in acc_nums) {
    acc_df <- df[df$account_number == acc_num, ]
    acc_df <- acc_df[order(acc_df$date, acc_df$time), ]

    acc_name <- acc_df$account_name[nrow(acc_df)]
    open_date <- min(acc_df$date)
    account_age <- as.integer(ref_date - open_date)
    current_balance <- acc_df$current_balance[nrow(acc_df)]

    eod_balances <- numeric(num_days)
    for (i in seq_len(num_days)) {
      d <- days_in_month[i]
      prior_tx <- acc_df[acc_df$date <= d, ]
      eod_balances[i] <- if (nrow(prior_tx) > 0) prior_tx$current_balance[nrow(prior_tx)] else 0.0
    }

    avg_daily_bal <- round(sum(eod_balances) / num_days, 2)
    if (avg_daily_bal >= 100000.00) {
      tier <- if (avg_daily_bal >= 1000000.00) "Platinum" else if (avg_daily_bal >= 500000.00) "Gold" else "Silver"
      rows[[length(rows) + 1]] <- data.frame(
        AccountName = acc_name,
        AccountAge = account_age,
        CurrentBalance = round(current_balance, 2),
        AverageDailyBalance = avg_daily_bal,
        PrivilegeTier = tier,
        stringsAsFactors = FALSE
      )
    }
  }

  if (length(rows) == 0) {
    return(data.frame(AccountName = character(0), AccountAge = integer(0),
                      CurrentBalance = numeric(0), AverageDailyBalance = numeric(0),
                      PrivilegeTier = character(0), stringsAsFactors = FALSE))
  }
  report_df <- do.call(rbind, rows)
  report_df <- report_df[order(-report_df$AverageDailyBalance), ]
  rownames(report_df) <- NULL
  return(report_df)
}

# Generates Report 2: 30-day Interest Expense Projection per client (REQ-0007)
# @param df Cleaned transactions data frame (PHP only)
# @return Sorted data frame of qualifying accounts (CurrentBalance >= 10,000)
# @pre calc_interest.bank_account is defined
generate_report2 <- function(df) {
  acc_nums <- unique(df$account_number)
  rows <- list()

  for (acc_num in acc_nums) {
    acc_df <- df[df$account_number == acc_num, ]
    acc_df <- acc_df[order(acc_df$date, acc_df$time), ]
    acc_name <- acc_df$account_name[nrow(acc_df)]
    current_balance <- acc_df$current_balance[nrow(acc_df)]

    if (current_balance >= 10000.00) {
      acc_obj <- new_bank_account(name = acc_name, account_number = acc_num, balance = current_balance)
      schedule <- calc_interest(acc_obj, days = 30, annual_rate = 0.05)
      expected_interest <- round(sum(schedule$Interest), 2)

      rows[[length(rows) + 1]] <- data.frame(
        AccountName = acc_name,
        CurrentBalance = round(current_balance, 2),
        ExpectedInterest = expected_interest,
        stringsAsFactors = FALSE
      )
    }
  }

  if (length(rows) == 0) {
    return(data.frame(AccountName = character(0), CurrentBalance = numeric(0),
                      ExpectedInterest = numeric(0), stringsAsFactors = FALSE))
  }
  report_df <- do.call(rbind, rows)
  report_df <- report_df[order(-report_df$ExpectedInterest), ]
  rownames(report_df) <- NULL
  return(report_df)
}

# Generates Report 3: Suspicious Client Behavior per account per day (REQ-0008)
# @param df Cleaned transactions data frame (PHP only)
# @return Sorted data frame (ascending by Date, descending by RiskFlagLevel)
# @pre df contains valid account_number, account_name, date, type, amount columns
generate_report3 <- function(df) {
  keys <- paste(df$account_number, df$date, sep = "|")
  groups <- split(df, keys)
  rows <- vector("list", length(groups))

  for (i in seq_along(groups)) {
    g <- groups[[i]]
    acc_name <- g$account_name[nrow(g)]
    tx_date  <- g$date[1]

    deposits    <- g[g$type == "deposit", ]
    withdrawals <- g[g$type == "withdrawal", ]

    num_deposits     <- nrow(deposits)
    total_deposit    <- round(sum(deposits$amount), 2)
    total_withdrawal <- round(sum(withdrawals$amount), 2)

    turnover_velocity <- if (total_deposit > 0) {
      round((total_withdrawal / total_deposit) * 100, 2)
    } else if (total_withdrawal > 0) {
      100.00
    } else {
      0.00
    }

    risk_flag <- if (total_withdrawal >= 500000.00 || turnover_velocity >= 90.00) "Suspicious" else "Normal"

    rows[[i]] <- data.frame(
      AccountName = acc_name,
      Date = tx_date,
      TotalWithdrawalAmounts = total_withdrawal,
      NumberOfDeposits = num_deposits,
      TotalDepositAmounts = total_deposit,
      TurnoverVelocity = turnover_velocity,
      RiskFlagLevel = risk_flag,
      stringsAsFactors = FALSE
    )
  }

  report_df <- do.call(rbind, rows)
  report_df <- report_df[order(report_df$Date, -xtfrm(report_df$RiskFlagLevel)), ]
  rownames(report_df) <- NULL
  return(report_df)
}

# Generates Report 4 (Custom): Peak Transaction Hours & Branch Traffic Analysis (REQ-0009)
# @param df Cleaned transactions data frame (PHP only)
# @return Data frame sorted descending by TotalTransactions
# @pre df contains valid time, type, amount columns
generate_report4 <- function(df) {
  hours <- suppressWarnings(as.integer(sub(":.*", "", trimws(df$time))))
  valid_idx <- !is.na(hours) & hours >= 0 & hours <= 23
  df_valid <- df[valid_idx, ]
  hours_valid <- hours[valid_idx]

  if (nrow(df_valid) == 0) {
    return(data.frame(
      HourSlot = character(0), TotalTransactions = integer(0),
      DepositCount = integer(0), WithdrawalCount = integer(0),
      TotalVolume = numeric(0), TrafficLevel = character(0),
      stringsAsFactors = FALSE
    ))
  }

  unique_hours <- sort(unique(hours_valid))
  rows <- vector("list", length(unique_hours))

  for (i in seq_along(unique_hours)) {
    h <- unique_hours[i]
    h_df <- df_valid[hours_valid == h, ]

    slot_label <- sprintf("%02d:00 - %02d:59", h, h)
    total_tx   <- nrow(h_df)
    dep_count  <- sum(h_df$type == "deposit")
    wd_count   <- sum(h_df$type == "withdrawal")
    total_vol  <- round(sum(h_df$amount), 2)

    rows[[i]] <- data.frame(
      HourSlot = slot_label,
      TotalTransactions = total_tx,
      DepositCount = dep_count,
      WithdrawalCount = wd_count,
      TotalVolume = total_vol,
      TrafficLevel = "",
      stringsAsFactors = FALSE
    )
  }

  report_df <- do.call(rbind, rows)

  q_low  <- quantile(report_df$TotalTransactions, 0.33, names = FALSE)
  q_high <- quantile(report_df$TotalTransactions, 0.67, names = FALSE)

  for (i in seq_len(nrow(report_df))) {
    cnt <- report_df$TotalTransactions[i]
    if (cnt >= q_high) {
      report_df$TrafficLevel[i] <- "Peak"
    } else if (cnt >= q_low) {
      report_df$TrafficLevel[i] <- "Moderate"
    } else {
      report_df$TrafficLevel[i] <- "Low"
    }
  }

  report_df <- report_df[order(-report_df$TotalTransactions), ]
  rownames(report_df) <- NULL
  return(report_df)
}

# Formats specified numeric columns with thousands commas and 2 decimal places (REQ-0012)
# @param df Data frame to format
# @param cols Character vector of column names to format
# @return Formatted data frame
# @pre cols exist in df
format_currency_cols <- function(df, cols) {
  if (nrow(df) == 0) return(df)
  for (col in cols) {
    df[[col]] <- formatC(df[[col]], format = "f", digits = 2, big.mark = ",")
  }
  return(df)
}

# Writes a formatted report data frame to a CSV file
# @param df Formatted data frame
# @param filename Output CSV file path
# @return none
# @pre filename is a non-empty string
export_report_csv <- function(df, filename) {
  write.csv(df, file = filename, row.names = FALSE)
}

# Computes aggregate stats across all accounts/reports and writes summary.json (REQ-0010)
# @param df Cleaned transactions data frame (PHP only)
# @param report2_df Unformatted Report 2 data frame (for total_interest_expense)
# @param filename Output JSON file path
# @return String containing the generated JSON content
# @pre df and report2_df are valid data frames
generate_summary_json <- function(df, report2_df, filename = "summary.json") {
  acc_nums <- unique(df$account_number)
  total_accounts <- length(acc_nums)
  total_deposits <- round(sum(df$amount[df$type == "deposit"]), 2)
  total_withdrawals <- round(sum(df$amount[df$type == "withdrawal"]), 2)

  latest_balances <- numeric(total_accounts)
  for (i in seq_along(acc_nums)) {
    acc_df <- df[df$account_number == acc_nums[i], ]
    acc_df <- acc_df[order(acc_df$date, acc_df$time), ]
    latest_balances[i] <- acc_df$current_balance[nrow(acc_df)]
  }
  total_balance <- round(sum(latest_balances), 2)
  total_interest_expense <- if (nrow(report2_df) > 0) round(sum(report2_df$ExpectedInterest), 2) else 0.00

  json_str <- sprintf(
    '{"total_accounts": %d, "total_deposits": %.2f, "total_withdrawals": %.2f, "total_balance": %.2f, "total_interest_expense": %.2f}',
    total_accounts, total_deposits, total_withdrawals, total_balance, total_interest_expense
  )
  cat(json_str, "\n", file = filename, sep = "")
  return(json_str)
}

# Prints a formatted 2-row pipe-separated console preview of a generated report
# @param title String report title
# @param df Formatted report data frame
# @param filename String exported CSV filename
# @return none
# @pre none
preview_report <- function(title, df, filename) {
  cat(sprintf("\n%s\n", title))
  if (nrow(df) == 0) {
    cat("(No matching records)\n")
  } else {
    top_df <- head(df, 2)
    col_names <- names(top_df)
    widths <- integer(length(col_names))

    for (j in seq_along(col_names)) {
      vals <- trimws(as.character(top_df[[j]]))
      widths[j] <- max(nchar(col_names[j]), nchar(vals))
    }

    header_cells <- mapply(function(name, w) sprintf("%-*s", w, name), col_names, widths)
    cat(paste(header_cells, collapse = " | "), "\n", sep = "")

    for (i in seq_len(nrow(top_df))) {
      row_vals <- trimws(as.character(unlist(top_df[i, ])))
      row_cells <- mapply(function(val, w) sprintf("%-*s", w, val), row_vals, widths)
      cat(paste(row_cells, collapse = " | "), "\n", sep = "")
    }
  }
  cat(sprintf("(Full table exported to %s)\n", filename))
}

# UI handler for registering new accounts with minimum Php 5,000.00 initial deposit
# @param transactions_df Cleaned transactions data frame
# @param file_path String path to the CSV file
# @return Updated transactions data frame
# @pre none
register_account <- function(transactions_df, file_path = CSV_FILE) {
  repeat {
    cat("Register Account\n")
    name <- trimws(read_line_prompt("Account Name: "))
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

# UI handler for depositing funds in any supported currency and saving to CSV
# @param transactions_df Cleaned transactions data frame
# @param rates_acc S3 'bank_account' object holding current exchange rates
# @param file_path String path to the CSV file
# @return Updated transactions data frame
# @pre none
deposit_amount <- function(transactions_df, rates_acc, file_path = CSV_FILE) {
  repeat {
    cat("Deposit Amount\n")
    query <- read_line_prompt("Account Name (or Number): ")
    account <- find_account(transactions_df, query, rates_acc$exchange_rates)

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

# UI handler for withdrawing funds with overdraft protection and saving to CSV
# @param transactions_df Cleaned transactions data frame
# @param rates_acc S3 'bank_account' object holding current exchange rates
# @param file_path String path to the CSV file
# @return Updated transactions data frame
# @pre none
withdraw_amount <- function(transactions_df, rates_acc, file_path = CSV_FILE) {
  repeat {
    cat("Withdraw Amount\n")
    query <- read_line_prompt("Account Name (or Number): ")
    account <- find_account(transactions_df, query, rates_acc$exchange_rates)

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
# @param rates_acc S3 'bank_account' object holding current exchange rates
# @return S3 'bank_account' object unchanged
# @pre none
currency_exchange <- function(rates_acc) {
  repeat {
    cat("Foreign Currency Exchange\n")
    display_currencies()

    source_choice <- get_valid_choice(1, length(rates_acc$currencies), prompt = "Source Currency: ")
    source_amount <- get_valid_amount(prompt = "Source Amount: ")

    cat("\nExchanged Currency\n")
    display_currencies()

    exchange_choice <- get_valid_choice(1, length(rates_acc$currencies), prompt = "Exchange Currency: ")
    exchange_currency <- rates_acc$currencies[exchange_choice]

    exchange_amount <- convert_curr(rates_acc, source_choice, exchange_choice, source_amount)
    cat(sprintf("Exchanged Amount: %.2f %s\n\n", exchange_amount, exchange_currency))

    if (!ask_yes_no("Convert another currency (Y/N)? ")) break
    cat("\n")
  }
  return(rates_acc)
}

# UI handler for updating foreign exchange rates (excluding base currency PHP)
# @param rates_acc S3 'bank_account' object holding current exchange rates
# @return Updated S3 'bank_account' object with new exchange rate
# @pre none
record_exchange_rates <- function(rates_acc) {
  cat("Record Exchange Rate\n\n")
  display_currencies()

  foreign_choice <- get_valid_choice(1, length(rates_acc$currencies), prompt = "Select Foreign Currency (1-6): ")
  while (foreign_choice == 1) {
    cat("PHP is the base currency and its rate cannot be changed!\n")
    foreign_choice <- get_valid_choice(1, length(rates_acc$currencies), prompt = "Select Foreign Currency (1-6): ")
  }
  selected_currency <- rates_acc$currencies[foreign_choice]
  new_rate <- get_valid_amount(prompt = "Exchange Rate: ")

  rates_acc <- set_rate(rates_acc, selected_currency, new_rate)
  cat(sprintf("Updated %s Exchange Rate: %.2f\n\n", selected_currency, new_rate))
  return(rates_acc)
}

# UI handler for calculating and rendering the expected daily interest schedule
# @param transactions_df Cleaned transactions data frame
# @param rates_acc S3 'bank_account' object holding current exchange rates
# @return none
# @pre none
show_interest_amount <- function(transactions_df, rates_acc) {
  cat("Show Interest Amount\n")
  query <- read_line_prompt("Account Name (or Number): ")
  account <- find_account(transactions_df, query, rates_acc$exchange_rates)

  if (is.null(account)) {
    cat("Account not found! Please register first (Option 1).\n")
    return(invisible(NULL))
  }

  cat(sprintf("Account Name: %s\n", account$name))
  cat(sprintf("Current Balance: %.2f\n", account$balance))
  cat(sprintf("Currency: %s\n", account$currency))
  cat("Interest Rate: 5%\n\n")

  repeat {
    raw_days <- read_line_prompt("Total Number of Days: ")
    pattern <- "^\\s*([0-9]+)\\s*(days?)?\\s*$"
    if (grepl(pattern, raw_days, ignore.case = TRUE)) {
      days <- suppressWarnings(as.integer(sub(pattern, "\\1", raw_days, ignore.case = TRUE)))
      if (!is.na(days) && days > 0) break
    }
    cat("Invalid Input. Please enter a valid positive number of days!\n")
  }

  schedule <- calc_interest(account, days)
  cat("\nDay | Interest | Balance |\n")
  for (i in seq_len(nrow(schedule))) {
    cat(sprintf("%-4d| %-9.2f| %-8.2f|\n", schedule$Day[i], schedule$Interest[i], schedule$Balance[i]))
  }
  cat("\n")
}

# UI handler for the Generate Reports submenu (Load file, Generate reports, or Back)
# @param transactions_df Cleaned transactions data frame (or NULL)
# @param is_loaded Logical indicating whether [1] Load the file has been run
# @param file_path String path to the CSV file
# @return List containing updated transactions data frame, is_loaded flag, and ask_continue flag
# @pre none
generate_reports_menu <- function(transactions_df, is_loaded = FALSE, file_path = CSV_FILE) {
  repeat {
    cat("Generate Reports\n")
    cat("[1] Load the file\n")
    cat("[2] Generate Reports\n")
    cat("[3] Back to Main Menu\n")
    sub_choice <- get_valid_choice(1, 3, prompt = "Enter choice: ")

    if (sub_choice == 1) {
      res <- load_transactions(file_path)
      if (!res$success) {
        cat(sprintf("Error: %s\n\n", res$error))
      } else {
        transactions_df <- res$data
        is_loaded <- TRUE
        cat(sprintf("Processing dataset... (%s rows loaded, %s filtered for PHP currency, %d invalid rows skipped)\n\n",
                    format(res$stats$total_rows, big.mark = ","),
                    format(res$stats$php_rows, big.mark = ","),
                    res$stats$invalid_rows))
      }
    } else if (sub_choice == 2) {
      if (!is_loaded || is.null(transactions_df) || nrow(transactions_df) == 0) {
        cat("Dataset not loaded or needs reload! Please select [1] Load the file first.\n\n")
        next
      }

      cat("Generating reports...\n")
      r1_raw <- generate_report1(transactions_df)
      r2_raw <- generate_report2(transactions_df)
      r3_raw <- generate_report3(transactions_df)
      r4_raw <- generate_report4(transactions_df)

      r1_out <- format_currency_cols(r1_raw, c("CurrentBalance", "AverageDailyBalance"))
      r2_out <- format_currency_cols(r2_raw, c("CurrentBalance", "ExpectedInterest"))

      r3_out <- r3_raw
      if (nrow(r3_out) > 0) {
        r3_out$Date <- format(r3_out$Date, "%d/%m/%Y")
        r3_out <- format_currency_cols(r3_out, c("TotalWithdrawalAmounts", "TotalDepositAmounts", "TurnoverVelocity"))
      }

      r4_out <- format_currency_cols(r4_raw, c("TotalVolume"))

      export_report_csv(r1_out, "report1_banking_privilege_tiers.csv")
      export_report_csv(r2_out, "report2_interest_expense_projection.csv")
      export_report_csv(r3_out, "report3_suspicious_client_behavior.csv")
      export_report_csv(r4_out, "report4_peak_transaction_hours.csv")
      json_str <- generate_summary_json(transactions_df, r2_raw, "summary.json")

      cat("Outputs saved to individual files...\n")
      preview_report("Report 1: Banking Privilege Tiers", r1_out, "report1_banking_privilege_tiers.csv")
      preview_report("Report 2: Interest Expense Projection", r2_out, "report2_interest_expense_projection.csv")
      preview_report("Report 3: Suspicious Client Behavior", r3_out, "report3_suspicious_client_behavior.csv")
      preview_report("Report 4: Peak Transaction Hours", r4_out, "report4_peak_transaction_hours.csv")

      cat("\nSummary State (summary.json):\n")
      cat(json_str, "\n\n")
      return(list(data = transactions_df, is_loaded = is_loaded, ask_continue = TRUE))
    } else {
      return(list(data = transactions_df, is_loaded = is_loaded, ask_continue = FALSE))
    }
  }
}

# MAIN
init_res <- load_transactions(CSV_FILE)
transactions_df <- if (init_res$success) init_res$data else NULL
reports_loaded <- FALSE
rates_acc <- new_bank_account()
is_running <- TRUE

while (is_running) {
  display_options()
  choice <- get_valid_choice(1, 7)
  cat("\n")

  prev_rows <- if (is.null(transactions_df)) 0 else nrow(transactions_df)
  ask_continue <- (choice != 4)
  switch(as.character(choice),
    "1" = { transactions_df <- register_account(transactions_df, CSV_FILE) },
    "2" = { transactions_df <- deposit_amount(transactions_df, rates_acc, CSV_FILE) },
    "3" = { transactions_df <- withdraw_amount(transactions_df, rates_acc, CSV_FILE) },
    "4" = { rates_acc <- currency_exchange(rates_acc) },
    "5" = { rates_acc <- record_exchange_rates(rates_acc) },
    "6" = { show_interest_amount(transactions_df, rates_acc) },
    "7" = {
      res <- generate_reports_menu(transactions_df, reports_loaded, CSV_FILE)
      transactions_df <- res$data
      reports_loaded <- res$is_loaded
      ask_continue <- res$ask_continue
    }
  )

  curr_rows <- if (is.null(transactions_df)) 0 else nrow(transactions_df)
  if (curr_rows != prev_rows) {
    reports_loaded <- FALSE
  }

  if (ask_continue && !ask_yes_no("Back to the Main Menu (Y/N): ")) {
    cat("Exiting The Program!\n")
    is_running <- FALSE
  }
  cat("\n")
}
