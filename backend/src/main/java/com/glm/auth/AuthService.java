package com.glm.auth;

import com.glm.auth.dto.LoginRequest;
import com.glm.auth.dto.RegisterRequest;
import com.glm.common.audit.AuditLogger;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.common.security.GlmUserDetails;
import com.glm.user.MailService;
import com.glm.user.TokenService;
import com.glm.user.UserService;
import com.glm.user.entity.User;
import com.glm.user.entity.VerificationToken.Purpose;
import com.glm.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final MailService mailService;
    private final LoginThrottleService throttle;
    private final AuditLogger audit;
    private final PasswordEncoder encoder;
    private final UserRepository userRepo;

    public AuthService(UserService userService, TokenService tokenService, MailService mailService,
                       LoginThrottleService throttle, AuditLogger audit,
                       PasswordEncoder encoder, UserRepository userRepo) {
        this.userService  = userService;
        this.tokenService = tokenService;
        this.mailService  = mailService;
        this.throttle     = throttle;
        this.audit        = audit;
        this.encoder      = encoder;
        this.userRepo     = userRepo;
    }

    public User register(RegisterRequest req) {
        User user = userService.register(req.name(), req.email(), req.password(), req.phone(), req.affiliation());
        String token = tokenService.createToken(user, Purpose.EMAIL_VERIFY);
        mailService.sendVerifyEmail(user.getEmail(), token);
        return user;
    }

    public User login(LoginRequest req, HttpServletRequest httpReq) {
        String email = req.email().toLowerCase();
        if (throttle.isLocked(email))
            throw new DomainException(ErrorCode.E_AUTH_LOCKED, "Too many attempts, try again in 15 min", 429);

        User user = userRepo.findByEmail(email).orElse(null);
        boolean valid = user != null && encoder.matches(req.password(), user.getPasswordHash());

        if (!valid) {
            throttle.recordFailure(email);
            if (user != null && throttle.isLocked(email))
                audit.log(user, "LOGIN_LOCKED", httpReq.getRemoteAddr());
            throw new DomainException(ErrorCode.E_AUTH_CRED, "Wrong email or password", 401);
        }

        if (user.getStatus() == User.Status.SUSPENDED)
            throw new DomainException(ErrorCode.E_AUTH_SUSPENDED, "Account suspended", 403);

        throttle.clearFailures(email);
        var details = new GlmUserDetails(user);
        var auth = UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        httpReq.getSession(true);
        audit.log(user, "LOGIN", httpReq.getRemoteAddr());
        return user;
    }

    public void verifyEmail(String rawToken) {
        User user = tokenService.consumeToken(rawToken, Purpose.EMAIL_VERIFY);
        userService.markEmailVerified(user);
    }

    public void forgotPassword(String email) {
        userRepo.findByEmail(email.toLowerCase()).ifPresent(user -> {
            String token = tokenService.createToken(user, Purpose.PASSWORD_RESET);
            mailService.sendPasswordReset(user.getEmail(), token);
        });
    }

    public void resetPassword(String rawToken, String newPassword) {
        User user = tokenService.consumeToken(rawToken, Purpose.PASSWORD_RESET);
        userService.resetPassword(user, newPassword);
    }
}
