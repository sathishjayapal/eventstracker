package com.sathish.runevents.events.data;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class DomainEventDTO {
    private Long id;
    private String eventId;
    private String type;
    private String payload;
    private String domainName;
}
