package com.sathish.runevents.domain.data;

import com.sathish.runevents.data.BaseEvents;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "domains", schema = "runeventsprojectschema")
@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class Domain extends BaseEvents {
    public Domain(String domainName, String status, String comments) {
        this.domainName = domainName;
        this.status = status;
        this.comments = comments;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "domain_id_seq")
    @SequenceGenerator(name = "domain_id_seq", sequenceName = "runeventsprojectschema.domain_id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "domain_name", nullable = false, length = Integer.MAX_VALUE)
    private String domainName;

    @NotNull
    @Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
    private String status;

    @Column(name = "comments", length = Integer.MAX_VALUE)
    private String comments;
}
