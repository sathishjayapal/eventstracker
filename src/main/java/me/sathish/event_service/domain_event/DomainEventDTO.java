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

    @NotNull @Size(max = 255)
    private String createdBy;

    @NotNull @Size(max = 255)
    private String updatedBy;

    private Long domain;
}
