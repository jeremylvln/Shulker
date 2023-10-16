use k8s_openapi::apimachinery::pkg::apis::meta::v1::{Condition, Time};

use shulker_utils::time;
use strum::{Display, IntoStaticStr};

#[derive(PartialEq, Clone, Debug, Default, IntoStaticStr, Display)]
pub enum ConditionStatus {
    True,
    False,
    #[default]
    Unknown,
}

pub trait HasConditions {
    fn conditions(&self) -> &Vec<Condition>;
    fn conditions_mut(&mut self) -> &mut Vec<Condition>;

    fn set_condition(
        &mut self,
        type_: String,
        status: ConditionStatus,
        reason: String,
        message: String,
    ) {
        let new_condition = Condition {
            type_,
            status: status.to_string(),
            reason,
            message,
            last_transition_time: Time(time::now()),
            observed_generation: None,
        };

        let existing_condition = self.find_condition_mut(&new_condition.type_);

        if let Some(existing_condition) = existing_condition {
            if existing_condition.status != new_condition.status {
                existing_condition.status = new_condition.status;
                existing_condition.last_transition_time = new_condition.last_transition_time;
            }

            existing_condition.reason = new_condition.reason;
            existing_condition.message = new_condition.message;
            existing_condition.observed_generation = new_condition.observed_generation;
        } else {
            self.conditions_mut().push(new_condition);
        }
    }

    fn find_condition(&self, condition_type: &str) -> Option<&Condition> {
        self.conditions().iter().find(|c| c.type_ == condition_type)
    }

    fn find_condition_mut(&mut self, condition_type: &str) -> Option<&mut Condition> {
        self.conditions_mut()
            .iter_mut()
            .find(|c| c.type_ == condition_type)
    }

    fn is_condition_true(&self, condition_type: &str) -> bool {
        self.has_condition_status(condition_type, ConditionStatus::True)
    }

    fn is_condition_false(&self, condition_type: &str) -> bool {
        self.has_condition_status(condition_type, ConditionStatus::False)
    }

    fn has_condition_status(&self, condition_type: &str, status: ConditionStatus) -> bool {
        self.find_condition(condition_type)
            .map(|c| c.status == status.to_string())
            .unwrap_or(false)
    }
}

#[cfg(test)]
mod tests {
    use k8s_openapi::apimachinery::pkg::apis::meta::v1::{Condition, Time};
    use shulker_utils::time;

    use crate::condition::ConditionStatus;

    use super::HasConditions;

    struct TestHasConditions(Vec<Condition>);

    impl HasConditions for TestHasConditions {
        fn conditions(&self) -> &Vec<Condition> {
            &self.0
        }

        fn conditions_mut(&mut self) -> &mut Vec<Condition> {
            &mut self.0
        }
    }

    #[test]
    fn find_condition_found() {
        // G
        let test_structure = TestHasConditions(vec![Condition {
            type_: "TestCondition".to_string(),
            status: ConditionStatus::True.to_string(),
            reason: "".to_string(),
            message: "".to_string(),
            last_transition_time: Time(time::now()),
            observed_generation: None,
        }]);

        // W
        let found_condition = test_structure.find_condition("TestCondition");

        // T
        assert_eq!(found_condition, Some(&test_structure.0[0]));
    }

    #[test]
    fn find_condition_not_found() {
        // G
        let test_structure = TestHasConditions(vec![]);

        // W
        let found_condition = test_structure.find_condition("TestCondition");

        // T
        assert_eq!(found_condition, None);
    }

    #[test]
    fn find_condition_mut_found() {
        // G
        let mut test_structure = TestHasConditions(vec![Condition {
            type_: "TestCondition".to_string(),
            status: ConditionStatus::True.to_string(),
            reason: "".to_string(),
            message: "".to_string(),
            last_transition_time: Time(time::now()),
            observed_generation: None,
        }]);

        // W
        let found_condition = test_structure.find_condition_mut("TestCondition");

        // T
        assert!(found_condition.is_some());
    }

    #[test]
    fn find_condition_mut_not_found() {
        // G
        let mut test_structure = TestHasConditions(vec![]);

        // W
        let found_condition = test_structure.find_condition_mut("TestCondition");

        // T
        assert_eq!(found_condition, None);
    }

    #[test]
    fn set_condition_not_existing() {
        // G
        let mut test_structure = TestHasConditions(vec![]);

        // W
        test_structure.set_condition(
            "TestCondition".to_string(),
            super::ConditionStatus::True,
            "A reason".to_string(),
            "A message".to_string(),
        );

        // T
        assert_eq!(test_structure.0.len(), 1);
        assert_eq!(test_structure.0[0].type_, "TestCondition");
        assert_eq!(test_structure.0[0].status, "True");
        assert_eq!(test_structure.0[0].reason, "A reason");
        assert_eq!(test_structure.0[0].message, "A message");
    }

    #[test]
    fn set_condition_existing() {
        // G
        let mut test_structure = TestHasConditions(vec![Condition {
            type_: "TestCondition".to_string(),
            status: ConditionStatus::True.to_string(),
            reason: "A reason".to_string(),
            message: "A message".to_string(),
            last_transition_time: Time(time::now()),
            observed_generation: None,
        }]);

        // W
        test_structure.set_condition(
            "TestCondition".to_string(),
            super::ConditionStatus::False,
            "A better reason".to_string(),
            "A better message".to_string(),
        );

        // T
        assert_eq!(test_structure.0.len(), 1);
        assert_eq!(test_structure.0[0].type_, "TestCondition");
        assert_eq!(test_structure.0[0].status, "False");
        assert_eq!(test_structure.0[0].reason, "A better reason");
        assert_eq!(test_structure.0[0].message, "A better message");
    }

    macro_rules! is_condition_true_tests {
        ($($name:ident: $value:expr,)*) => {
        $(
            #[test]
            fn $name() {
                // G
                let (condition_type, condition_status, test_type, eq) = $value;
                let test_structure = TestHasConditions(vec![Condition {
                    type_: condition_type.to_string(),
                    status: condition_status.to_string(),
                    reason: "".to_string(),
                    message: "".to_string(),
                    last_transition_time: Time(time::now()),
                    observed_generation: None,
                }]);

                // T
                assert_eq!(test_structure.is_condition_true(test_type), eq);
            }
        )*
        }
    }

    is_condition_true_tests! {
        is_condition_true: ("TestCondition", ConditionStatus::True, "TestCondition", true),
        is_condition_true_while_false: ("TestCondition", ConditionStatus::False, "TestCondition", false),
        is_condition_true_unknown_condition: ("TestCondition", ConditionStatus::True, "OtherCondition", false),
    }

    macro_rules! is_condition_false_tests {
        ($($name:ident: $value:expr,)*) => {
        $(
            #[test]
            fn $name() {
                // G
                let (condition_type, condition_status, test_type, eq) = $value;
                let test_structure = TestHasConditions(vec![Condition {
                    type_: condition_type.to_string(),
                    status: condition_status.to_string(),
                    reason: "".to_string(),
                    message: "".to_string(),
                    last_transition_time: Time(time::now()),
                    observed_generation: None,
                }]);

                // T
                assert_eq!(test_structure.is_condition_false(test_type), eq);
            }
        )*
        }
    }

    is_condition_false_tests! {
        is_condition_false: ("TestCondition", ConditionStatus::False, "TestCondition", true),
        is_condition_false_while_true: ("TestCondition", ConditionStatus::True, "TestCondition", false),
        is_condition_false_unknown_condition: ("TestCondition", ConditionStatus::False, "OtherCondition", false),
    }

    macro_rules! has_condition_status_tests {
        ($($name:ident: $value:expr,)*) => {
        $(
            #[test]
            fn $name() {
                // G
                let (condition_type, condition_status, test_type, test_status, eq) = $value;
                let test_structure = TestHasConditions(vec![Condition {
                    type_: condition_type.to_string(),
                    status: condition_status.to_string(),
                    reason: "".to_string(),
                    message: "".to_string(),
                    last_transition_time: Time(time::now()),
                    observed_generation: None,
                }]);

                // T
                assert_eq!(test_structure.has_condition_status(test_type, test_status), eq);
            }
        )*
        }
    }

    has_condition_status_tests! {
        has_condition_status_true: ("TestCondition", ConditionStatus::True, "TestCondition", ConditionStatus::True, true),
        has_condition_status_true_while_false: ("TestCondition", ConditionStatus::False, "TestCondition", ConditionStatus::True, false),
        has_condition_status_false: ("TestCondition", ConditionStatus::False, "TestCondition", ConditionStatus::False, true),
        has_condition_status_false_while_true: ("TestCondition", ConditionStatus::True, "TestCondition", ConditionStatus::False, false),
        has_condition_status_unknown_condition: ("TestCondition", ConditionStatus::True, "OtherCondition", ConditionStatus::True, false),
    }
}
