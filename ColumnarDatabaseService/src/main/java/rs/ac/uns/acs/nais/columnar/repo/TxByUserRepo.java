package rs.ac.uns.acs.nais.columnar.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.model.TxByUserKey;

@Repository
public interface TxByUserRepo extends CassandraRepository<TxByUser, TxByUserKey> {

    @Query("SELECT * FROM transactions_by_user WHERE user_id=:uid AND tx_date=:d LIMIT :lim")
    List<TxByUser> findDay(UUID uid, LocalDate d, int lim);

    @Query("SELECT * FROM transactions_by_user WHERE user_id=:uid AND tx_date=:d AND tx_time >= :from AND tx_time <= :to LIMIT :lim")
    List<TxByUser> findByTimeRange(UUID uid, LocalDate d, UUID from, UUID to, int lim);
}
