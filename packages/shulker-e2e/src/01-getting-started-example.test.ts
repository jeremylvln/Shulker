import * as k8s from '@kubernetes/client-node';
import path from 'path';
import { execAndWait } from './utils/process.ts';
import type { MinecraftCluster } from './utils/types.ts';
import { logger, REPOSITORY_ROOT } from './utils/index.ts';
import { KC, TEST_NAMESPACE } from './utils/kubernetes.ts';
import { waitForPod } from './utils/kubernetes-watch.ts';
import { getProxyLogs, getServerLogs } from './utils/kubernetes-logs.ts';
import retry from 'async-retry';

describe('01 - Getting Started Example', () => {
  const GETTING_STARTED_EXAMPLE_PATH = path.join(
    REPOSITORY_ROOT,
    'examples',
    'getting-started',
  );

  let proxyPodName: string;
  let serverPodName: string;

  beforeAll(() => {
    KC.loadFromDefault();
  });

  afterAll(async () => {
    await execAndWait([
      'kubectl delete -n shulker-test -k',
      GETTING_STARTED_EXAMPLE_PATH,
    ]);
  });

  it('applies the manifests', async () => {
    await execAndWait([
      `kubectl apply -n ${TEST_NAMESPACE} -k`,
      GETTING_STARTED_EXAMPLE_PATH,
    ]);

    const minecraftClusters = await KC.makeApiClient(k8s.CustomObjectsApi)
      .listNamespacedCustomObject({
        namespace: TEST_NAMESPACE,
        group: 'shulkermc.io',
        version: 'v1alpha1',
        plural: 'minecraftclusters',
      })
      .then((res) => res as k8s.KubernetesListObject<MinecraftCluster>);
    const gettingStartedCluster = minecraftClusters.items.find(
      (cluster) => cluster.metadata?.name === 'getting-started',
    );

    expect(gettingStartedCluster).toBeDefined();
  });

  it('creates a managed Redis deployment', async () => {
    await retry(
      async () => {
        const statefulSets = await KC.makeApiClient(
          k8s.AppsV1Api,
        ).listNamespacedStatefulSet({
          namespace: TEST_NAMESPACE,
        });
        const redisStatefulSet = statefulSets.items.find(
          (sts) => sts.metadata?.name === 'getting-started-redis-managed',
        );

        expect(redisStatefulSet).toBeDefined();
      },
      {
        minTimeout: 500,
        retries: 5,
      },
    );

    logger.info('waiting for Redis pod to be ready');
    await waitForPod('getting-started-redis-managed-0');
  });

  it('has created a proxy pod', async () => {
    await retry(
      async () => {
        const pods = await KC.makeApiClient(k8s.CoreV1Api).listNamespacedPod({
          namespace: TEST_NAMESPACE,
        });
        const proxyPod = pods.items.find((pod) =>
          pod.metadata?.name?.startsWith('public-'),
        );

        expect(proxyPod).toBeDefined();
        proxyPodName = proxyPod!.metadata!.name!;
      },
      {
        minTimeout: 500,
        retries: 5,
      },
    );
  });

  it('waits until the proxy is ready', async () => {
    await waitForPod(proxyPodName);

    const proxyLogs = await getProxyLogs(proxyPodName);
    expect(proxyLogs).toInclude('Proxy is ready');
  });

  it('has created a server pod', async () => {
    await retry(
      async () => {
        const pods = await KC.makeApiClient(k8s.CoreV1Api).listNamespacedPod({
          namespace: TEST_NAMESPACE,
        });
        const serverPod = pods.items.find((pod) =>
          pod.metadata?.name?.startsWith('lobby-'),
        );

        // Then
        expect(serverPod).toBeDefined();
        serverPodName = serverPod!.metadata!.name!;
      },
      {
        minTimeout: 500,
        retries: 5,
      },
    );
  });

  it('waits until the server is ready', async () => {
    await waitForPod(serverPodName);

    await retry(
      async () => {
        const serverLogs = await getServerLogs(serverPodName);
        expect(serverLogs).toInclude('For help, type \"help\"');
      },
      {
        minTimeout: 1000,
        retries: 10,
      },
    );
  });

  it('ensures the proxy has registered the server', async () => {
    await retry(
      async () => {
        const proxyLogs = await getProxyLogs(proxyPodName);
        expect(proxyLogs).toInclude(
          `Added server '${serverPodName}' to directory`,
        );
      },
      {
        minTimeout: 500,
        retries: 5,
      },
    );
  });
});
