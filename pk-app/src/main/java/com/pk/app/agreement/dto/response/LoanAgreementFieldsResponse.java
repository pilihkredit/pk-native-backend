package com.pk.app.agreement.dto.response;

public record LoanAgreementFieldsResponse(
        String agreementNo,
        String lenderName,
        String lenderLaw,
        String lenderAddress,
        String lenderRepName,
        String lenderRepTitle,
        String borrowerName,
        String borrowerNik,
        String borrowerAddress,
        String borrowerBirthDate,
        Integer borrowerAge,
        String borrowerEmail,
        String borrowerPhone,
        String borrowerBankAccount,
        String borrowerAccountHolder,
        String fundingPurpose,
        String eSignFeeDisplay,
        String effectiveDateDisplay,
        String maturityDateDisplay
) {
}
