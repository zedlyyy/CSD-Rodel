package com.rodel.rodel.database;

import org.springframework.data.repository.CrudRepository;

import com.rodel.rodel.objects.Account;

public interface CrudRepositoryAccounts extends CrudRepository<Account, String> {

}
