package io.shulkermc.directory;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class ShulkerProxyDirectory extends Plugin {
    private final ProxyServer proxyServer;

    private String shulkerClusterName;
    private CustomObjectsApi kubernetesObjectApi;
    private SharedInformerFactory kubernetesInformerFactory;

    public ShulkerProxyDirectory() {
        this.proxyServer = ProxyServer.getInstance();
    }

    @Override
    public void onEnable() {
        this.shulkerClusterName = System.getenv("SHULKER_CLUSTER_NAME");

        if (this.shulkerClusterName == null) {
            this.getLogger().warning("No SHULKER_CLUSTER_NAME found in environment. Halting.");
            this.proxyServer.stop();
            return;
        }

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

        this.kubernetesObjectApi = new CustomObjectsApi(kubernetesClient);
        this.kubernetesInformerFactory = new SharedInformerFactory();

        this.createInformer();

        try {
            this.syncServerDirectory();
        } catch (ApiException ex) {
            this.getLogger().severe("Failed to synchronize server directory");
            ex.printStackTrace();
        }

        this.kubernetesInformerFactory.startAllRegisteredInformers();
    }

    @Override
    public void onDisable() {
        this.kubernetesInformerFactory.stopAllRegisteredInformers();
    }

    private void syncServerDirectory() throws ApiException {
        V1alpha1MinecraftClusterStatus status = (V1alpha1MinecraftClusterStatus) this.kubernetesObjectApi.getClusterCustomObjectStatus(
                "shulkermc.io", "v1alpha1", "minecraftclusters", this.shulkerClusterName);
        this.updateServerDirectory(status.getServerPool());
    }

    private void updateServerDirectory(List<V1alpha1MinecraftClusterStatusServerPool> serverPool) {
        Map<String, ServerInfo> proxyServers = this.proxyServer.getServers();

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

    private void createInformer() {
        SharedIndexInformer<V1alpha1MinecraftCluster> minecraftClusterInformer =
                this.kubernetesInformerFactory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> this.kubernetesObjectApi.listClusterCustomObjectCall(
                                "shulkermc.io",
                                "v1alpha1",
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

        minecraftClusterInformer.addEventHandler(new ResourceEventHandler<>() {
            @Override
            public void onAdd(V1alpha1MinecraftCluster obj) {}

            @Override
            public void onUpdate(V1alpha1MinecraftCluster oldObj, V1alpha1MinecraftCluster newObj) {
                if (oldObj.getMetadata() == null
                        || oldObj.getMetadata().getName() == null
                        || !oldObj.getMetadata().getName().equals(ShulkerProxyDirectory.this.shulkerClusterName)) return;

                V1alpha1MinecraftClusterStatus clusterStatus = newObj.getStatus();
                if (clusterStatus == null) return;

                ShulkerProxyDirectory.this.updateServerDirectory(clusterStatus.getServerPool());
            }

            @Override
            public void onDelete(V1alpha1MinecraftCluster obj, boolean deletedFinalStateUnknown) {}
        });
    }
}
