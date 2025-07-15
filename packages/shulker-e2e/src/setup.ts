import { deleteCluster, startCluster } from './utils-cluster.ts';

export default async function () {
  deleteCluster();
  startCluster();
}
