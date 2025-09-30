package rs.ac.uns.acs.nais.columnar.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import rs.ac.uns.acs.nais.columnar.model.TxByCategory;
import rs.ac.uns.acs.nais.columnar.model.TxByCategoryKey;

@Repository
public interface TxByCategoryRepo extends CassandraRepository<TxByCategory, TxByCategoryKey> {

    @Query("SELECT * FROM transactions_by_category WHERE category_id=?0 AND tx_date=?1 LIMIT ?2")
    List<TxByCategory> findDay(UUID categoryId, LocalDate date, int limit);

    @Query("SELECT * FROM transactions_by_category WHERE category_id=?0 AND tx_date=?1 AND tx_time < ?2 LIMIT ?3")
    List<TxByCategory> findDayBefore(UUID categoryId, LocalDate date, UUID before, int limit);

    @Query("SELECT * FROM transactions_by_category WHERE category_id=?0 AND tx_date=?1 AND tx_time=?2")
    List<TxByCategory> findExact(UUID categoryId, LocalDate date, UUID timeUuid);

    @Query("DELETE FROM transactions_by_category WHERE category_id=:cid AND tx_date=:d AND tx_time=:t")
    void deleteExact(UUID cid, LocalDate d, UUID t);
    
}
