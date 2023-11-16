package io.shulkermc.sdk;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ShulkerSDK {
    CompletableFuture<String> allocateFromFleet(String namespace, String fleetName);
    CompletableFuture<String> allocateFromFleet(String namespace, String fleetName, boolean summonIfNeeded);
    CompletableFuture<String> allocateFromFleet(String namespace, String fleetName, boolean summonIfNeeded, Map<String, String> customAnnotations);
}
