package me.sathish.event_service.domain_event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.sathish.event_service.domain.Domain;
import me.sathish.event_service.domain.DomainRepository;
import me.sathish.event_service.util.ApplicationProperties;
import me.sathish.event_service.util.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class DomainEventServiceTest {

    @Mock
    private DomainEventRepository domainEventRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private DomainEventMapper domainEventMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private DomainEventService domainEventService;

    private DomainEvent testDomainEvent;
    private DomainEventDTO testDomainEventDTO;
    private Domain testDomain;

    @BeforeEach
    void setUp() {
        testDomain = new Domain();
        testDomain.setId(1L);
        testDomain.setDomainName("Test Domain");

        testDomainEvent = new DomainEvent();
        testDomainEvent.setId(1L);
        testDomainEvent.setEventType("TEST_EVENT");
        testDomainEvent.setPayload("Test event payload");
        testDomainEvent.setEventId(UUID.randomUUID().toString());
        testDomainEvent.setDomain(testDomain);
        testDomainEvent.setDateCreated(OffsetDateTime.now());

        testDomainEventDTO = new DomainEventDTO();
        testDomainEventDTO.setId(1L);
        testDomainEventDTO.setEventId(UUID.randomUUID().toString());
        testDomainEventDTO.setEventType("TEST_EVENT");
        testDomainEventDTO.setPayload("Test event payload");
        testDomainEventDTO.setDomain(1L);
    }

    @Test
    void findAll_ShouldReturnAllDomainEvents() {
        // Given
        List<DomainEvent> domainEvents = List.of(testDomainEvent);
        when(domainEventRepository.findAll(Sort.by("id"))).thenReturn(domainEvents);
        when(domainEventMapper.updateDomainEventDTO(any(DomainEvent.class), any(DomainEventDTO.class)))
                .thenReturn(testDomainEventDTO);

        // When
        List<DomainEventDTO> result = domainEventService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDomainEventDTO, result.get(0));
        verify(domainEventRepository).findAll(Sort.by("id"));
        verify(domainEventMapper).updateDomainEventDTO(eq(testDomainEvent), any(DomainEventDTO.class));
    }

    @Test
    void get_WithValidId_ShouldReturnDomainEvent() {
        // Given
        Long eventId = 1L;
        when(domainEventRepository.findById(eventId)).thenReturn(Optional.of(testDomainEvent));
        when(domainEventMapper.updateDomainEventDTO(any(DomainEvent.class), any(DomainEventDTO.class)))
                .thenReturn(testDomainEventDTO);

        // When
        DomainEventDTO result = domainEventService.get(eventId);

        // Then
        assertNotNull(result);
        assertEquals(testDomainEventDTO, result);
        verify(domainEventRepository).findById(eventId);
        verify(domainEventMapper).updateDomainEventDTO(eq(testDomainEvent), any(DomainEventDTO.class));
    }

    @Test
    void get_WithInvalidId_ShouldThrowNotFoundException() {
        // Given
        Long eventId = 999L;
        when(domainEventRepository.findById(eventId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> domainEventService.get(eventId));
        verify(domainEventRepository).findById(eventId);
        verifyNoInteractions(domainEventMapper);
    }

    @Test
    void create_ShouldSaveEventAndPublishMessage() {
        // Given
        when(domainEventRepository.save(any(DomainEvent.class))).thenReturn(testDomainEvent);
        when(applicationProperties.garminExchange()).thenReturn("test-exchange");
        when(applicationProperties.garminNewRunQueue()).thenReturn("test-queue");

        // When
        Long result = null;
        try {
            result = domainEventService.create(testDomainEventDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Then
        assertEquals(testDomainEvent.getId(), result);
        verify(domainEventMapper)
                .updateDomainEvent(eq(testDomainEventDTO), any(DomainEvent.class), eq(domainRepository));
        verify(domainEventRepository).save(any(DomainEvent.class));
        verify(rabbitTemplate).convertAndSend("test-exchange", "test-queue", testDomainEventDTO);
    }

    @Test
    void create_WhenRabbitMQFails_ShouldThrowRuntimeException() {
        // Given
        when(domainEventRepository.save(any(DomainEvent.class))).thenReturn(testDomainEvent);
        when(applicationProperties.garminExchange()).thenReturn("test-exchange");
        when(applicationProperties.garminNewRunQueue()).thenReturn("test-queue");
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate)
                .convertAndSend(anyString());

        // When & Then

        Exception exception = assertThrows(Exception.class, () -> domainEventService.create(testDomainEventDTO));
        assertEquals("Failed to publish domain event message", exception.getMessage());

        verify(domainEventRepository).save(any(DomainEvent.class));
        verify(rabbitTemplate).convertAndSend("test-exchange", "test-queue", testDomainEventDTO);
    }

    @Test
    void update_WithValidId_ShouldUpdateEvent() {
        // Given
        Long eventId = 1L;
        when(domainEventRepository.findById(eventId)).thenReturn(Optional.of(testDomainEvent));
        when(domainEventRepository.save(any(DomainEvent.class))).thenReturn(testDomainEvent);

        // When
        domainEventService.update(eventId, testDomainEventDTO);

        // Then
        verify(domainEventRepository).findById(eventId);
        verify(domainEventMapper).updateDomainEvent(testDomainEventDTO, testDomainEvent, domainRepository);
        verify(domainEventRepository).save(testDomainEvent);
    }

    @Test
    void update_WithInvalidId_ShouldThrowNotFoundException() {
        // Given
        Long eventId = 999L;
        when(domainEventRepository.findById(eventId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> domainEventService.update(eventId, testDomainEventDTO));
        verify(domainEventRepository).findById(eventId);
        verifyNoInteractions(domainEventMapper);
        verify(domainEventRepository, never()).save(any());
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        // Given
        Long eventId = 1L;

        // When
        domainEventService.delete(eventId);

        // Then
        verify(domainEventRepository).deleteById(eventId);
    }
}
