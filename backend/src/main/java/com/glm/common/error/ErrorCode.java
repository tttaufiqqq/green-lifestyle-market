package com.glm.common.error;

/**
 * All error codes from docs/error-catalogue.md.
 * New codes must be added here first before use (fail loud, rule 12).
 */
public enum ErrorCode {
    E_VAL("E-VAL"),
    E_AUTH_CRED("E-AUTH-CRED"),
    E_AUTH_LOCKED("E-AUTH-LOCKED"),
    E_AUTH_SUSPENDED("E-AUTH-SUSPENDED"),
    E_AUTH_OWN("E-AUTH-OWN"),
    E_AUTH_VERIFY("E-AUTH-VERIFY"),
    E_NOTFOUND("E-NOTFOUND"),
    E_LIST_FULFIL("E-LIST-FULFIL"),
    E_LIST_QTY_HELD("E-LIST-QTY-HELD"),
    E_LIST_OPEN_ORDERS("E-LIST-OPEN-ORDERS"),
    E_CART_OWN("E-CART-OWN"),
    E_CART_STOCK("E-CART-STOCK"),
    E_CHK_EMPTY("E-CHK-EMPTY"),
    E_CHK_MIN("E-CHK-MIN"),
    E_CHK_ADDRESS("E-CHK-ADDRESS"),
    E_CHK_STOCK("E-CHK-STOCK"),
    E_PAY_GATEWAY("E-PAY-GATEWAY"),
    E_PAY_STATE("E-PAY-STATE"),
    E_ORD_STATE("E-ORD-STATE"),
    E_REF_EXISTS("E-REF-EXISTS"),
    E_REF_PAIDOUT("E-REF-PAIDOUT"),
    E_PO_BANK("E-PO-BANK"),
    E_PO_DUP("E-PO-DUP"),
    E_UPLOAD_TYPE("E-UPLOAD-TYPE"),
    E_UPLOAD_SIZE("E-UPLOAD-SIZE"),
    E_RATE("E-RATE"),
    E_INTERNAL("E-INTERNAL");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
