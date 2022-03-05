package io.shulkermc.directory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.CallGeneratorParams;
import io.kubernetes.client.util.ClientBuilder;
import io.shulkermc.models.V1alpha1MinecraftCluster;
import io.shulkermc.models.V1alpha1MinecraftClusterList;
import io.shulkermc.models.V1alpha1MinecraftClusterStatus;
import io.shulkermc.models.V1alpha1MinecraftClusterStatusServerPool;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ShulkerProxyDirectory extends Plugin {
    private final ProxyServer proxyServer;

    private String shulkerClusterNamespace;
    private String shulkerClusterName;
    private SharedInformerFactory informerFactory;

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

        ApiClient kubernetesClient;
        try {
            kubernetesClient = ClientBuilder.cluster().build();
            OkHttpClient httpClient = kubernetesClient.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
            kubernetesClient.setHttpClient(httpClient);
            Configuration.setDefaultApiClient(kubernetesClient);
        } catch (IOException ex) {
            this.getLogger().severe("Failed to create Kubernetes client");
            ex.printStackTrace();

            this.proxyServer.stop();
            return;
        }

        CustomObjectsApi customObjectsApi = new CustomObjectsApi(kubernetesClient);
        this.informerFactory = new SharedInformerFactory();

        SharedIndexInformer<V1alpha1MinecraftCluster> clusterInformer =
                this.informerFactory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> customObjectsApi.listNamespacedCustomObjectCall(
                                "shulkermc.io",
                                "v1alpha1",
                                this.shulkerClusterNamespace,
                                "minecraftclusters",
                                null,
                                null,
                                null,
                                null,
                                null,
                                params.resourceVersion,
                                params.timeoutSeconds,
                                params.watch,
                                null),
                        V1alpha1MinecraftCluster.class,
                        V1alpha1MinecraftClusterList.class);

        clusterInformer.addEventHandler(
                new ResourceEventHandler<>() {
                    @Override
                    public void onAdd(V1alpha1MinecraftCluster cluster) {}

                    @Override
                    public void onUpdate(V1alpha1MinecraftCluster oldCluster, V1alpha1MinecraftCluster newCluster) {
                        if (newCluster.getMetadata() == null || newCluster.getMetadata().getName() == null) return;
                        if (!newCluster.getMetadata().getName().equals(ShulkerProxyDirectory.this.shulkerClusterName)) return;

                        V1alpha1MinecraftClusterStatus status = newCluster.getStatus();
                        if (status == null) return;

                        ShulkerProxyDirectory.this.updateServerDirectory(status.getServerPool());
                    }

                    @Override
                    public void onDelete(V1alpha1MinecraftCluster cluster, boolean deletedFinalStateUnknown) {}
                });

        this.informerFactory.startAllRegisteredInformers();

        try {
            V1alpha1MinecraftClusterStatus status = ShulkerProxyDirectory.responseToStatusObject(customObjectsApi.getNamespacedCustomObjectStatus(
                    "shulkermc.io", "v1alpha1", this.shulkerClusterNamespace, "minecraftclusters", this.shulkerClusterName));
             this.updateServerDirectory(status.getServerPool());
        } catch (ApiException ex) {
            this.getLogger().severe("Failed to synchronize server directory");
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (this.informerFactory != null) {
            this.informerFactory.stopAllRegisteredInformers();
        }
    }

    private synchronized void updateServerDirectory(List<V1alpha1MinecraftClusterStatusServerPool> serverPool) {
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

    private static V1alpha1MinecraftClusterStatus responseToStatusObject(Object o) {
        if (o == null) return null;
        Gson gson = new JSON().getGson();
        JsonElement jsonElement = gson.toJsonTree(o);
        return gson.fromJson(jsonElement, V1alpha1MinecraftClusterStatus.class);
    }
}
