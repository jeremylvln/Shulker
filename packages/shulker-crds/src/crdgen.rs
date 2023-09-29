use kube::CustomResourceExt;
use std::fs::File;
use std::io::Write;
use std::path::Path;

macro_rules! generate_crd {
    ($crd_type:ty) => {{
        let group = <$crd_type>::api_resource().group;
        let plural = <$crd_type>::api_resource().plural;
        let file_name = format!("{}_{}.yaml", group, plural);
        let path = Path::new(".")
            .join("kube")
            .join("resources")
            .join("crd")
            .join("bases")
            .join(file_name);

        println!("Generating CRD for {}", stringify!($crd_type));
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
}
