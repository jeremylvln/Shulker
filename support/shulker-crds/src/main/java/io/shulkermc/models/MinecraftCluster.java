package io.shulkermc.models;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("shulkermc.io")
public class MinecraftCluster extends CustomResource<MinecraftClusterSpec, MinecraftClusterStatus> implements Namespaced {
}
