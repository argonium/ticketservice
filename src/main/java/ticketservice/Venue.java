package ticketservice;

/**
 * POJO representing a concert venue.
 */
final class Venue {

    /** Default number of rows and columns in the venue. */
    private static final int DEFAULT_ROWS = 30;
    private static final int DEFAULT_COLS = 50;

    /** Number of rows. */
    private final int numRows;

    /** Number of seats per row. */
    private final int numCols;

    /** The computed total number of seats. */
    private int totalNumberOfSeats;

    /**
     * Default constructor.
     */
    public Venue() {
        this(DEFAULT_ROWS, DEFAULT_COLS);
    }

    /**
     * Constructor taking the number of rows and columns.
     *
     * @param numRows the number of rows in the venue
     * @param numCols the number of seats per row in the venue
     */
    public Venue(final int numRows, final int numCols) {

        // The inputs must be positive
        if (numRows <= 0 || numCols <= 0) {
            throw new IllegalArgumentException("Illegal argument to Venue c'tor: rows and columns must be positive");
        }

        // Save the inputs and compute the total number of seats
        this.numRows = numRows;
        this.numCols = numCols;
        totalNumberOfSeats = numRows * numCols;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getNumberOfSeats() {
        return totalNumberOfSeats;
    }

    @Override
    public String toString() {
        return String.format("Venue {rows: %d, columns: %d", numRows, numCols);
    }
}
