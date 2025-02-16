package com.sathish.runevents.events.controller;

import com.sathish.runevents.events.data.DomainEventDTO;
import com.sathish.runevents.events.service.DomainEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/domain-events")
public class DomainEventController {
    private final DomainEventService domainEventService;

    public DomainEventController(DomainEventService domainEventService) {
        this.domainEventService = domainEventService;
    }

    @PostMapping
    public ResponseEntity<DomainEventDTO> createDomainEvent(@RequestBody DomainEventDTO domainEventDTO) {
        DomainEventDTO createdEvent = domainEventService.createDomainEvent(domainEventDTO);
        return ResponseEntity.ok(createdEvent);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomainEventDTO> getDomainEvent(@PathVariable Long id) {
        DomainEventDTO domainEventDTO = domainEventService.getDomainEvent(id);
        if (domainEventDTO != null) {
            return ResponseEntity.ok(domainEventDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
