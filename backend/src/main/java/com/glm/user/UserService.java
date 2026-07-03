package com.glm.user;

import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.user.dto.BankAccountRequest;
import com.glm.user.dto.UpdateProfileRequest;
import com.glm.user.entity.SellerBankAccount;
import com.glm.user.entity.User;
import com.glm.user.entity.User.Affiliation;
import com.glm.user.repository.SellerBankAccountRepository;
import com.glm.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final SellerBankAccountRepository bankRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, SellerBankAccountRepository bankRepo, PasswordEncoder encoder) {
        this.userRepo  = userRepo;
        this.bankRepo  = bankRepo;
        this.encoder   = encoder;
    }

    @Transactional
    public User register(String name, String email, String password, String phone, String affiliation) {
        if (userRepo.existsByEmail(email.toLowerCase()))
            throw new DomainException(ErrorCode.E_VAL, "Email already registered", 400);
        User u = new User();
        u.setName(name);
        u.setEmail(email.toLowerCase());
        u.setPasswordHash(encoder.encode(password));
        u.setPhone(phone);
        u.setAffiliation(toAffiliation(affiliation));
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        return userRepo.save(u);
    }

    @Transactional
    public User markEmailVerified(User user) {
        user.setEmailVerifiedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userRepo.save(user);
    }

    @Transactional
    public User updateProfile(User user, UpdateProfileRequest req) {
        user.setName(req.name());
        user.setPhone(req.phone());
        user.setAffiliation(toAffiliation(req.affiliation()));
        user.setUpdatedAt(Instant.now());
        return userRepo.save(user);
    }

    @Transactional
    public void changePassword(User user, String current, String next) {
        if (!encoder.matches(current, user.getPasswordHash()))
            throw new DomainException(ErrorCode.E_AUTH_CRED, "Current password is incorrect", 401);
        user.setPasswordHash(encoder.encode(next));
        user.setUpdatedAt(Instant.now());
        userRepo.save(user);
    }

    @Transactional
    public void resetPassword(User user, String newPassword) {
        user.setPasswordHash(encoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        userRepo.save(user);
    }

    @Transactional
    public SellerBankAccount upsertBankAccount(User user, BankAccountRequest req) {
        SellerBankAccount ba = bankRepo.findByUserId(user.getId()).orElse(new SellerBankAccount());
        if (ba.getId() == null) { ba.setUser(user); ba.setCreatedAt(Instant.now()); }
        ba.setBankName(req.bankName());
        ba.setAccountNo(req.accountNo());
        ba.setHolderName(req.holderName());
        ba.setVerified(false);
        ba.setUpdatedAt(Instant.now());
        return bankRepo.save(ba);
    }

    private static Affiliation toAffiliation(String value) {
        if (value == null) return Affiliation.PUBLIC;
        try { return Affiliation.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException e) { return Affiliation.PUBLIC; }
    }
}
