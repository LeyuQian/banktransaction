package com.comp5348.banktransaction.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity object for transaction_record database table.
 */
@Getter @Setter
@NoArgsConstructor
@Entity
public class TransactionRecord {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private Double amount;

    private String memo;

    // add merchant fee not null
    @Column(nullable = false)
    private Double merchantFee = 0.0;

    @Column(nullable = false)
    private LocalDateTime time;

    @ManyToOne
    @JoinColumn
    private Account toAccount;

    @ManyToOne
    @JoinColumn
    private Account fromAccount;

    @Version
    private int version;

    public TransactionRecord(Double amount, Account toAccount, Account fromAccount, String memo) {
        this.amount = amount;
        this.time = LocalDateTime.now();
        this.toAccount = toAccount;
        this.fromAccount = fromAccount;
        this.memo = memo;
        this.merchantFee = 0.0;
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "id=" + id +
                ", amount=" + amount +
                ", memo='" + memo + '\'' +
                ", merchantFee=" + merchantFee +
                ", time=" + time +
                ", toAccount=" + toAccount +
                ", fromAccount=" + fromAccount +
                ", version=" + version +
                '}';
    }
}
