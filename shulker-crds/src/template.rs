pub trait Template<S: TemplateSpec + Clone> {
    fn spec(&'_ self) -> &'_ S;
}

pub trait TemplateSpec {
    fn inherit_from(&'_ self) -> &'_ Option<Vec<String>>;
}
