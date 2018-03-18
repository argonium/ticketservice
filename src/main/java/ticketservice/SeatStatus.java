package ticketservice;

/**
 * Possible status values for a venue seat.
 */
public final class SeatStatus {

    /** Status values for a seat. */
    public enum Status {
        OPEN, HELD, RESERVED
    }

    /** The seat ID (seats are numbered 0 to (n - 1). */
    private int seatId = -1;

    /** The status of the seat.  Default to OPEN. */
    private Status seatStatus = Status.OPEN;

    /** If the seat is held or reserved, this is the key for the map in TicketServiceImpl. */
    private int seatLookupId = -1;

    /**
     * Make the default constructor private.
     */
    private SeatStatus() {
        super();
    }

    /**
     * Constructor taking some initial values for the instance.
     *
     * @param seatId the seat number
     * @param seatStatus the seat status
     */
    public SeatStatus(final int seatId, final Status seatStatus) {
        this.seatId = seatId;
        this.seatStatus = seatStatus;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public Status getSeatStatus() {
        return seatStatus;
    }

    public void setSeatStatus(Status seatStatus) {
        this.seatStatus = seatStatus;

        // If we're making the seat open, then clear the map lookup value
        if (Status.OPEN.equals(seatStatus)) {
            seatLookupId = -1;
        }
    }

    public int getSeatLookupId() {
        return seatLookupId;
    }

    public void setSeatLookupId(int seatLookupId) {
        this.seatLookupId = seatLookupId;
    }

    /**
     * Check if the seat is available - either the seat is open, or it's held
     * but the hold has timed out.
     *
     * @param seatBlock the hold block of the seat (if any), or null
     * @param maxAge the max allowed age of a seat hold
     * @return whether the seat is available or not
     */
    public boolean isAvailable(final SeatBlock seatBlock,
                               final long maxAge) {
        return (seatStatus.equals(Status.OPEN) ||
                (seatStatus.equals(Status.HELD) && ((seatBlock == null) || (seatBlock.getAge() >= maxAge))));
    }

    @Override
    public String toString() {
        return "SeatStatus{" +
                "seatId=" + seatId +
                ", seatStatus=" + seatStatus +
                ", seatLookupId=" + seatLookupId +
                '}';
    }
}
