import * as k8s from '@kubernetes/client-node';

import { KC, TEST_NAMESPACE } from './kubernetes.ts';

export async function getPodLogs(
  name: string,
  container: string,
  namespace = TEST_NAMESPACE,
): Promise<string> {
  return KC.makeApiClient(k8s.CoreV1Api).readNamespacedPodLog({
    namespace,
    name,
    container,
  });
}

export async function getProxyLogs(
  name: string,
  namespace = TEST_NAMESPACE,
): Promise<string> {
  return getPodLogs(name, 'proxy', namespace);
}

export async function getServerLogs(
  name: string,
  namespace = TEST_NAMESPACE,
): Promise<string> {
  return getPodLogs(name, 'minecraft-server', namespace);
}
