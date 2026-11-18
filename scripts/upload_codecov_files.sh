#!/bin/bash
CODECOV="${CODECOV:-codecov}"
$CODECOV -t $CODECOV_TOKEN -F google-agones-crds -f ./coverage/rust/google-agones-crds/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F google-agones-sdk-bindings-java -f .\dist\java\packages\google-agones-sdk\test-results\test\binary -f .\dist\java\packages\google-agones-sdk\jacoco\test.exec -f .\dist\java\packages\google-agones-sdk\reports\tests\test -f .\dist\java\packages\google-agones-sdk\test-results\test
$CODECOV -t $CODECOV_TOKEN -F google-agones-sdk-bindings-rust -f ./coverage/rust/google-agones-sdk-bindings-rust/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F google-open-match-sdk-bindings-java -f .\dist\java\packages\google-open-match-sdk\test-results\test\binary -f .\dist\java\packages\google-open-match-sdk\jacoco\test.exec -f .\dist\java\packages\google-open-match-sdk\reports\tests\test -f .\dist\java\packages\google-open-match-sdk\test-results\test
$CODECOV -t $CODECOV_TOKEN -F google-open-match-sdk-bindings-rust -f ./coverage/rust/google-open-match-sdk-bindings-rust/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-addon-matchmaking -f ./coverage/rust/shulker-addon-matchmaking/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-cli -f ./coverage/rust/shulker-cli/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-cluster-api -f .\dist\java\packages\shulker-cluster-api\test-results\test\binary -f .\dist\java\packages\shulker-cluster-api\jacoco\test.exec -f .\dist\java\packages\shulker-cluster-api\reports\tests\test -f .\dist\java\packages\shulker-cluster-api\test-results\test
$CODECOV -t $CODECOV_TOKEN -F shulker-cluster-api-impl -f .\dist\java\packages\shulker-cluster-api-impl\test-results\test\binary -f .\dist\java\packages\shulker-cluster-api-impl\jacoco\test.exec -f .\dist\java\packages\shulker-cluster-api-impl\reports\tests\test -f .\dist\java\packages\shulker-cluster-api-impl\test-results\test
$CODECOV -t $CODECOV_TOKEN -F shulker-crds -f ./coverage/rust/shulker-crds/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-kube-utils -f ./coverage/rust/shulker-kube-utils/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-operator -f ./coverage/rust/shulker-operator/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-proxy-agent -f .\dist\java\packages\shulker-proxy-agent\test-results\test\binary -f .\dist\java\packages\shulker-proxy-agent\jacoco\test.exec -f .\dist\java\packages\shulker-proxy-agent\reports\tests\test -f .\dist\java\packages\shulker-proxy-agent\test-results\test
$CODECOV -t $CODECOV_TOKEN -F shulker-proxy-api -f .\dist\java\packages\shulker-proxy-api\test-results\test\binary -f .\dist\java\packages\shulker-proxy-api\jacoco\test.exec -f .\dist\java\packages\shulker-proxy-api\reports\tests\test -f .\dist\java\packages\shulker-proxy-api\test-results\test
$CODECOV -t $CODECOV_TOKEN -F shulker-sdk-bindings-java -f .\dist\java\packages\shulker-sdk\test-results\test\binary -f .\dist\java\packages\shulker-sdk\jacoco\test.exec -f .\dist\java\packages\shulker-sdk\reports\tests\test -f .\dist\java\packages\shulker-sdk\test-results\test
$CODECOV -t $CODECOV_TOKEN -F shulker-sdk-bindings-rust -f ./coverage/rust/shulker-sdk-bindings-rust/cobertura.xml
$CODECOV -t $CODECOV_TOKEN -F shulker-server-agent -f .\dist\java\packages\shulker-server-agent\test-results\test\binary -f .\dist\java\packages\shulker-server-agent\jacoco\test.exec -f .\dist\java\packages\shulker-server-agent\reports\tests\test -f .\dist\java\packages\shulker-server-agent\test-results\test
$CODECOV -t $CODECOV_TOKEN -F shulker-server-api -f .\dist\java\packages\shulker-server-api\test-results\test\binary -f .\dist\java\packages\shulker-server-api\jacoco\test.exec -f .\dist\java\packages\shulker-server-api\reports\tests\test -f .\dist\java\packages\shulker-server-api\test-results\test
$CODECOV -t $CODECOV_TOKEN -F shulker-server-minestom-demo -f .\dist\java\packages\shulker-server-minestom-demo\test-results\test\binary -f .\dist\java\packages\shulker-server-minestom-demo\jacoco\test.exec -f .\dist\java\packages\shulker-server-minestom-demo\reports\tests\test -f .\dist\java\packages\shulker-server-minestom-demo\test-results\test
$CODECOV -t $CODECOV_TOKEN -F shulker-utils -f ./coverage/rust/shulker-utils/cobertura.xml
