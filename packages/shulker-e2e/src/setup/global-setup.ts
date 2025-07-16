import 'jest-extended';
import { createCluster, deleteCluster } from '../utils/kubernetes-k3d.ts';
import { prepareCluster } from '../utils/kubernetes-prepare.ts';

export default async function () {
  console.log();

  await deleteCluster();
  await createCluster();
  await prepareCluster();

  console.log();
}
