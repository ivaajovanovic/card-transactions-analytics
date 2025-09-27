package rs.ac.uns.acs.nais.columnar.mapper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;
import rs.ac.uns.acs.nais.columnar.model.*;

public final class TxMapper {

    private TxMapper() {}

    public static TxByUser toUserEntity(TransactionDTO t, LocalDate date, UUID timeUuid) {
        return TxByUser.builder()
                .key(TxByUserKey.builder()
                        .userId(t.getUserId())
                        .txDate(date)
                        .txTime(timeUuid)
                        .build())
                .txId(t.getTxId())
                .cardId(t.getCardId())
                .merchantId(t.getMerchantId())
                .categoryId(t.getCategoryId())
                .amountCents(t.getAmountCents())
                .currency(t.getCurrency())
                .status(t.getStatus())
                .build();
    }

    public static TxByMerchant toMerchantEntity(TransactionDTO t, LocalDate date, UUID timeUuid) {
        return TxByMerchant.builder()
                .key(TxByMerchantKey.builder()
                        .merchantId(t.getMerchantId())
                        .txDate(date)
                        .txTime(timeUuid)
                        .build())
                .txId(t.getTxId())
                .userId(t.getUserId())
                .cardId(t.getCardId())
                .categoryId(t.getCategoryId())
                .amountCents(t.getAmountCents())
                .currency(t.getCurrency())
                .status(t.getStatus())
                .build();
    }

    public static TxByCategory toCategoryEntity(TransactionDTO t, LocalDate date, UUID timeUuid) {
        return TxByCategory.builder()
                .key(TxByCategoryKey.builder()
                        .categoryId(t.getCategoryId())
                        .txDate(date)
                        .txTime(timeUuid)
                        .build())
                .txId(t.getTxId())
                .userId(t.getUserId())
                .cardId(t.getCardId())
                .merchantId(t.getMerchantId())
                .amountCents(t.getAmountCents())
                .currency(t.getCurrency())
                .status(t.getStatus())
                .build();
    }

    public static LocalDate toUtcDate(java.time.Instant instant) {
        return instant.atOffset(ZoneOffset.UTC).toLocalDate();
    }
}
