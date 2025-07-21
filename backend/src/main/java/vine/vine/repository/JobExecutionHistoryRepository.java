package vine.vine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vine.vine.domain.JobExecutionHistoryEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobExecutionHistoryRepository extends JpaRepository<JobExecutionHistoryEntity, Long> {

    List<JobExecutionHistoryEntity> findByJobNameAndJobGroupOrderByStartTimeDesc(String jobName, String jobGroup);

    @Query("SELECT j FROM JobExecutionHistoryEntity j WHERE j.jobName = :jobName AND j.jobGroup = :jobGroup ORDER BY j.startTime DESC")
    List<JobExecutionHistoryEntity> findJobHistory(@Param("jobName") String jobName, @Param("jobGroup") String jobGroup);

    @Query("SELECT j FROM JobExecutionHistoryEntity j WHERE j.jobName = :jobName AND j.jobGroup = :jobGroup AND j.status = 'COMPLETED' ORDER BY j.startTime DESC")
    List<JobExecutionHistoryEntity> findSuccessfulExecutions(@Param("jobName") String jobName, @Param("jobGroup") String jobGroup);

    @Query("SELECT j FROM JobExecutionHistoryEntity j WHERE j.jobName = :jobName AND j.jobGroup = :jobGroup AND j.startTime >= :fromDate ORDER BY j.startTime DESC")
    List<JobExecutionHistoryEntity> findJobHistoryFromDate(@Param("jobName") String jobName, @Param("jobGroup") String jobGroup, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT j FROM JobExecutionHistoryEntity j WHERE j.jobName = :jobName AND j.jobGroup = :jobGroup AND j.status = 'COMPLETED' ORDER BY j.endTime DESC LIMIT 1")
    JobExecutionHistoryEntity findLastSuccessfulExecution(@Param("jobName") String jobName, @Param("jobGroup") String jobGroup);
}

