package com.glm.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/**
 * HTTP adapter for ToyyibPay FPX gateway.
 * Security note: secretKey is injected from env and never logged.
 * Trust policy: callback is a hint only — verifyPaid is the source of truth (FR-P2).
 */
@Component
public class ToyyibPayClient {

    private static final Logger log = LoggerFactory.getLogger(ToyyibPayClient.class);

    private final String       baseUrl;
    private final String       secretKey;
    private final String       categoryCode;
    private final RestClient   restClient;
    private final ObjectMapper objectMapper;

    public ToyyibPayClient(
        @Value("${app.toyyibpay.base-url}")      String baseUrl,
        @Value("${app.toyyibpay.secret-key}")     String secretKey,
        @Value("${app.toyyibpay.category-code}")  String categoryCode,
        ObjectMapper objectMapper) {
        this.baseUrl      = baseUrl;
        this.secretKey    = secretKey;
        this.categoryCode = categoryCode;
        this.objectMapper = objectMapper;
        this.restClient   = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Creates a ToyyibPay bill and returns the billCode.
     * Throws RuntimeException on parse failure (caller maps to E-PAY-GATEWAY).
     */
    public String createBill(String paymentNo, String buyerName, String buyerEmail,
                              String buyerPhone, long amountSen, String appBaseUrl) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("userSecretKey",         secretKey);
        form.add("categoryCode",          categoryCode);
        form.add("billName",              truncate("GLM " + paymentNo, 30));
        form.add("billDescription",       "Green Lifestyle Market order");
        form.add("billPriceSetting",      "1");
        form.add("billPayorInfo",         "1");
        form.add("billAmount",            String.valueOf(amountSen));
        form.add("billReturnUrl",         appBaseUrl + "/api/v1/payments/toyyibpay/return");
        form.add("billCallbackUrl",       appBaseUrl + "/api/v1/payments/toyyibpay/callback");
        form.add("billExternalReferenceNo", paymentNo);
        form.add("billTo",                buyerName);
        form.add("billEmail",             buyerEmail);
        form.add("billPhone",             buyerPhone != null ? buyerPhone : "");
        form.add("billPaymentChannel",    "0");
        form.add("billExpiryDays",        "1");

        log.info("[TOYYIBPAY] createBill paymentNo={} amountSen={}", paymentNo, amountSen);
        String resp = restClient.post()
            .uri("/index.php/api/createBill")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(String.class);

        try {
            String billCode = objectMapper.readTree(resp).get(0).get("BillCode").asText();
            log.info("[TOYYIBPAY] createBill success billCode={}", billCode);
            return billCode;
        } catch (Exception e) {
            log.error("[TOYYIBPAY] createBill parse error resp={}", resp, e);
            throw new RuntimeException("Failed to parse ToyyibPay createBill response", e);
        }
    }

    /**
     * Server-side verification (FR-P2): returns true iff a SUCCESS transaction exists
     * for this bill AND the paid amount (in sen) matches expectedSen.
     * Throws RuntimeException on API error — caller maps to REVIEW state.
     */
    public boolean verifyPaid(String billCode, long expectedSen) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("userSecretKey", secretKey);
        form.add("billCode",      billCode);

        String resp = restClient.post()
            .uri("/index.php/api/getBillTransactions")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(String.class);

        try {
            if (resp == null || !resp.trim().startsWith("[")) {
                log.warn("[TOYYIBPAY] getBillTransactions no data billCode={}", billCode);
                return false;
            }
            JsonNode root = objectMapper.readTree(resp);
            for (JsonNode txn : root) {
                if (!"1".equals(txn.path("billpaymentStatus").asText(""))) continue;
                // Amount is returned in RM (e.g. "45.90") — convert to sen for comparison
                long paidSen = new BigDecimal(txn.path("billpaymentAmount").asText("0"))
                    .multiply(BigDecimal.valueOf(100)).longValue();
                if (paidSen == expectedSen) {
                    log.info("[TOYYIBPAY] verifyPaid SUCCESS billCode={}", billCode);
                    return true;
                }
                log.warn("[TOYYIBPAY] AMOUNT_MISMATCH billCode={} expected={} paid={}",
                    billCode, expectedSen, paidSen);
                return false; // mismatch — do not settle
            }
        } catch (Exception e) {
            log.error("[TOYYIBPAY] getBillTransactions error billCode={}", billCode, e);
            throw new RuntimeException("ToyyibPay verify failed", e);
        }
        return false;
    }

    public String getPaymentUrl(String billCode) { return baseUrl + "/" + billCode; }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
