package net.binder.api.common.binsetup.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.binder.api.common.kakaomap.dto.ProcessedBinData;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BinBatchInsertRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsertInitialBins(List<ProcessedBinData> dataList){
        String sql = """ 
                INSERT IGNORE INTO bin (title, type, point, address, like_count, dislike_count, bookmark_count, image_url, created_at)\s
                  VALUES (?, ?, ST_GeomFromText(?, 4326), ?, ?, ?, ?, ?, ?)
                  """;
        
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ProcessedBinData processedBinData = dataList.get(i);
                if(processedBinData==null){
                    return;
                }
                ps.setString(1, processedBinData.getTitle());
                ps.setString(2, processedBinData.getType().name());
                ps.setString(3, String.format("POINT(%.12f %.13f)", processedBinData.getLatitude(), processedBinData.getLongitude()));
                ps.setString(4, processedBinData.getAddress());
                ps.setLong(5, 0L);
                ps.setLong(6, 0L);
                ps.setLong(7, 0L);
                ps.setString(8, null);
                ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            }

            @Override
            public int getBatchSize() {
                return dataList.size();
            }
        });
    }
}
