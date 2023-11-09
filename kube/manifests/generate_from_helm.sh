#!/usr/bin/env sh

CHART_PATH="../helm"
CHART_NAME="shulker-operator"

function generate() {
  variant="$1"
  shift

  echo "Generating $variant.yaml..."
  helm template "$CHART_PATH" --name-template "$CHART_NAME" $@ > "$variant.yaml"

  # Fix the RoleBindings and ClusterRoleBindings having a "default" namespace
  yq -e -i '(select(.subjects).subjects[] | select(.namespace == "default") | .namespace) = "shulker-system"' "$variant.yaml"

  # Fix references to Shulker Operator's service having a "default" namespace
  yq -e -i '(select(.spec.template.spec.containers).spec.template.spec.containers[].env[] | select(.name == "SHULKER_API_HOST") | .value) = "shulker-operator.shulker-system"' "$variant.yaml"
}

(cd ../helm && helm dependency update)

generate stable

generate stable-with-prometheus \
  --set operator.metrics.enabled=true \
  --set operator.metrics.servicemonitor.enabled=true

generate next \
  --set operator.image.tag=next \
  --set shulker-addon-matchmaking.enabled=true \
  --set shulker-addon-matchmaking.image.tag=next

generate next-with-prometheus \
  --set operator.image.tag=next \
  --set operator.metrics.enabled=true \
  --set operator.metrics.servicemonitor.enabled=true \
  --set shulker-addon-matchmaking.enabled=true \
  --set shulker-addon-matchmaking.image.tag=next \
  --set shulker-addon-matchmaking.director.metrics.enabled=true \
  --set shulker-addon-matchmaking.director.metrics.servicemonitor.enabled=true \
  --set shulker-addon-matchmaking.mmf.metrics.enabled=true \
  --set shulker-addon-matchmaking.mmf.metrics.servicemonitor.enabled=true
