package rs.ac.uns.acs.nais.columnar.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class DedupService {

    private final CqlSession session;
    private final PreparedStatement insertIfNotExists;

    public DedupService(CqlSession session) {
        this.session = session;
        this.insertIfNotExists = session.prepare(
            "INSERT INTO tx_dedup (tx_id, seen_at) VALUES (?, ?) IF NOT EXISTS"
        );
    }

    /** @return true ako je prvi put viđen tx_id, false ako je duplikat */
    public boolean tryMarkSeen(UUID txId) {
        BoundStatement bs = insertIfNotExists.bind(txId, Instant.now());
        ResultSet rs = session.execute(bs);
        return rs.wasApplied();
    }
}
