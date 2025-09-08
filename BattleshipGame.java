import java.util.*;

public class BattleshipGame {
    // Σταθερές παιχνιδιού
    static final int SIZE = 7;                  // μέγεθος ταμπλό 7x7
    static final int EMPTY = -1, MISS = 0, HIT = 1;
    static final int NUM_SHIPS = 4;             // 2 πλοία μήκους 2, 2 πλοία μήκους 1
    static final int[] SHIP_SIZES = {2, 2, 1, 1};

    //  Ρυθμίσεις βοήθειας 
    static final boolean DEBUG_SHOW_SHIPS = false; // true μόνο προσωρινά για να δεις θέσεις
    static Random random = new Random();           // για σταθερά screenshots: new Random(42)

    // Κατάσταση παιχνιδιού
    static final Scanner SC = new Scanner(System.in);
    static int[][] board = new int[SIZE][SIZE];       // ορατό ταμπλό: -1 ~ , 0 * , 1 X
    static int[][] shipIdBoard = new int[SIZE][SIZE]; // id πλοίου ανά κελί ή -1 αν κενό
    static int[] shipHits = new int[NUM_SHIPS];       // πόσα κελιά του κάθε πλοίου έχουν χτυπηθεί
    static int totalTargets = 0;                       // συνολικά κελιά-στόχοι (2+2+1+1 = 6)
    static int hitsTotal = 0;                          // πόσα κελιά-στόχοι βρήκαμε
    static int attempts = 0;                           // πόσες έγκυρες προσπάθειες έγιναν

    // Αρχικοποιήσεις
    static void initBoard() {
        for (int i = 0; i < SIZE; i++) {
            Arrays.fill(board[i], EMPTY);
            Arrays.fill(shipIdBoard[i], -1);
        }
        Arrays.fill(shipHits, 0);
        hitsTotal = 0;
        attempts = 0;
        totalTargets = 0;
        for (int s : SHIP_SIZES) totalTargets += s;
    }

    static void initShips() {
        for (int shipId = 0; shipId < NUM_SHIPS; shipId++) {
            int len = SHIP_SIZES[shipId];
            boolean placed = false;
            while (!placed) {
                boolean horizontal = random.nextBoolean();
                int row = random.nextInt(SIZE);
                int col = random.nextInt(SIZE);

                if (horizontal) {
                    if (col + len - 1 >= SIZE) continue;
                    boolean overlap = false;
                    for (int c = col; c < col + len; c++) {
                        if (shipIdBoard[row][c] != -1) { overlap = true; break; }
                    }
                    if (overlap) continue;
                    for (int c = col; c < col + len; c++) shipIdBoard[row][c] = shipId;
                    placed = true;
                } else {
                    if (row + len - 1 >= SIZE) continue;
                    boolean overlap = false;
                    for (int r = row; r < row + len; r++) {
                        if (shipIdBoard[r][col] != -1) { overlap = true; break; }
                    }
                    if (overlap) continue;
                    for (int r = row; r < row + len; r++) shipIdBoard[r][col] = shipId;
                    placed = true;
                }
            }
        }

        if (DEBUG_SHOW_SHIPS) {
            System.out.println("DEBUG – Θέσεις πλοίων (shipIdBoard):");
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    System.out.print((shipIdBoard[i][j] == -1 ? "." : shipIdBoard[i][j]) + " ");
                }
                System.out.println();
            }
        }
    }

    // Εμφάνιση ταμπλό
    static void showBoard() {
        System.out.println("\n  1 2 3 4 5 6 7");
        for (int i = 0; i < SIZE; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < SIZE; j++) {
                int v = board[i][j];
                if (v == EMPTY) System.out.print("~ ");
                else if (v == MISS) System.out.print("* ");
                else System.out.print("X ");
            }
            System.out.println();
        }
    }

    // Είσοδος παίκτη με έλεγχο
    static int[] getShot() {
        int r, c;
        while (true) {
            System.out.print("Εισάγετε σειρά (1-7): ");
            if (!SC.hasNextInt()) { System.out.println("Δώσε αριθμό 1-7."); SC.next(); continue; }
            r = SC.nextInt() - 1;

            System.out.print("Εισάγετε στήλη (1-7): ");
            if (!SC.hasNextInt()) { System.out.println("Δώσε αριθμό 1-7."); SC.next(); continue; }
            c = SC.nextInt() - 1;

            if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) {
                System.out.println("Εκτός ορίων. Προσπάθησε ξανά (1-7).");
                continue;
            }
            if (board[r][c] != EMPTY) {
                System.out.println("Έχεις ήδη στοχεύσει (" + (r + 1) + "," + (c + 1) + "). Διάλεξε άλλο κελί.");
                continue;
            }
            break;
        }
        return new int[]{r, c};
    }

    // Εφαρμογή βολής
    static boolean isHit(int r, int c) { return shipIdBoard[r][c] != -1; }

    static void applyShot(int r, int c) {
        if (isHit(r, c)) {
            board[r][c] = HIT;
            int id = shipIdBoard[r][c];
            shipHits[id]++;
            hitsTotal++;
            if (shipHits[id] == SHIP_SIZES[id]) {
                System.out.println("Βύθισες πλοίο μήκους " + SHIP_SIZES[id] + "!");
            } else {
                System.out.println("Χτύπημα!");
            }
        } else {
            board[r][c] = MISS;
            System.out.println("Άστοχο.");
            hint(r, c);
        }
    }

    // Υπόδειξη σε άστοχη βολή
    static void hint(int r, int c) {
        boolean[] seenRow = new boolean[NUM_SHIPS];
        boolean[] seenCol = new boolean[NUM_SHIPS];
        int rowCount = 0, colCount = 0;

        for (int j = 0; j < SIZE; j++) {
            int id = shipIdBoard[r][j];
            if (id != -1 && !seenRow[id]) { seenRow[id] = true; rowCount++; }
        }
        for (int i = 0; i < SIZE; i++) {
            int id = shipIdBoard[i][c];
            if (id != -1 && !seenCol[id]) { seenCol[id] = true; colCount++; }
        }
        System.out.println("Υπόδειξη: σειρά " + (r + 1) + " → " + rowCount +
                           " πλοίο/πλοία, στήλη " + (c + 1) + " → " + colCount + " πλοίο/πλοία.");
    }

    //  Κύρια ροή
    public static void main(String[] args) {
        initBoard();

        // random = new Random(42);

        initShips();
        System.out.println("=== ΠΑΙΧΝΙΔΙ ΝΑΥΜΑΧΙΑ (7x7) ===");
        System.out.println("Υπάρχουν 4 πλοία: 2×(μήκος 2) και 2×(μήκος 1).");

        while (hitsTotal < totalTargets) {
            showBoard();
            int[] shot = getShot();   // λαμβάνει ΜΟΝΟ έγκυρη και μη-ξαναπαιγμένη βολή
            attempts++;
            applyShot(shot[0], shot[1]);
            System.out.println("Επιτυχίες: " + hitsTotal + "/" + totalTargets +
                               " | Προσπάθειες: " + attempts);
        }

        showBoard();
        System.out.println("\nΣΥΓΧΑΡΗΤΗΡΙΑ! Βύθισες όλα τα πλοία σε " + attempts + " προσπάθειες.");
    }
}
