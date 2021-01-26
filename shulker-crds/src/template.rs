/// Trait describing a CRD which could be
/// templated. Enforces a `spec` method which
/// must return a `TemplateSpec`.
pub trait Template<S: TemplateSpec + Clone> {
    fn spec(&'_ self) -> &'_ S;
}

/// Trait describing a CRD spec which could be
/// templated. A template must be able to inherit
/// properties from other templates, identified by
/// their resource name in Kubernetes.
pub trait TemplateSpec {
    fn inherit_from(&'_ self) -> &'_ Option<Vec<String>>;
}
