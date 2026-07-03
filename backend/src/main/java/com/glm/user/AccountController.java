package com.glm.user;

import com.glm.common.security.GlmUserDetails;
import com.glm.user.dto.*;
import com.glm.user.repository.SellerBankAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
public class AccountController {

    private final UserService userService;
    private final SellerBankAccountRepository bankRepo;

    public AccountController(UserService userService, SellerBankAccountRepository bankRepo) {
        this.userService = userService;
        this.bankRepo    = bankRepo;
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@AuthenticationPrincipal GlmUserDetails d) {
        return ProfileResponse.from(d.getUser());
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(@AuthenticationPrincipal GlmUserDetails d,
                                         @Valid @RequestBody UpdateProfileRequest req) {
        return ProfileResponse.from(userService.updateProfile(d.getUser(), req));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal GlmUserDetails d,
                                               @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(d.getUser(), req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bank-account")
    public ResponseEntity<BankAccountResponse> getBankAccount(@AuthenticationPrincipal GlmUserDetails d) {
        return bankRepo.findByUserId(d.getUserId())
                .map(ba -> ResponseEntity.ok(BankAccountResponse.from(ba)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/bank-account")
    public BankAccountResponse upsertBankAccount(@AuthenticationPrincipal GlmUserDetails d,
                                                 @Valid @RequestBody BankAccountRequest req) {
        return BankAccountResponse.from(userService.upsertBankAccount(d.getUser(), req));
    }
}
