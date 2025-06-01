package me.sathish.event_service.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DomainDTO {

    private Long id;

    @NotNull
    private String domainName;

    @NotNull
    private String status;

    private String comments;

}
