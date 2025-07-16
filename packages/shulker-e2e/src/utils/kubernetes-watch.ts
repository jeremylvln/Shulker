import * as k8s from '@kubernetes/client-node';
import { KC, TEST_NAMESPACE } from './kubernetes.ts';

function isDeploymentReady(deployment: k8s.V1Deployment): boolean {
  return (
    typeof deployment.spec?.replicas === 'number' &&
    typeof deployment.status?.availableReplicas === 'number' &&
    typeof deployment.status?.updatedReplicas === 'number' &&
    deployment.status.availableReplicas >= deployment.spec?.replicas &&
    deployment.status.updatedReplicas >= deployment.spec?.replicas &&
    (deployment.status.unavailableReplicas ?? 0) <= 0
  );
}

function isPodReady(pod: k8s.V1Pod): boolean {
  return (
    pod.status?.conditions?.some(
      (condition) => condition.type === 'Ready' && condition.status === 'True',
    ) ?? false
  );
}

export async function waitForDeployment(
  name: string,
  namespace = TEST_NAMESPACE,
  timeoutMillis = 1000 * 60 * 3,
): Promise<void> {
  await new Promise<void>(async (resolve, reject) => {
    const watchRequest = await new k8s.Watch(KC).watch(
      `/apis/apps/v1/namespaces/${namespace}/deployments`,
      {
        fieldSelector: `metadata.name=${name}`,
        timeoutSeconds: timeoutMillis / 1000,
      },
      (phase: string, apiObj: k8s.V1Deployment) => {
        if (
          ['ADDED', 'MODIFIED'].includes(phase) &&
          isDeploymentReady(apiObj)
        ) {
          watchRequest?.abort();
          resolve();
        } else if (phase === 'DELETED' || phase === 'ERROR') {
          watchRequest?.abort();
          reject(new Error('Deployment was deleted or is in error state'));
        }
      },
      (err) => {
        if (err && !(err instanceof k8s.AbortError)) {
          return reject(err);
        }
        resolve();
      },
    );
  });
}

export async function waitForPod(
  name: string,
  namespace = TEST_NAMESPACE,
  timeoutMillis = 1000 * 60 * 3,
): Promise<void> {
  await new Promise<void>(async (resolve, reject) => {
    const watchRequest = await new k8s.Watch(KC).watch(
      `/api/v1/namespaces/${namespace}/pods`,
      {
        fieldSelector: `metadata.name=${name}`,
        timeoutSeconds: timeoutMillis / 1000,
      },
      (phase: string, apiObj: k8s.V1Pod) => {
        if (['ADDED', 'MODIFIED'].includes(phase) && isPodReady(apiObj)) {
          watchRequest?.abort();
          resolve();
        } else if (phase === 'DELETED' || phase === 'ERROR') {
          watchRequest?.abort();
          reject(new Error('Pod was deleted or is in error state'));
        }
      },
      (err) => {
        if (err && !(err instanceof k8s.AbortError)) {
          return reject(err);
        }
        resolve();
      },
    );
  });
}
