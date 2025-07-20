package vine.vine.service.Impl;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vine.vine.domain.SysImageEntity;

@Service
@RequiredArgsConstructor
public class SysImageService {
    private final JdbcTemplate jdbcTemplate;

    public List<SysImageEntity> getImagesForNameId(Long nameId) {
        String sql = """
            SELECT syskey, sysid, ext1, addtime
            FROM sys_img
            WHERE syskey = 'N' AND sysid = ?
            ORDER BY addtime DESC
        """;

        return jdbcTemplate.query(sql, new Object[]{nameId}, (rs, rowNum) -> {
            SysImageEntity img = new SysImageEntity();
            img.setSystemKey(rs.getString("syskey"));
            img.setSystemId(rs.getLong("sysid"));
            img.setExt1(rs.getInt("ext1"));
            img.setAddTime(rs.getTimestamp("addtime").toLocalDateTime());
            return img;
        });
    }
}
