use base64::Engine;
use std::fs;
use std::path::Path;

fn main() {
    let out_dir = std::env::var("OUT_DIR").unwrap();

    let icon_path = Path::new("assets").join("default-server-icon.png");
    let icon_out_path = Path::new(&out_dir).join("default-server-icon.txt");
    let icon = fs::read(icon_path).unwrap();
    let icon_b64 = base64::engine::general_purpose::STANDARD.encode(icon);
    fs::write(icon_out_path, icon_b64).unwrap();

    println!("cargo:rerun-if-changed=build.rs");
    println!("cargo:rerun-if-changed=assets/default-server-icon.png");
}
