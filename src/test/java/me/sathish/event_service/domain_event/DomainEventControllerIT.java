package me.sathish.event_service.domain_event;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.sathish.event_service.config.EventserviceconfigSecurityConfig;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.security.EventserviceconfigUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DomainEventController.class)
@Import(EventserviceconfigSecurityConfig.class)
class DomainEventControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DomainEventService domainEventService;

    @MockitoBean
    private DomainRepository domainRepository;

    @MockitoBean
    private EventserviceconfigUserDetailsService eventserviceconfigUserDetailsService;

    @BeforeEach
    void setUp() {
        when(domainRepository.findAll(any(Sort.class))).thenReturn(List.of());
        when(domainEventService.findAll()).thenReturn(List.of());
    }

    @Test
    @WithMockUser(authorities = "AUTH_USER")
    void authUser_canListDomainEvents() throws Exception {
        mockMvc.perform(get("/domainEvents"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "AUTH_USER")
    void authUser_listPage_doesNotShowEditOrDeleteButtons() throws Exception {
        DomainEventDTO event = sampleEvent();
        when(domainEventService.findAll()).thenReturn(List.of(event));

        mockMvc.perform(get("/domainEvents"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/domainEvents/edit/"))))
                .andExpect(content().string(not(containsString("/domainEvents/delete/"))));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void admin_listPage_showsEditAndDeleteButtons() throws Exception {
        DomainEventDTO event = sampleEvent();
        when(domainEventService.findAll()).thenReturn(List.of(event));

        mockMvc.perform(get("/domainEvents"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/domainEvents/edit/")))
                .andExpect(content().string(containsString("/domainEvents/delete/")));
    }

    @Test
    @WithMockUser(authorities = "AUTH_USER")
    void authUser_cannotAccessEditPage() throws Exception {
        mockMvc.perform(get("/domainEvents/edit/1100"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "AUTH_USER")
    void authUser_cannotAccessAddPage() throws Exception {
        mockMvc.perform(get("/domainEvents/add"))
                .andExpect(status().isOk()); // add is not restricted beyond AUTH_USER
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void admin_canAccessEditPage() throws Exception {
        when(domainEventService.get(1100L)).thenReturn(sampleEvent());

        mockMvc.perform(get("/domainEvents/edit/1100"))
                .andExpect(status().isOk());
    }

    private DomainEventDTO sampleEvent() {
        DomainEventDTO dto = new DomainEventDTO();
        dto.setId(1100L);
        dto.setEventId("evt-1");
        dto.setEventType("GARMIN");
        dto.setPayload("{}");
        dto.setCreatedBy("test");
        dto.setUpdatedBy("test");
        return dto;
    }
}