public class BankingApp {
    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            Display.showMainMenu();
            int choice = BankFunction.readInt("Choose Option: ", 1, 7);

            System.out.println();
            System.out.println("=================================");

            switch (choice) {
                case 1:
                    System.out.println("Going to REGISTER...\n");
                    BankFunction.registerAccount();
                    break;
                case 2:
                    System.out.println("Going to DEPOSIT...\n");
                    BankFunction.depositAmount();
                    break;
                case 3:
                    System.out.println("Going to WITHDRAW...\n");
                    BankFunction.withdrawAmount();
                    break;
                case 4:
                    System.out.println("Going to CURRENCY...\n");
                    BankFunction.currencyExchange();
                    break;
                case 5:
                    System.out.println("Going to RECORD...\n");
                    BankFunction.recordExchangeRate();
                    break;
                case 6:
                    System.out.println("Going to INTEREST...\n");
                    BankFunction.computeInterest();
                    break;
                case 7:
                    System.out.println("Going to EXIT...\n");
                    System.out.println("Thank you for using Banking and Currency Exchange!");
                    running = false;
                    break;
            }
        }
        BankFunction.closeInput();
    }
}
