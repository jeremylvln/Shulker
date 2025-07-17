import path from 'node:path';
import { execAndWait } from './process.ts';
import { waitForDeployment } from './kubernetes-watch.ts';
import { readFileSync } from 'node:fs';
import { logger, REPOSITORY_ROOT } from './index.ts';

type HelmRepository = {
  name: string;
  url: string;
  chartName?: string;
  chartVersion?: string;
};

async function installHelmChart(
  repository: HelmRepository,
  namespace: string,
  releaseName: string,
  values: Record<string, string | number | boolean> = {},
  deploymentsToWait?: readonly string[],
): Promise<void> {
  const isRemoteChart = repository.url.startsWith('https://');

  if (isRemoteChart) {
    logger.info('registering Helm repository');
    await execAndWait(`helm repo add ${repository.name} ${repository.url}`);
  }

  const chartPath = isRemoteChart
    ? `${repository.name}/${repository.chartName}`
    : repository.url;

  logger.info(`installing Helm chart ${releaseName}`);
  await execAndWait([
    `helm upgrade --install ${releaseName} ${chartPath}`,
    `--namespace ${namespace}`,
    '--create-namespace',
    ...(repository.chartVersion
      ? [`--version ${repository.chartVersion}`]
      : []),
    ...Object.entries(values).map(([k, v]) => `--set "${k}=${v}"`),
  ]);

  if (Array.isArray(deploymentsToWait)) {
    logger.info('waiting for related deployments to be ready');
    await Promise.all(
      deploymentsToWait.map((deploymentName) =>
        waitForDeployment(deploymentName, namespace),
      ),
    );
  }
}

export async function installCertManager(): Promise<void> {
  logger.info('installing cert-manager');
  await installHelmChart(
    {
      name: 'jetstack',
      url: 'https://charts.jetstack.io',
      chartName: 'cert-manager',
      chartVersion: 'v1.18.2',
    },
    'cert-manager',
    'cert-manager',
    {
      'crds.enabled': true,
    },
    ['cert-manager'],
  );
}

export async function installAgones(): Promise<void> {
  logger.info('installing Agones');
  await installHelmChart(
    {
      name: 'agones',
      url: 'https://agones.dev/chart/stable',
      chartName: 'agones',
      chartVersion: '1.42.0',
    },
    'agones-system',
    'agones',
    {
      'gameservers.namespaces[0]': 'shulker-system',
      'gameservers.namespaces[1]': 'shulker-test',
      'agones.controller.replicas': 1,
      'agones.extensions.replicas': 1,
      'agones.ping.install': false,
      'agones.allocator.install': true,
      'agones.allocator.replicas': 1,
      'agones.allocator.service.serviceType': 'ClusterIP',
    },
    ['agones-controller', 'agones-allocator'],
  );
}

export async function installOpenMatch(): Promise<void> {
  logger.info('installing Open-Match');
  await installHelmChart(
    {
      name: 'open-match',
      url: 'https://open-match.dev/chart/stable',
      chartName: 'open-match',
      chartVersion: 'v1.8.1',
    },
    'open-match',
    'open-match',
    {
      ci: true,
      'global.image.registry': 'ghcr.io/jeremylvln',
      'global.image.tag': 'dev',
      'open-match-customize.enabled': true,
      'open-match-customize.evaluator.enabled': true,
      'open-match-customize.evaluator.replicas': 1,
      'open-match-override.enabled': true,
      'open-match-core.swaggerui.enabled': false,
      'query.replicas': 1,
      'frontend.replicas': 1,
      'backend.replicas': 1,
      'redis.sentinel.enabled': false,
      'redis.replica.replicaCount': 0,
      'redis.metrics.enabled': false,
    },
    [
      'open-match-backend',
      'open-match-evaluator',
      'open-match-frontend',
      'open-match-query',
      'open-match-synchronizer',
    ],
  );
}

export async function installShulker(): Promise<void> {
  const chartPath = path.resolve(REPOSITORY_ROOT, 'kube', 'helm');
  const shulkerVersion = JSON.parse(
    readFileSync(path.join(REPOSITORY_ROOT, 'package.json'), 'utf-8'),
  ).version;

  logger.info('installing Shulker');
  await installHelmChart(
    {
      name: 'shulker',
      url: chartPath,
    },
    'shulker-system',
    'shulker',
    {
      'operator.image.tag': 'e2e',
      'operator.extraArgs[0]':
        '--agent-maven-repository=https://maven.jeremylvln.fr/repository/shulker-snapshots',
      'operator.extraArgs[1]': `--agent-version=${shulkerVersion}-SNAPSHOT`,
      // 'shulker-addon-matchmaking.enabled': true,
      // 'shulker-addon-matchmaking.director.image.tag': 'e2e',
      // 'shulker-addon-matchmaking.mmf.image.tag': 'e2e',
    },
    ['shulker-shulker-operator'],
    // shulker-addon-matchmaking-director, shulker-addon-matchmaking-mmf
  );
}
