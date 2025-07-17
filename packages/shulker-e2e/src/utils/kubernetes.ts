import * as k8s from '@kubernetes/client-node';

export const TEST_NAMESPACE = 'shulker-test';
export const KC = new k8s.KubeConfig();

// export async function restartDeployment(
//   namespace: string,
//   name: string,
// ): Promise<void> {
//   await KC.makeApiClient(k8s.AppsV1Api).patchNamespacedDeployment({
//     namespace,
//     name,
//     body: {
//       spec: {
//         template: {
//           metadata: {
//             annotations: {
//               'kubectl.kubernetes.io/restartedAt': new Date().toISOString(),
//             },
//           },
//         },
//       },
//     },
//   });
// }
