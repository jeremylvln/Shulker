import { logger } from './index.ts';
import { KC } from './kubernetes.ts';
import { execAndWait } from './process.ts';

const K3D_CLUSTER_NAME = process.env.K3D_CLUSTER_NAME ?? 'shulker-test';
const K3D_IS_PROVIDED = typeof process.env.K3D_CLUSTER_NAME === 'string';

export async function createCluster(): Promise<void> {
  if (!K3D_IS_PROVIDED) {
    logger.info(`creating k3d cluster ${K3D_CLUSTER_NAME}`);
    await execAndWait(`k3d cluster create ${K3D_CLUSTER_NAME} --no-lb || true`);
  }

  KC.loadFromDefault();
}

export async function deleteCluster(): Promise<void> {
  if (K3D_IS_PROVIDED) {
    return;
  }

  logger.info(`destroying k3d cluster ${K3D_CLUSTER_NAME}`);
  await execAndWait(`k3d cluster delete ${K3D_CLUSTER_NAME} || true`);
}

export async function importImagesIntoCluster(
  tags: readonly string[],
): Promise<void> {
  logger.info(`importing images into k3d cluster ${K3D_CLUSTER_NAME}`);
  await execAndWait([`k3d image import -c ${K3D_CLUSTER_NAME}`, ...tags]);
}
