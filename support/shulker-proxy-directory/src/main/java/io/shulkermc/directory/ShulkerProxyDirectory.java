package io.shulkermc.directory;

import io.fabric8.kubernetes.client.*;
import io.shulkermc.models.MinecraftCluster;
import io.shulkermc.models.MinecraftClusterList;
import io.shulkermc.models.MinecraftClusterStatus;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class ShulkerProxyDirectory extends Plugin {
    private final ProxyServer proxyServer;
    private Map<String, MinecraftClusterStatus.ServerPoolEntry> serverPool;
    private Map<String, List<String>> serversPerTag;

    public ShulkerProxyDirectory() {
        this.proxyServer = ProxyServer.getInstance();
        this.serverPool = new HashMap<>();
        this.serversPerTag = new HashMap<>();
    }

    @Override
    public void onEnable() {
        String shulkerClusterNamespace = System.getenv("SHULKER_CLUSTER_NAMESPACE");
        if (shulkerClusterNamespace == null) {
            this.getLogger().warning("No SHULKER_CLUSTER_NAMESPACE found in environment. Halting.");
            this.proxyServer.stop();
            return;
        }

        String shulkerClusterName = System.getenv("SHULKER_CLUSTER_NAME");
        if (shulkerClusterName == null) {
            this.getLogger().warning("No SHULKER_CLUSTER_NAME found in environment. Halting.");
            this.proxyServer.stop();
            return;
        }

        this.getLogger().info(String.format("Shulker cluster is \"%s/%s\"", shulkerClusterNamespace, shulkerClusterName));

        KubernetesClient kubernetesClient = new DefaultKubernetesClient();
        var minecraftClusterClient = kubernetesClient.customResources(MinecraftCluster.class, MinecraftClusterList.class);
        var minecraftClusterTarget = minecraftClusterClient.inNamespace(shulkerClusterNamespace).withName(shulkerClusterName);

        minecraftClusterTarget.watch(new Watcher<>() {
            @Override
            public void eventReceived(Action action, MinecraftCluster cluster) {
                if (action != Action.MODIFIED) return;

                MinecraftClusterStatus status = cluster.getStatus();
                if (status == null) return;

                ShulkerProxyDirectory.this.updateServerDirectory(status.getServerPool());
            }

            @Override
            public void onClose(WatcherException cause) {
                cause.asClientException().printStackTrace();
            }
        });

        try {
            this.updateServerDirectory(minecraftClusterTarget.get().getStatus().getServerPool());
        } catch (KubernetesClientException ex) {
            this.getLogger().severe("Failed to synchronize server directory");
            ex.printStackTrace();
        }

        this.proxyServer.getPluginManager().registerListener(this, new ServerConnectListener(this));
    }

    public Optional<List<String>> getServersByTag(String tag) {
        return Optional.ofNullable(this.serversPerTag.get(tag));
    }

    private synchronized void updateServerDirectory(List<MinecraftClusterStatus.ServerPoolEntry> serverPool) {
        Map<String, ServerInfo> proxyServers = this.proxyServer.getServers();

        this.getLogger().info("Updating server directory");

        List<String> serverPoolNames = serverPool.parallelStream()
                .map(MinecraftClusterStatus.ServerPoolEntry::getName).toList();

        serverPool.stream()
                .filter((server) -> server != null && server.getName() != null && server.getAddress() != null && !proxyServers.containsKey(server.getName()))
                .map((server) -> {
                    InetSocketAddress socketAddress = new InetSocketAddress(server.getAddress(), 25565);
                    return this.proxyServer.constructServerInfo(server.getName(), socketAddress, null, false);
                })
                .peek((serverInfo) -> this.getLogger().info(String.format("Adding server %s (%s) to directory", serverInfo.getName(), serverInfo.getSocketAddress())))
                .forEach((serverInfo) -> proxyServers.put(serverInfo.getName(), serverInfo));

        new HashSet<>(proxyServers.keySet()).stream()
                .filter((serverName) -> !serverPoolNames.contains(serverName))
                .peek((serverName) -> this.getLogger().info(String.format("Removing server %s from directory", serverName)))
                .forEach(proxyServers::remove);

        this.serverPool = serverPool.parallelStream()
                .collect(Collectors.toMap(MinecraftClusterStatus.ServerPoolEntry::getName, (serverEntry) -> serverEntry));

        Map<String, List<String>> serversPerTag = new HashMap<>();
        serverPool.parallelStream()
                .flatMap((serverEntry) -> serverEntry.getTags().stream().map((tag) -> Pair.of(tag, serverEntry)))
                .forEach((pair) -> {
                    serversPerTag.putIfAbsent(pair.getKey(), new ArrayList<>());
                    serversPerTag.get(pair.getKey()).add(pair.getValue().getName());
                });
        this.serversPerTag = serversPerTag;
    }
}
