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

    @Query("SELECT * FROM transactions_by_merchant WHERE merchant_id=:mid AND tx_date=:d LIMIT :lim")
    List<TxByMerchant> findDay(UUID mid, LocalDate d, int lim);
}
