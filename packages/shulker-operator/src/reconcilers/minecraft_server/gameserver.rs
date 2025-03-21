use k8s_openapi::api::core::v1::PodSecurityContext;

// ...

async fn get_pod_template_spec(
    resourceref_resolver: &ResourceRefResolver,
    context: &GameServerBuilderContext<'a>,
    minecraft_server: &MinecraftServer,
) -> Result<PodTemplateSpec, anyhow::Error> {
    // Add fsGroup here via the PodSecurityContext.
    let mut pod_spec = PodSpec {
        security_context: Some(PodSecurityContext {
            run_as_user: Some(1000),
            run_as_group: Some(1000),
            run_as_non_root: Some(true),
            fs_group: Some(1000),
            ..PodSecurityContext::default()
        }),
        init_containers: Some(vec![Container {
            image: Some("alpine:latest".to_string()),
            name: "init-fs".to_string(),
            command: Some(vec![
                "sh".to_string(),
                format!("{}/init-fs.sh", MINECRAFT_SERVER_SHULKER_CONFIG_DIR),
            ]),
            env: Some(
                Self::get_init_env(resourceref_resolver, context, minecraft_server).await?,
            ),
            security_context: Some(PROXY_SECURITY_CONTEXT.clone()),
            volume_mounts: Some(vec![
                VolumeMount {
                    name: "shulker-config".to_string(),
                    mount_path: MINECRAFT_SERVER_SHULKER_CONFIG_DIR.to_string(),
                    read_only: Some(true),
                    ..VolumeMount::default()
                },
                VolumeMount {
                    name: "server-config".to_string(),
                    mount_path: MINECRAFT_SERVER_CONFIG_DIR.to_string(),
                    ..VolumeMount::default()
                },
            ]),
            ..Container::default()
        }]),
        containers: vec![Container {
            image: Some(constants::MINECRAFT_SERVER_IMAGE.to_string()),
            name: "minecraft-server".to_string(),
            ports: Some(vec![ContainerPort {
                name: Some("minecraft".to_string()),
                container_port: 25565,
                ..ContainerPort::default()
            }]),
            env: Some(Self::get_env(resourceref_resolver, context, minecraft_server).await?),
            image_pull_policy: Some("IfNotPresent".to_string()),
            security_context: Some(PROXY_SECURITY_CONTEXT.clone()),
            volume_mounts: Some(vec![
                VolumeMount {
                    name: "server-config".to_string(),
                    mount_path: MINECRAFT_SERVER_CONFIG_DIR.to_string(),
                    ..VolumeMount::default()
                },
                VolumeMount {
                    name: "server-data".to_string(),
                    mount_path: MINECRAFT_SERVER_DATA_DIR.to_string(),
                    ..VolumeMount::default()
                },
                VolumeMount {
                    name: "server-tmp".to_string(),
                    mount_path: "/tmp".to_string(),
                    ..VolumeMount::default()
                },
            ]),
            ..Container::default()
        }],
        subdomain: Some(format!(
            "{}-cluster",
            &minecraft_server.spec.cluster_ref.name
        )),
        service_account_name: Some(format!(
            "shulker-{}-server",
            &minecraft_server.spec.cluster_ref.name
        )),
        restart_policy: Some("Never".to_string()),
        volumes: Some(vec![
            Volume {
                name: "shulker-config".to_string(),
                config_map: Some(ConfigMapVolumeSource {
                    name: minecraft_server
                        .spec
                        .config
                        .existing_config_map_name
                        .clone()
                        .unwrap_or_else(|| ConfigMapBuilder::name(minecraft_server)),
                    ..ConfigMapVolumeSource::default()
                }),
                ..Volume::default()
            },
            Volume {
                name: "server-config".to_string(),
                empty_dir: Some(EmptyDirVolumeSource::default()),
                ..Volume::default()
            },
            Volume {
                name: "server-data".to_string(),
                empty_dir: Some(EmptyDirVolumeSource::default()),
                ..Volume::default()
            },
            Volume {
                name: "server-tmp".to_string(),
                empty_dir: Some(EmptyDirVolumeSource::default()),
                ..Volume::default()
            },
        ]),
        ..PodSpec::default()
    };

    // ... (the rest of your function that applies any podOverrides, labels, annotations, etc.)

    Ok(PodTemplateSpec {
        metadata: Some(ObjectMeta {
            labels: Some({
                let mut labels = minecraft_server.labels().clone();
                labels.append(&mut MinecraftServerReconciler::get_labels(
                    minecraft_server,
                    "minecraft-server".to_string(),
                    "minecraft-server".to_string(),
                ));
                labels
            }),
            annotations: Some({
                let mut annotations = minecraft_server.annotations().clone();
                annotations.append(&mut BTreeMap::<String, String>::from([(
                    "kubectl.kubernetes.io/default-container".to_string(),
                    "minecraft-server".to_string(),
                )]));
                annotations
            }),
            ..ObjectMeta::default()
        }),
        spec: Some(pod_spec),
    })
}
