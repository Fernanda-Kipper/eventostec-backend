package com.eventostec.api.service;

import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.coupon.CouponRequestDTO;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.repositories.CouponRepository;
import com.eventostec.api.repositories.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_shouldSaveCoupon() {
        UUID eventId = UUID.randomUUID();
        Event event = new Event();
        event.setId(eventId);
        CouponRequestDTO couponData = new CouponRequestDTO("TESTCODE", 20, new Date().getTime());
        Coupon coupon = new Coupon();
        coupon.setCode(couponData.code());
        coupon.setDiscount(couponData.discount());
        coupon.setValid(new Date(couponData.valid()));
        coupon.setEvent(event);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        Coupon savedCoupon = couponService.addCouponToEvent(eventId, couponData);

        assertNotNull(savedCoupon);
        assertEquals("TESTCODE", savedCoupon.getCode());
        verify(eventRepository, times(1)).findById(eventId);
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void test_shouldThrowExceptionWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();
        CouponRequestDTO couponData = new CouponRequestDTO("TESTCODE", 20, new Date().getTime());

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> couponService.addCouponToEvent(eventId, couponData));
        verify(eventRepository, times(1)).findById(eventId);
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void test_shouldReturnListOfCoupons() {
        UUID eventId = UUID.randomUUID();
        Date currentDate = new Date();

        Event event = new Event();
        event.setId(eventId);
        event.setTitle("Teste de evento");
        event.setDescription("Descrição do evento");
        event.setDate(new Date());
        event.setEventUrl("https://evento.com");

        List<Coupon> coupons = List.of(new Coupon(eventId, "CP123", 20, currentDate, event));

        when(couponRepository.findByEventIdAndValidAfter(eventId, currentDate)).thenReturn(coupons);

        List<Coupon> result = couponService.consultCoupons(eventId, currentDate);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("CP123", result.get(0).getCode());
        verify(couponRepository, times(1)).findByEventIdAndValidAfter(eventId, currentDate);
    }

    @Test
    void test_shouldReturnEmptyListWhenNoCouponsAvailable() {
        UUID eventId = UUID.randomUUID();
        Date currentDate = new Date();

        when(couponRepository.findByEventIdAndValidAfter(eventId, currentDate)).thenReturn(Collections.emptyList());

        List<Coupon> result = couponService.consultCoupons(eventId, currentDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(couponRepository, times(1)).findByEventIdAndValidAfter(eventId, currentDate);
    }
}
