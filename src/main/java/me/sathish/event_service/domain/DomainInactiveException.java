package me.sathish.event_service.domain;

public class DomainInactiveException extends RuntimeException {

    private final String domainName;

    public DomainInactiveException(String domainName) {
        super("Domain '%s' is not active".formatted(domainName));
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }
}
