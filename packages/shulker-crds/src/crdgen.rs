use kube::CustomResourceExt;
use std::fs::{create_dir_all, File};
use std::io::Write;
use std::path::Path;

macro_rules! generate_crd {
    ($crd_type:ty) => {{
        generate_crd!("", $crd_type)
    }};
    ($subfolder:expr, $crd_type:ty) => {{
        let group = <$crd_type>::api_resource().group;
        let plural = <$crd_type>::api_resource().plural;
        let file_name = format!("{}_{}.yaml", group, plural);

        let path = if !$subfolder.is_empty() {
            Path::new(".")
                .join("kube")
                .join("resources")
                .join("crd")
                .join($subfolder)
                .join(file_name)
        } else {
            Path::new(".")
                .join("kube")
                .join("resources")
                .join("crd")
                .join(file_name)
        };

        println!("Generating CRD for {}", stringify!($crd_type));
        create_dir_all(path.parent().unwrap()).unwrap();
        File::create(path)
            .unwrap()
            .write_all(
                serde_yaml::to_string(&<$crd_type>::crd())
                    .unwrap()
                    .as_bytes(),
            )
            .unwrap();
    }};
}

fn main() {
    generate_crd!(shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster);
    generate_crd!(shulker_crds::v1alpha1::proxy_fleet::ProxyFleet);
    generate_crd!(shulker_crds::v1alpha1::minecraft_server::MinecraftServer);
    generate_crd!(shulker_crds::v1alpha1::minecraft_server_fleet::MinecraftServerFleet);

    generate_crd!(
        "matchmaking",
        shulker_crds::matchmaking::v1alpha1::matchmaking_queue::MatchmakingQueue
    );
}
