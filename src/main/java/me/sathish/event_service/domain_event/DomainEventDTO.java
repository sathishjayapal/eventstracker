package me.sathish.event_service.domain_event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;


@Getter
@Setter
public class DomainEventDTO {

    private Long id;

    @NotNull
    private String eventId;

    @NotNull
    private String eventType;

    @NotNull
    private String payload;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime createdAt;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime updatedAt;

    @NotNull
    @Size(max = 20)
    private String createdBy;

    @NotNull
    @Size(max = 20)
    private String updatedBy;

    private Long domain;

}
