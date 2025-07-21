package vine.vine.domain.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerHistoryResponse {
    private List<JobExecutionDto> executions;
    private long totalCount;
    private int page;
    private int size;
}