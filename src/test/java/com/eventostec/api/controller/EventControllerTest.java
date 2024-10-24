package com.eventostec.api.controller;

import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test_createEventSuccess() throws Exception {
        EventRequestDTO requestDTO = new EventRequestDTO("Evento Teste", "Descrição do evento", new Date().getTime(), "Cidade Teste", "UF", true, "https://evento.com", null);
        Event responseEvent = new Event();

        when(eventService.createEvent(requestDTO)).thenReturn(responseEvent);

        mockMvc.perform(multipart("/api/event")
                        .file("image", new byte[0])
                        .param("title", requestDTO.title())
                        .param("description", requestDTO.description())
                        .param("date", String.valueOf(requestDTO.date()))
                        .param("city", requestDTO.city())
                        .param("state", requestDTO.state())
                        .param("remote", String.valueOf(requestDTO.remote()))
                        .param("eventUrl", requestDTO.eventUrl())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @Test
    void test_getEventDetails() throws Exception {
        UUID eventId = UUID.randomUUID();
        List<EventDetailsDTO.CouponDTO> coupons = List.of(
                new EventDetailsDTO.CouponDTO("CODE123", 10, new Date()),
                new EventDetailsDTO.CouponDTO("CODE456", 20, new Date())
        );
        EventDetailsDTO eventDetailsDTO = new EventDetailsDTO(
                UUID.fromString("b4919825-dd11-4f6c-b77d-faffb48c1801"),
                "Teste de evento",
                "A maior conferencia do mundo",
                new Date(),
                "Brasilia",
                "DF",
                "",
                "https://www.teste.com",
                coupons
        );

        when(eventService.getEventDetails(eventId)).thenReturn(eventDetailsDTO);

        mockMvc.perform(get("/api/event/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void test_getEvents() throws Exception {
        List<EventResponseDTO> responseList = getEventResponseDTO();

        when(eventService.getUpcomingEvents(0, 10)).thenReturn(responseList);

        mockMvc.perform(get("/api/event")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void test_deleteEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        String adminKey = "testAdminKey";

        doNothing().when(eventService).deleteEvent(eventId, adminKey);

        mockMvc.perform(delete("/api/event/{eventId}", eventId)
                        .content(adminKey)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isNoContent());
    }

    @Test
    void test_getFilteredEvents() throws Exception {
        List<EventResponseDTO> responseList = getEventResponseDTO();
        String startDate = "2024-10-24";
        String endDate = "2024-10-25";

        when(eventService.getFilteredEvents(0, 10, "Brasilia", "DF",
                new SimpleDateFormat("yyyy-MM-dd").parse(startDate),
                new SimpleDateFormat("yyyy-MM-dd").parse(endDate))).thenReturn(responseList);

        mockMvc.perform(get("/api/event/filter")
                        .param("page", "0")
                        .param("size", "10")
                        .param("city", "Brasilia")
                        .param("uf", "DF")
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void test_getSearchEvents() throws Exception {
        List<EventResponseDTO> responseList = getEventResponseDTO();

        when(eventService.searchEvents("evento")).thenReturn(responseList);

        mockMvc.perform(get("/api/event/search")
                        .param("title", "evento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    private static List<EventResponseDTO> getEventResponseDTO() {
        return List.of(new EventResponseDTO(
                UUID.fromString("b4919825-dd11-4f6c-b77d-faffb48c1801"),
                "Teste de evento",
                "A maior conferencia do mundo",
                new Date(),
                "Brasilia",
                "DF",
                false,
                "https://www.teste.com",
                ""
        ));
    }
}
