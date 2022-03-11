package io.shulkermc.models;

public class MinecraftClusterStatus {
    private int proxies;
    private int servers;

    public void setProxies(int proxies) {
        this.proxies = proxies;
    }

    public void setServers(int servers) {
        this.servers = servers;
    }

    public int getProxies() {
        return this.proxies;
    }

    public int getServers() {
        return this.servers;
    }
}
