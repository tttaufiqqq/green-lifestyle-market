package com.glm.user.dto;

import jakarta.validation.constraints.*;

public record BankAccountRequest(
    @NotBlank @Size(max = 50)  String bankName,
    @NotBlank @Size(max = 20)  String accountNo,
    @NotBlank @Size(max = 100) String holderName
) {}
