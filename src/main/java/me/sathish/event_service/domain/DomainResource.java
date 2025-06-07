package me.sathish.event_service.domain;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import me.sathish.event_service.security.UserRoles;
import me.sathish.event_service.util.ReferencedException;
import me.sathish.event_service.util.ReferencedWarning;
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
@RequestMapping(value = "/api/domains", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('" + UserRoles.AUTH_USER + "')")
public class DomainResource {

    private final DomainService domainService;

    public DomainResource(final DomainService domainService) {
        this.domainService = domainService;
    }

    @GetMapping
    public ResponseEntity<List<DomainDTO>> getAllDomains() {
        return ResponseEntity.ok(domainService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomainDTO> getDomain(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(domainService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createDomain(@RequestBody @Valid final DomainDTO domainDTO) {
        final Long createdId = domainService.create(domainDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateDomain(
            @PathVariable(name = "id") final Long id, @RequestBody @Valid final DomainDTO domainDTO) {
        domainService.update(id, domainDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteDomain(@PathVariable(name = "id") final Long id) {
        final ReferencedWarning referencedWarning = domainService.getReferencedWarning(id);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        domainService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
