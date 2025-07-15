import path from 'path';
import { execAndWait, logger } from './utils.ts';
import { waitForPod } from './utils-cluster.ts';

describe('01 - Getting Started Example', () => {
  const GETTING_STARTED_EXAMPLE_PATH = path.join(
    import.meta.dirname,
    '..',
    '..',
    '..',
    'examples',
    'getting-started',
  );

  afterAll(async () => {
    await execAndWait([
      'kubectl delete -n shulker-test -k',
      GETTING_STARTED_EXAMPLE_PATH,
    ]);
  });

  it('01.1 - Applies the manifests', async () => {
    // Given

    // When
    await execAndWait([
      'kubectl apply -n shulker-test -k',
      GETTING_STARTED_EXAMPLE_PATH,
    ]);

    // Then
    const clusterName = await execAndWait(
      'kubectl get minecraftclusters/getting-started -n shulker-test -o name',
      { silent: true },
    );
    expect(clusterName).toStrictEqual(
      'minecraftcluster.shulkermc.io/getting-started',
    );
  });

  it('01.2 - Creates a managed Redis deployment', async () => {
    // Given

    // When
    const statefulSetName = await execAndWait(
      'kubectl get sts getting-started-redis-managed -n shulker-test -o name',
      { silent: true },
    );

    // Then
    expect(statefulSetName).toStrictEqual(
      'statefulset.apps/getting-started-redis-managed',
    );
    logger.info('waiting for Redis pod to be ready');
    await waitForPod('shulker-test', 'getting-started-redis-managed-0');
  });

  it('01.3 - Creates a proxy pod', async () => {
    // Given

    // When
    const podName = await execAndWait(
      'kubectl get pods -n shulker-test -o name | grep public-',
      { silent: true },
    );

    // Then
    expect(podName).toStartWith('pod/public-');
    logger.info('waiting for Proxy pod to be ready');
    await waitForPod('shulker-test', podName);
  });
});
