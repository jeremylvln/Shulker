package io.shulkermc.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("shulkermc.io")
public class MinecraftCluster extends CustomResource<MinecraftClusterSpec, MinecraftClusterStatus> implements Namespaced {
    @JsonProperty("pool")
    protected MinecraftClusterPool pool;

    public void setPool(MinecraftClusterPool pool) {
        this.pool = pool;
    }

    public MinecraftClusterPool getPool() {
        return this.pool;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        MinecraftCluster that = (MinecraftCluster) o;
        return this.pool.equals(that.pool);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.pool.hashCode();
        return result;
    }
}
