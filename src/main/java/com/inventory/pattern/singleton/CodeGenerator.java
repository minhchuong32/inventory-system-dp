package com.inventory.pattern.singleton;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class CodeGenerator {

    private final AtomicLong importCounter = new AtomicLong(0);
    
    private final AtomicLong exportCounter = new AtomicLong(0);

    public String generateImportCode() {
        long seq = importCounter.incrementAndGet();
        return String.format("PN%06d", seq);
    }

    public String generateExportCode() {
        long seq = exportCounter.incrementAndGet();
        return String.format("PX%06d", seq);
    }
}