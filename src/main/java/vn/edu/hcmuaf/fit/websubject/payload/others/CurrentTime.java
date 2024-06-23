package vn.edu.hcmuaf.fit.websubject.payload.others;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class CurrentTime {
    public static Date getCurrentTimeInVietnam() {
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime localDateTime = LocalDateTime.now(zoneId);
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }
}
