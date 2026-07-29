package com.pk.app.agreement.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.app.agreement.dto.response.LoanAgreementFieldsResponse;
import com.pk.app.agreement.dto.response.ProductSummaryFieldsResponse;
import com.pk.app.agreement.dto.response.StatementFieldsResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.display.DisplayFormatters;
import com.pk.core.loan.LoanTrialQuoteDetail;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.infra.agreement.AgreementFieldConfigLoader;
import com.pk.infra.profile.ProfileQueryFacade;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgreementFieldApplicationService {
    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<DateTimeFormatter> BIRTHDAY_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DISPLAY_DATE,
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    private final ProfileQueryFacade profileQueryFacade;
    private final LoanQuoteRepository loanQuoteRepository;
    private final AgreementFieldConfigLoader configLoader;
    private final Clock clock;

    @Autowired
    public AgreementFieldApplicationService(
            ProfileQueryFacade profileQueryFacade,
            LoanQuoteRepository loanQuoteRepository,
            AgreementFieldConfigLoader configLoader
    ) {
        this(
                profileQueryFacade,
                loanQuoteRepository,
                configLoader,
                Clock.system(JAKARTA_ZONE)
        );
    }

    AgreementFieldApplicationService(
            ProfileQueryFacade profileQueryFacade,
            LoanQuoteRepository loanQuoteRepository,
            AgreementFieldConfigLoader configLoader,
            Clock clock
    ) {
        this.profileQueryFacade = profileQueryFacade;
        this.loanQuoteRepository = loanQuoteRepository;
        this.configLoader = configLoader;
        this.clock = clock;
    }

    public ProductSummaryFieldsResponse productSummary(AuthenticatedPrincipal principal) {
        requirePrincipal(principal);
        AgreementFieldConfigLoader.AgreementFieldConfig config = configLoader.load();
        return new ProductSummaryFieldsResponse(
                config.publisherName(),
                config.productType(),
                config.productDescription(),
                config.provisionFeeDisplay()
        );
    }

    public StatementFieldsResponse statement(AuthenticatedPrincipal principal) {
        requirePrincipal(principal);
        JsonNode profile = profileQueryFacade.query(principal.partnerUserId(), List.of("identity"));
        return new StatementFieldsResponse(
                text(profile, "identity", "name"),
                text(profile, "identity", "idNo"),
                LocalDate.now(clock).format(DISPLAY_DATE)
        );
    }

    public LoanAgreementFieldsResponse loanPreview(
            AuthenticatedPrincipal principal,
            String quoteNo
    ) {
        requirePrincipal(principal);
        String normalizedQuoteNo = requireText(quoteNo);
        LoanQuoteRepository.LoanQuoteRecord quoteRecord = loanQuoteRepository
                .findByQuoteNo(normalizedQuoteNo)
                .orElseThrow(() -> new ApiException(ApiCode.QUOTE_SNAPSHOT_EXPIRED));
        if (quoteRecord.userId() == null || quoteRecord.userId() != principal.userId()) {
            throw new ApiException(ApiCode.QUOTE_SNAPSHOT_MISMATCH);
        }

        JsonNode profile = profileQueryFacade.query(
                principal.partnerUserId(),
                List.of("mobileNo", "identity", "bankCard")
        );
        AgreementFieldConfigLoader.AgreementFieldConfig config = configLoader.load();
        LoanTrialQuoteDetail quote = quoteRecord.quote();
        LocalDate birthday = parseBirthday(text(profile, "identity", "ocrResult", "birthday"));
        String borrowerName = text(profile, "identity", "name");
        String bankAccount = defaultBankAccount(profile.path("bankCardList"));

        return new LoanAgreementFieldsResponse(
                null,
                config.lenderName(),
                config.lenderLaw(),
                config.lenderAddress(),
                config.lenderRepName(),
                config.lenderRepTitle(),
                borrowerName,
                text(profile, "identity", "idNo"),
                text(profile, "identity", "ocrResult", "address"),
                birthday == null ? null : birthday.format(DISPLAY_DATE),
                birthday == null ? null : Period.between(birthday, LocalDate.now(clock)).getYears(),
                null,
                text(profile, "mobileNo", "mobileNo"),
                bankAccount,
                bankAccount == null ? null : borrowerName,
                config.fundingPurpose(),
                electronicSignatureFee(quote),
                DisplayFormatters.formatJakartaDate(quote.lendingDate()),
                DisplayFormatters.formatJakartaDate(quote.lastRepayDate())
        );
    }

    private static void requirePrincipal(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank() || value.length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return value.trim();
    }

    private static String text(JsonNode root, String... path) {
        JsonNode node = root;
        for (String element : path) {
            node = node.path(element);
        }
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value;
    }

    private static String defaultBankAccount(JsonNode bankCardList) {
        if (!bankCardList.isArray()) {
            return null;
        }
        for (JsonNode bankCard : bankCardList) {
            if (bankCard.path("isDefault").asBoolean(false)) {
                return text(bankCard, "cardNumber");
            }
        }
        return null;
    }

    private static LocalDate parseBirthday(String value) {
        if (value == null) {
            return null;
        }
        for (DateTimeFormatter formatter : BIRTHDAY_FORMATS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported lender date format.
            }
        }
        return null;
    }

    private static String electronicSignatureFee(LoanTrialQuoteDetail quote) {
        BigDecimal amount = matchingFee(quote.fee1Name(), quote.fee1());
        if (amount == null) {
            amount = matchingFee(quote.fee2Name(), quote.fee2());
        }
        if (amount == null) {
            amount = matchingFee(quote.fee3Name(), quote.fee3());
        }
        return amount == null ? null : DisplayFormatters.formatIdrAmount(amount);
    }

    private static BigDecimal matchingFee(String name, BigDecimal amount) {
        if (name == null || amount == null) {
            return null;
        }
        String normalized = name.toLowerCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");
        return normalized.contains("esign")
                || normalized.contains("electronicsignature")
                || normalized.contains("tandatanganelektronik")
                ? amount
                : null;
    }
}
