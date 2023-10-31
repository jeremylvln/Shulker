package io.shulkermc.sdk;

import java.util.concurrent.CompletableFuture;

public interface ShulkerSDK {
    CompletableFuture<String> summonFromFleet(String namespace, String fleetName);
}
