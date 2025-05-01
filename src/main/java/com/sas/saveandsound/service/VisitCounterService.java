package com.sas.saveandsound.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class VisitCounterService {

    private final AtomicInteger visitCounter = new AtomicInteger(0);

    public int incrementAndGet() {
        return visitCounter.incrementAndGet();
    }

    public int getVisitCount() {
        return visitCounter.get();
    }
}
