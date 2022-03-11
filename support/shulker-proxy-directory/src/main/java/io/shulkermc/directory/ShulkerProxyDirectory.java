package io.shulkermc.directory;

import io.fabric8.kubernetes.client.*;
import io.shulkermc.models.MinecraftCluster;
import io.shulkermc.models.MinecraftClusterList;
import io.shulkermc.models.MinecraftClusterPool;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class ShulkerProxyDirectory extends Plugin {
    private final ProxyServer proxyServer;

    private String shulkerClusterNamespace;
    private String shulkerClusterName;

    public ShulkerProxyDirectory() {
        this.proxyServer = ProxyServer.getInstance();
    }

    @Override
    public void onEnable() {
        this.shulkerClusterNamespace = System.getenv("SHULKER_CLUSTER_NAMESPACE");
        if (this.shulkerClusterNamespace == null) {
            this.getLogger().warning("No SHULKER_CLUSTER_NAMESPACE found in environment. Halting.");
            this.proxyServer.stop();
            return;
        }

        this.shulkerClusterName = System.getenv("SHULKER_CLUSTER_NAME");
        if (this.shulkerClusterName == null) {
            this.getLogger().warning("No SHULKER_CLUSTER_NAME found in environment. Halting.");
            this.proxyServer.stop();
            return;
        }

        this.getLogger().info(String.format("Shulker cluster is \"%s/%s\"", this.shulkerClusterNamespace, this.shulkerClusterName));

        KubernetesClient kubernetesClient = new DefaultKubernetesClient();
        var minecraftClusterClient = kubernetesClient.customResources(MinecraftCluster.class, MinecraftClusterList.class);
        var minecraftClusterTarget = minecraftClusterClient.inNamespace(this.shulkerClusterNamespace).withName(this.shulkerClusterName);

        minecraftClusterTarget.watch(new Watcher<>() {
            @Override
            public void eventReceived(Action action, MinecraftCluster cluster) {
                if (action != Action.MODIFIED) return;

                MinecraftClusterPool pool = cluster.getPool();
                if (pool == null) return;

                ShulkerProxyDirectory.this.updateServerDirectory(pool);
            }

            @Override
            public void onClose(WatcherException cause) {
                cause.asClientException().printStackTrace();
            }
        });

        try {
            this.updateServerDirectory(minecraftClusterTarget.get().getPool());
        } catch (KubernetesClientException ex) {
            this.getLogger().severe("Failed to synchronize server directory");
            ex.printStackTrace();
        }
    }

    private synchronized void updateServerDirectory(MinecraftClusterPool pool) {
        Map<String, ServerInfo> proxyServers = this.proxyServer.getServers();
        this.getLogger().info("Updating server directory");

        List<String> serverPoolNames = pool.getServers().parallelStream()
                .map(MinecraftClusterPool.ServerEntry::getName).toList();

        pool.getServers().parallelStream()
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
