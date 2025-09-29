package rs.ac.uns.acs.nais.columnar.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import rs.ac.uns.acs.nais.columnar.model.TxByMerchant;
import rs.ac.uns.acs.nais.columnar.model.TxByMerchantKey;

@Repository
public interface TxByMerchantRepo extends CassandraRepository<TxByMerchant, TxByMerchantKey> {

    @Query("SELECT * FROM transactions_by_merchant WHERE merchant_id=?0 AND tx_date=?1 LIMIT ?2")
    List<TxByMerchant> findDay(UUID merchantId, LocalDate date, int limit);

    @Query("SELECT * FROM transactions_by_merchant WHERE merchant_id=?0 AND tx_date=?1 AND tx_time < ?2 LIMIT ?3")
    List<TxByMerchant> findDayBefore(UUID merchantId, LocalDate date, UUID before, int limit);

    @Query("SELECT * FROM transactions_by_merchant WHERE merchant_id=?0 AND tx_date=?1 AND tx_time=?2")
    List<TxByMerchant> findExact(UUID merchantId, LocalDate date, UUID timeUuid);

    @Query("DELETE FROM transactions_by_merchant WHERE merchant_id=:mid AND tx_date=:d AND tx_time=:t")
    void deleteExact(UUID mid, LocalDate d, UUID t);
}
