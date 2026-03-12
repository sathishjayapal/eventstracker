package me.sathish.event_service.domain_event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainEventDTO {

    private Long id;

    @NotNull private String eventId;

    @NotNull private String eventType;

    @NotNull private String payload;

    @Size(max = 255)
    private String createdBy;

    @Size(max = 255)
    private String updatedBy;

    @NotNull
    private Long domain;

    private String domainName;

    private String dateCreated;

    private String lastUpdated;
}