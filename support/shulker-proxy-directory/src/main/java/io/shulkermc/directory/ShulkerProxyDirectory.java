package io.shulkermc.directory;

import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class ShulkerProxyDirectory extends Plugin {
    private final ProxyServer proxyServer;
    private Map<String, List<String>> serversPerTag;

    public ShulkerProxyDirectory() {
        this.proxyServer = ProxyServer.getInstance();
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
        var endpointsClient = kubernetesClient.endpoints().inNamespace(shulkerClusterNamespace);
        var endpointsServerTarget = endpointsClient.withName(String.format("%s-server-discovery", shulkerClusterName));

        endpointsServerTarget.watch(new Watcher<>() {
            @Override
            public void eventReceived(Action action, Endpoints endpoints) {
                if (action != Action.MODIFIED) return;
                ShulkerProxyDirectory.this.updateServerDirectory(endpoints.getSubsets());
            }

            @Override
            public void onClose(WatcherException cause) {
                cause.asClientException().printStackTrace();
            }
        });

        try {
            this.updateServerDirectory(endpointsServerTarget.get().getSubsets());
        } catch (KubernetesClientException ex) {
            this.getLogger().severe("Failed to synchronize server directory");
            ex.printStackTrace();
        }

        this.proxyServer.getPluginManager().registerListener(this, new ServerConnectListener(this));
    }

    public Optional<List<String>> getServersByTag(String tag) {
        return Optional.ofNullable(this.serversPerTag.get(tag));
    }

    private void updateServerDirectory(List<EndpointSubset> subsets) {
        this.getLogger().info("Updating server directory");

        Map<String, String> reducedSubsets = reduceEndpointSubsetsToServerMap(subsets);
        Map<String, ServerInfo> proxyServers = this.proxyServer.getServers();

        new HashSet<>(proxyServers.keySet()).stream()
                .filter((serverName) -> !serverName.equals("lobby") && !reducedSubsets.containsKey(serverName))
                .peek((serverName) -> this.getLogger().info(String.format("Removing server %s from directory", serverName)))
                .forEach(proxyServers::remove);

        reducedSubsets.entrySet().stream()
                .map((entry) -> {
                    InetSocketAddress socketAddress = new InetSocketAddress(entry.getValue(), 25565);
                    return this.proxyServer.constructServerInfo(entry.getKey(), socketAddress, null, false);
                })
                .peek((serverInfo) -> this.getLogger().info(String.format("Adding server %s (%s) to directory", serverInfo.getName(), serverInfo.getSocketAddress())))
                .forEach((serverInfo) -> proxyServers.put(serverInfo.getName(), serverInfo));

        Map<String, List<String>> serversPerTag = new HashMap<>();
        reducedSubsets.entrySet().parallelStream()
                .filter((entry) -> entry.getKey().startsWith("limbo-"))
                .forEach((entry) -> {
                    serversPerTag.putIfAbsent("limbo", new ArrayList<>());
                    serversPerTag.get("limbo").add(entry.getKey());
                });
        this.serversPerTag = serversPerTag;
    }

    private static Map<String, String> reduceEndpointSubsetsToServerMap(List<EndpointSubset> subsets) {
        return subsets.parallelStream()
                .flatMap(subset -> subset.getAddresses().parallelStream().map((address) -> Pair.of(address.getTargetRef().getName(), address.getIp())))
                .map((pair) -> {
                    String[] parts = pair.getLeft().split("-");
                    return Pair.of(String.format("%s-%s-%s", parts[2], parts[3], parts[4]), pair.getRight());
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }
}
