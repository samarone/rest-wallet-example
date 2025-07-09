package br.com.samarone.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private LocalDateTime timestamp;
    private BigDecimal amount;

    public Transaction(LocalDateTime timestamp, BigDecimal amount) {
        this.timestamp = timestamp;
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
