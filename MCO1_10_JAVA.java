/******************************************
 
Lastnames:Bobadilla, Calvez, Casao, Sanico
Language: Java
Paradigm: Object Oriented Programming

*******************************************/

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class  MCO1_10_JAVA extends JFrame {

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
    // how many PIN tries the user gets
    static final int MAX_PIN_ATTEMPTS = 3;

    // the bank that stores the accounts, currencies and transactions
    Bank bank = new Bank();

    // the customer that is logged in (null if nobody is logged in)
    SavingsAccount currentAccount;
    // true while the admin is logged in
    boolean adminMode;
    // the account that is waiting for the correct PIN
    SavingsAccount loginAccount;
    // the account that receives money (send money)
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
    // the account the admin is updating
    SavingsAccount profileAccount;
    // temporary details used while registering
    String tempName;
    String tempPin;
    int pinTries;

    // checks the text and returns an error message (null if it is okay)
    interface InputChecker {
        String check(String text);
    }

    // runs when the user picks a currency (the number of the currency starting from 0)
    interface CurrencyPicked {
        void picked(int index);
    }

    // sets up the window and shows the login menu
    public  MCO1_10_JAVA() {
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

        // loads the saved accounts, rates and transactions from the files
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

    // changes a currency to text like Philippine Peso (PHP)
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

        // runs when a key on the screen is clicked (the key is the letter or number, or BACK to delete)
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

    // currency screen: shows 6 buttons, one for each currency (extra gives a third line for each button, can be null)
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

    // table screen: shows rows in a table (note is a message under the table, can be null)
    void showTable(String screenTitle, String[] columns, ArrayList<Object[]> rows, int[] widths, String note) {
        JPanel p = makeScreen(screenTitle, false);

        JPanel box = makeWhiteBox(140, 90, 440, 310);

        // each data in the table
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
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
        
        scroll.setBounds(25, 20, 390, tableHeight);
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
            noteLabel.setBounds(25, 20 + tableHeight + 6, 390, 40);
            box.add(noteLabel);
        }

        p.add(box);
        addBackButton(p);
        showScreen(p);
    }

    // error pop up: shows the error message with a BACK TO MENU button
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

    // login menu: shows the 4 buttons
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

    // customer menu: shows the 10 buttons
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

    // admin menu: shows the 5 buttons
    void showAdminMenu() {
        JPanel p = makeScreen("BANKING AND CURRENCY MACHINE", false);
        addSubtitle(p, "WELCOME, ADMIN");

        // 3 buttons on the left side
        addMenuButton(p, "RECORD\nEXCHANGE RATES", true, 150, 130, 165, 55, () -> startRecordRate());
        addMenuButton(p, "VIEW/UPDATE\nPROFILE", true, 150, 215, 165, 55, () -> startProfile());
        addMenuButton(p, "VIEW TRANSACTION\nHISTORY", true, 150, 300, 165, 55, () -> showAllTransactions());

        // 2 buttons on the right side
        addMenuButton(p, "CHANGE BASE\nCURRENCY", false, 405, 130, 165, 55, () -> startSetBase());
        addMenuButton(p, "LOGOUT", false, 405, 215, 165, 55, () -> logout());

        showScreen(p);
    }

    // logs out the customer or the admin and shows the login menu
    void logout() {
        currentAccount = null;
        adminMode = false;
        showLoginMenu();
    }


    // finds an account using the account number, null if not found
    SavingsAccount findByAccountText(String text) {
        if (!text.matches("\\d{5,6}")) {
            return null;
        }
        SavingsAccount found = bank.findByNumber(Integer.parseInt(text));
        if (found == null || !Bank.accNo(found.getNumber()).equals(text)) {
            return null;
        }
        return found;
    }

    // asks for the account number and finds the account
    void startLogin() {
        showInput("LOGIN", "ENTER ACCOUNT NUMBER", 2, 6, false, text -> {
            if (text.length() < 5) {
                return "Enter the full account number (example: 00001).";
            }
            // the account with that number (null if there is none)
            SavingsAccount found = findByAccountText(text);
            // if no account found, show the error pop up with the REGISTER ACCOUNT button
            if (found == null) {
                showError("LOGIN", "Account not found.", true);
                return null;
            }
            loginAccount = found;
            askLoginPin();
            return null;
        });
    }

    // asks for the PIN (3 tries) and opens the customer menu when it is correct
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

    // asks for the account name (letters, spaces and periods only)
    void startRegister() {
        if (bank.isFull()) {
            showError("REGISTER ACCOUNT", "The system is full. Cannot register a new account.", false);
            return;
        }
        showInput("REGISTER ACCOUNT", "ENTER NAME", 1, 30, false, text -> {
            if (!text.matches("[a-z. ]+") || !text.matches(".*[a-z].*")) {
                return "Use lowercase letters, spaces and periods only.";
            }
            tempName = text;
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

    // asks for a contact number of 10 to 15 digits, then creates the account
    void askRegisterContact() {
        showInput("REGISTER ACCOUNT", "ENTER CONTACT NUMBER", 2, 15, false, text -> {
            if (text.length() < 10 || text.length() > 15) {
                return "Contact number must be 10 to 15 digits.";
            }
            // the new account saved in the bank
            SavingsAccount account = bank.register(tempName, tempPin, text);

            // the account labels 
            String[] titles = { "ACCOUNT NAME", "ACCOUNT NUMBER", "ACCOUNT PIN", "CONTACT NUMBER", "NOTE" };
            // the values shown on the details screen (PIN is hidden)
            String[] infos = { account.getName(), Bank.accNo(account.getNumber()), "****", account.getContact(),
                    "Use your account number to log in." };
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

    // checks the amount text and returns an error message (null if it is okay and the amount is saved in parsedAmount)
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

            currentAccount.deposit(walletIndex, amount);
            bank.record(currentAccount.getNumber(), "Deposit", cur.getCode(), amount,
                    currentAccount.getWallet(walletIndex), 0);
            bank.save();

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

            currentAccount.withdraw(walletIndex, amount);
            bank.record(currentAccount.getNumber(), "Withdraw", cur.getCode(), -amount,
                    currentAccount.getWallet(walletIndex), 0);
            bank.save();

            String[] titles = { "ACCOUNT NAME", "ACCOUNT NUMBER", "WALLET", "AMOUNT WITHDRAWN", "UPDATED BALANCE" };
            String[] infos = { currentAccount.getName(), Bank.accNo(currentAccount.getNumber()),
                    currencyText(cur), money(amount) + " " + cur.getCode(),
                    walletText(currentAccount, walletIndex) };
            showDetails("WITHDRAW AMOUNT", "WITHDRAW DETAILS", titles, infos);
            return null;
        });
    }

    // check balance function: shows the balance of every wallet
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

    // check currency exchange function: shows all the rates and has a button to convert an amount
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

        currentAccount.withdraw(sourceIndex, pendingAmount);
        currentAccount.deposit(targetIndex, pendingResult);

        bank.record(currentAccount.getNumber(), "Convert Out", source.getCode(), -pendingAmount,
                currentAccount.getWallet(sourceIndex), 0);
        bank.record(currentAccount.getNumber(), "Convert In", target.getCode(), pendingResult,
                currentAccount.getWallet(targetIndex), 0);
        bank.save();

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
        // the interest of the first period (used to warn about very small balances)
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
        showInput("SEND MONEY", "ENTER RECIPIENT ACCOUNT NUMBER", 2, 6, false, text -> {
            if (text.length() < 5) {
                return "Enter the full account number (example: 00001).";
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

    // sends the money and records both sides of the transfer
    void doSend() {
        Currency cur = currencyAt(walletIndex);

        currentAccount.withdraw(walletIndex, pendingAmount);
        recipient.deposit(walletIndex, pendingAmount);

        bank.record(currentAccount.getNumber(), "Sent", cur.getCode(), -pendingAmount,
                currentAccount.getWallet(walletIndex), recipient.getNumber());
        bank.record(recipient.getNumber(), "Received", cur.getCode(), pendingAmount,
                recipient.getWallet(walletIndex), currentAccount.getNumber());
        bank.save();

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

    // transaction history function: shows all the transactions of the customer
    void showHistory() {
        ArrayList<Object[]> rows = new ArrayList<>();
        int count = 0;

        for (Transaction t : bank.getTransactions()) {
            if (t.getAccountNumber() == currentAccount.getNumber()) {
                count++;
                rows.add(new Object[] { count, t.getType(), t.getCurrency(), money(t.getAmount()),
                        money(t.getBalanceAfter()), otherText(t) });
            }
        }

        if (count == 0) {
            String[] titles = { "NO TRANSACTIONS YET" };
            String[] infos = { "" };
            showDetails("TRANSACTION HISTORY", "ACCOUNT " + Bank.accNo(currentAccount.getNumber()), titles, infos);
            return;
        }

        String[] columns = { "NO.", "TYPE", "CUR", "AMOUNT", "BALANCE", "OTHER" };
        int[] widths = { 30, 92, 36, 80, 90, 62 };
        showTable("TRANSACTION HISTORY", columns, rows, widths, null);
    }

    // the other account of a transaction as text ("-" if none)
    String otherText(Transaction t) {
        if (t.getOtherAccount() == 0) {
            return "-";
        }
        return Bank.accNo(t.getOtherAccount());
    }

    // record rate function: choose the currency to change
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
                    bank.save();

                    String[] titles = { "CURRENCY", "EXCHANGE RATE", "BASE CURRENCY" };
                    String[] infos = { currencyText(selected),
                            "1 " + selected.getCode() + " = " + Bank.rate(selected.getRate()) + " " + base.getCode(),
                            currencyText(base) };
                    showDetails("RECORD RATE", "RATE DETAILS", titles, infos);
                    return null;
                });
    }

    // change base currency function: choose the new base currency and update all the rates
    void startSetBase() {
        showCurrencyChoice("CHANGE BASE CURRENCY", "CHOOSE A CURRENCY", i -> {
            Currency c = currencyAt(i);
            if (c == bank.getBaseCurrency()) {
                showError("CHANGE BASE CURRENCY", currencyText(c) + " is already the base currency.", false);
                return;
            }
            bank.setBaseCurrency(c);
            bank.save();

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

    // view/update profile function: asks for the account number of the customer
    void startProfile() {
        if (bank.getAccounts().isEmpty()) {
            showError("VIEW/UPDATE PROFILE", "There are no registered accounts.", false);
            return;
        }
        showInput("VIEW/UPDATE PROFILE", "ENTER ACCOUNT NUMBER", 2, 6, false, text -> {
            if (text.length() < 5) {
                return "Enter the full account number (example: 00001).";
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

    // shows the profile of the customer with the buttons to update it
    void showProfile() {
        JPanel p = makeScreen("VIEW/UPDATE PROFILE", false);
        addSubtitle(p, "CUSTOMER PROFILE");

        JPanel box = makeWhiteBox(150, 105, 420, 215);

        // the profile text with the balance of each wallet
        String text = "ACCOUNT NUMBER\n    " + Bank.accNo(profileAccount.getNumber())
                + "\n\nACCOUNT NAME\n    " + profileAccount.getName()
                + "\n\nCONTACT NUMBER\n    " + profileAccount.getContact()
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

    // asks for the new name of the customer
    void askNewName() {
        showInput("VIEW/UPDATE PROFILE", "ENTER NEW NAME", 1, 30, false, text -> {
            if (!text.matches("[a-z. ]+") || !text.matches(".*[a-z].*")) {
                return "Use lowercase letters, spaces and periods only.";
            }
            profileAccount.setName(text);
            bank.save();
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
            bank.save();
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
            bank.save();
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

    // view transaction history function: shows the transactions of all accounts
    void showAllTransactions() {
        ArrayList<Object[]> rows = new ArrayList<>();
        int number = 1;

        for (Transaction t : bank.getTransactions()) {
            rows.add(new Object[] { number, Bank.accNo(t.getAccountNumber()), t.getType(), t.getCurrency(),
                    money(t.getAmount()), money(t.getBalanceAfter()), otherText(t) });
            number++;
        }

        if (rows.isEmpty()) {
            String[] titles = { "NO TRANSACTIONS RECORDED" };
            String[] infos = { "" };
            showDetails("TRANSACTION HISTORY", "ALL ACCOUNTS", titles, infos);
            return;
        }

        String[] columns = { "NO.", "ACC #", "TYPE", "CUR", "AMOUNT", "BALANCE", "OTHER" };
        int[] widths = { 26, 50, 88, 32, 66, 78, 50 };
        showTable("TRANSACTION HISTORY", columns, rows, widths, null);
    }

    // starts the program by creating the window
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> new  MCO1_10_JAVA());
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
    // what the key types (BACK deletes the last character)
    String value;
    // the position and size of the key
    Rectangle area;

    public Key(String label, String value, int x, int y, int w, int h) {
        this.label = label;
        this.value = value;
        this.area = new Rectangle(x, y, w, h);
    }
}

// base of the keyboard and the number pad: draws the keys and runs the key that is clicked
abstract class KeyDisplay extends JPanel {
    // color used for the lines and letters
    protected final Color green = new Color(0, 102, 51);
    // all the keys on this display
    protected final ArrayList<Key> keys = new ArrayList<>();
    // the key that is being pressed
    private Key pressed;

    public KeyDisplay(Consumer<String> onKey) {
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                pressed = keyAt(e.getPoint());
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                // the key is typed when the mouse is released on the same key that was pressed
                Key released = keyAt(e.getPoint());
                if (pressed != null && pressed == released) {
                    onKey.accept(pressed.value);
                }
                pressed = null;
                repaint();
            }
        });
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
        int x = k.area.x + (k.area.width - fm.stringWidth(k.label)) / 2;
        int y = k.area.y + (k.area.height + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(k.label, x, y);
    }
}

// displays the keyboard (lowercase letters only) that the user clicks
class KeyBoardDisplay extends KeyDisplay {
    public KeyBoardDisplay(Consumer<String> onKey) {
        super(onKey);

        // the letters of each row (no capital letters)
        String[] rows = { "qwertyuiop", "asdfghjkl", "zxcvbnm." };
        // size of each key 
        int keyW = 24;
        int gap = 4;
        // makes the letter keys, centered in each row
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

        // the space bar and the delete key on the last row
        int spaceW = 150;
        int delW = 56;
        int startX = (320 - (spaceW + gap + delW)) / 2;
        int y = 20 + 3 * 30;
        keys.add(new Key("SPACE", " ", startX, y, spaceW, 24));
        keys.add(new Key("DEL", "BACK", startX + spaceW + gap, y, delW, 24));
    }

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
    public KeyPadDisplay(boolean withDot, Consumer<String> onKey) {
        super(onKey);

        // the numbers on the numpad
        String[] labels = { "7", "8", "9", "4", "5", "6", "1", "2", "3" };
        // makes the numbers 1 to 9 in rows of 3
        for (int i = 0; i < labels.length; i++) {
            keys.add(new Key(labels[i], labels[i], 6 + (i % 3) * 41, 6 + (i / 3) * 41, 38, 38));
        }

        // the last row: delete key, 0 key, and the dot key when needed
        keys.add(new Key("DEL", "BACK", 6, 6 + 3 * 41, 38, 38));
        keys.add(new Key("0", "0", 6 + 41, 6 + 3 * 41, 38, 38));
        if (withDot) {
            keys.add(new Key(".", ".", 6 + 2 * 41, 6 + 3 * 41, 38, 38));
        }
    }

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

// class for one bank account
class Account {
    private final int number;
    private String name;
    private String pin;
    private String contact;
    // the money in each wallet 
    private final double[] wallets;

    // creates a new account with all wallets at 0
    public Account(int number, String name, String pin, String contact, int walletCount) {
        this.number = number;
        this.name = name;
        this.pin = pin;
        this.contact = contact;
        this.wallets = new double[walletCount];
    }

    public int getNumber() { 
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

    // checks if the PIN inputted is the same as the account PIN
    public boolean checkPin(String enteredPin) {
        return pin.equals(enteredPin);
    }

    public double getWallet(int index) { 
        return wallets[index]; 
    }

    // sets the balance of a wallet (used when loading the saved data)
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

    // adds money to a wallet (false if the amount is zero or less or passes the maximum balance)
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

// class for an account that earns 5% interest per year
class SavingsAccount extends Account {
    // the yearly interest rate (5%)
    private static final double annualRate = 0.05; 

    // creates a savings account 
    public SavingsAccount(int number, String name, String pin, String contact, int walletCount) {
        super(number, name, pin, contact, walletCount);
    }

    public double getAnnualRate() { 
        return annualRate; 
    }

    public double getDailyRate() { 
        return annualRate / 365; 
    }

    // creates a table of the interest and balance for each day (stops if the balance would pass the maximum)
    public ArrayList<double[]> projectDaily(double startBalance, int days) {
        // the table (day, interest, balance)
        ArrayList<double[]> rows = new ArrayList<>();
        // money used to compute the interest
        double balance = startBalance;

        // computes the interest and new balance for each day
        for (int day = 1; day <= days; day++) {
            // the interest for the day
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
        // money used to compute the interest
        double balance = startBalance;

        // computes the interest and new balance for each year
        for (int year = 1; year <= years; year++) {
            // the interest for the year
            double interest = round(balance * annualRate);
            if (balance + interest > Bank.MAX_BALANCE) break;
            balance = round(balance + interest);
            rows.add(new double[] { year, interest, balance });
        }
        return rows;
    }
}

// class for one saved transaction
class Transaction {
    private final int accountNumber;
    private final String type;
    private final String currency;
    // the amount is negative when money goes out
    private final double amount;
    private final double balanceAfter;
    // the other account involved (0 if none)
    private final int otherAccount;

    // creates a new transaction record
    public Transaction(int accountNumber, String type, String currency, double amount,
                       double balanceAfter, int otherAccount) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.currency = currency;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.otherAccount = otherAccount;
    }

    public int getAccountNumber() { 
        return accountNumber; 
    }

    public String getType() { 
        return type; 
    }

    public String getCurrency() { 
        return currency; 
    }

    public double getAmount() { 
        return amount; 
    }

    public double getBalanceAfter() { 
        return balanceAfter; 
    }

    public int getOtherAccount() { 
        return otherAccount; 
    }
}

// class for keeping the list of accounts, currencies and transactions
class Bank {
    // the limits of the system
    public static final int MAX_ACCOUNTS = 1000;
    public static final int MAX_ACCOUNT_NUMBER = 999999;
    public static final int MAX_TRANSACTIONS = 1000000;
    public static final double MAX_BALANCE = 999999999999.99;
    public static final double MIN_CONVERTED = 0.0099999;

    // changes a number to text with 2 decimal places
    public static String money(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    // changes a number to text with 6 decimal places
    public static String rate(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    // changes an account number to text with 5 digits (example: 00001)
    public static String accNo(int number) {
        return String.format("%05d", number);
    }

    // creates the list that stores all registered accounts
    private final ArrayList<SavingsAccount> accounts = new ArrayList<>();
    // creates the list that stores all transactions
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    // creates the list of currencies with their starting rates (compared to PHP)
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
    // the account number given to the next new account
    private int nextAccountNumber = 1;
    // is false if a data file could not be read (nothing is saved to protect the files)
    private boolean dataOk = true;

    public Currency[] getCurrencies() { 
        return currencies; 
    }

    public Currency getBaseCurrency() { 
        return baseCurrency; 
    } 

    public ArrayList<SavingsAccount> getAccounts() { 
        return accounts; 
    }

    public ArrayList<Transaction> getTransactions() { 
        return transactions; 
    }

    public int getNextAccountNumber() { 
        return nextAccountNumber; 
    }

    public void setNextAccountNumber(int next) { 
        nextAccountNumber = next; 
    }

    public boolean isDataOk() { 
        return dataOk; 
    }

    public void setDataOk(boolean ok) { 
        dataOk = ok; 
    }

    // changes the base currency and updates every rate to match
    public void setBaseCurrency(Currency newBase) {
        // the rate of the new base currency
        double divisor = newBase.getRate();
        // divides the rate of each currency by the divisor
        for (Currency c : currencies) {
            c.setRate(c.getRate() / divisor);
        }
        newBase.setRate(1.00);
        baseCurrency = newBase;
    }

    // sets the base currency without changing the rates (used when loading the saved data)
    public void restoreBaseCurrency(Currency base) {
        baseCurrency = base;
    }

    // looks for a currency by its code (null if not found)
    public Currency findCurrency(String code) {
        for (Currency c : currencies) {
            if (c.getCode().equals(code)) return c;
        }
        return null;
    }

    // converts an amount from one currency to another (not rounded)
    public double convertRaw(double amount, Currency from, Currency to) {
        return amount * from.getRate() / to.getRate();
    }

    // looks for an account by account number (null if not found)
    public SavingsAccount findByNumber(int number) {
        // checks each account in the list
        for (SavingsAccount a : accounts) {
            if (a.getNumber() == number) return a;
        }
        return null;
    }

    // checks if no more accounts can be registered
    public boolean isFull() {
        return accounts.size() >= MAX_ACCOUNTS || nextAccountNumber > MAX_ACCOUNT_NUMBER;
    }

    // creates a new savings account with the next account number and saves it
    public SavingsAccount register(String name, String pin, String contact) {
        // the new savings account
        SavingsAccount account = new SavingsAccount(nextAccountNumber, name, pin, contact, currencies.length);
        accounts.add(account);
        nextAccountNumber++;
        save();
        return account;
    }

    // removes an account from the list and saves
    public void closeAccount(SavingsAccount account) {
        accounts.remove(account);
        save();
    }

    // checks if the transaction list can still hold more records
    public boolean historyHasRoom(int needed) {
        return transactions.size() + needed <= MAX_TRANSACTIONS;
    }

    // saves a transaction in memory and adds it to the transactions file
    public void record(int accountNumber, String type, String currency, double amount,
                       double balanceAfter, int otherAccount) {
        Transaction t = new Transaction(accountNumber, type, currency, amount, balanceAfter, otherAccount);
        transactions.add(t);
        if (dataOk) {
            Storage.appendTransaction(t);
        }
    }

    // loads the accounts, rates and transactions from the files
    public void load() {
        dataOk = Storage.loadAll(this);
    }

    // saves the accounts and rates unless a data file had a problem when loading
    public void save() {
        if (!dataOk) return;
        Storage.saveAccounts(this);
        Storage.saveRates(this);
    }
}

// class for one currency
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

// class that reads and writes the data files (accounts.txt, rates.txt and transactions.txt)
class Storage {
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String RATES_FILE = "rates.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    // loads all the files (false if any file could not be read correctly)
    public static boolean loadAll(Bank bank) {
        boolean ok = true;
        ok &= loadAccounts(bank);
        ok &= loadRates(bank);
        ok &= loadTransactions(bank);
        return ok;
    }

    // reads the accounts file (line 1: next account number, then 2 lines per account)
    private static boolean loadAccounts(Bank bank) {
        File file = new File(ACCOUNTS_FILE);
        if (!file.exists()) return true;

        int walletCount = bank.getCurrencies().length;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String first = br.readLine();
            if (first == null || first.trim().isEmpty()) return true;

            int next = Integer.parseInt(first.trim());
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (bank.getAccounts().size() >= Bank.MAX_ACCOUNTS) break;

                String[] p = line.trim().split("\\s+");
                if (p.length != 3 + walletCount) return false;

                // the name is saved on its own line
                String name = br.readLine();
                if (name == null) return false;

                int number = Integer.parseInt(p[0]);
                SavingsAccount account = new SavingsAccount(number, name, p[1], p[2], walletCount);
                for (int w = 0; w < walletCount; w++) {
                    account.setWallet(w, Double.parseDouble(p[3 + w]));
                }

                bank.getAccounts().add(account);
                if (number >= next) next = number + 1;
            }

            bank.setNextAccountNumber(next);
            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }

    // reads the rates file (line 1: base currency code, then one line per currency)
    private static boolean loadRates(Bank bank) {
        File file = new File(RATES_FILE);
        if (!file.exists()) return true;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String first = br.readLine();
            if (first == null || first.trim().isEmpty()) return true;

            Currency base = bank.findCurrency(first.trim());
            if (base == null) return false;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

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

    // reads the transactions file (2 lines per transaction: the numbers, then the type)
    private static boolean loadTransactions(Bank bank) {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) return true;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (bank.getTransactions().size() >= Bank.MAX_TRANSACTIONS) break;

                String[] p = line.trim().split("\\s+");
                if (p.length != 5) return false;

                // the type is saved on its own line
                String type = br.readLine();
                if (type == null) return false;

                bank.getTransactions().add(new Transaction(
                        Integer.parseInt(p[0]), type.trim(), p[1],
                        Double.parseDouble(p[2]), Double.parseDouble(p[3]),
                        Integer.parseInt(p[4])));
            }
            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }

    // writes all the accounts to the accounts file
    public static void saveAccounts(Bank bank) {
        try (PrintWriter out = new PrintWriter(new FileWriter(ACCOUNTS_FILE))) {
            out.println(bank.getNextAccountNumber());

            for (SavingsAccount a : bank.getAccounts()) {
                StringBuilder line = new StringBuilder();
                line.append(a.getNumber()).append(" ").append(a.getPin()).append(" ").append(a.getContact());
                for (int w = 0; w < bank.getCurrencies().length; w++) {
                    line.append(" ").append(Bank.money(a.getWallet(w)));
                }
                out.println(line);
                out.println(a.getName());
            }
        } catch (IOException e) {
            System.err.println();
            System.err.println("Warning: could not save " + ACCOUNTS_FILE + ".");
        }
    }

    // writes the base currency and all the exchange rates to the rates file
    public static void saveRates(Bank bank) {
        try (PrintWriter out = new PrintWriter(new FileWriter(RATES_FILE))) {
            out.println(bank.getBaseCurrency().getCode());

            for (Currency c : bank.getCurrencies()) {
                out.println(c.getCode() + " " + c.getRate());
            }
        } catch (IOException e) {
            System.err.println();
            System.err.println("Warning: could not save " + RATES_FILE + ".");
        }
    }

    // adds one transaction to the end of the transactions file
    public static void appendTransaction(Transaction t) {
        try (PrintWriter out = new PrintWriter(new FileWriter(TRANSACTIONS_FILE, true))) {
            out.println(t.getAccountNumber() + " " + t.getCurrency() + " " + Bank.money(t.getAmount())
                    + " " + Bank.money(t.getBalanceAfter()) + " " + t.getOtherAccount());
            out.println(t.getType());
        } catch (IOException e) {
            System.err.println();
            System.err.println("Warning: could not save " + TRANSACTIONS_FILE + ".");
        }
    }
}