use k8s_openapi::apimachinery::pkg::apis::meta::v1::{Condition, Time};

use shulker_utils::time;

pub enum ConditionStatus {
    True,
    False,
    Unknown,
}

pub trait HasConditions {
    fn conditions(&self) -> &Vec<Condition>;
    fn conditions_mut(&mut self) -> &mut Vec<Condition>;

    fn find_condition(&self, condition_type: &str) -> Option<&Condition> {
        self.conditions().iter().find(|c| c.type_ == condition_type)
    }

    fn find_condition_mut(&mut self, condition_type: &str) -> Option<&mut Condition> {
        self.conditions_mut()
            .iter_mut()
            .find(|c| c.type_ == condition_type)
    }

    fn set_condition(
        &mut self,
        type_: String,
        status: ConditionStatus,
        reason: String,
        message: String,
    ) {
        let status_str = match status {
            ConditionStatus::True => "True".to_string(),
            ConditionStatus::False => "False".to_string(),
            ConditionStatus::Unknown => "Unknown".to_string(),
        };

        let new_condition = Condition {
            type_,
            status: status_str,
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

    fn is_condition_true(&self, condition_type: &str) -> bool {
        self.has_condition_status(condition_type, "True")
    }

    fn is_condition_false(&self, condition_type: &str) -> bool {
        self.has_condition_status(condition_type, "False")
    }

    fn has_condition_status(&self, condition_type: &str, status: &str) -> bool {
        self.find_condition(condition_type)
            .map(|c| c.status == status)
            .unwrap_or(false)
    }
}
