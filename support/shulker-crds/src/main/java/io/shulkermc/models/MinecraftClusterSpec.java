package io.shulkermc.models;

public class MinecraftClusterSpec {
    private String mavenSecretName;

    public void setMavenSecretName(String mavenSecretName) {
        this.mavenSecretName = mavenSecretName;
    }

    public String getMavenSecretName() {
        return this.mavenSecretName;
    }
}
