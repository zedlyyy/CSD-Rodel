package com.rodel.rodel.database;

import org.springframework.data.repository.CrudRepository;

import com.rodel.rodel.objects.Transaction;

public interface CrudRepositoryTransactions extends CrudRepository<Transaction, Long> {
}
