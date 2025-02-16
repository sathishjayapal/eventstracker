package com.sathish.runevents.domain.util;

import com.sathish.runevents.domain.data.Domain;
import com.sathish.runevents.domain.repo.DomainRepo;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DomainDataServiceUtil {
    private final DomainRepo domainRepo;

    public DomainDataServiceUtil(DomainRepo domainRepo) {
        this.domainRepo = domainRepo;
    }

    public void loadDomainData() {
        if (domainRepo.count() > 0) {
            return;
        }

        List<Domain> domainList = Arrays.asList(
                new Domain("GARMIN", "Active", "Garmin domain related data"),
                new Domain("NIKE", "Inactive", "Nike domain related data"),
                new Domain("APPLE", "Active", "Apple domain related data"));

        domainRepo.persistAll(domainList);
    }
}
