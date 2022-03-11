package io.shulkermc.models;

import java.time.LocalDateTime;
import java.util.List;

public class MinecraftClusterStatus {
    private List<Condition> conditions;
    private int proxies;
    private int servers;
    private List<ServerPoolEntry> serverPool;

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public void setProxies(int proxies) {
        this.proxies = proxies;
    }

    public void setServers(int servers) {
        this.servers = servers;
    }

    public void setServerPool(List<ServerPoolEntry> serverPool) {
        this.serverPool = serverPool;
    }

    public List<Condition> getConditions() {
        return this.conditions;
    }

    public int getProxies() {
        return this.proxies;
    }

    public int getServers() {
        return this.servers;
    }

    public List<ServerPoolEntry> getServerPool() {
        return this.serverPool;
    }

    public static class Condition {
        private String type;
        private String status;
        private int observedGeneration;
        private LocalDateTime lastTransitionTime;
        private String reason;
        private String message;

        public void setType(String type) {
            this.type = type;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setObservedGeneration(int observedGeneration) {
            this.observedGeneration = observedGeneration;
        }

        public void setLastTransitionTime(LocalDateTime lastTransitionTime) {
            this.lastTransitionTime = lastTransitionTime;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return this.type;
        }

        public String getStatus() {
            return this.status;
        }

        public int getObservedGeneration() {
            return this.observedGeneration;
        }

        public LocalDateTime getLastTransitionTime() {
            return this.lastTransitionTime;
        }

        public String getReason() {
            return this.reason;
        }

        public String getMessage() {
            return this.message;
        }
    }

    public static class ServerPoolEntry {
        private String name;
        private String address;

        public void setName(String name) {
            this.name = name;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getName() {
            return this.name;
        }

        public String getAddress() {
            return this.address;
        }
    }
}
