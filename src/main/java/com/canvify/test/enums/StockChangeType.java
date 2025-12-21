package com.canvify.test.enums;

public enum StockChangeType {
    INBOUND,            // Adding stock (purchase / restock)
    SALE,               // Order placed
    RETURN,             // Returned by customer
    ADJUSTMENT,         // Manual stock correction
    RESERVATION,        // Cart or allocated stock
    CANCEL_RESERVATION  // Reverse reservation
}
