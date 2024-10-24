package com.eventostec.api.service;

import com.eventostec.api.domain.event.*;
import com.eventostec.api.repositories.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Utilities s3Utilities;

    @Mock
    private AddressService addressService;

    @Mock
    private CouponService couponService;

    @Mock
    private EventRepository repository;

    @InjectMocks
    private EventService eventService;

    private final String adminKey = "test-admin-key";
    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventService = new EventService(s3Client, addressService, couponService, repository);
        // Configurando valores diretamente nos atributos usando ReflectionTestUtils
        ReflectionTestUtils.setField(eventService, "adminKey", adminKey);
        ReflectionTestUtils.setField(eventService, "bucketName", bucketName);
        // Mockando S3Utilities
        when(s3Client.utilities()).thenReturn(s3Utilities);
    }

    @Test
    void test_shouldSaveEvent() {
        EventRequestDTO requestDTO = new EventRequestDTO("Evento Teste", "Descrição do evento", new Date().getTime(), "Cidade Teste", "UF", true, "https://evento.com", null);
        Event event = new Event();

        when(repository.save(any(Event.class))).thenReturn(event);

        Event savedEvent = eventService.createEvent(requestDTO);

        assertNotNull(savedEvent);
        verify(repository, times(1)).save(any(Event.class));
    }

    @Test
    void test_shouldReturnListOfEvents() {
        Pageable pageable = PageRequest.of(0, 10);
        List<EventAddressProjection> events = List.of(mock(EventAddressProjection.class));
        Page<EventAddressProjection> eventsPage = new PageImpl<>(events);

        when(repository.findUpcomingEvents(any(Date.class), eq(pageable))).thenReturn(eventsPage);

        List<EventResponseDTO> result = eventService.getUpcomingEvents(0, 10);

        assertFalse(result.isEmpty());
        verify(repository, times(1)).findUpcomingEvents(any(Date.class), eq(pageable));
    }

    @Test
    void test_shouldReturnEventDetails() {
        UUID eventId = UUID.randomUUID();
        Event event = new Event();
        event.setId(eventId);
        event.setTitle("Teste de evento");
        event.setDescription("Descrição do evento");
        event.setDate(new Date());
        event.setEventUrl("https://evento.com");

        when(repository.findById(eventId)).thenReturn(Optional.of(event));
        when(addressService.findByEventId(eventId)).thenReturn(Optional.empty());
        when(couponService.consultCoupons(eventId, new Date())).thenReturn(Collections.emptyList());

        EventDetailsDTO result = eventService.getEventDetails(eventId);

        assertNotNull(result);
        assertEquals(eventId, result.id());
        verify(repository, times(1)).findById(eventId);
    }

    @Test
    void test_shouldDeleteEvent() {
        UUID eventId = UUID.randomUUID();
        Event event = new Event();
        event.setId(eventId);

        when(repository.findById(eventId)).thenReturn(Optional.of(event));

        // Use a mesma chave de administrador configurada
        eventService.deleteEvent(eventId, adminKey);

        verify(repository, times(1)).delete(event);
    }

    @Test
    void test_shouldReturnMatchingEvents() {
        String title = "Evento Teste";
        List<EventAddressProjection> events = List.of(mock(EventAddressProjection.class));

        when(repository.findEventsByTitle(title)).thenReturn(events);

        List<EventResponseDTO> result = eventService.searchEvents(title);

        assertFalse(result.isEmpty());
        verify(repository, times(1)).findEventsByTitle(title);
    }

    @Test
    void test_shouldReturnFilteredEvents() {
        Pageable pageable = PageRequest.of(0, 10);
        List<EventAddressProjection> events = List.of(mock(EventAddressProjection.class));
        Page<EventAddressProjection> eventsPage = new PageImpl<>(events);

        when(repository.findFilteredEvents(anyString(), anyString(), any(Date.class), any(Date.class), eq(pageable))).thenReturn(eventsPage);

        List<EventResponseDTO> result = eventService.getFilteredEvents(0, 10, "Cidade Teste", "UF", new Date(), new Date());

        assertFalse(result.isEmpty());
        verify(repository, times(1)).findFilteredEvents(anyString(), anyString(), any(Date.class), any(Date.class), eq(pageable));
    }

    @Test
    void test_shouldReturnUrlOnUploadImage() throws Exception {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        when(multipartFile.getOriginalFilename()).thenReturn("imagem.jpg");
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(URI.create("https://s3.amazonaws.com/teste/imagem.jpg").toURL());

        ReflectionTestUtils.setField(eventService, "bucketName", bucketName);
        String result = ReflectionTestUtils.invokeMethod(eventService, "uploadImg", multipartFile);

        assertNotNull(result);
        assertTrue(result.contains("https://s3.amazonaws.com"));
    }
}
