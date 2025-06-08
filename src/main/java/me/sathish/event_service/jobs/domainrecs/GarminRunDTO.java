package me.sathish.event_service.jobs.domainrecs;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GarminRunDTO {

    private Long id;

    private Integer activityID;

    private String activityDate;

    @Size(max = 25)
    private String activityType;

    @Size(max = 100)
    private String activityName;

    @Size(max = 255)
    private String activityDescription;

    @Size(max = 255)
    private String elapsedTime;

    @Size(max = 255)
    private String distance;

    @Size(max = 255)
    private String maxHeartRate;

    private Long runner;
}
