package ticketservice;

/**
 * Details on a hold or reservation for a collection of seats.
 */
public final class SeatBlock {

    private final int id;
    private final long createdTime;
    private final String email;
    private final int startingSeat;
    private final int numberSeats;

    /**
     * Make the default constructor private.
     */
    private SeatBlock() {
        this(0, null, -1, -1);
    }

    /**
     * Constructor taking arguments for the fields that can be supplied.
     * A block consists of consecutive seats, but a block can wrap from
     * the end of one row to the start of the next row.
     *
     * @param id the block ID
     * @param email the customer's email
     * @param startingSeat the starting seat number
     * @param numberSeats the number of seats
     */
    public SeatBlock(final int id, final String email, final int startingSeat, final int numberSeats) {
        this.id = id;
        createdTime = System.currentTimeMillis();
        this.email = email;
        this.startingSeat = startingSeat;
        this.numberSeats = numberSeats;
    }

    public int getId() {
        return id;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getAge() {
        return System.currentTimeMillis() - createdTime;
    }

    public String getEmail() {
        return email;
    }

    public int getStartingSeat() {
        return startingSeat;
    }

    public int getNumberSeats() {
        return numberSeats;
    }

    @Override
    public String toString() {
        return "SeatBlock{" +
                "id=" + id +
                ", createdTime=" + createdTime +
                ", email='" + email + '\'' +
                ", startingSeat=" + startingSeat +
                ", numberSeats=" + numberSeats +
                '}';
    }
}
