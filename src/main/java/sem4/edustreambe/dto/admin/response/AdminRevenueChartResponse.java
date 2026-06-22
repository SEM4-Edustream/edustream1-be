package sem4.edustreambe.dto.admin.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminRevenueChartResponse {
    LocalDate date;
    BigDecimal revenue;

    public AdminRevenueChartResponse(java.sql.Date date, BigDecimal revenue) {
        this.date = date != null ? date.toLocalDate() : null;
        this.revenue = revenue;
    }
}
