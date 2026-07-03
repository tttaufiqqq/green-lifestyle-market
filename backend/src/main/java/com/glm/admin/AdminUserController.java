package com.glm.admin;

import com.glm.admin.dto.UserAdminView;
import com.glm.admin.dto.UserStatusRequest;
import com.glm.common.audit.AuditLog;
import com.glm.common.audit.AuditLogRepository;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.common.security.GlmUserDetails;
import com.glm.user.entity.User;
import com.glm.user.entity.SellerBankAccount;
import com.glm.user.repository.SellerBankAccountRepository;
import com.glm.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserRepository           userRepo;
    private final SellerBankAccountRepository bankRepo;
    private final AuditLogRepository       auditRepo;

    public AdminUserController(UserRepository userRepo, SellerBankAccountRepository bankRepo,
                                AuditLogRepository auditRepo) {
        this.userRepo  = userRepo;
        this.bankRepo  = bankRepo;
        this.auditRepo = auditRepo;
    }

    @GetMapping
    public List<UserAdminView> list() {
        return userRepo.findAllByOrderByCreatedAtDesc().stream().map(this::toView).toList();
    }

    @GetMapping("/{id}")
    public UserAdminView get(@PathVariable Long id) {
        return toView(require(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody UserStatusRequest req,
        @AuthenticationPrincipal GlmUserDetails principal) {
        User user = require(id);
        user.setStatus(User.Status.valueOf(req.status()));
        user.setUpdatedAt(Instant.now());
        userRepo.save(user);
        audit(principal.getUser(), "USER_STATUS_" + req.status(), "user", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/bank-account/verify")
    public ResponseEntity<Void> verifyBank(
        @PathVariable Long id,
        @RequestParam boolean verified,
        @AuthenticationPrincipal GlmUserDetails principal) {
        SellerBankAccount bank = bankRepo.findByUserId(id).orElseThrow(() ->
            new DomainException(ErrorCode.E_NOTFOUND, "Bank account not found for user " + id, 404));
        bank.setVerified(verified);
        bank.setUpdatedAt(Instant.now());
        bankRepo.save(bank);
        audit(principal.getUser(), verified ? "BANK_VERIFIED" : "BANK_UNVERIFIED", "seller_bank_accounts", bank.getId());
        return ResponseEntity.noContent().build();
    }

    private User require(Long id) {
        return userRepo.findById(id).orElseThrow(() ->
            new DomainException(ErrorCode.E_NOTFOUND, "User not found: " + id, 404));
    }

    private void audit(User admin, String action, String entityType, Long entityId) {
        AuditLog al = new AuditLog();
        al.setUser(admin); al.setAction(action);
        al.setEntityType(entityType); al.setEntityId(entityId);
        al.setCreatedAt(Instant.now());
        auditRepo.save(al);
    }

    private UserAdminView toView(User u) {
        return new UserAdminView(u.getId(), u.getName(), u.getEmail(), u.getPhone(),
            u.getRole().name(), u.getStatus().name(),
            u.getEmailVerifiedAt() != null, u.getCreatedAt());
    }
}
