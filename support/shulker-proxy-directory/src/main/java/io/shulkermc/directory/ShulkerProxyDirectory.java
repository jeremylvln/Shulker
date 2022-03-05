package io.shulkermc.directory;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.ModelMapper;
import io.kubernetes.client.util.Watch;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.shulkermc.models.V1alpha1MinecraftCluster;
import io.shulkermc.models.V1alpha1MinecraftClusterStatus;
import io.shulkermc.models.V1alpha1MinecraftClusterStatusServerPool;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShulkerProxyDirectory extends Plugin {
    private final ProxyServer proxyServer;

    private String shulkerClusterNamespace;
    private String shulkerClusterName;
    private Thread reconcilerThread;
    private final AtomicBoolean reconcilerContinue = new AtomicBoolean(true);

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

        this.getLogger().info(String.format("Shulker cluster name is \"%s\"", this.shulkerClusterName));

        ApiClient kubernetesClient;
        try {
            kubernetesClient = ClientBuilder.cluster().build();
            Configuration.setDefaultApiClient(kubernetesClient);
        } catch (IOException ex) {
            this.getLogger().severe("Failed to create Kubernetes client");
            ex.printStackTrace();

            this.proxyServer.stop();
            return;
        }

        CustomObjectsApi customObjectsApi = new CustomObjectsApi(kubernetesClient);

        this.reconcilerThread = new Thread(() -> {
            while (this.reconcilerContinue.get()) {
                try {
                    this.getLogger().info("Reconciling cluster status");
                    Watch<V1alpha1MinecraftCluster> watch = Watch.createWatch(
                            kubernetesClient,
                            customObjectsApi.getNamespacedCustomObjectCall(
                                    "shulkermc.io",
                                    "v1alpha1",
                                    this.shulkerClusterNamespace,
                                    "minecraftclusters",
                                    this.shulkerClusterName,
                                    null
                            ),
                            new TypeToken<Watch.Response<V1alpha1MinecraftCluster>>(){}.getType());

                    for (var event : watch) {
                        if (!event.type.equals("MODIFIED")) continue;
                        V1alpha1MinecraftCluster cluster = event.object;
                        if (cluster.getStatus() != null) continue;
                        this.updateServerDirectory(cluster.getStatus().getServerPool());
                    }
                } catch (Exception ex) {
                    this.getLogger().severe("Failed to watch cluster status");
                    ex.printStackTrace();

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }, "ShulkerClusterReconciler");
        this.reconcilerThread.start();

        try {
            V1alpha1MinecraftCluster cluster = (V1alpha1MinecraftCluster) customObjectsApi.getNamespacedCustomObject(
                    "shulkermc.io", "v1alpha1", this.shulkerClusterNamespace, "minecraftclusters", this.shulkerClusterName);

            if (cluster.getStatus() != null) {
                this.updateServerDirectory(cluster.getStatus().getServerPool());
            }
        } catch (ApiException ex) {
            this.getLogger().severe("Failed to synchronize server directory");
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (this.reconcilerThread != null) {
            try {
                this.reconcilerContinue.set(false);
                this.getLogger().info("Waiting for reconciler thread to finish");
                this.reconcilerThread.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void updateServerDirectory(List<V1alpha1MinecraftClusterStatusServerPool> serverPool) {
        Map<String, ServerInfo> proxyServers = this.proxyServer.getServers();
        this.getLogger().info("Updating server directory");

        List<String> serverPoolNames = serverPool.parallelStream()
                .map(V1alpha1MinecraftClusterStatusServerPool::getName).toList();

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

    static {
        ModelMapper.addModelMap("shulkermc.io", "v1alpha1", "MinecraftCluster", "minecraftclusters", V1alpha1MinecraftCluster.class);
    }
}
