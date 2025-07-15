import path from 'path';
import { execAndWait, logger } from './utils.ts';
import { readFileSync } from 'fs';

const K3D_DEFAULT_CLUSTER_NAME = 'shulker-test';
const K3D_CLUSTER_NAME =
  process.env.K3D_CLUSTER_NAME ?? K3D_DEFAULT_CLUSTER_NAME;

export async function waitForDeployment(
  namespace: string,
  name: string,
): Promise<void> {
  await execAndWait(
    `kubectl rollout status deployment ${name} -n ${namespace} --watch --timeout=1m`,
  );
}

export async function waitForPod(
  namespace: string,
  name: string,
): Promise<void> {
  await execAndWait(
    `kubectl wait --for=condition=Ready pod/${name} -n ${namespace} --timeout=1m`,
  );
}

async function prepareNamespaces(): Promise<void> {
  logger.info('preparing namespaces');
  await execAndWait(`kubectl create namespace shulker-system || true`);
  await execAndWait(`kubectl create namespace shulker-test || true`);
}

async function prepareImages(): Promise<void> {
  logger.info(`building images`);
  await execAndWait(`SKIP_PUSH=true npx nx docker shulker-operator e2e`);
  // execSyncInherit(`SKIP_PUSH=true npx nx docker shulker-addon-matchmaking e2e`);

  logger.info(`importing images`);
  // const tags = ['shulker-operator', 'shulker-addon-matchmaking'].map(
  const tags = ['shulker-operator'].map(
    (name) => `ghcr.io/jeremylvln/${name}:e2e`,
  );
  await execAndWait([`k3d image import -c ${K3D_CLUSTER_NAME}`, ...tags]);
}

async function installCertManager(): Promise<void> {
  logger.info('installing cert-manager');
  await execAndWait(
    'kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.18.2/cert-manager.yaml',
  );

  logger.info('waiting for cert-manager to be ready');
  await waitForDeployment('cert-manager', 'cert-manager');
}

async function installAgones(): Promise<void> {
  logger.info('installing agones');
  await execAndWait('helm repo add agones https://agones.dev/chart/stable');
  await execAndWait([
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
  await waitForDeployment('agones-system', 'agones-controller');
  await waitForDeployment('agones-system', 'agones-allocator');
}

async function installOpenMatch(): Promise<void> {
  logger.info('installing open-match');
  await execAndWait(
    'helm repo add open-match https://open-match.dev/chart/stable',
  );
  await execAndWait([
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
  await waitForDeployment('open-match', 'open-match-backend');
  await waitForDeployment('open-match', 'open-match-evaluator');
  await waitForDeployment('open-match', 'open-match-frontend');
  await waitForDeployment('open-match', 'open-match-query');
  await waitForDeployment('open-match', 'open-match-synchronizer');
}

async function installShulker(): Promise<void> {
  const chartPath = path.resolve(
    import.meta.dirname,
    '..',
    '..',
    '..',
    'kube',
    'helm',
  );

  const shulkerVersion = JSON.parse(
    readFileSync(
      path.join(import.meta.dirname, '..', '..', '..', 'package.json'),
      'utf-8',
    ),
  ).version;

  logger.info('installing shulker');
  await execAndWait([
    `helm upgrade --install shulker ${chartPath}`,
    '--namespace shulker-system',
    '--create-namespace',
    '--set "operator.image.tag=e2e"',
    '--set "operator.extraArgs[0]=--agent-maven-repository=https://maven.jeremylvln.fr/repository/shulker-snapshots"',
    `--set "operator.extraArgs[1]=--agent-version=${shulkerVersion}-SNAPSHOT"`,
    // '--set "shulker-addon-matchmaking.enabled=true"',
    // '--set "shulker-addon-matchmaking.director.image.tag=e2e"',
    // '--set "shulker-addon-matchmaking.mmf.image.tag=e2e"',
  ]);

  logger.info('waiting for shulker to be ready');
  await waitForDeployment('shulker-system', 'shulker-shulker-operator');
  // await waitForDeployment('shulker-system', 'shulker-addon-matchmaking-director');
  // await waitForDeployment('shulker-system', 'shulker-addon-matchmaking-mmf');
}

export async function startCluster(): Promise<void> {
  logger.info(`creating k3d cluster ${K3D_CLUSTER_NAME}`);
  await execAndWait(`k3d cluster create ${K3D_CLUSTER_NAME} --no-lb || true`);

  await prepareNamespaces();

  if (process.env.SKIP_BUILD_IMAGES !== 'true') {
    await prepareImages();
  } else {
    logger.info('skipping building images');
  }

  if (process.env.SKIP_INSTALL_DEPS !== 'true') {
    await installCertManager();
    await installAgones();
    // await installOpenMatch();
  } else {
    logger.info('skipping installing dependencies');
  }

  await installShulker();
  logger.info('kubernetes cluster is ready');
}

export async function deleteCluster(): Promise<void> {
  if (K3D_CLUSTER_NAME !== K3D_DEFAULT_CLUSTER_NAME) {
    return;
  }

  logger.info(`destroying k3d cluster ${K3D_CLUSTER_NAME}`);
  await execAndWait(`k3d cluster delete ${K3D_CLUSTER_NAME} || true`);
}
