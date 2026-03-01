package me.sathish.event_service.domain;

public class DomainNotFoundException extends RuntimeException {

    private final String lookup;

    public DomainNotFoundException(String domainName) {
        super("Domain '%s' not found".formatted(domainName));
        this.lookup = domainName;
    }

    public DomainNotFoundException(Long domainId) {
        super("Domain with id '%d' not found".formatted(domainId));
        this.lookup = String.valueOf(domainId);
    }

    public String getLookup() {
        return lookup;
    }
}
