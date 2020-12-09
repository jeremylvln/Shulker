use std::collections::HashMap;
use std::hash::Hash;

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
