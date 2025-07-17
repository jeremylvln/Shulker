import * as k8s from '@kubernetes/client-node';
import { execAndWait } from './process.ts';
import path from 'path';
import { importImagesIntoCluster } from './kubernetes-k3d.ts';
import {
  installAgones,
  installCertManager,
  installOpenMatch,
  installShulker,
} from './kubernetes-helm.ts';
import { KC, TEST_NAMESPACE } from './kubernetes.ts';
import { logger, REPOSITORY_ROOT } from './index.ts';

async function createNamespaces(namespaces: readonly string[]): Promise<void> {
  const coreV1 = KC.makeApiClient(k8s.CoreV1Api);
  const existingNamespaces = await coreV1.listNamespace();

  const namespacesToCreate = namespaces.filter(
    (namespace) =>
      !existingNamespaces.items.some(
        (existingNamespace) => existingNamespace.metadata?.name === namespace,
      ),
  );

  for (const namespace of namespacesToCreate) {
    if (
      existingNamespaces.items.some(
        (existingNamespace) => existingNamespace.metadata?.name === namespace,
      )
    ) {
      logger.debug(`namespace ${namespace} already exists`);
      continue;
    }

    logger.info(`creating namespace ${namespace}`);
    await coreV1.createNamespace({
      body: {
        metadata: {
          name: namespace,
        },
      },
    });
  }
}

async function buildImages(apps: readonly string[]): Promise<void> {
  for (const app of apps) {
    logger.info(`building image ${app}`);
    await execAndWait([
      'SKIP_PUSH=true',
      'node',
      path.join('scripts', 'build_docker.cjs'),
      app,
      path.join('packages', app, 'Dockerfile'),
      'e2e',
    ]);
  }

  await importImagesIntoCluster(
    apps.map((app) => `ghcr.io/jeremylvln/${app}:e2e`),
  );
}

export async function prepareCluster() {
  logger.info('preparing cluster');
  await createNamespaces(['shulker-system', TEST_NAMESPACE]);

  if (process.env.SKIP_BUILD_IMAGES !== 'true') {
    await buildImages(['shulker-operator']);
  } else {
    logger.info('skipping building images');
  }

  if (process.env.SKIP_INSTALL_DEPS !== 'true') {
    await installCertManager();
    await installAgones();
    await installOpenMatch();
  } else {
    logger.info('skipping installing dependencies');
  }

  await installShulker();
  logger.info('kubernetes cluster is ready');
}
