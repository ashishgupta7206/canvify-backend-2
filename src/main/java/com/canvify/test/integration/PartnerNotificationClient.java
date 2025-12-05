package com.canvify.test.integration;

import java.util.Map;

public interface PartnerNotificationClient {

    /**
     * Notify partner of new assignment. Return true if delivered to partner system/SMS.
     */
    boolean notifyAssignment(Long partnerId, Map<String, Object> payload);

    /**
     * Send update/confirmation messages to partner.
     */
    boolean notifyUpdate(Long partnerId, Map<String,Object> payload);
}