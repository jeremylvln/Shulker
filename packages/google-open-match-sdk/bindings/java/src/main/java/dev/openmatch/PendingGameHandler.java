package dev.openmatch;

public interface PendingGameHandler {
    void endMatchmaking();
    void updateAvailableSlots(int missingPlayers);
}
