package com.glm.user.dto;

import com.glm.user.entity.SellerBankAccount;

public record BankAccountResponse(
    String bankName,
    String accountNo,
    String holderName,
    boolean verified
) {
    public static BankAccountResponse from(SellerBankAccount ba) {
        return new BankAccountResponse(ba.getBankName(), ba.getAccountNo(), ba.getHolderName(), ba.isVerified());
    }
}
