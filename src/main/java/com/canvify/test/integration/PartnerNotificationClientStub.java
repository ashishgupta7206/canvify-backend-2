package com.canvify.test.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PartnerNotificationClientStub implements PartnerNotificationClient {

    private static final Logger log = LoggerFactory.getLogger(PartnerNotificationClientStub.class);

    @Override
    public boolean notifyAssignment(Long partnerId, Map<String, Object> payload) {
        log.info("Stub notifyAssignment partnerId={} payload={}", partnerId, payload);
        return true; // pretend success
    }

    @Override
    public boolean notifyUpdate(Long partnerId, Map<String, Object> payload) {
        log.info("Stub notifyUpdate partnerId={} payload={}", partnerId, payload);
        return true;
    }
}