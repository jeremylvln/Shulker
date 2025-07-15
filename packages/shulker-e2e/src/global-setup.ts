import 'jest-extended';

import { deleteCluster, startCluster } from './utils-cluster.ts';

export default async function () {
  console.log();
  await deleteCluster();
  await startCluster();
  console.log();
}
