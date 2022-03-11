package io.shulkermc.models;

import java.util.List;

public class MinecraftClusterPool {
    private List<ServerEntry> servers;

    public void setServers(List<ServerEntry> servers) {
        this.servers = servers;
    }

    public List<ServerEntry> getServers() {
        return this.servers;
    }

    public static class ServerEntry {
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
