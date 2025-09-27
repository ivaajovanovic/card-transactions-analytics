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

    @Query("SELECT * FROM transactions_by_category WHERE category_id=:cid AND tx_date=:d LIMIT :lim")
    List<TxByCategory> findDay(UUID cid, LocalDate d, int lim);
}
