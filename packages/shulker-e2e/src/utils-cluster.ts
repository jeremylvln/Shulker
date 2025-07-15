import { pino } from 'pino';
import { default as pretty } from 'pino-pretty';

import { execSync } from 'child_process';

const K3D_DEFAULT_CLUSTER_NAME = 'shulker-test';
const K3D_CLUSTER_NAME =
  process.env.K3D_CLUSTER_NAME ?? K3D_DEFAULT_CLUSTER_NAME;

const logger = pino(pretty({ sync: true }));

function execSyncInherit(command: string | string[]) {
  const joinedCommand = Array.isArray(command) ? command.join(' ') : command;
  logger.debug(`running command '${joinedCommand}'`);
  execSync(joinedCommand, {
    stdio: 'inherit',
  });
}

function waitForDeployment(namespace: string, name: string) {
  execSyncInherit(
    `kubectl rollout status deployment ${name} -n ${namespace} --watch --timeout=1m`,
  );
}

function prepareNamespaces() {
  logger.info('preparing namespaces');

  const createNamespace = (name) => {
    execSyncInherit(
      `kubectl get namespace ${name} || kubectl create namespace ${name}`,
    );
  };

  createNamespace('shulker-system');
  createNamespace('shulker-test');
}

function installCertManager() {
  logger.info('installing cert-manager');
  execSyncInherit(
    'kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.18.2/cert-manager.yaml',
  );

  logger.info('waiting for cert-manager to be ready');
  waitForDeployment('cert-manager', 'cert-manager');
}

export function installAgones() {
  logger.info('installing agones');
  execSyncInherit('helm repo add agones https://agones.dev/chart/stable');
  execSyncInherit([
    'helm upgrade --install agones agones/agones',
    '--namespace agones-system',
    '--create-namespace',
    '--version 1.42.0',
    '--set "gameservers.namespaces[0]=shulker-system"',
    '--set "gameservers.namespaces[1]=shulker-test"',
    '--set "agones.controller.replicas=1"',
    '--set "agones.extensions.replicas=1"',
    '--set "agones.ping.install=false"',
    '--set "agones.allocator.install=true"',
    '--set "agones.allocator.replicas=1"',
    '--set "agones.allocator.service.serviceType=ClusterIP"',
  ]);

  logger.info('waiting for agones to be ready');
  waitForDeployment('agones-system', 'agones-controller');
  waitForDeployment('agones-system', 'agones-allocator');
}

export function installOpenMatch() {
  logger.info('installing open-match');
  execSyncInherit(
    'helm repo add open-match https://open-match.dev/chart/stable',
  );
  execSyncInherit([
    'helm upgrade --install open-match open-match/open-match',
    '--namespace open-match',
    '--create-namespace',
    '--version v1.8.1',
    '--set "ci=true"',
    '--set "global.image.registry=ghcr.io/jeremylvln"',
    '--set "global.image.tag=dev"',
    '--set "open-match-customize.enabled=true"',
    '--set "open-match-customize.evaluator.enabled=true"',
    '--set "open-match-customize.evaluator.replicas=1"',
    '--set "open-match-override.enabled=true"',
    '--set "open-match-core.swaggerui.enabled=false"',
    '--set "query.replicas=1"',
    '--set "frontend.replicas=1"',
    '--set "backend.replicas=1"',
    '--set "redis.sentinel.enabled=false"',
    '--set "redis.replica.replicaCount=0"',
    '--set "redis.metrics.enabled=false"',
  ]);

  logger.info('waiting for open-match to be ready');
  waitForDeployment('open-match', 'open-match-backend');
  waitForDeployment('open-match', 'open-match-evaluator');
  waitForDeployment('open-match', 'open-match-frontend');
  waitForDeployment('open-match', 'open-match-query');
  waitForDeployment('open-match', 'open-match-synchronizer');
}

export function startCluster() {
  if (K3D_CLUSTER_NAME === K3D_DEFAULT_CLUSTER_NAME) {
    logger.info(`creating k3d cluster ${K3D_CLUSTER_NAME}`);
    execSyncInherit(`k3d cluster create ${K3D_CLUSTER_NAME} --no-lb`);
  } else {
    logger.info(`using provided k3d cluster ${K3D_CLUSTER_NAME}`);
  }

  prepareNamespaces();
  installCertManager();
  installAgones();
  installOpenMatch();
}

export function deleteCluster() {
  if (K3D_CLUSTER_NAME !== K3D_DEFAULT_CLUSTER_NAME) {
    return;
  }

  logger.info(`destroying k3d cluster ${K3D_CLUSTER_NAME}`);
  execSyncInherit(`k3d cluster delete ${K3D_CLUSTER_NAME} || true`);
}
