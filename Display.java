public class Display {
    
    public static void showMainMenu() {
        System.out.println();
        System.out.println("|-------------------------------|");
        System.out.println("| BANKING AND CURRENCY EXCHANGE |");
        System.out.println("|-------------------------------|");
        System.out.println("| [1] REGISTER ACCOUNT          |");
        System.out.println("| [2] DEPOSIT AMOUNT            |");
        System.out.println("| [3] WITHDRAW AMOUNT           |");
        System.out.println("| [4] CURRENCY EXCHANGE         |");
        System.out.println("| [5] RECORD RATE               |");
        System.out.println("| [6] INTEREST                  |");
        System.out.println("| [7] EXIT                      |");
        System.out.println("|-------------------------------|");
        System.out.println();
    }

    public static void printHeader(String title) {
        int width = 40;
        int left = (width - title.length()) / 2;
        int right = width - title.length() - left;
        String bar = "|" + "-".repeat(width) + "|";
        System.out.println(bar);
        System.out.println("|" + " ".repeat(left) + title + " ".repeat(right) + "|");
        System.out.println(bar);
    }

    public static void printFooter() {
        System.out.println("|" + "-".repeat(40) + "|");
    }

    public static void printInterestTable(String periodLabel, double[][] rows) {
        String[] heads = { periodLabel, "INTEREST", "BALANCE" };
        int[] widths = { heads[0].length(), heads[1].length(), heads[2].length() };
        String[][] cells = new String[rows.length][3];

        for (int i = 0; i < rows.length; i++) {
            cells[i][0] = String.valueOf((int) rows[i][0]);
            cells[i][1] = money(rows[i][1]);
            cells[i][2] = money(rows[i][2]);
            for (int c = 0; c < 3; c++) {
                widths[c] = Math.max(widths[c], cells[i][c].length());
            }
        }

        printTableRow(heads, widths);
        for (String[] row : cells) {
            printTableRow(row, widths);
        }

    }

    private static void printTableRow(String[] values, int[] widths) {
        StringBuilder line = new StringBuilder("|");
        for (int c = 0; c < values.length; c++) {
            line.append(" ").append(String.format("%-" + widths[c] + "s", values[c])).append(" |");
        }
        System.out.println(line);
    }

    public static String money(double value) {
        return String.format("%.2f", value);
    }
}
