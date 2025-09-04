package com.comp5348.banktransaction.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Entity object for account database table.
 */
@Getter
@NoArgsConstructor
@Entity
public class Account {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Customer customer;

    @Version
    private int version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double balance = 0.0;

    // ⭐ 新增：账户类型
    @Enumerated(EnumType.STRING)   // 存储为字符串，例如 "PERSONAL"
    @Column(nullable = false)
    private AccountType accountType; // 默认个人账户

    @OneToMany(mappedBy = "fromAccount")
    private Collection<TransactionRecord> fromTransactionRecords;

    @OneToMany(mappedBy = "toAccount")
    private Collection<TransactionRecord> toTransactionRecords;

    public Account(Customer customer, String name, AccountType accountType) {
        this.customer = customer;
        this.name = name;
        this.fromTransactionRecords = new ArrayList<>();
        this.toTransactionRecords = new ArrayList<>();
        this.accountType = accountType;
    }

    public void modifyBalance(Double amount) {
        this.balance = this.balance + amount;
    }
}
