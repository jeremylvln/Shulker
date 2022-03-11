package io.shulkermc.directory;

import io.fabric8.kubernetes.client.*;
import io.shulkermc.models.MinecraftCluster;
import io.shulkermc.models.MinecraftClusterList;
import io.shulkermc.models.MinecraftClusterStatus;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class ShulkerProxyDirectory extends Plugin {
    private final ProxyServer proxyServer;

    public ShulkerProxyDirectory() {
        this.proxyServer = ProxyServer.getInstance();
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
    }

    private synchronized void updateServerDirectory(List<MinecraftClusterStatus.ServerPoolEntry> serverPool) {
        Map<String, ServerInfo> proxyServers = this.proxyServer.getServers();
        this.getLogger().info("Updating server directory");

        List<String> serverPoolNames = serverPool.parallelStream()
                .map(MinecraftClusterStatus.ServerPoolEntry::getName).toList();

        serverPool.parallelStream()
                .filter((server) -> server != null && server.getName() != null && server.getAddress() != null)
                .map((server) -> {
                    InetSocketAddress socketAddress = new InetSocketAddress(server.getAddress(), 25565);
                    return this.proxyServer.constructServerInfo(server.getName(), socketAddress, null, false);
                })
                .peek((serverInfo) -> this.getLogger().info(String.format("Adding server %s (%s) to directory", serverInfo.getName(), serverInfo.getSocketAddress())))
                .forEach((serverInfo) -> proxyServers.put(serverInfo.getName(), serverInfo));

        proxyServers.keySet().stream()
                .filter((serverName) -> !serverPoolNames.contains(serverName))
                .peek((serverName) -> this.getLogger().info(String.format("Removing server %s from directory", serverName)))
                .forEach(proxyServers::remove);
    }
}
