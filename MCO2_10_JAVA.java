/******************************************

Lastnames: Bobadilla, Calvez, Casao, Sanico
Language: Java
Paradigm: Object Oriented Programming

*******************************************/

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class MCO2_10_JAVA extends JFrame {

    // main color used for the panel, titles and text
    Color darkGreen = new Color(0, 102, 51);
    // color used for the borders
    Color borderGreen = new Color(0, 70, 35);
    // color used for the background on the sides
    Color grayBg = new Color(200, 196, 195);
    // color used for the title bar and the pop up
    Color lightGray = new Color(216, 216, 216);
    // color used for the error screen
    Color errorGreen = new Color(7, 74, 40);

    // font sizes
    Font titleFont = new Font("SansSerif", Font.BOLD, 20);
    Font subtitleFont = new Font("SansSerif", Font.BOLD, 16);
    Font buttonFont = new Font("SansSerif", Font.BOLD, 12);

    // the admin login details
    static final String ADMIN_NAME = "admin";
    static final String ADMIN_PIN = "0000";
    // how many PIN tries the admin gets
    static final int MAX_PIN_ATTEMPTS = 3;

    // the bank that stores the accounts, currencies and transactions
    Bank bank;

    // the reports and the summary
    ReportGenerator[] reports = {
        new Report1BankingPrivilegeTiers(),
        new Report2InterestExpenseProjection(),
        new Report3SuspiciousClientBehavior(),
        new Report4DormantAccounts(),
        new SummaryJsonWriter()
    };

    // the customer that is logged in (null if nobody is logged in)
    SavingsAccount currentAccount;
    // true while the admin is logged in
    boolean adminMode;
    // the account that receives money
    SavingsAccount recipient;
    // the wallets chosen by the user (starting from 0)
    int walletIndex;
    int sourceIndex;
    int targetIndex;
    // the amounts waiting for the user to confirm
    double pendingAmount;
    double pendingResult;
    // the amount the last amount check accepted
    double parsedAmount;
    // the account that is typing its PIN at the login screen
    SavingsAccount loginAccount;
    // the account shown on the view/update profile screen 
    SavingsAccount profileAccount;
    // temporary name, PIN and contact number used while registering
    String tempName;
    String tempPin;
    String tempContact;
    int pinTries;

    // checks the text and returns an error message (null if it is okay)
    interface InputChecker {
        String check(String text);
    }

    // runs when the user picks a currency 
    interface CurrencyPicked {
        void picked(int index);
    }

    // sets up the window and shows the login menu
    public MCO2_10_JAVA(String csvFile) {
        setTitle("Banking and Currency Machine");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // the window is 720 wide and 480 tall
        getContentPane().setPreferredSize(new Dimension(720, 480));
        setResizable(false);

        // pressing esc will return to the menu
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "backToMenu");
        getRootPane().getActionMap().put("backToMenu", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                goHome();
            }
        });

        // loads the accounts, wallet balances and rates from bank_transactions.csv 
        bank = new Bank(csvFile);
        bank.load();

        // shows the login menu first, then fits the window and shows it in the center of the screen
        showLoginMenu();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // warns the user if a data file could not be read
        if (!bank.isDataOk()) {
            showError("WARNING", "A data file could not be read. Changes will NOT be saved.", false);
        }
    }

    // makes an empty screen
    JPanel makeScreen(String title, boolean dim) {
        final String screenTitle = title;
        final boolean isDim = dim;

        // the panel that draws the screen background
        JPanel p = new JPanel(null) {
            // draws the screen background and the title
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // color of the background for the whole screen
                g2.setColor(grayBg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // color of the title bar in the middle
                g2.setColor(lightGray);
                g2.fillRect(130, 0, 460, getHeight());

                // color of the error screen
                if (isDim) {
                    g2.setColor(errorGreen);
                } else {
                    g2.setColor(darkGreen);
                }
                g2.fillRoundRect(130, 60, 460, getHeight(), 60, 60);

                if (isDim) {
                    // color and size of the box behind the error pop up
                    g2.setColor(new Color(30, 90, 60));
                    g2.fillRect(150, 110, 420, 290);
                }

                // color of the title text
                g2.setColor(darkGreen);
                g2.setFont(titleFont);
                FontMetrics fm = g2.getFontMetrics();
                int x = 130 + (460 - fm.stringWidth(screenTitle)) / 2;
                g2.drawString(screenTitle, x, 38);
            }
        };
        // sets every screen to 720 wide and 480 tall
        p.setPreferredSize(new Dimension(720, 480));
        return p;
    }

    // replaces what is on the window with a new screen
    void showScreen(JPanel p) {
        getContentPane().removeAll();
        getContentPane().add(p);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    // adds a subtitle at the default height (72)
    void addSubtitle(JPanel p, String text) {
        addSubtitle(p, text, 72);
    }

    // adds a subtitle at height y
    void addSubtitle(JPanel p, String text, int y) {
        JLabel sub = new JLabel(text, SwingConstants.CENTER);
        // color of the text for the subtitle
        sub.setForeground(Color.WHITE);
        sub.setFont(subtitleFont);
        // sets the size of the subtitle to 460 wide and 30 tall
        sub.setBounds(130, y, 460, 30);
        p.add(sub);
    }

    // a button clicked by the user
    void addMenuButton(JPanel p, String text, boolean onLeft, int x, int y, int w, int h, Runnable action) {
        // the white text box of the button
        JLabel label = new JLabel("<html><center>" + text.replace("\n", "<br>") + "</center></html>",
                SwingConstants.CENTER);
        label.setOpaque(true);
        // color of the text and border
        label.setBackground(Color.WHITE);
        label.setForeground(darkGreen);
        label.setFont(buttonFont);
        label.setBorder(new LineBorder(borderGreen, 2));
        // size of the label
        label.setBounds(x, y, w, h);
        p.add(label);

        // the arrow button (points where the label is )
        ArrowButton arrow = new ArrowButton(onLeft);
        if (onLeft) {
            // size of arrow button on the left side
            arrow.setBounds(45, y + (h - 40) / 2, 55, 40);
        } else {
            // size of the arrow button on the right side
            arrow.setBounds(620, y + (h - 40) / 2, 55, 40);
        }
        // runs the action when the arrow is clicked
        arrow.addActionListener(e -> action.run());
        p.add(arrow);
    }

    // button that goes back to the menu of the user (login menu, customer menu or admin menu)
    void addBackButton(JPanel p) {
        addMenuButton(p, "BACK TO MENU", false, 405, 415, 165, 45, () -> goHome());
    }

    // makes a white box for (input screen, details screen and table screen)
    JPanel makeWhiteBox(int x, int y, int w, int h) {
        JPanel box = new JPanel(null) {
            // draws the gradient at the bottom of the white box
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // color of the box
                g2.setPaint(new GradientPaint(0, getHeight() - 70, new Color(255, 255, 255, 0),
                        0, getHeight(), new Color(215, 225, 220)));
                g2.fillRect(0, getHeight() - 70, getWidth(), 70);
            }
        };

        box.setBackground(Color.WHITE);
        box.setBorder(new LineBorder(borderGreen, 2));
        // size of the white box
        box.setBounds(x, y, w, h);
        return box;
    }

    // changes a number to text with 2 decimal places
    String money(double value) {
        return Bank.money(value);
    }

    // changes a currency to text
    String currencyText(Currency c) {
        return c.getName() + " (" + c.getCode() + ")";
    }

    // the currency with this number (starting from 0)
    Currency currencyAt(int index) {
        return bank.getCurrencies()[index];
    }

    // text of a wallet like "1000.00 USD"
    String walletText(SavingsAccount a, int index) {
        return money(a.getWallet(index)) + " " + currencyAt(index).getCode();
    }

    // goes back to the menu of the user (login menu, customer menu or admin menu)
    void goHome() {
        if (adminMode) {
            showAdminMenu();
        } else if (currentAccount != null) {
            showCustomerMenu();
        } else {
            showLoginMenu();
        }
    }

    // shows the inputs screen
    void showInput(String screenTitle, String prompt, int type, int maxLength, boolean hidden,
                   InputChecker checker) {
        JPanel p = makeScreen(screenTitle, false);
        addSubtitle(p, prompt, 95);

        // size of the white box that holds the text field and the keys (420 x 260)
        JPanel box = makeWhiteBox(148, 135, 420, 260);

        // where the inputted text is displayed
        JTextField field = hidden ? new JPasswordField() : new JTextField();

        // size: the text field is 320 wide and 30 tall
        field.setBounds(50, 26, 320, 30);
        field.setFont(new Font("SansSerif", Font.BOLD, 16));
        // color of the text and border
        field.setForeground(darkGreen);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(new LineBorder(darkGreen, 2));
        // the text field cannot be typed in, the text only comes from the on-screen keys
        field.setEditable(false);
        field.setFocusable(false);
        field.setBackground(Color.WHITE);
        box.add(field);

        // message under the text field (shows the errors)
        JLabel hint = new JLabel("", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.BOLD, 12));
        // color of the error message
        hint.setForeground(Color.RED);
        // size of the message
        hint.setBounds(0, 57, 420, 16);
        box.add(hint);

        // runs when a key on the screen is clicked 
        Consumer<String> press = key -> {
            // the text that is already in the text field
            String current = field.getText();
            if (key.equals("BACK")) {
                // deletes the last character
                if (current.length() > 0) {
                    field.setText(current.substring(0, current.length() - 1));
                }
            } else if (current.length() < maxLength) {
                // only one dot is allowed and only for amounts and names
                if (key.equals(".") && (type == 2 || current.contains(".") && type == 3)) {
                    return;
                }
                field.setText(current + key);
            }
            // clears the error message when the user keeps typing
            hint.setText("");
        };

        if (type == 1) {
            KeyBoardDisplay keyboard = new KeyBoardDisplay(press);
            // size of the keyboard display
            keyboard.setBounds(50, 78, 320, 150);
            box.add(keyboard);
        } else {
            KeyPadDisplay keypad = new KeyPadDisplay(type == 3, press);
            // size of the number pad display
            keypad.setBounds(144, 77, 132, 173);
            box.add(keypad);
        }

        // puts the white box on the screen
        p.add(box);

        // runs when CONTINUE is clicked and checks the text and shows the error message
        Runnable submit = () -> {
            String message = checker.check(field.getText().trim());
            if (message != null) {
                hint.setText(message);
                if (hidden) {
                    field.setText("");
                }
            }
        };

        // creates a BACK TO MENU button on the left side and CONTINUE button on the right side
        addMenuButton(p, "BACK TO MENU", true, 142, 405, 160, 45, () -> goHome());
        addMenuButton(p, "CONTINUE", false, 430, 405, 145, 45, submit);

        showScreen(p);
    }

    // shows 6 buttons, one for each currency 
    void showCurrencyChoice(String screenTitle, String subtitle, CurrencyPicked handler,
                            IntFunction<String> extra) {
        JPanel p = makeScreen(screenTitle, false);
        addSubtitle(p, subtitle);

        Currency[] list = bank.getCurrencies();
        for (int i = 0; i < list.length; i++) {
            final int index = i;
            String text = list[i].getName().toUpperCase() + "\n(" + list[i].getCode() + ")";
            if (extra != null) {
                text = text + "\n" + extra.apply(i);
            }
            // the first 4 currencies go on the left side, the rest on the right side
            if (i < 4) {
                addMenuButton(p, text, true, 150, 125 + i * 85, 190, 55, () -> handler.picked(index));
            } else {
                addMenuButton(p, text, false, 380, 125 + (i - 4) * 85, 190, 55, () -> handler.picked(index));
            }
        }

        showScreen(p);
    }

    // currency screen that shows the balance of each wallet of the customer
    void showWalletChoice(String screenTitle, String subtitle, CurrencyPicked handler) {
        showCurrencyChoice(screenTitle, subtitle, handler, i -> "BAL: " + walletText(currentAccount, i));
    }

    // makes the details screen: shows titles with their info in a box (the buttons are added by the caller)
    JPanel makeDetails(String screenTitle, String subtitle, String[] titles, String[] infos) {
        JPanel p = makeScreen(screenTitle, false);
        addSubtitle(p, subtitle);

        // size of white box for the details
        JPanel box = makeWhiteBox(150, 110, 420, 290);

        String text = "";
        // adds each title and its info to the text
        for (int i = 0; i < titles.length; i++) {
            text = text + titles[i] + "\n";
            if (!infos[i].equals("")) {
                text = text + "    " + infos[i].replace("\n", "\n    ") + "\n";
            }
            text = text + "\n";
        }

        // the text area that shows the details
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        // long lines go to the next line instead of needing a side scroll bar
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        // color of the text
        area.setForeground(darkGreen);
        area.setMargin(new Insets(10, 15, 10, 10));
        area.setCaretPosition(0);

        // adds a scroll bar when the text is long
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(new LineBorder(darkGreen, 2));
        // size of the scroll area inside the white box
        scroll.setBounds(25, 20, 370, 250);
        box.add(scroll);

        p.add(box);
        return p;
    }

    // details screen with a BACK TO MENU button
    void showDetails(String screenTitle, String subtitle, String[] titles, String[] infos) {
        JPanel p = makeDetails(screenTitle, subtitle, titles, infos);
        addBackButton(p);
        showScreen(p);
    }

    // details screen with an extra button on the left side and a BACK TO MENU button
    void showDetails(String screenTitle, String subtitle, String[] titles, String[] infos,
                     String extraText, Runnable extraAction) {
        JPanel p = makeDetails(screenTitle, subtitle, titles, infos);
        addMenuButton(p, extraText, true, 150, 415, 165, 45, extraAction);
        addBackButton(p);
        showScreen(p);
    }

    // details screen that asks the user to CONFIRM or CANCEL
    void showConfirm(String screenTitle, String subtitle, String[] titles, String[] infos, Runnable onConfirm) {
        JPanel p = makeDetails(screenTitle, subtitle, titles, infos);
        addMenuButton(p, "CANCEL", true, 142, 405, 160, 45, () -> goHome());
        addMenuButton(p, "CONFIRM", false, 430, 405, 145, 45, onConfirm);
        showScreen(p);
    }

    // shows rows in a table 
    void showTable(String screenTitle, String[] columns, ArrayList<Object[]> rows, int[] widths, String note) {
        JPanel p = makeScreen(screenTitle, false);

        JPanel box = makeWhiteBox(140, 90, 440, 310);

        // each data in the table
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            // the cells of the table cannot be edited
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // adds each row to the table
        for (Object[] row : rows) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setRowHeight(24);

        table.setGridColor(Color.BLACK);
        table.setForeground(darkGreen);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);

        JTableHeader header = table.getTableHeader();

        header.setBackground(darkGreen);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // when the columns are wider than the box, the table gets a side scroll bar instead of squeezing them
        int totalWidth = 0;
        for (int w : widths) {
            totalWidth = totalWidth + w;
        }
        if (totalWidth > 410) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }

        // the table is shorter when there is a note under it
        int tableHeight = 270;
        if (note != null) {
            tableHeight = 230;
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(darkGreen, 2));
        scroll.getViewport().setBackground(Color.WHITE);
        // shows the column titles above the table
        scroll.setColumnHeaderView(table.getTableHeader());

        scroll.setBounds(15, 20, 410, tableHeight);
        box.add(scroll);

        if (note != null) {
            JTextArea noteLabel = new JTextArea(note);
            noteLabel.setLineWrap(true);
            noteLabel.setWrapStyleWord(true);
            noteLabel.setEditable(false);
            noteLabel.setFocusable(false);
            noteLabel.setOpaque(false);
            noteLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            noteLabel.setForeground(darkGreen);
            noteLabel.setBounds(15, 20 + tableHeight + 6, 410, 40);
            box.add(noteLabel);
        }

        p.add(box);
        addBackButton(p);
        showScreen(p);
    }

    // shows the error message with a BACK TO MENU button
    void showError(String screenTitle, String message, boolean offerRegister) {
        JPanel p = makeScreen(screenTitle, true);

        JPanel popup = new JPanel(null);
        // color of the background for the pop up
        popup.setBackground(lightGray);
        // size of the pop up
        popup.setBounds(240, 125, 240, 220);

        // the heading is WARNING for the warning screen and ERROR for the others
        String headingText = "ERROR";
        if (screenTitle.equals("WARNING")) {
            headingText = "WARNING";
        }
        JLabel heading = new JLabel(headingText, SwingConstants.CENTER);
        heading.setFont(titleFont);
        // color of the heading
        heading.setForeground(darkGreen);
        heading.setBounds(0, 20, 240, 30);
        popup.add(heading);

        //  the error message
        JTextArea text = new JTextArea(message);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setFocusable(false);
        text.setOpaque(false);
        text.setFont(new Font("SansSerif", Font.BOLD, 14));
        text.setForeground(darkGreen);
        text.setBounds(30, 65, 180, 130);
        popup.add(text);

        p.add(popup);

        // REGISTER ACCOUNT button only shows when the account is not found
        if (offerRegister) {
            addMenuButton(p, "REGISTER ACCOUNT", true, 150, 415, 165, 45, () -> startRegister());
        }

        addBackButton(p);
        showScreen(p);
    }

    // shows the 4 buttons for the login menu
    void showLoginMenu() {
        JPanel p = makeScreen("BANKING AND CURRENCY MACHINE", false);

        // 2 buttons on the left side, each 165 wide and 55 tall
        addMenuButton(p, "LOGIN", true, 150, 130, 165, 55, () -> startLogin());
        addMenuButton(p, "REGISTER\nACCOUNT", true, 150, 215, 165, 55, () -> startRegister());

        // 2 buttons on the right side, each 165 wide and 55 tall
        addMenuButton(p, "ADMIN\nLOGIN", false, 405, 130, 165, 55, () -> startAdminLogin());
        addMenuButton(p, "EXIT", false, 405, 215, 165, 55, () -> System.exit(0));

        showScreen(p);
    }

    // shows the 10 buttons for customer menu
    void showCustomerMenu() {
        JPanel p = makeScreen("BANKING AND CURRENCY MACHINE", false);
        addSubtitle(p, "WELCOME, " + currentAccount.getName());

        // 5 buttons on the left side, each 165 wide and 52 tall
        addMenuButton(p, "DEPOSIT\nAMOUNT", true, 150, 108, 165, 52, () -> startDeposit());
        addMenuButton(p, "WITHDRAW\nAMOUNT", true, 150, 176, 165, 52, () -> startWithdraw());
        addMenuButton(p, "CHECK\nBALANCE", true, 150, 244, 165, 52, () -> showBalance());
        addMenuButton(p, "CHECK CURRENCY\nEXCHANGE", true, 150, 312, 165, 52, () -> showRates());
        addMenuButton(p, "EXCHANGE\nCURRENCY", true, 150, 380, 165, 52, () -> startExchange());

        // 5 buttons on the right side, each 165 wide and 52 tall
        addMenuButton(p, "SHOW INTEREST\nAMOUNT", false, 405, 108, 165, 52, () -> startInterest());
        addMenuButton(p, "SEND\nMONEY", false, 405, 176, 165, 52, () -> startSend());
        addMenuButton(p, "CLOSE\nACCOUNT", false, 405, 244, 165, 52, () -> startClose());
        addMenuButton(p, "TRANSACTION\nHISTORY", false, 405, 312, 165, 52, () -> showHistory());
        addMenuButton(p, "LOGOUT", false, 405, 380, 165, 52, () -> logout());

        showScreen(p);
    }

    // shows the 6 buttons for admin menu
    void showAdminMenu() {
        JPanel p = makeScreen("BANKING AND CURRENCY MACHINE", false);
        addSubtitle(p, "WELCOME, ADMIN");

        // 3 buttons on the left side
        addMenuButton(p, "RECORD\nEXCHANGE RATES", true, 150, 130, 165, 55, () -> startRecordRate());
        addMenuButton(p, "VIEW/UPDATE\nPROFILE", true, 150, 215, 165, 55, () -> startProfile());
        addMenuButton(p, "VIEW TRANSACTION\nHISTORY", true, 150, 300, 165, 55, () -> showAllTransactions());

        // 3 buttons on the right side
        addMenuButton(p, "CHANGE BASE\nCURRENCY", false, 405, 130, 165, 55, () -> startSetBase());
        addMenuButton(p, "GENERATE\nREPORTS", false, 405, 215, 165, 55, () -> showReportMenu());
        addMenuButton(p, "LOGOUT", false, 405, 300, 165, 55, () -> logout());

        showScreen(p);
    }

    // logs out the customer or the admin and shows the login menu
    void logout() {
        currentAccount = null;
        adminMode = false;
        showLoginMenu();
    }


    // finds an account using the 10-digit account number, null if not found
    SavingsAccount findByAccountText(String text) {
        if (!text.matches("\\d{10}")) {
            return null;
        }
        return bank.findByNumber(Long.parseLong(text));
    }

    // asks for the 10-digit account number and finds the account
    void startLogin() {
        showInput("LOGIN", "ENTER ACCOUNT NUMBER", 2, 10, false, text -> {
            if (text.length() < 10) {
                return "Enter the full 10-digit account number.";
            }
            // the account with that number (null if there is none)
            SavingsAccount found = findByAccountText(text);
            // if no account found, show the error pop up with the REGISTER ACCOUNT button
            if (found == null) {
                showError("LOGIN", "Account not found.", true);
                return null;
            }
            loginAccount = found;
            // an account that came from the CSV file has no PIN yet, so it creates one first
            if (loginAccount.getPin().equals("")) {
                askCreateLoginPin();
            } else {
                askLoginPin();
            }
            return null;
        });
    }

    // asks for the PIN and opens the customer menu when it is correct
    void askLoginPin() {
        pinTries = 0;
        showInput("LOGIN", "ENTER PIN NUMBER", 2, 4, true, text -> {
            if (text.length() != 4) {
                return "PIN must be 4 digits.";
            }
            // count the try and stop after 3 tries
            if (!loginAccount.checkPin(text)) {
                pinTries++;
                if (pinTries >= MAX_PIN_ATTEMPTS) {
                    showError("LOGIN", "Too many incorrect attempts.", false);
                    return null;
                }
                return "Incorrect PIN. Attempts left: " + (MAX_PIN_ATTEMPTS - pinTries);
            }
            currentAccount = loginAccount;
            showCustomerMenu();
            return null;
        });
    }

    // asks for a new PIN when the account has none yet 
    void askCreateLoginPin() {
        showInput("LOGIN", "CREATE A 4-DIGIT PIN", 2, 4, true, text -> {
            if (text.length() != 4) {
                return "PIN must be 4 digits.";
            }
            loginAccount.setPin(text);
            bank.saveProfiles();
            currentAccount = loginAccount;
            showCustomerMenu();
            return null;
        });
    }

    // asks for the admin name
    void startAdminLogin() {
        showInput("ADMIN LOGIN", "ENTER ADMIN NAME", 1, 15, false, text -> {
            if (!text.equals(ADMIN_NAME)) {
                return "Invalid admin name.";
            }
            askAdminPin();
            return null;
        });
    }

    // asks for the admin PIN (3 tries) and opens the admin menu when it is correct
    void askAdminPin() {
        pinTries = 0;
        showInput("ADMIN LOGIN", "ENTER PIN NUMBER", 2, 4, true, text -> {
            if (text.length() != 4) {
                return "PIN must be 4 digits.";
            }
            if (!text.equals(ADMIN_PIN)) {
                pinTries++;
                if (pinTries >= MAX_PIN_ATTEMPTS) {
                    showError("ADMIN LOGIN", "Too many incorrect attempts.", false);
                    return null;
                }
                return "Incorrect PIN. Attempts left: " + (MAX_PIN_ATTEMPTS - pinTries);
            }
            adminMode = true;
            showAdminMenu();
            return null;
        });
    }

    // validates the account name
    String nameError(String text) {
        String name = text.replaceAll("\\s+", " ");
        if (!name.matches("[A-Za-z.'\\-]+( [A-Za-z.'\\-]+)+") || !name.matches(".*[A-Za-z].*")) {
            return "Enter first name and last name (letters, spaces and periods).";
        }
        return null;
    }

    // asks for the account name 
    void startRegister() {
        if (bank.isFull()) {
            showError("REGISTER ACCOUNT", "The system is full. Cannot register a new account.", false);
            return;
        }
        if (noHistoryRoom("REGISTER ACCOUNT", 1)) return;
        showInput("REGISTER ACCOUNT", "ENTER NAME (FIRST NAME LAST NAME)", 1, 30, false, text -> {
            String error = nameError(text);
            if (error != null) {
                return error;
            }
            tempName = text.replaceAll("\\s+", " ");
            askRegisterPin();
            return null;
        });
    }

    // asks for a 4-digit PIN
    void askRegisterPin() {
        showInput("REGISTER ACCOUNT", "CREATE A 4-DIGIT PIN", 2, 4, true, text -> {
            if (text.length() != 4) {
                return "PIN must be 4 digits.";
            }
            tempPin = text;
            askRegisterContact();
            return null;
        });
    }

    // asks for a contact number of 10 to 15 digits
    void askRegisterContact() {
        showInput("REGISTER ACCOUNT", "ENTER CONTACT NUMBER", 2, 15, false, text -> {
            if (text.length() < 10 || text.length() > 15) {
                return "Contact number must be 10 to 15 digits.";
            }
            tempContact = text;
            askInitialDeposit();
            return null;
        });
    }

    // asks for the initial deposit (minimum Php 5,000.00), then creates the account
    void askInitialDeposit() {
        showInput("REGISTER ACCOUNT", "ENTER INITIAL DEPOSIT (MIN " + money(Bank.MIN_INITIAL_DEPOSIT) + ")",
                3, 15, false, text -> {
            String error = amountError(text);
            if (error != null) {
                return error;
            }
            double amount = parsedAmount;
            if (amount < Bank.MIN_INITIAL_DEPOSIT) {
                return "The initial deposit must be at least Php " + money(Bank.MIN_INITIAL_DEPOSIT) + ".";
            }
            if (amount > Bank.MAX_BALANCE) {
                return "A wallet cannot hold more than " + money(Bank.MAX_BALANCE) + ".";
            }

            // the new account saved in the bank, the initial deposit is saved in the CSV file
            SavingsAccount account = bank.register(tempName, tempPin, tempContact, amount);

            // the account labels
            String[] titles = { "ACCOUNT NAME", "ACCOUNT NUMBER", "ACCOUNT PIN", "CONTACT NUMBER", "INITIAL DEPOSIT",
                    "NOTE" };
            // the values shown on the details screen 
            String[] infos = { account.getName(), Bank.accNo(account.getNumber()), "****", account.getContact(),
                    money(amount) + " PHP", "Use your account number to log in." };
            showDetails("REGISTER ACCOUNT", "ACCOUNT REGISTERED", titles, infos);
            return null;
        });
    }

    // shows an error and returns true if the transaction list cannot hold more records
    boolean noHistoryRoom(String screenTitle, int needed) {
        if (!bank.historyHasRoom(needed)) {
            showError(screenTitle, "The transaction log is full.", false);
            return true;
        }
        return false;
    }

    // checks the amount text and returns an error message
    String amountError(String text) {
        if (!text.matches("\\d{1,12}(\\.\\d{1,2})?")) {
            return "Invalid amount (example: 1000 or 1000.50)";
        }
        double amount = Double.parseDouble(text);
        if (amount <= 0) {
            return "Amount must be greater than zero.";
        }
        parsedAmount = amount;
        return null;
    }

    // deposit function: choose a wallet, then enter the amount
    void startDeposit() {
        if (noHistoryRoom("DEPOSIT AMOUNT", 1)) return;
        showWalletChoice("DEPOSIT AMOUNT", "CHOOSE A WALLET", i -> {
            walletIndex = i;
            askDepositAmount();
        });
    }

    // asks for the amount, adds it to the wallet and shows the details
    void askDepositAmount() {
        Currency cur = currencyAt(walletIndex);
        showInput("DEPOSIT AMOUNT", "ENTER AMOUNT (" + cur.getCode() + ")", 3, 15, false, text -> {
            String error = amountError(text);
            if (error != null) {
                return error;
            }
            double amount = parsedAmount;
            if (!currentAccount.canHold(walletIndex, amount)) {
                return "A wallet cannot hold more than " + money(Bank.MAX_BALANCE) + ".";
            }

            // adds the money to the wallet and saves the transaction in the CSV file
            bank.deposit(currentAccount, walletIndex, amount);

            String[] titles = { "ACCOUNT NAME", "ACCOUNT NUMBER", "WALLET", "AMOUNT DEPOSITED", "UPDATED BALANCE" };
            String[] infos = { currentAccount.getName(), Bank.accNo(currentAccount.getNumber()),
                    currencyText(cur), money(amount) + " " + cur.getCode(),
                    walletText(currentAccount, walletIndex) };
            showDetails("DEPOSIT AMOUNT", "DEPOSIT DETAILS", titles, infos);
            return null;
        });
    }

    // withdraw function: choose a wallet (stops if the wallet is empty), then enter the amount
    void startWithdraw() {
        if (noHistoryRoom("WITHDRAW AMOUNT", 1)) return;
        showWalletChoice("WITHDRAW AMOUNT", "CHOOSE A WALLET", i -> {
            if (currentAccount.getWallet(i) <= 0) {
                showError("WITHDRAW AMOUNT", "This wallet has zero balance. Unable to withdraw.", false);
                return;
            }
            walletIndex = i;
            askWithdrawAmount();
        });
    }

    // asks for the amount, subtracts it from the wallet and shows the details
    void askWithdrawAmount() {
        Currency cur = currencyAt(walletIndex);
        showInput("WITHDRAW AMOUNT", "ENTER AMOUNT (" + cur.getCode() + ")", 3, 15, false, text -> {
            String error = amountError(text);
            if (error != null) {
                return error;
            }
            double amount = parsedAmount;
            if (amount > currentAccount.getWallet(walletIndex)) {
                return "Insufficient balance. Max: " + walletText(currentAccount, walletIndex);
            }

            // takes the money out of the wallet and saves the transaction in the CSV file
            bank.withdraw(currentAccount, walletIndex, amount);

            String[] titles = { "ACCOUNT NAME", "ACCOUNT NUMBER", "WALLET", "AMOUNT WITHDRAWN", "UPDATED BALANCE" };
            String[] infos = { currentAccount.getName(), Bank.accNo(currentAccount.getNumber()),
                    currencyText(cur), money(amount) + " " + cur.getCode(),
                    walletText(currentAccount, walletIndex) };
            showDetails("WITHDRAW AMOUNT", "WITHDRAW DETAILS", titles, infos);
            return null;
        });
    }

    // shows the balance of every wallet
    void showBalance() {
        Currency[] list = bank.getCurrencies();
        String[] titles = new String[list.length + 2];
        String[] infos = new String[list.length + 2];

        titles[0] = "ACCOUNT NAME";
        infos[0] = currentAccount.getName();
        titles[1] = "ACCOUNT NUMBER";
        infos[1] = Bank.accNo(currentAccount.getNumber());

        // adds the balance of each wallet
        for (int i = 0; i < list.length; i++) {
            titles[i + 2] = currencyText(list[i]);
            infos[i + 2] = walletText(currentAccount, i);
        }
        showDetails("CHECK BALANCE", "WALLET BALANCES", titles, infos);
    }

    // shows all the rates and has a button to convert an amount
    void showRates() {
        Currency base = bank.getBaseCurrency();
        Currency[] list = bank.getCurrencies();
        String[] titles = new String[list.length + 1];
        String[] infos = new String[list.length + 1];

        titles[0] = "BASE CURRENCY";
        infos[0] = currencyText(base);

        // adds the rate of each currency against the base currency
        for (int i = 0; i < list.length; i++) {
            titles[i + 1] = currencyText(list[i]);
            infos[i + 1] = "1 " + list[i].getCode() + " = " + Bank.rate(list[i].getRate()) + " " + base.getCode();
        }
        showDetails("CURRENCY EXCHANGE", "EXCHANGE RATES", titles, infos, "CONVERT\nAMOUNT", () -> startConvert());
    }

    // convert amount function: choose the source currency, enter the amount, then choose the target currency
    void startConvert() {
        showCurrencyChoice("CONVERT AMOUNT", "CHOOSE SOURCE CURRENCY", i -> {
            sourceIndex = i;
            askConvertAmount();
        }, null);
    }

    // asks for the amount to convert
    void askConvertAmount() {
        Currency source = currencyAt(sourceIndex);
        showInput("CONVERT AMOUNT", "ENTER AMOUNT (" + source.getCode() + ")", 3, 15, false, text -> {
            String error = amountError(text);
            if (error != null) {
                return error;
            }
            pendingAmount = parsedAmount;
            chooseConvertTarget();
            return null;
        });
    }

    // choose the target currency and shows the converted result
    void chooseConvertTarget() {
        showCurrencyChoice("CONVERT AMOUNT", "CHOOSE TARGET CURRENCY", i -> {
            if (i == sourceIndex) {
                showError("CONVERT AMOUNT", "The source and target currencies must be different.", false);
                return;
            }
            Currency source = currencyAt(sourceIndex);
            Currency target = currencyAt(i);
            // the converted amount before rounding
            double raw = bank.convertRaw(pendingAmount, source, target);

            if (raw > Bank.MAX_BALANCE) {
                showError("CONVERT AMOUNT", "The converted amount is too large to display.", false);
                return;
            }
            if (raw < Bank.MIN_CONVERTED) {
                showError("CONVERT AMOUNT", "The converted amount is too small (less than 0.01 "
                        + target.getCode() + ").", false);
                return;
            }
            double result = Account.round(raw);

            String[] titles = { "SOURCE CURRENCY", "SOURCE AMOUNT", "TARGET CURRENCY", "TARGET AMOUNT",
                    "EXCHANGE RATE" };
            String[] infos = { currencyText(source), money(pendingAmount) + " " + source.getCode(),
                    currencyText(target), money(result) + " " + target.getCode(),
                    "1 " + source.getCode() + " = " + Bank.rate(source.getRate() / target.getRate()) + " "
                            + target.getCode() };
            showDetails("CONVERT AMOUNT", "CONVERSION DETAILS", titles, infos);
        }, null);
    }

    // exchange currency function: choose the wallet to take from, then the wallet to put in
    void startExchange() {
        if (noHistoryRoom("EXCHANGE CURRENCY", 2)) return;
        showWalletChoice("EXCHANGE CURRENCY", "EXCHANGE FROM", i -> {
            if (currentAccount.getWallet(i) <= 0) {
                showError("EXCHANGE CURRENCY", "The " + currencyAt(i).getCode()
                        + " wallet has no balance to exchange.", false);
                return;
            }
            sourceIndex = i;
            showWalletChoice("EXCHANGE CURRENCY", "EXCHANGE TO", j -> {
                if (j == sourceIndex) {
                    showError("EXCHANGE CURRENCY", "The source and target currencies must be different.", false);
                    return;
                }
                targetIndex = j;
                askExchangeAmount();
            });
        });
    }

    // asks for the amount to exchange, checks it, then asks the user to confirm
    void askExchangeAmount() {
        Currency source = currencyAt(sourceIndex);
        Currency target = currencyAt(targetIndex);
        showInput("EXCHANGE CURRENCY", "ENTER AMOUNT (" + source.getCode() + ")", 3, 15, false, text -> {
            String error = amountError(text);
            if (error != null) {
                return error;
            }
            double amount = parsedAmount;
            if (amount > currentAccount.getWallet(sourceIndex)) {
                return "Insufficient balance. Max: " + walletText(currentAccount, sourceIndex);
            }
            // the converted amount before rounding
            double raw = bank.convertRaw(amount, source, target);
            if (raw > Bank.MAX_BALANCE) {
                return "The converted amount is too large.";
            }
            if (raw < Bank.MIN_CONVERTED) {
                return "The converted amount is too small (less than 0.01).";
            }
            double result = Account.round(raw);
            if (!currentAccount.canHold(targetIndex, result)) {
                return "The " + target.getCode() + " wallet cannot hold more than " + money(Bank.MAX_BALANCE) + ".";
            }

            pendingAmount = amount;
            pendingResult = result;

            String[] titles = { "EXCHANGE RATE", "YOU WILL EXCHANGE", "YOU WILL RECEIVE" };
            String[] infos = {
                    "1 " + source.getCode() + " = " + Bank.rate(source.getRate() / target.getRate()) + " "
                            + target.getCode(),
                    money(amount) + " " + source.getCode(), money(result) + " " + target.getCode() };
            showConfirm("EXCHANGE CURRENCY", "PROCEED WITH THE EXCHANGE?", titles, infos, () -> doExchange());
            return null;
        });
    }

    // moves the money from one wallet to the other and shows the details
    void doExchange() {
        Currency source = currencyAt(sourceIndex);
        Currency target = currencyAt(targetIndex);

        bank.exchange(currentAccount, sourceIndex, targetIndex, pendingAmount, pendingResult);

        String[] titles = { "EXCHANGED", "RECEIVED", source.getCode() + " BALANCE", target.getCode() + " BALANCE" };
        String[] infos = { money(pendingAmount) + " " + source.getCode(), money(pendingResult) + " " + target.getCode(),
                walletText(currentAccount, sourceIndex), walletText(currentAccount, targetIndex) };
        showDetails("EXCHANGE CURRENCY", "EXCHANGE SUCCESSFUL", titles, infos);
    }

    // interest function: choose a wallet, then show the interest menu
    void startInterest() {
        showWalletChoice("INTEREST", "CHOOSE A WALLET", i -> {
            walletIndex = i;
            showInterestMenu();
        });
    }

    // shows the annual and daily interest buttons
    void showInterestMenu() {
        JPanel p = makeScreen("INTEREST", false);
        addSubtitle(p, "BALANCE: " + walletText(currentAccount, walletIndex));

        // the interest rate shown beside the annual interest label 
        JLabel rate = new JLabel("5%", SwingConstants.RIGHT);
        rate.setForeground(darkGreen);
        rate.setFont(new Font("SansSerif", Font.BOLD, 13));
        rate.setBounds(150 + 165 - 50, 130 + 2, 42, 51);
        p.add(rate);

        addMenuButton(p, "ANNUAL\nINTEREST", true, 150, 130, 165, 55, () -> askYears());
        addMenuButton(p, "DAILY\nINTEREST", true, 150, 215, 165, 55, () -> askDays());

        showScreen(p);
    }

    // asks for the number of years (1 to 100)
    void askYears() {
        showInput("ANNUAL INTEREST", "ENTER NUMBER OF YEARS", 2, 3, false, text -> {
            if (text.equals("")) {
                return "Please enter a number.";
            }
            int years = Integer.parseInt(text);
            if (years < 1 || years > 100) {
                return "Enter 1 to 100 years.";
            }
            showInterestTable(true, years);
            return null;
        });
    }

    // asks for the number of days (1 to 3650)
    void askDays() {
        showInput("DAILY INTEREST", "ENTER NUMBER OF DAYS", 2, 4, false, text -> {
            if (text.equals("")) {
                return "Please enter a number.";
            }
            int days = Integer.parseInt(text);
            if (days < 1 || days > 3650) {
                return "Enter 1 to 3650 days.";
            }
            showInterestTable(false, days);
            return null;
        });
    }

    // shows the interest table
    void showInterestTable(boolean annual, int count) {
        // the money used to compute the interest
        double balance = currentAccount.getWallet(walletIndex);
        ArrayList<double[]> projection;
        String firstColumn;
        String screenTitle;
        // the interest of the first period 
        double firstInterest;

        // gets the data for years or days
        if (annual) {
            projection = currentAccount.projectAnnual(balance, count);
            firstColumn = "YEAR";
            screenTitle = "ANNUAL INTEREST";
            firstInterest = Account.round(balance * currentAccount.getAnnualRate());
        } else {
            projection = currentAccount.projectDaily(balance, count);
            firstColumn = "DAY";
            screenTitle = "DAILY INTEREST";
            firstInterest = Account.round(balance * currentAccount.getDailyRate());
        }

        // adds each row (period, interest, balance) to the table
        ArrayList<Object[]> rows = new ArrayList<>();
        for (double[] r : projection) {
            rows.add(new Object[] { (int) r[0], money(r[1]), money(r[2]) });
        }

        // the message under the table (null if there is nothing to warn about)
        String note = null;
        if (projection.size() < count) {
            note = "Projection stopped on " + (annual ? "year " : "day ") + (projection.size() + 1)
                    + ": the balance would exceed " + money(Bank.MAX_BALANCE) + ".";
        } else if (balance < 0.01) {
            note = "The balance is 0.00 so no interest will be earned.";
        } else if (firstInterest < 0.01) {
            note = "Interest is rounded to the nearest centavo each period so a balance this small earns 0.00.";
        }

        String[] columns = { firstColumn, "INTEREST", "BALANCE" };
        int[] widths = { 70, 150, 170 };
        showTable(screenTitle, columns, rows, widths, note);
    }

    // send money function: choose a wallet, enter the recipient, then enter the amount
    void startSend() {
        if (noHistoryRoom("SEND MONEY", 2)) return;
        showWalletChoice("SEND MONEY", "CHOOSE A WALLET", i -> {
            walletIndex = i;
            askRecipient();
        });
    }

    // asks for the account number of the recipient
    void askRecipient() {
        showInput("SEND MONEY", "ENTER RECIPIENT ACCOUNT NUMBER", 2, 10, false, text -> {
            if (text.length() < 10) {
                return "Enter the full 10-digit account number.";
            }
            SavingsAccount found = findByAccountText(text);
            if (found == null) {
                return "Recipient account not found.";
            }
            if (found == currentAccount) {
                return "You cannot send money to your own account.";
            }
            recipient = found;
            askSendAmount();
            return null;
        });
    }

    // asks for the amount to send, checks it, then asks the user to confirm
    void askSendAmount() {
        Currency cur = currencyAt(walletIndex);
        showInput("SEND MONEY", "ENTER AMOUNT (" + cur.getCode() + ")", 3, 15, false, text -> {
            String error = amountError(text);
            if (error != null) {
                return error;
            }
            double amount = parsedAmount;
            if (amount > currentAccount.getWallet(walletIndex)) {
                return "Insufficient balance. Max: " + walletText(currentAccount, walletIndex);
            }
            if (!recipient.canHold(walletIndex, amount)) {
                return "The recipient's wallet cannot hold that much.";
            }

            pendingAmount = amount;

            String[] titles = { "RECIPIENT NAME", "RECIPIENT ACCOUNT NUMBER", "AMOUNT TO SEND" };
            String[] infos = { recipient.getName(), Bank.accNo(recipient.getNumber()),
                    money(amount) + " " + cur.getCode() };
            showConfirm("SEND MONEY", "SEND THIS AMOUNT?", titles, infos, () -> doSend());
            return null;
        });
    }

    // sends the money
    void doSend() {
        Currency cur = currencyAt(walletIndex);

        bank.transfer(currentAccount, recipient, walletIndex, pendingAmount);

        String[] titles = { "SENT", "TO", "UPDATED BALANCE" };
        String[] infos = { money(pendingAmount) + " " + cur.getCode(),
                recipient.getName() + " (" + Bank.accNo(recipient.getNumber()) + ")",
                walletText(currentAccount, walletIndex) };
        showDetails("SEND MONEY", "TRANSFER SUCCESSFUL", titles, infos);
    }

    // close account function: asks for the PIN, then checks the wallets, then asks the user to confirm
    void startClose() {
        showInput("CLOSE ACCOUNT", "ENTER PIN TO CONTINUE", 2, 4, true, text -> {
            if (text.length() != 4) {
                return "PIN must be 4 digits.";
            }
            if (!currentAccount.checkPin(text)) {
                return "Incorrect PIN. The account was not closed.";
            }

            // the account cannot be closed while a wallet still has money
            if (currentAccount.hasBalance()) {
                ArrayList<String> titleList = new ArrayList<>();
                ArrayList<String> infoList = new ArrayList<>();
                titleList.add("WITHDRAW THESE BALANCES FIRST");
                infoList.add("");
                for (int i = 0; i < bank.getCurrencies().length; i++) {
                    if (currentAccount.getWallet(i) > 0) {
                        titleList.add(currencyText(currencyAt(i)));
                        infoList.add(walletText(currentAccount, i));
                    }
                }
                showDetails("CLOSE ACCOUNT", "ACCOUNT STILL HAS MONEY", titleList.toArray(new String[0]),
                        infoList.toArray(new String[0]));
                return null;
            }

            String[] titles = { "ACCOUNT NAME", "ACCOUNT NUMBER", "WARNING" };
            String[] infos = { currentAccount.getName(), Bank.accNo(currentAccount.getNumber()),
                    "This action cannot be undone." };
            showConfirm("CLOSE ACCOUNT", "CLOSE THIS ACCOUNT?", titles, infos, () -> doClose());
            return null;
        });
    }

    // removes the account and logs the customer out
    void doClose() {
        String name = currentAccount.getName();
        String number = Bank.accNo(currentAccount.getNumber());

        bank.closeAccount(currentAccount);
        currentAccount = null;

        String[] titles = { "ACCOUNT NAME", "ACCOUNT NUMBER", "NOTE" };
        String[] infos = { name, number, "Thank you for banking with us." };
        showDetails("CLOSE ACCOUNT", "ACCOUNT CLOSED", titles, infos);
    }

    // the type of a transaction as text
    String typeText(Transaction t) {
        return t.getLabel();
    }

    // the amount of a transaction as text
    String amountText(Transaction t) {
        if (t.isWithdrawal()) {
            return "-" + money(t.getAmount());
        }
        return money(t.getAmount());
    }

    // the other account of a transaction as text 
    String otherText(Transaction t) {
        if (t.getOtherAccount() == 0) {
            return "-";
        }
        return Bank.accNo(t.getOtherAccount());
    }

    // shows all the transactions of the customer
    void showHistory() {
        ArrayList<Object[]> rows = new ArrayList<>();
        int count = 0;

        for (Transaction t : bank.historyOf(currentAccount)) {
            count++;
            rows.add(new Object[] { count, t.dateText(), t.timeText(), t.getCurrency(), typeText(t),
                    amountText(t), money(t.getCurrentBalance()), otherText(t) });
        }

        if (count == 0) {
            String[] titles = { "NO TRANSACTIONS YET" };
            String[] infos = { "" };
            showDetails("TRANSACTION HISTORY", "ACCOUNT " + Bank.accNo(currentAccount.getNumber()), titles, infos);
            return;
        }

        String[] columns = { "NO.", "DATE", "TIME", "CUR", "TYPE", "AMOUNT", "BALANCE", "OTHER" };
        int[] widths = { 34, 80, 66, 38, 92, 84, 90, 96 };
        showTable("TRANSACTION HISTORY", columns, rows, widths, null);
    }

    // records a new currency rate 
    void startRecordRate() {
        showCurrencyChoice("RECORD RATE", "CHOOSE A CURRENCY", i -> {
            Currency base = bank.getBaseCurrency();
            if (currencyAt(i) == base) {
                showError("RECORD RATE", currencyText(base) + " is the base currency. Its rate is always 1.00.",
                        false);
                return;
            }
            sourceIndex = i;
            askRate();
        }, i -> "RATE: " + Bank.rate(currencyAt(i).getRate()));
    }

    // ask the new exchange rate (up to 6 decimals)
    void askRate() {
        Currency base = bank.getBaseCurrency();
        Currency selected = currencyAt(sourceIndex);
        showInput("RECORD RATE", "1 " + selected.getCode() + " = ? " + base.getCode(), 3, 16, false,
                text -> {
                    if (!text.matches("\\d{1,9}(\\.\\d{1,6})?") || Double.parseDouble(text) <= 0) {
                        return "Invalid rate (up to 6 decimals).";
                    }
                    selected.setRate(Double.parseDouble(text));
                    bank.saveRates();

                    String[] titles = { "CURRENCY", "EXCHANGE RATE", "BASE CURRENCY" };
                    String[] infos = { currencyText(selected),
                            "1 " + selected.getCode() + " = " + Bank.rate(selected.getRate()) + " " + base.getCode(),
                            currencyText(base) };
                    showDetails("RECORD RATE", "RATE DETAILS", titles, infos);
                    return null;
                });
    }

    // changes the base currency
    void startSetBase() {
        showCurrencyChoice("CHANGE BASE CURRENCY", "CHOOSE A CURRENCY", i -> {
            Currency c = currencyAt(i);
            if (c == bank.getBaseCurrency()) {
                showError("CHANGE BASE CURRENCY", currencyText(c) + " is already the base currency.", false);
                return;
            }
            bank.setBaseCurrency(c);
            bank.saveRates();

            String rates = "";
            Currency[] list = bank.getCurrencies();
            for (int k = 0; k < list.length; k++) {
                rates = rates + "1 " + list[k].getCode() + " = " + Bank.rate(list[k].getRate()) + " " + c.getCode();
                if (k < list.length - 1) {
                    rates = rates + "\n";
                }
            }

            String[] titles = { "NEW BASE CURRENCY", "UPDATED EXCHANGE RATES" };
            String[] infos = { currencyText(c), rates };
            showDetails("CHANGE BASE CURRENCY", "BASE CURRENCY DETAILS", titles, infos);
        }, null);
    }

    // view and updates a user's profile
    void startProfile() {
        if (bank.getAccounts().isEmpty()) {
            showError("VIEW/UPDATE PROFILE", "There are no registered accounts.", false);
            return;
        }
        showInput("VIEW/UPDATE PROFILE", "ENTER ACCOUNT NUMBER", 2, 10, false, text -> {
            if (text.length() < 10) {
                return "Enter the full 10-digit account number.";
            }
            SavingsAccount found = findByAccountText(text);
            if (found == null) {
                return "Account not found.";
            }
            profileAccount = found;
            showProfile();
            return null;
        });
    }

    // the contact number as text ("NOT SET" if the account has none)
    String contactText(SavingsAccount a) {
        if (a.getContact().equals("")) {
            return "NOT SET";
        }
        return a.getContact();
    }

    // shows the profile of the customer with the buttons to update it
    void showProfile() {
        JPanel p = makeScreen("VIEW/UPDATE PROFILE", false);
        addSubtitle(p, "CUSTOMER PROFILE");

        JPanel box = makeWhiteBox(150, 105, 420, 215);

        // the profile text with the balance of each wallet
        String text = "ACCOUNT NUMBER\n    " + Bank.accNo(profileAccount.getNumber())
                + "\n\nACCOUNT NAME\n    " + profileAccount.getName()
                + "\n\nCONTACT NUMBER\n    " + contactText(profileAccount)
                + "\n\nWALLET BALANCES";
        for (int i = 0; i < bank.getCurrencies().length; i++) {
            text = text + "\n    " + currencyText(currencyAt(i)) + ": " + walletText(profileAccount, i);
        }

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        area.setForeground(darkGreen);
        area.setMargin(new Insets(10, 15, 10, 10));
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(new LineBorder(darkGreen, 2));
        scroll.setBounds(25, 15, 370, 185);
        box.add(scroll);
        p.add(box);

        // the buttons that update the profile
        addMenuButton(p, "UPDATE NAME", true, 150, 335, 165, 45, () -> askNewName());
        addMenuButton(p, "RESET PIN", false, 405, 335, 165, 45, () -> askNewPin());
        addMenuButton(p, "UPDATE CONTACT", true, 150, 415, 165, 45, () -> askNewContact());
        addBackButton(p);

        showScreen(p);
    }

    // asks for the new name of the customer (First name Last name)
    void askNewName() {
        showInput("VIEW/UPDATE PROFILE", "ENTER NEW NAME (FIRST NAME LAST NAME)", 1, 30, false, text -> {
            String error = nameError(text);
            if (error != null) {
                return error;
            }
            profileAccount.setName(text.replaceAll("\\s+", " "));
            bank.saveProfiles();
            showProfileUpdated("ACCOUNT NAME", profileAccount.getName());
            return null;
        });
    }

    // asks for the new PIN of the customer
    void askNewPin() {
        showInput("VIEW/UPDATE PROFILE", "ENTER NEW 4-DIGIT PIN", 2, 4, true, text -> {
            if (text.length() != 4) {
                return "PIN must be 4 digits.";
            }
            profileAccount.setPin(text);
            bank.saveProfiles();
            showProfileUpdated("ACCOUNT PIN", "****");
            return null;
        });
    }

    // asks for the new contact number of the customer
    void askNewContact() {
        showInput("VIEW/UPDATE PROFILE", "ENTER NEW CONTACT NUMBER", 2, 15, false, text -> {
            if (text.length() < 10 || text.length() > 15) {
                return "Contact number must be 10 to 15 digits.";
            }
            profileAccount.setContact(text);
            bank.saveProfiles();
            showProfileUpdated("CONTACT NUMBER", profileAccount.getContact());
            return null;
        });
    }

    // shows what was updated in the profile
    void showProfileUpdated(String what, String value) {
        String[] titles = { "ACCOUNT NUMBER", "ACCOUNT NAME", "UPDATED " + what };
        String[] infos = { Bank.accNo(profileAccount.getNumber()), profileAccount.getName(), value };
        showDetails("VIEW/UPDATE PROFILE", "PROFILE UPDATED", titles, infos);
    }

    // shows the transactions of all accounts (all the rows of the CSV file)
    void showAllTransactions() {
        ArrayList<Object[]> rows = new ArrayList<>();
        int number = 1;

        for (Transaction t : bank.getLedger()) {
            rows.add(new Object[] { number, Bank.accNo(t.getAccountNumber()), t.dateText(), t.timeText(),
                    t.getCurrency(), typeText(t), amountText(t), money(t.getCurrentBalance()), otherText(t) });
            number++;
        }

        if (rows.isEmpty()) {
            String[] titles = { "NO TRANSACTIONS RECORDED" };
            String[] infos = { "" };
            showDetails("TRANSACTION HISTORY", "ALL ACCOUNTS", titles, infos);
            return;
        }

        String[] columns = { "NO.", "ACCOUNT", "DATE", "TIME", "CUR", "TYPE", "AMOUNT", "BALANCE", "OTHER" };
        int[] widths = { 40, 92, 80, 66, 38, 92, 84, 90, 96 };
        showTable("TRANSACTION HISTORY", columns, rows, widths, null);
    }

    // generate reports function: shows one button for each report (nothing is generated until the admin picks one)
    void showReportMenu() {
        JPanel p = makeScreen("GENERATE REPORTS", false);
        addSubtitle(p, "CHOOSE A REPORT");

        // the buttons for the 4 reports and the summary
        String[] names = { "REPORT 1\nPRIVILEGE TIERS", "REPORT 2\nINTEREST EXPENSE",
                "REPORT 3\nSUSPICIOUS CLIENTS", "REPORT 4\nDORMANT ACCOUNTS", "SUMMARY\n(summary.json)" };
        for (int i = 0; i < reports.length; i++) {
            final ReportGenerator report = reports[i];
            if (i < 3) {
                addMenuButton(p, names[i], true, 150, 120 + i * 85, 165, 55, () -> generateReport(report));
            } else {
                addMenuButton(p, names[i], false, 405, 120 + (i - 3) * 85, 165, 55, () -> generateReport(report));
            }
        }

        addBackButton(p);
        showScreen(p);
    }

    // loads the file automatically, then generates the report that the admin picked
    void generateReport(ReportGenerator report) {
        // loads the file: reads the CSV, checks and cleans every row and keeps the PHP rows 
        IngestionResult data;
        try {
            data = DataIngestion.ingest(bank.getCsvPath());
        } catch (IOException e) {
            showError("GENERATE REPORTS", "Could not read " + bank.getCsvPath() + ".", false);
            return;
        }
        if (!data.isFileFound()) {
            showError("GENERATE REPORTS", "File not found: " + bank.getCsvPath(), false);
            return;
        }

        // makes the report 
        ReportResult result;
        try {
            result = report.generate(data);
        } catch (IOException e) {
            showError("GENERATE REPORTS", "Could not write " + report.getOutputFileName() + ".", false);
            return;
        }

        // the total row count and the errors found
        String loaded = String.format("%,d", data.getTotalRows()) + " rows loaded\n"
                + String.format("%,d", data.getPhpRows().size()) + " filtered for PHP currency";
        String validation = String.format("%,d", data.getInvalidCount()) + " invalid or incomplete row(s) skipped\n"
                + String.format("%,d", data.getNonPhpCount()) + " valid row(s) in other currencies excluded";
        int shown = Math.min(data.getErrors().size(), 10);
        for (int i = 0; i < shown; i++) {
            validation = validation + "\n" + data.getErrors().get(i);
        }
        if (data.getErrors().size() > shown) {
            validation = validation + "\n... and " + (data.getErrors().size() - shown) + " more.";
        }

        // shows the first 2 rows of the report as a preview
        String preview = "";
        if (result.rows.isEmpty()) {
            preview = "(no rows)";
        }
        for (int i = 0; i < Math.min(2, result.rows.size()); i++) {
            if (i > 0) {
                preview = preview + "\n\n";
            }
            for (int c = 0; c < result.header.length; c++) {
                preview = preview + result.header[c] + ": " + result.rows.get(i)[c];
                if (c < result.header.length - 1) {
                    preview = preview + "\n";
                }
            }
        }

        // where the full table was saved
        String saved;
        if (result.fileName.endsWith(".json")) {
            saved = "Saved to " + result.fileName;
        } else {
            saved = String.format("%,d", result.rows.size()) + " row(s). Full table exported to\n" + result.fileName;
        }

        ArrayList<String> titleList = new ArrayList<>();
        ArrayList<String> infoList = new ArrayList<>();
        titleList.add("FILE LOADED");
        infoList.add(loaded);
        titleList.add("VALIDATION");
        infoList.add(validation);
        if (result.note != null) {
            titleList.add("NOTE");
            infoList.add(result.note);
        }
        titleList.add("PREVIEW (FIRST 2 ROWS)");
        infoList.add(preview);
        titleList.add("EXPORTED");
        infoList.add(saved);

        showDetails("GENERATE REPORTS", result.title, titleList.toArray(new String[0]),
                infoList.toArray(new String[0]), "ANOTHER\nREPORT", () -> showReportMenu());
    }

    // starts the program by creating the window
    public static void main(String[] args) {
        String csvFile = "bank_transactions.csv";
        if (args.length > 0) {
            csvFile = args[0];
        }
        final String file = csvFile;

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> new MCO2_10_JAVA(file));
    }
}

// the square arrow button on the sides of the screen
class ArrowButton extends JButton {
    boolean pointRight;

    // sets how the arrow button looks
    public ArrowButton(boolean pointRight) {
        this.pointRight = pointRight;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // draws the button
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // color of  the button when pressed and when it is not
        if (getModel().isPressed()) {
            g2.setColor(new Color(215, 215, 215));
        } else {
            g2.setColor(Color.WHITE);
        }
        // size of the whole button
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

        // color of the arrow
        g2.setColor(new Color(0, 102, 51));
        // size of the arrow
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int[] xs;
        if (pointRight) {
            xs = new int[] { cx - 8, cx - 8, cx + 10 };
        } else {
            xs = new int[] { cx + 8, cx + 8, cx - 10 };
        }
        int[] ys = { cy - 12, cy + 12, cy };
        g2.fillPolygon(xs, ys, 3);
    }
}

// one key of the on-screen keyboard or number pad
class Key {
    // the text drawn on the key
    String label;
    // what the key types 
    String value;
    // the position and size of the key
    Rectangle area;

    // creates a key with its label, value, position and size
    public Key(String label, String value, int x, int y, int w, int h) {
        this.label = label;
        this.value = value;
        this.area = new Rectangle(x, y, w, h);
    }
}

// base of the keyboard and the number pad
abstract class KeyDisplay extends JPanel {
    // color used for the lines and letters
    protected final Color green = new Color(0, 102, 51);
    // all the keys on this display
    protected final ArrayList<Key> keys = new ArrayList<>();
    // the key that is being pressed
    private Key pressed;
    // runs when a key is typed
    protected final Consumer<String> onKey;

    // sets up the display and listens to the mouse clicks on the keys
    public KeyDisplay(Consumer<String> onKey) {
        this.onKey = onKey;
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            // remembers the key that is being pressed
            public void mousePressed(MouseEvent e) {
                pressed = keyAt(e.getPoint());
                repaint();
            }

            // runs when the mouse button is released
            public void mouseReleased(MouseEvent e) {
                // the key is typed when the mouse is released on the same key that was pressed
                Key released = keyAt(e.getPoint());
                if (pressed != null && pressed == released) {
                    keyClicked(pressed.value);
                }
                pressed = null;
                repaint();
            }
        });
    }

    // runs when a key is clicked 
    protected void keyClicked(String value) {
        onKey.accept(value);
    }

    // the text drawn on a key 
    protected String labelOf(Key k) {
        return k.label;
    }

    // true if the key should be highlighted (the keyboard highlights CAPS while it is on)
    protected boolean isHighlighted(Key k) {
        return false;
    }

    // finds the key at a point (null if there is none)
    Key keyAt(Point point) {
        for (Key k : keys) {
            if (k.area.contains(point)) {
                return k;
            }
        }
        return null;
    }

    // draws one key (it is gray while pressed)
    void drawKey(Graphics2D g2, Key k, Font font) {
        if (k == pressed) {
            g2.setColor(new Color(215, 215, 215));
        } else if (isHighlighted(k)) {
            g2.setColor(new Color(170, 215, 190));
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.fillRoundRect(k.area.x, k.area.y, k.area.width, k.area.height, 8, 8);

        g2.setColor(green);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(k.area.x, k.area.y, k.area.width, k.area.height, 8, 8);

        // draws the label in the middle of the key
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        String text = labelOf(k);
        int x = k.area.x + (k.area.width - fm.stringWidth(text)) / 2;
        int y = k.area.y + (k.area.height + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, x, y);
    }
}

// displays the keyboard that the user clicks
class KeyBoardDisplay extends KeyDisplay {
    // true when the CAPS key is on
    private boolean shift = false;

    // makes all the keys of the keyboard
    public KeyBoardDisplay(Consumer<String> onKey) {
        super(onKey);

        // the letters of each row 
        String[] rows = { "qwertyuiop", "asdfghjkl", "zxcvbnm." };
        // size of each key
        int keyW = 24;
        int gap = 4;
        // makes the letter keys
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            int rowWidth = row.length() * keyW + (row.length() - 1) * gap;
            int startX = (320 - rowWidth) / 2;
            int y = 20 + r * 30;
            for (int i = 0; i < row.length(); i++) {
                String letter = row.substring(i, i + 1);
                keys.add(new Key(letter, letter, startX + i * (keyW + gap), y, keyW, 24));
            }
        }

        // the CAPS key, the space bar and the delete key on the last row
        int capsW = 56;
        int spaceW = 150;
        int delW = 56;
        int startX = (320 - (capsW + gap + spaceW + gap + delW)) / 2;
        int y = 20 + 3 * 30;
        keys.add(new Key("CAPS", "CAPS", startX, y, capsW, 24));
        keys.add(new Key("SPACE", " ", startX + capsW + gap, y, spaceW, 24));
        keys.add(new Key("DEL", "BACK", startX + capsW + gap + spaceW + gap, y, delW, 24));
    }

    // CAPS turns capital letters on or off
    protected void keyClicked(String value) {
        if (value.equals("CAPS")) {
            shift = !shift;
        } else if (shift && value.length() == 1 && Character.isLetter(value.charAt(0))) {
            onKey.accept(value.toUpperCase());
            shift = false;
        } else {
            onKey.accept(value);
        }
        repaint();
    }

    // shows capital letters on the keys while CAPS is on
    protected String labelOf(Key k) {
        if (shift && k.label.length() == 1 && Character.isLetter(k.label.charAt(0))) {
            return k.label.toUpperCase();
        }
        return k.label;
    }

    // the CAPS key is highlighted while it is on
    protected boolean isHighlighted(Key k) {
        return shift && k.value.equals("CAPS");
    }

    // draws the border and the keys of the keyboard
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(green);
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 30, 30);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(9, 9, getWidth() - 19, getHeight() - 19, 22, 22);

        // font size of each letter
        Font font = new Font("SansSerif", Font.BOLD, 12);
        for (Key k : keys) {
            drawKey(g2, k, font);
        }
    }
}

// displays the number pad that the user clicks
class KeyPadDisplay extends KeyDisplay {
    // makes all the keys of the number pad
    public KeyPadDisplay(boolean withDot, Consumer<String> onKey) {
        super(onKey);

        // the numbers on the numpad
        String[] labels = { "7", "8", "9", "4", "5", "6", "1", "2", "3" };
        // makes the numbers 1 to 9 in rows of 3
        for (int i = 0; i < labels.length; i++) {
            keys.add(new Key(labels[i], labels[i], 6 + (i % 3) * 41, 6 + (i / 3) * 41, 38, 38));
        }

        // the delete key, 0 key, and the dot key 
        keys.add(new Key("DEL", "BACK", 6, 6 + 3 * 41, 38, 38));
        keys.add(new Key("0", "0", 6 + 41, 6 + 3 * 41, 38, 38));
        if (withDot) {
            keys.add(new Key(".", ".", 6 + 2 * 41, 6 + 3 * 41, 38, 38));
        }
    }

    // draws the number pad and its keys
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // color behind the number keys
        g2.setColor(green);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        // font size of the numbers and of the DEL key
        Font numberFont = new Font("SansSerif", Font.BOLD, 18);
        Font delFont = new Font("SansSerif", Font.BOLD, 12);
        for (Key k : keys) {
            drawKey(g2, k, k.label.length() > 1 ? delFont : numberFont);
        }
    }
}

class Currency {

    private final String code;
    private final String name;
    private double rate;

    // creates a new currency
    public Currency(String code, String name, double rate) {
        this.code = code;
        this.name = name;
        this.rate = rate;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getRate() {
        return rate;
    }

    // sets a new rate (false if the rate is zero or less)
    public boolean setRate(double newRate) {
        if (newRate <= 0) return false;
        this.rate = newRate;
        return true;
    }
}


class Account {
    private final long number;
    private String name;
    private String pin;
    private String contact;
    // the money in each wallet 
    private final double[] wallets;

    // creates a new account with all wallets at 0
    public Account(long number, String name, String pin, String contact, int walletCount) {
        this.number = number;
        this.name = name;
        this.pin = pin;
        this.contact = contact;
        this.wallets = new double[walletCount];
    }

    // creates an account without a PIN and contact number (an account found in the CSV file)
    public Account(long number, String name, int walletCount) {
        this(number, name, "", "", walletCount);
    }

    public long getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    // checks if the PIN typed is the PIN of the account
    public boolean checkPin(String enteredPin) {
        return pin.equals(enteredPin);
    }

    public double getWallet(int index) {
        return wallets[index];
    }

    // sets the balance of a wallet 
    public void setWallet(int index, double value) {
        wallets[index] = round(value);
    }

    // checks if a wallet can receive an amount without passing the maximum balance
    public boolean canHold(int index, double amount) {
        return wallets[index] + amount <= Bank.MAX_BALANCE;
    }

    // checks if any wallet still has money
    public boolean hasBalance() {
        for (double w : wallets) {
            if (w > 0) return true;
        }
        return false;
    }

    // adds money to a wallet (false if the amount is zero, less, or passes the maximum balance)
    public boolean deposit(int index, double amount) {
        if (amount <= 0 || !canHold(index, amount)) return false;
        wallets[index] = round(wallets[index] + amount);
        return true;
    }

    // subtracts money out of a wallet (false if the amount is invalid or more than the balance)
    public boolean withdraw(int index, double amount) {
        if (amount <= 0 || amount > wallets[index]) return false;
        wallets[index] = round(wallets[index] - amount);
        return true;
    }

    // rounds a number to 2 decimal places
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

class SavingsAccount extends Account {
    // the yearly interest rate (5%)
    private static final double annualRate = 0.05;

    // creates a savings account with a PIN and a contact number
    public SavingsAccount(long number, String name, String pin, String contact, int walletCount) {
        super(number, name, pin, contact, walletCount);
    }

    // creates a savings account without a PIN and a contact number (an account found in the CSV file)
    public SavingsAccount(long number, String name, int walletCount) {
        super(number, name, walletCount);
    }

    public double getAnnualRate() {
        return annualRate;
    }

    public double getDailyRate() {
        return annualRate / 365;
    }

    // the total interest earned over a number of days starting from a balance 
    public static double expectedInterest(double startBalance, int days) {
        double balance = startBalance;
        for (int day = 1; day <= days; day++) {
            balance = balance + balance * annualRate / 365;
        }
        return round(balance - startBalance);
    }

    // creates a table of the interest and balance for each day (stops if the balance would pass the maximum)
    public ArrayList<double[]> projectDaily(double startBalance, int days) {
        ArrayList<double[]> rows = new ArrayList<>();
        double balance = startBalance;

        for (int day = 1; day <= days; day++) {
            double interest = round(balance * getDailyRate());
            if (balance + interest > Bank.MAX_BALANCE) break;
            balance = round(balance + interest);
            rows.add(new double[] { day, interest, balance });
        }
        return rows;
    }

    // creates a table of the interest and balance for each year (stops if the balance would pass the maximum)
    public ArrayList<double[]> projectAnnual(double startBalance, int years) {
        // the table (year, interest, balance)
        ArrayList<double[]> rows = new ArrayList<>();
        double balance = startBalance;

        for (int year = 1; year <= years; year++) {
            double interest = round(balance * annualRate);
            if (balance + interest > Bank.MAX_BALANCE) break;
            balance = round(balance + interest);
            rows.add(new double[] { year, interest, balance });
        }
        return rows;
    }
}

// the type of a transaction, the same as in the CSV file
enum TransactionType {
    DEPOSIT("deposit"),
    WITHDRAWAL("withdrawal");

    // the text saved in the CSV file
    private final String text;

    TransactionType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    // finds the type from its text (null if it is not deposit or withdrawal)
    public static TransactionType fromText(String text) {
        for (TransactionType t : values()) {
            if (t.text.equals(text)) {
                return t;
            }
        }
        return null;
    }
}

class Transaction {
    // formats used for date: dd/mm/yyyy and 24-hour hh:mm:ss 
    static final DateTimeFormatter DATE_OUT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_OUT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // the name of the account owner
    private final String accountName;
    // the 10-digit account number
    private final long accountNumber;
    // the date and time of the transaction
    private final LocalDateTime dateTime;
    // the currency code 
    private final String currency;
  
    private final double amount;
    // deposit or withdrawal
    private final TransactionType type;
    // the balance of this wallet after the transaction
    private final double currentBalance;
    // how the transaction is shown on the screens 
    private String label;
    // the other account involved in a transfer (0 if none)
    private long otherAccount;

    public Transaction(String accountName, long accountNumber, LocalDateTime dateTime, String currency,
                       double amount, TransactionType type, double currentBalance) {
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.dateTime = dateTime;
        this.currency = currency;
        this.amount = amount;
        this.type = type;
        this.currentBalance = currentBalance;
    }

    // sets the screen label and the other account
    public void setDetails(String label, long otherAccount) {
        this.label = label;
        this.otherAccount = otherAccount;
    }

    // the label shown on the screens
    public String getLabel() {
        if (label != null) {
            return label;
        }
        if (isDeposit()) {
            return "Deposit";
        }
        return "Withdraw";
    }

    public long getOtherAccount() {
        return otherAccount;
    }

    // text that finds this transaction again when the program starts 
    public String key() {
        return accountNumber + "|" + dateText() + "|" + timeText() + "|" + currency + "|" + type.getText() + "|"
                + Bank.money(amount) + "|" + Bank.money(currentBalance);
    }

    public String getAccountName() { 
        return accountName; 
    }

    public long getAccountNumber() { 
        return accountNumber; 
    }

    public LocalDateTime getDateTime() { 
        return dateTime; 
    }

    public LocalDate getDate() { 
        return dateTime.toLocalDate(); 
    }

    public LocalTime getTime() { 
        return dateTime.toLocalTime(); 
    }

    public String getCurrency() { 
        return currency; 
    }

    public double getAmount() { 
        return amount; 
    }

    public TransactionType getType() { 
        return type; 
    }

    public double getCurrentBalance() { 
        return currentBalance; 
    }

    public boolean isDeposit() { 
        return type == TransactionType.DEPOSIT; 
    }

    public boolean isWithdrawal() { 
        return type == TransactionType.WITHDRAWAL; 
    }

    // the date as dd/mm/yyyy text
    public String dateText() {
        return dateTime.format(DATE_OUT);
    }

    // the time as 24-hour hh:mm:ss text
    public String timeText() {
        return dateTime.format(TIME_OUT);
    }

    // one line of the CSV file 
    public String toCsvLine() {
        return ReportUtil.csv(accountName) + "," + accountNumber + "," + dateText() + "," + timeText() + ","
                + currency + "," + Bank.money(amount) + "," + type.getText() + "," + Bank.money(currentBalance);
    }
}


class Bank {
    // the limits of the system
    public static final int MAX_TRANSACTIONS = 1000000;
    public static final double MAX_BALANCE = 999999999999.99;
    public static final double MIN_CONVERTED = 0.0099999;
    // minimum initial deposit
    public static final double MIN_INITIAL_DEPOSIT = 5000.00;
    // account number of the very first account when the file is empty
    public static final long FIRST_ACCOUNT_NUMBER = 1092000001L;
    public static final long MAX_ACCOUNT_NUMBER = 9999999999L;

    // changes a number to text with 2 decimal places
    public static String money(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    // changes a number to text with 6 decimal places
    public static String rate(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    // changes an account number to text with 10 digits
    public static String accNo(long number) {
        return String.format("%010d", number);
    }

    // the open accounts
    private final ArrayList<SavingsAccount> accounts = new ArrayList<>();
    // every transaction of the file, oldest first
    private final ArrayList<Transaction> ledger = new ArrayList<>();
    // numbers of the accounts that were closed
    private ArrayList<Long> closedAccounts = new ArrayList<Long>();

    // the currencies with their starting rates compared to PHP
    private final Currency[] currencies = {
        new Currency("PHP", "Philippine Peso", 1.00),
        new Currency("USD", "United States Dollar", 62.00),
        new Currency("JPY", "Japanese Yen", 0.40),
        new Currency("GBP", "British Pound Sterling", 84.00),
        new Currency("EUR", "Euro", 72.00),
        new Currency("CNY", "Chinese Yuan Renminbi", 9.00)
    };

    // the base currency (PHP at the start)
    private Currency baseCurrency = currencies[0];
    // the highest account number found in the file (0 if the file has no accounts)
    private long highestAccountNumber = 0;
    // false if a data file could not be read 
    private boolean dataOk = true;

    // the name of the CSV file with the transactions
    private final String csvPath;
    // adds the new transactions to the CSV file
    private final TransactionCsvStore csvStore;
    // reads and writes the rates, profiles and other small files
    private final SettingsStore settings = new SettingsStore();

    // creates the bank that reads and writes the given CSV file
    public Bank(String csvFileName) {
        this.csvPath = csvFileName;
        this.csvStore = new TransactionCsvStore(csvPath);
    }

    public String getCsvPath() { 
        return csvPath; 
    }

    public Currency[] getCurrencies() { 
        return currencies; 
    }

    public Currency getBaseCurrency() { 
        return baseCurrency; 
    }

    public ArrayList<SavingsAccount> getAccounts() { 
        return accounts; 
    }

    public boolean isDataOk() { 
        return dataOk; 
    }

    // reads the CSV file and the rates file and rebuilds the accounts. 
    public IngestionResult load() {
        IngestionResult result;
        try {
            result = DataIngestion.ingest(csvPath);
        } catch (IOException e) {
            dataOk = false;
            result = new IngestionResult(false, 0, new ArrayList<Transaction>(), new ArrayList<String>());
        }
        if (!settings.loadRates(this)) {
            dataOk = false;
        }
        closedAccounts = settings.loadClosedAccounts();

        buildFromLedger(result.getValidRows());

        // the PIN, contact number and name of each account, and the labels of the transfers and exchanges
        if (!settings.loadProfiles(this)) {
            dataOk = false;
        }
        Map<String, String[]> details = settings.loadDetails();
        for (Transaction t : ledger) {
            String[] d = details.get(t.key());
            if (d != null) {
                t.setDetails(d[0], Long.parseLong(d[1]));
            }
        }
        return result;
    }

    // rebuilds the ledger, the accounts and the wallet balances from the valid rows of the file
    private void buildFromLedger(List<Transaction> rows) {
        ledger.clear();
        ledger.addAll(rows);

        accounts.clear();
        // all the accounts found in the file, in the order they first appear
        Map<Long, SavingsAccount> found = new LinkedHashMap<>();
        highestAccountNumber = 0;

        for (Transaction t : ledger) {
            if (t.getAccountNumber() > highestAccountNumber) {
                highestAccountNumber = t.getAccountNumber();
            }

            // gets the account of this row 
            SavingsAccount account = found.computeIfAbsent(t.getAccountNumber(),
                    number -> new SavingsAccount(number, t.getAccountName(), currencies.length));

            // the newest row has the newest name and the newest balance of that wallet
            account.setName(t.getAccountName());
            int index = currencyIndex(t.getCurrency());
            if (index >= 0) {
                account.setWallet(index, t.getCurrentBalance());
            }
        }

        for (SavingsAccount a : found.values()) {
            if (!closedAccounts.contains(a.getNumber())) {
                accounts.add(a);
            }
        }
    }

    // the number of a currency
    public int currencyIndex(String code) {
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].getCode().equals(code)) return i;
        }
        return -1;
    }

    // looks for a currency by its code (null if not found)
    public Currency findCurrency(String code) {
        int index = currencyIndex(code);
        return index < 0 ? null : currencies[index];
    }

    // changes the base currency and updates every rate to match
    public void setBaseCurrency(Currency newBase) {
        double divisor = newBase.getRate();
        for (Currency c : currencies) {
            c.setRate(c.getRate() / divisor);
        }
        newBase.setRate(1.00);
        baseCurrency = newBase;
    }

    // sets the base currency without changing the rates
    public void restoreBaseCurrency(Currency base) {
        baseCurrency = base;
    }

    // converts an amount from one currency to another
    public double convertRaw(double amount, Currency from, Currency to) {
        return amount * from.getRate() / to.getRate();
    }

    // saves the PIN, contact number and name of every account
    public void saveProfiles() {
        if (dataOk) {
            settings.saveProfiles(this);
        }
    }

    // saves the rates
    public void saveRates() {
        if (dataOk) {
            settings.saveRates(this);
        }
    }

    // text of a currency 
    public String currencyText(Currency c) {
        return c.getName() + " (" + c.getCode() + ")";
    }

    // text of a wallet
    public String walletText(Account a, int index) {
        return money(a.getWallet(index)) + " " + currencies[index].getCode();
    }

    // looks for an account by account number (null if not found)
    public SavingsAccount findByNumber(long number) {
        for (SavingsAccount a : accounts) {
            if (a.getNumber() == number) return a;
        }
        return null;
    }

    // seraches all accounts with the same name
    public List<SavingsAccount> findByName(String name) {
        List<SavingsAccount> found = new ArrayList<>();
        for (SavingsAccount a : accounts) {
            if (a.getName().equalsIgnoreCase(name)) found.add(a);
        }
        return found;
    }

    // checks if no more accounts can be registered
    public boolean isFull() {
        return highestAccountNumber >= MAX_ACCOUNT_NUMBER;
    }

    // registers a new account
    public SavingsAccount register(String name, String pin, String contact, double initialDeposit) {
        long number = (highestAccountNumber == 0) ? FIRST_ACCOUNT_NUMBER : highestAccountNumber + 1;
        highestAccountNumber = number;

        SavingsAccount account = new SavingsAccount(number, name, pin, contact, currencies.length);
        accounts.add(account);
        saveProfiles();

        int php = currencyIndex("PHP");
        account.deposit(php, initialDeposit);
        record(account, php, TransactionType.DEPOSIT, initialDeposit);
        return account;
    }

    // removes an account 
    public void closeAccount(SavingsAccount account) {
        accounts.remove(account);
        closedAccounts.add(account.getNumber());
        if (dataOk) {
            settings.addClosedAccount(account.getNumber());
        }
    }

    // adds money to a wallet and saves the transaction (false if the amount is not allowed)
    public boolean deposit(SavingsAccount account, int index, double amount) {
        if (!account.deposit(index, amount)) return false;
        record(account, index, TransactionType.DEPOSIT, amount);
        return true;
    }

    // takes money out of a wallet and saves the transaction (false if the amount is not allowed)
    public boolean withdraw(SavingsAccount account, int index, double amount) {
        if (!account.withdraw(index, amount)) return false;
        record(account, index, TransactionType.WITHDRAWAL, amount);
        return true;
    }

    // send money to another account
    public boolean transfer(SavingsAccount from, SavingsAccount to, int index, double amount) {
        if (amount > from.getWallet(index) || !to.canHold(index, amount)) return false;
        from.withdraw(index, amount);
        record(from, index, TransactionType.WITHDRAWAL, amount, "Sent", to.getNumber());
        to.deposit(index, amount);
        record(to, index, TransactionType.DEPOSIT, amount, "Received", from.getNumber());
        return true;
    }

    // exchange a currency from the same account
    public boolean exchange(SavingsAccount account, int sourceIndex, int targetIndex, double amount, double result) {
        if (amount > account.getWallet(sourceIndex) || !account.canHold(targetIndex, result)) return false;
        account.withdraw(sourceIndex, amount);
        record(account, sourceIndex, TransactionType.WITHDRAWAL, amount, "Convert Out", 0);
        account.deposit(targetIndex, result);
        record(account, targetIndex, TransactionType.DEPOSIT, result, "Convert In", 0);
        return true;
    }

    // records the transaction
    private void record(SavingsAccount account, int index, TransactionType type, double amount) {
        record(account, index, type, amount, null, 0);
    }

    private void record(SavingsAccount account, int index, TransactionType type, double amount, String label, long other) {
        Transaction t = new Transaction(account.getName(), account.getNumber(),
                LocalDateTime.now().withNano(0), currencies[index].getCode(), amount, type,
                account.getWallet(index));
        t.setDetails(label, other);
        ledger.add(t);
        if (dataOk) {
            if (!csvStore.append(t)) {
                System.err.println("Warning: could not save the transaction to " + csvPath + ".");
            } else if (label != null) {
                settings.appendDetail(t);
            }
        }
    }

    // checks if the ledger can still hold more records
    public boolean historyHasRoom(int needed) {
        return ledger.size() + needed <= MAX_TRANSACTIONS;
    }

    // all the transactions of every account in the order of the file 
    public List<Transaction> getLedger() {
        return ledger;
    }

    // all the transactions of one account, oldest first
    public List<Transaction> historyOf(Account account) {
        List<Transaction> list = new ArrayList<>();
        for (Transaction t : ledger) {
            if (t.getAccountNumber() == account.getNumber()) list.add(t);
        }
        return list;
    }
}

class DataIngestion {
    // the number of columns in each row of the CSV file
    private static final int COLUMNS = 8;
    // the names of the columns
    private static final String[] HEADER = { "account_name", "account_number", "date", "time", "currency",
            "amount", "type", "current_balance" };

    private static final DateTimeFormatter DATE_IN =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_IN =
            DateTimeFormatter.ofPattern("H:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    public static IngestionResult ingest(String fileName) throws IOException {
        // the rows that passed the checks
        List<Transaction> valid = new ArrayList<>();
        // one message for each row that was skipped
        List<String> errors = new ArrayList<>();

        if (!new File(fileName).exists()) {
            return new IngestionResult(false, 0, valid, errors);
        }

        // counts every data row of the file
        int total = 0;
        // InputStreamReader replaces bad characters instead of failing the whole file
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8))) {
            String line;
            int lineNo = 0;
            boolean firstDataLine = true;

            while ((line = br.readLine()) != null) {
                lineNo++;
                // removes the BOM that some programs put at the start of a file
                if (lineNo == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                if (line.trim().isEmpty()) continue;

                // splits the line into its columns
                String[] fields = splitCsv(line);

                // skips the header line
                if (firstDataLine) {
                    firstDataLine = false;
                    if (fields[0].trim().equalsIgnoreCase(HEADER[0])) continue;
                }

                // counts the row, then checks a bad row
                total++;
                try {
                    valid.add(parseRow(fields));
                } catch (IllegalArgumentException e) {
                    errors.add("Line " + lineNo + ": " + e.getMessage());
                }
            }
        }
        return new IngestionResult(true, total, valid, errors);
    }

    // validates one row and converts it
    private static Transaction parseRow(String[] f) {
        if (f.length != COLUMNS) {
            throw new IllegalArgumentException("expected " + COLUMNS + " columns but found " + f.length);
        }
        // missing values
        for (int i = 0; i < COLUMNS; i++) {
            if (f[i].trim().isEmpty()) {
                throw new IllegalArgumentException("missing value for '" + HEADER[i] + "'");
            }
        }

        // the name with the extra spaces removed
        String name = f[0].trim().replaceAll("\\s+", " ");

        // the account number must be exactly 10 digits
        String numberText = f[1].trim();
        if (!numberText.matches("\\d{10}")) {
            throw new IllegalArgumentException("invalid account_number '" + numberText + "' (must be 10 digits)");
        }
        long number = Long.parseLong(numberText);

        // the date must be a real date (dd/mm/yyyy)
        LocalDate date;
        try {
            date = LocalDate.parse(f[2].trim(), DATE_IN);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid date '" + f[2].trim() + "' (expected dd/mm/yyyy)");
        }

        // the time must be hh:mm:ss
        LocalTime time;
        try {
            time = LocalTime.parse(f[3].trim(), TIME_IN);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid time '" + f[3].trim() + "' (expected hh:mm:ss)");
        }

        // the currency must be 3 letters
        String currency = f[4].trim().toUpperCase();
        if (!currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("invalid currency '" + f[4].trim() + "'");
        }

        // the amount must be greater than zero
        double amount = parseMoney(f[5], "amount");
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero ('" + f[5].trim() + "')");
        }

        // the type must be deposit or withdrawal
        TransactionType type = TransactionType.fromText(f[6].trim().toLowerCase());
        if (type == null) {
            throw new IllegalArgumentException("invalid type '" + f[6].trim() + "' (deposit or withdrawal)");
        }

        // the balance after the transaction
        double balance = parseMoney(f[7], "current_balance");

        return new Transaction(name, number, LocalDateTime.of(date, time), currency, amount, type, balance);
    }

    // converts a money text to a number rounded to 2 decimals 
    private static double parseMoney(String raw, String column) {
        String text = raw.trim().replace(",", "");
        if (!text.matches("\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException("invalid " + column + " '" + raw.trim() + "'");
        }
        double value = Double.parseDouble(text);
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new IllegalArgumentException("invalid " + column + " '" + raw.trim() + "'");
        }
        return Account.round(value);
    }

    // splits one CSV line into fields
    static String[] splitCsv(String line) {
        // the columns found so far
        List<String> fields = new ArrayList<>();
        // the text of the column that is being read
        StringBuilder current = new StringBuilder();
        // true while inside quotes 
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            // the character that is being checked
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        // adds the last column
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}

class IngestionResult {
    private final boolean fileFound;
    // data rows read from the file (header and blank lines are not counted)
    private final int totalRows;
    // rows that passed validation, with proper data types (all currencies)
    private final List<Transaction> validRows;
    // one message per rejected row 
    private final List<String> errors;
    // the valid rows in Philippine Peso only 
    private final List<Transaction> phpRows = new ArrayList<Transaction>();

    // saves what the ingestion found and keeps the PHP rows in their own list
    public IngestionResult(boolean fileFound, int totalRows, List<Transaction> validRows, List<String> errors) {
        this.fileFound = fileFound;
        this.totalRows = totalRows;
        this.validRows = validRows;
        this.errors = errors;
        for (Transaction t : validRows) {
            if (t.getCurrency().equals("PHP")) {
                phpRows.add(t);
            }
        }
    }

    public boolean isFileFound() {
        return fileFound;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public List<Transaction> getValidRows() {
        return validRows;
    }

    public List<String> getErrors() {
        return errors;
    }

    public int getInvalidCount() {
        return errors.size();
    }

    // the filtered PHP rows that the reports use
    public List<Transaction> getPhpRows() {
        return phpRows;
    }

    // valid rows that were dropped because they are not in PHP
    public int getNonPhpCount() {
        return validRows.size() - phpRows.size();
    }
}

class TransactionCsvStore {
    // the first line of a new CSV file
    private static final String HEADER = "account_name,account_number,date,time,currency,amount,type,current_balance";

    // the CSV file where the transactions are added
    private final String fileName;

    // sets the CSV file where the transactions are added
    public TransactionCsvStore(String fileName) {
        this.fileName = fileName;
    }

    // adds one transaction line (creates the file with a header if it does not exist). False if it failed.
    public boolean append(Transaction t) {
        try {
            // the CSV file
            File file = new File(fileName);
            // false when the file does not exist yet or is empty
            boolean hasContent = file.exists() && file.length() > 0;

            // the end of line used by the file (the dataset uses \r\n)
            String eol = System.lineSeparator();
            String text = "";

            if (!hasContent) {
                text = HEADER + eol;
            } else {
                // reads the file to find its line ending
                byte[] bytes = Files.readAllBytes(file.toPath());
                eol = findLineEnding(bytes);

                // if the last line had no line ending, add one first
                byte last = bytes[bytes.length - 1];
                if (last != '\n' && last != '\r') {
                    text = eol;
                }
            }
            text = text + t.toCsvLine() + eol;

            // true = add to the end of the file
            FileOutputStream out = new FileOutputStream(file, true);
            try {
                out.write(text.getBytes(StandardCharsets.UTF_8));
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // looks at the start of the file to see if lines end with \r\n or \n
    private String findLineEnding(byte[] bytes) {
        int limit = Math.min(bytes.length, 2048);
        for (int i = 0; i < limit; i++) {
            if (bytes[i] == '\n') {
                if (i > 0 && bytes[i - 1] == '\r') {
                    return "\r\n";
                }
                return "\n";
            }
        }
        return System.lineSeparator();
    }
}

class SettingsStore {
    // the names of the side files
    private static final String RATES_FILE = "rates.txt";
    private static final String CLOSED_FILE = "closed_accounts.txt";
    private static final String PROFILES_FILE = "account_profiles.txt";
    private static final String DETAILS_FILE = "transaction_details.txt";

    // reads the rates file into the bank (false if the file could not be read correctly)
    public boolean loadRates(Bank bank) {
        File file = new File(RATES_FILE);
        // a missing file is not an error (there is nothing to load yet)
        if (!file.exists()) return true;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String first = br.readLine();
            if (first == null || first.trim().isEmpty()) return true;

            // line 1 is the code of the base currency
            Currency base = bank.findCurrency(first.trim());
            if (base == null) return false;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // each line is a currency code and its rate
                String[] p = line.trim().split("\\s+");
                if (p.length != 2) return false;

                Currency c = bank.findCurrency(p[0]);
                if (c == null || !c.setRate(Double.parseDouble(p[1]))) return false;
            }

            bank.restoreBaseCurrency(base);
            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }

    // writes the base currency and all the exchange rates to the rates file
    public void saveRates(Bank bank) {
        try (PrintWriter out = new PrintWriter(new FileWriter(RATES_FILE))) {
            out.println(bank.getBaseCurrency().getCode());
            for (Currency c : bank.getCurrencies()) {
                out.println(c.getCode() + " " + c.getRate());
            }
        } catch (IOException e) {
            System.err.println("Warning: could not save " + RATES_FILE + ".");
        }
    }

    // reads the account numbers of closed accounts 
    public ArrayList<Long> loadClosedAccounts() {
        // the numbers of the closed accounts
        ArrayList<Long> closed = new ArrayList<Long>();
        File file = new File(CLOSED_FILE);
        // a missing file is not an error (there is nothing to load yet)
        if (!file.exists()) return closed;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.matches("\\d{10}")) {
                    closed.add(Long.parseLong(line));
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read " + CLOSED_FILE + ".");
        }
        return closed;
    }

    // reads the PIN, contact number and name of each account into the bank (false if the file is not correct)
    public boolean loadProfiles(Bank bank) {
        File file = new File(PROFILES_FILE);
        // a missing file is not an error (there is nothing to load yet)
        if (!file.exists()) return true;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // each line is the number, PIN, contact number and name of an account
                String[] p = line.trim().split("\\s+", 4);
                if (p.length < 3) return false;

                // an account that is not open anymore (closed) is skipped
                SavingsAccount account = bank.findByNumber(Long.parseLong(p[0]));
                if (account == null) continue;

                if (!p[1].equals("-")) account.setPin(p[1]);
                if (!p[2].equals("-")) account.setContact(p[2]);
                if (p.length == 4) account.setName(p[3]);
            }
            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }

    // writes the PIN, contact number and name of all the open accounts
    public void saveProfiles(Bank bank) {
        try (PrintWriter out = new PrintWriter(new FileWriter(PROFILES_FILE))) {
            for (SavingsAccount a : bank.getAccounts()) {
                String pin = a.getPin();
                if (pin.equals("")) pin = "-";
                String contact = a.getContact();
                if (contact.equals("")) contact = "-";
                out.println(a.getNumber() + " " + pin + " " + contact + " " + a.getName());
            }
        } catch (IOException e) {
            System.err.println("Warning: could not save " + PROFILES_FILE + ".");
        }
    }

    public Map<String, String[]> loadDetails() {
        // the key finds the transaction again, the value is its label and the other account
        Map<String, String[]> details = new HashMap<>();
        File file = new File(DETAILS_FILE);
        // a missing file is not an error 
        if (!file.exists()) return details;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length != 9) continue;
                String key = p[0] + "|" + p[1] + "|" + p[2] + "|" + p[3] + "|" + p[4] + "|" + p[5] + "|" + p[6];
                details.put(key, new String[] { p[7], p[8] });
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read " + DETAILS_FILE + ".");
        }
        return details;
    }

    // adds the label and the other account of one transaction to the details file
    public void appendDetail(Transaction t) {
        try (PrintWriter out = new PrintWriter(new FileWriter(DETAILS_FILE, true))) {
            out.println(t.key() + "|" + t.getLabel() + "|" + t.getOtherAccount());
        } catch (IOException e) {
            System.err.println("Warning: could not save " + DETAILS_FILE + ".");
        }
    }

    // remembers that an account was closed
    public void addClosedAccount(long accountNumber) {
        try (PrintWriter out = new PrintWriter(new FileWriter(CLOSED_FILE, true))) {
            out.println(accountNumber);
        } catch (IOException e) {
            System.err.println("Warning: could not save " + CLOSED_FILE + ".");
        }
    }
}

class ReportResult {
    String title;
    String[] header;
    List<String[]> rows;
    String fileName;
    // an extra message for the screen (null if there is none)
    String note;

    // creates the result of a report
    ReportResult(String title, String[] header, List<String[]> rows, String fileName, String note) {
        this.title = title;
        this.header = header;
        this.rows = rows;
        this.fileName = fileName;
        this.note = note;
    }
}

interface ReportGenerator {

    // the heading shown in the menu
    String getTitle();

    // the CSV file the report is written to
    String getOutputFileName();

    ReportResult generate(IngestionResult data) throws IOException;
}

class ReportUtil {

    // changes an amount in pesos to whole centavos
    static long cents(double amount) {
        return Math.round(amount * 100.0);
    }

    // changes centavos to comma-formatted text with 2 decimals
    static String money(long cents) {
        return String.format(Locale.US, "%,d.%02d", cents / 100, cents % 100);
    }

    // changes a date to dd/mm/yyyy text
    static String dateText(LocalDate date) {
        return date.format(Transaction.DATE_OUT);
    }

    // wraps a text in quotes if it has a comma or a quote
    static String csv(String text) {
        if (text.contains(",") || text.contains("\"")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    // puts the fields of one row in one CSV line
    private static String joinCsv(String[] fields) {
        return Arrays.stream(fields).map(ReportUtil::csv).collect(Collectors.joining(","));
    }

    // writes the full table to a CSV file (a header-only file if there are no rows)
    static void writeCsv(String fileName, String[] header, List<String[]> rows) throws IOException {
        try (PrintWriter out = new PrintWriter(fileName, "UTF-8")) {
            out.println(joinCsv(header));
            for (String[] row : rows) {
                out.println(joinCsv(row));
            }
        }
    }

    // groups the rows by account 
    static Map<Long, List<Transaction>> groupByAccount(List<Transaction> rows) {
        return rows.stream().collect(
                Collectors.groupingBy(Transaction::getAccountNumber, LinkedHashMap::new, Collectors.toList()));
    }

    // the newest date in the data
    static LocalDate newestDate(List<Transaction> rows) {
        return rows.stream().map(Transaction::getDate).max(Comparator.naturalOrder()).get();
    }

    // the newest row of each account
    static List<Transaction> latestRowOfEachAccount(List<Transaction> rows) {
        List<Transaction> latest = new ArrayList<>();
        for (List<Transaction> group : groupByAccount(rows).values()) {
            latest.add(latestRow(group));
        }
        return latest;
    }

    // the row with the newest date and time (the later row in the file when the time is the same)
    static Transaction latestRow(List<Transaction> rows) {
        Transaction best = rows.get(0);
        for (Transaction t : rows) {
            if (!t.getDateTime().isBefore(best.getDateTime())) {
                best = t;
            }
        }
        return best;
    }

    // the row with the oldest date and time (the earlier row in the file when the time is the same)
    static Transaction earliestRow(List<Transaction> rows) {
        Transaction best = rows.get(0);
        for (Transaction t : rows) {
            if (t.getDateTime().isBefore(best.getDateTime())) {
                best = t;
            }
        }
        return best;
    }
}


// the privilege tier of Report 1
enum PrivilegeTier {
    SILVER("Silver"),
    GOLD("Gold"),
    PLATINUM("Platinum");

    // the text shown in the report
    private final String text;

    PrivilegeTier(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

// one row of Report 1 
record TierRow(String name, long ageDays, long balanceCents, long sumCents, int daysInMonth, PrivilegeTier tier,
                long accountNumber) {
}

class Report1BankingPrivilegeTiers implements ReportGenerator {

    // the limits in centavos 
    static final long MIN_AVERAGE_CENTS = 10000000L;
    static final long GOLD_FROM_CENTS = 50000000L;
    static final long PLATINUM_FROM_CENTS = 100000000L;

    // the names of the columns
    private static final String[] HEADER =
            { "AccountName", "AccountAge", "CurrentBalance", "AverageDailyBalance", "PrivilegeTier" };

    // the order of the rows
    private static final Comparator<TierRow> ORDER =
            Comparator.comparingLong(TierRow::sumCents).reversed().thenComparingLong(TierRow::accountNumber);

    // the heading of the report
    public String getTitle() {
        return "Report 1: Banking Privilege Tiers";
    }

    // the file the report is saved to
    public String getOutputFileName() {
        return "report1_banking_privilege_tiers.csv";
    }

    // builds the report, saves the full table to its file and returns the table for the preview
    public ReportResult generate(IngestionResult data) throws IOException {
        // finds the accounts that pass the filter and sorts them
        List<TierRow> result = buildRows(data.getPhpRows());
        result.sort(ORDER);

        // the full table of the report (each row is one line of the file)
        List<String[]> table = new ArrayList<String[]>();
        for (TierRow r : result) {
            // the average in centavos, rounded to the nearest centavo
            long averageCents = Math.round((double) r.sumCents() / r.daysInMonth());
            table.add(new String[] { r.name(), String.valueOf(r.ageDays()), ReportUtil.money(r.balanceCents()),
                    ReportUtil.money(averageCents), r.tier().getText() });
        }
        // saves the full table to the report file
        ReportUtil.writeCsv(getOutputFileName(), HEADER, table);
        return new ReportResult(getTitle(), HEADER, table, getOutputFileName(), null);
    }

    // finds the accounts that pass the filter and works out their tier
    private List<TierRow> buildRows(List<Transaction> phpRows) {
        List<TierRow> result = new ArrayList<TierRow>();
        if (phpRows.isEmpty()) {
            return result;
        }

        // the reference date is the newest date in the data, and the month is the calendar month before it
        LocalDate reference = ReportUtil.newestDate(phpRows);
        LocalDate monthStart = reference.withDayOfMonth(1).minusMonths(1);
        int daysInMonth = monthStart.lengthOfMonth();

        for (List<Transaction> list : ReportUtil.groupByAccount(phpRows).values()) {
            // the day the account was opened
            LocalDate opened = list.stream().map(Transaction::getDate).min(Comparator.naturalOrder()).get();

            // adds up the end of day balance of every day of the month
            long sum = 0;
            for (int day = 0; day < daysInMonth; day++) {
                sum = sum + endOfDayCents(list, monthStart.plusDays(day));
            }

            // keeps only average >= 100,000.00
            if (sum < MIN_AVERAGE_CENTS * daysInMonth) {
                continue;
            }

            // the name comes from the first transaction of the account and the balance from the last one
            Transaction first = ReportUtil.earliestRow(list);
            Transaction last = ReportUtil.latestRow(list);
            result.add(new TierRow(first.getAccountName(), ChronoUnit.DAYS.between(opened, reference),
                    ReportUtil.cents(last.getCurrentBalance()), sum, daysInMonth, tierOf(sum, daysInMonth),
                    last.getAccountNumber()));
        }
        return result;
    }

    // the balance at the end of the day (the last balance up to that day, or 0 if there is none yet)
    private long endOfDayCents(List<Transaction> list, LocalDate date) {
        return list.stream()
                .filter(t -> !t.getDate().isAfter(date))
                // keeps the newer date and time, and the later row when two rows have the same time
                .reduce((a, b) -> b.getDateTime().isBefore(a.getDateTime()) ? a : b)
                .map(t -> ReportUtil.cents(t.getCurrentBalance()))
                .orElse(0L);
    }

    // the tier of an account from the sum of its  end of day balances 
    private PrivilegeTier tierOf(long sum, int daysInMonth) {
        if (sum >= PLATINUM_FROM_CENTS * daysInMonth) {
            return PrivilegeTier.PLATINUM;
        }
        if (sum >= GOLD_FROM_CENTS * daysInMonth) {
            return PrivilegeTier.GOLD;
        }
        return PrivilegeTier.SILVER;
    }
}


// one row of Report 2 
record InterestRow(String name, long balanceCents, long interestCents, long accountNumber) {
}

class Report2InterestExpenseProjection implements ReportGenerator {

    // the names of the columns
    private static final String[] HEADER = { "AccountName", "CurrentBalance", "ExpectedInterest" };

    // the order of the rows
    private static final Comparator<InterestRow> ORDER =
            Comparator.comparingLong(InterestRow::interestCents).reversed().thenComparingLong(InterestRow::accountNumber);

    // the heading of the report
    public String getTitle() {
        return "Report 2: Interest Expense Projection";
    }

    // the file the report is saved to
    public String getOutputFileName() {
        return "report2_interest_expense_projection.csv";
    }

    // builds the report, saves the full table to its file and returns the table for the preview
    public ReportResult generate(IngestionResult data) throws IOException {
        // the latest row of each account
        List<Transaction> latest = ReportUtil.latestRowOfEachAccount(data.getPhpRows());
        // all the rows of each account (the name comes from the first one)
        Map<Long, List<Transaction>> groups = ReportUtil.groupByAccount(data.getPhpRows());

        // keeps only the accounts with a balance of 10,000.00 or more
        List<InterestRow> rows = new ArrayList<InterestRow>();
        for (Transaction last : latest) {
            // the latest balance of the account
            double balance = last.getCurrentBalance();
            if (balance < SummaryJsonWriter.INTEREST_MIN_BALANCE) {
                continue;
            }

            // the name, balance and expected interest of the account 
            rows.add(new InterestRow(ReportUtil.earliestRow(groups.get(last.getAccountNumber())).getAccountName(),
                    ReportUtil.cents(balance), ReportUtil.cents(
                    SavingsAccount.expectedInterest(balance, SummaryJsonWriter.INTEREST_DAYS)),
                    last.getAccountNumber()));
        }
        rows.sort(ORDER);

        // the full table of the report
        List<String[]> table = new ArrayList<String[]>();
        for (InterestRow r : rows) {
            table.add(new String[] { r.name(), ReportUtil.money(r.balanceCents()), ReportUtil.money(r.interestCents()) });
        }
        // saves the full table to the report file
        ReportUtil.writeCsv(getOutputFileName(), HEADER, table);
        return new ReportResult(getTitle(), HEADER, table, getOutputFileName(), null);
    }
}


// one row of Report 3 
record DailyRow(long accountNumber, String name, LocalDate date, long withdrawalCents, int depositCount,
                long depositCents, boolean suspicious) {
}

// the account and the day that a row of Report 3 belongs to
record DayKey(long accountNumber, LocalDate date) {
}

class Report3SuspiciousClientBehavior implements ReportGenerator {

    // 500,000.00 in centavos
    static final long WITHDRAWAL_LIMIT_CENTS = 50000000L;
    // true flags a day with withdrawals but no deposits
    static final boolean FLAG_WITHDRAWAL_WITHOUT_DEPOSIT = false;

    // the names of the columns
    private static final String[] HEADER = { "AccountName", "Date", "TotalWithdrawalAmounts", "NumberOfDeposits",
            "TotalDepositAmounts", "TurnoverVelocity(%)", "RiskFlagLevel" };

    // the order of the rows
    private static final Comparator<DailyRow> ORDER = Comparator.comparing(DailyRow::date)
            .thenComparing(DailyRow::suspicious, Comparator.reverseOrder())
            .thenComparing(DailyRow::name);

    // the heading of the report
    public String getTitle() {
        return "Report 3: Suspicious Client Behavior";
    }

    // the file the report is saved to
    public String getOutputFileName() {
        return "report3_suspicious_client_behavior.csv";
    }

    // builds the report, saves the full table to its file and returns the table for the preview
    public ReportResult generate(IngestionResult data) throws IOException {
        // groups the rows by account and day (in the order each account and day first appears)
        Map<DayKey, List<Transaction>> days = data.getPhpRows().stream().collect(Collectors.groupingBy(
                t -> new DayKey(t.getAccountNumber(), t.getDate()), LinkedHashMap::new, Collectors.toList()));

        // makes one row for each account and day
        List<DailyRow> rows = new ArrayList<DailyRow>();
        for (Map.Entry<DayKey, List<Transaction>> day : days.entrySet()) {
            rows.add(makeRow(day.getKey(), day.getValue()));
        }
        rows.sort(ORDER);

        // the full table of the report 
        List<String[]> table = new ArrayList<String[]>();
        for (DailyRow r : rows) {
            // the risk flag shown in the table
            String flag = r.suspicious() ? "Suspicious" : "Normal";
            table.add(new String[] { r.name(), ReportUtil.dateText(r.date()), ReportUtil.money(r.withdrawalCents()),
                    String.valueOf(r.depositCount()), ReportUtil.money(r.depositCents()), velocityText(r), flag });
        }
        // saves the full table to the report file
        ReportUtil.writeCsv(getOutputFileName(), HEADER, table);
        return new ReportResult(getTitle(), HEADER, table, getOutputFileName(), null);
    }

    // adds up the deposits and withdrawals of one account on one day and marks the day
    private DailyRow makeRow(DayKey key, List<Transaction> list) {
        int depositCount = 0;
        long depositCents = 0;
        long withdrawalCents = 0;
        for (Transaction t : list) {
            long cents = ReportUtil.cents(t.getAmount());
            if (t.isDeposit()) {
                depositCount++;
                depositCents = depositCents + cents;
            } else if (t.isWithdrawal()) {
                withdrawalCents = withdrawalCents + cents;
            }
        }
        // the last row of the day has the newest name of the account
        String name = list.get(list.size() - 1).getAccountName();
        return new DailyRow(key.accountNumber(), name, key.date(), withdrawalCents, depositCount, depositCents,
                isSuspicious(withdrawalCents, depositCents));
    }

    // the risk rule: withdrawals >= 500,000.00 OR velocity >= 90%
    private boolean isSuspicious(long withdrawalCents, long depositCents) {
        if (withdrawalCents >= WITHDRAWAL_LIMIT_CENTS) {
            return true;
        }
        if (depositCents == 0) {
            return FLAG_WITHDRAWAL_WITHOUT_DEPOSIT && withdrawalCents > 0;
        }
        // withdrawal / deposit x 100 >= 90  
        return withdrawalCents * 10 >= depositCents * 9;
    }

    // the velocity in percent, rounded to 2 decimals ("N/A" when there are no deposits)
    private String velocityText(DailyRow r) {
        if (r.depositCents() == 0) {
            return "N/A";
        }
        // the percent in hundredths (example: 12.34% = 1234), rounded to the nearest hundredth
        long hundredths = Math.round((double) (r.withdrawalCents() * 10000L) / r.depositCents());
        return ReportUtil.money(hundredths);
    }
}


// one row of Report 4
record DormantRow(String name, LocalDate lastDate, long daysInactive, double balance) {
}

class Report4DormantAccounts implements ReportGenerator {

    // an account is dormant after this many days without a transaction.
    static final int DORMANT_AFTER_DAYS = 7;

    // the names of the columns
    private static final String[] HEADER = { "AccountName", "LastTransactionDate", "DaysInactive", "CurrentBalance" };

    // the order of the rows: most inactive first; the name breaks ties so the file is the same on every run
    private static final Comparator<DormantRow> ORDER =
            Comparator.comparingLong(DormantRow::daysInactive).reversed().thenComparing(DormantRow::name);

    // the heading of the report
    public String getTitle() {
        return "Report 4: Dormant Accounts";
    }

    // the file the report is saved to
    public String getOutputFileName() {
        return "report4_dormant_accounts.csv";
    }

    // builds the report, saves the full table to its file and returns the table for the preview
    public ReportResult generate(IngestionResult data) throws IOException {
        List<DormantRow> rows = buildRows(data.getPhpRows());
        rows.sort(ORDER);

        // the full table
        List<String[]> table = new ArrayList<String[]>();
        for (DormantRow r : rows) {
            table.add(new String[] { r.name(), ReportUtil.dateText(r.lastDate()), String.valueOf(r.daysInactive()),
                    String.format(Locale.US, "%,.2f", r.balance()) });
        }
        // saves the full table to the report file
        ReportUtil.writeCsv(getOutputFileName(), HEADER, table);

        String note = "Dormant = no transaction for " + DORMANT_AFTER_DAYS
                + " or more days (counted up to the latest date in the data).";
        return new ReportResult(getTitle(), HEADER, table, getOutputFileName(), note);
    }

    // finds the dormant accounts from the cleaned PHP rows
    private List<DormantRow> buildRows(List<Transaction> phpRows) {
        List<DormantRow> result = new ArrayList<DormantRow>();
        if (phpRows.isEmpty()) {
            return result;
        }

        // the reference date is the newest date in the data
        LocalDate reference = ReportUtil.newestDate(phpRows);

        for (List<Transaction> list : ReportUtil.groupByAccount(phpRows).values()) {
            // the newest transaction date of the account
            LocalDate lastDate = ReportUtil.newestDate(list);
            long days = ChronoUnit.DAYS.between(lastDate, reference);
            if (days >= DORMANT_AFTER_DAYS) {
                // the last row in file order has the latest balance and name
                Transaction last = list.get(list.size() - 1);
                result.add(new DormantRow(last.getAccountName(), lastDate, days, last.getCurrentBalance()));
            }
        }
        return result;
    }
}

class SummaryJsonWriter implements ReportGenerator {

    static final double INTEREST_MIN_BALANCE = 10000.00;
    static final int INTEREST_DAYS = 30;

    // the heading of the report
    public String getTitle() {
        return "Summary State (summary.json)";
    }

    // the file the report is saved to
    public String getOutputFileName() {
        return "summary.json";
    }

    // builds the report, saves the full table to its file and returns the table for the preview
    public ReportResult generate(IngestionResult data) throws IOException {
        List<Transaction> rows = data.getPhpRows();

        // the total of the deposits and of the withdrawals (in centavos)
        long depositCents = rows.stream().filter(Transaction::isDeposit)
                .mapToLong(t -> ReportUtil.cents(t.getAmount())).sum();
        long withdrawalCents = rows.stream().filter(Transaction::isWithdrawal)
                .mapToLong(t -> ReportUtil.cents(t.getAmount())).sum();

        // the latest row of each account
        List<Transaction> latest = ReportUtil.latestRowOfEachAccount(rows);

        // the total of the latest balances, and the expected interest of the accounts that are in Report 2
        long balanceCents = latest.stream().mapToLong(t -> ReportUtil.cents(t.getCurrentBalance())).sum();
        long interestCents = latest.stream().filter(t -> t.getCurrentBalance() >= INTEREST_MIN_BALANCE)
                .mapToLong(t -> ReportUtil.cents(
                        SavingsAccount.expectedInterest(t.getCurrentBalance(), INTEREST_DAYS)))
                .sum();

        // the summary written as JSON
        String json = """
                {
                  "total_accounts": %d,
                  "total_deposits": %s,
                  "total_withdrawals": %s,
                  "total_balance": %s,
                  "total_interest_expense": %s
                }""".formatted(latest.size(), plain(depositCents), plain(withdrawalCents), plain(balanceCents),
                plain(interestCents));

        try (PrintWriter out = new PrintWriter(getOutputFileName(), "UTF-8")) {
            out.println(json);
        }

        // the summary as one row so the screen can show it like the reports
        String[] keys = { "total_accounts", "total_deposits", "total_withdrawals", "total_balance",
                "total_interest_expense" };
        // the full table of the report (each row is one line of the file)
        List<String[]> table = new ArrayList<String[]>();
        table.add(new String[] { String.valueOf(latest.size()), plain(depositCents), plain(withdrawalCents),
                plain(balanceCents), plain(interestCents) });
        return new ReportResult(getTitle(), keys, table, getOutputFileName(), null);
    }

    // changes centavos to a plain number text with 2 decimals (no commas)
    private String plain(long cents) {
        return String.format(Locale.US, "%d.%02d", cents / 100, cents % 100);
    }
}
