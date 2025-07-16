import { deleteCluster } from '../utils/kubernetes-k3d.ts';

export default async function () {
  await deleteCluster();
}
