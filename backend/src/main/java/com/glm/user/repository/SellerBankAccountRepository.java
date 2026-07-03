package com.glm.user.repository;

import com.glm.user.entity.SellerBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SellerBankAccountRepository extends JpaRepository<SellerBankAccount, Long> {
    Optional<SellerBankAccount> findByUserId(Long userId);
}
