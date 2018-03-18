# TicketService

Coding challenge for a ticketing service

## Commands
To build the JAR:

```
$ ./gradlew jar
```

To run the unit tests:

```
$ ./gradlew test
```

## Assumptions

* The supported seating chart of a venue is M rows with each
  row having N seats.
* The algorithm to determine the best available seats is
  very simple.  It starts with the left-most seat of the first
  row, proceeds left-to-right on that row, and then continues
  the process with the next row, until it finds a contiguous
  block of available seats matching the number of requested
  seats.  Since the algorithm doesn't distinguish between
  the end of a row and the start of the next row, the block
  may span two or more rows.
* The nature of the above algorithm means that a venue
  may have, say, 10 available seats left, but if they're
  not contiguous, then an attempt to hold 10 seats will fail.
* Since the methods of the TicketService interface don't
  throw any exceptions, the implementation returns null on
  error rather than throw a runtime exception (e.g., if
  a customer attempts to reserve -1 seats, or passes a null
  email address).

