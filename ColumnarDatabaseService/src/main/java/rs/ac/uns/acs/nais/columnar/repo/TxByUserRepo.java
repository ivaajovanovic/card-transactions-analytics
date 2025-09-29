package rs.ac.uns.acs.nais.columnar.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.model.TxByUserKey;

@Repository
public interface TxByUserRepo extends CassandraRepository<TxByUser, TxByUserKey> {

    // Prva stranica (najnovije prvo)
    @Query("SELECT * FROM tx_by_user " +
           "WHERE user_id=:userId AND tx_date=:date " +
           "ORDER BY tx_time DESC " +
           "LIMIT :limit")
    List<TxByUser> findDay(@Param("userId") UUID userId,
                           @Param("date") LocalDate date,
                           @Param("limit") int limit);

    // Sledeće stranice pre kursora (strogo '<' da nema duplikata poslednjeg reda prethodne stranice)
    @Query("SELECT * FROM tx_by_user " +
           "WHERE user_id=:userId AND tx_date=:date AND tx_time < :before " +
           "ORDER BY tx_time DESC " +
           "LIMIT :limit")
    List<TxByUser> findDayBefore(@Param("userId") UUID userId,
                                 @Param("date") LocalDate date,
                                 @Param("before") UUID before,
                                 @Param("limit") int limit);

    @Query("DELETE FROM tx_by_user WHERE user_id=:uid AND tx_date=:d AND tx_time=:t")
    void deleteExact(UUID uid, LocalDate d, UUID t);
}
