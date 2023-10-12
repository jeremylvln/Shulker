#!/bin/bash
CODECOV="${CODECOV:-codecov}"
$CODECOV -t $CODECOV_TOKEN -F shulker-sdk-bindings-rust -f ./coverage/rust/shulker-sdk-bindings/rust/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F google-agones-crds -f ./coverage/rust/google-agones-sdk/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-operator -f ./coverage/rust/shulker-operator/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-utils -f ./coverage/rust/shulker-utils/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-crds -f ./coverage/rust/shulker-crds/cobertura.xml
