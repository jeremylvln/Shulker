#!/bin/bash
CODECOV="${CODECOV:-codecov}"
$CODECOV -t $CODECOV_TOKEN -F google-agones-crds -f ./coverage/rust/google-agones-crds/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F google-agones-sdk-bindings-rust -f ./coverage/rust/google-agones-sdk-bindings-rust/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F google-open-match-sdk-bindings-rust -f ./coverage/rust/google-open-match-sdk-bindings-rust/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-addon-matchmaking -f ./coverage/rust/shulker-addon-matchmaking/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-crds -f ./coverage/rust/shulker-crds/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-kube-utils -f ./coverage/rust/shulker-kube-utils/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-operator -f ./coverage/rust/shulker-operator/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-sdk-bindings-rust -f ./coverage/rust/shulker-sdk-bindings-rust/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-utils -f ./coverage/rust/shulker-utils/cobertura.xml
