use std::collections::HashMap;
use std::hash::Hash;

/// Merge two HashMap losslessly. Meant to be used
/// with the `merge` crate.
///
/// # Arguments
///
/// * `left` - The HashMap to extend
/// * `right` - The HashMap to merge into the `left` one
pub fn merge_hash_map<K: Hash + Eq + Clone, V: Eq + Clone>(
    left: &mut Option<HashMap<K, V>>,
    right: Option<HashMap<K, V>>,
) {
    if None == right {
        return;
    }
    if left.is_none() {
        *left = right;
        return;
    }
    left.as_mut().unwrap().extend(right.unwrap().into_iter());
}
