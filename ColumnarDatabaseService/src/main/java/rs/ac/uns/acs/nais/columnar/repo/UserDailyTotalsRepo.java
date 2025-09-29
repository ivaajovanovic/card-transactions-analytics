package rs.ac.uns.acs.nais.columnar.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import rs.ac.uns.acs.nais.columnar.model.UserDailyTotal;

@Repository
public interface UserDailyTotalsRepo extends CassandraRepository<UserDailyTotal, UUID> {

    @Query("SELECT * FROM user_daily_totals WHERE user_id=:uid AND tx_date=:d")
    Optional<UserDailyTotal> findOneDay(UUID uid, LocalDate d);

    @Query("SELECT * FROM user_daily_totals WHERE user_id=:uid AND tx_date >= :from AND tx_date <= :to")
    List<UserDailyTotal> findRange(UUID uid, LocalDate from, LocalDate to);
}
