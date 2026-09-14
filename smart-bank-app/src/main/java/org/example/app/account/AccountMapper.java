package org.example.app.account;

import org.example.app.account.dto.AccountResponse;
import org.example.model.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface AccountMapper {

    // simple fields (same name) → mapped automatically
    // fields needing custom logic → @Mapping tells MapStruct how

    @Mapping(target = "accountType",
            expression = "java(account.getAccountType().displayName())")
    @Mapping(target = "status",
            expression = "java(account.getStatus().name())")
    AccountResponse toResponse(BankAccount account);

   }