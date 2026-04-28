package net.blackhacker.ares.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BatchJobExecutionDTO {
    private Long id;
    private String jobName;
    private String status;
    private String exitCode;
    private String exitMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
