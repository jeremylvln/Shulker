/// Utilities for merging structures.
pub mod merge;

/// Base logo with component name and version
/// placeholders.
const LOGO: [&'static str; 7] = [
    "    ########",
    "#####      ##",
    "##          ##     Shulker __component__",
    "###   #########    Version __version__",
    "# #####       #",
    "#  ##         #",
    "   ##",
];

/// Returns a formatted logo with the component name
/// and version placeholders replaced with the values
/// given as parameter.
///
/// # Arguments
///
/// * `component` - Name of the component
/// * `version` - Version of the component
///
/// # Exemples
///
/// ```
/// // To draw the logo, a simple iterator is sufficient:
/// shulker_common::create_logo("My Component", "1.0.0")
///     .into_iter()
///     .map(|l| info!(l));
/// ```
pub fn create_logo(component: &str, version: &str) -> Vec<String> {
    LOGO.iter()
        .map(|line| {
            line.replace("__component__", component)
                .replace("__version__", version)
        })
        .collect()
}
