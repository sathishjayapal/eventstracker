package me.sathish.event_service.domain_event;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.util.List;

import me.sathish.event_service.security.UserRoles;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/domainEvents", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('" + UserRoles.AUTH_USER + "')")
public class DomainEventResource {

    private final DomainEventService domainEventService;

    public DomainEventResource(final DomainEventService domainEventService) {
        this.domainEventService = domainEventService;
    }

    @GetMapping
    public ResponseEntity<List<DomainEventDTO>> getAllDomainEvents() {
        return ResponseEntity.ok(domainEventService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomainEventDTO> getDomainEvent(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(domainEventService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createDomainEvent(@RequestBody @Valid final DomainEventDTO domainEventDTO) {
        final Long createdId;
        try {
            createdId = domainEventService.create(domainEventDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateDomainEvent(
            @PathVariable(name = "id") final Long id, @RequestBody @Valid final DomainEventDTO domainEventDTO) {
        domainEventService.update(id, domainEventDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteDomainEvent(@PathVariable(name = "id") final Long id) {
        domainEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
