# Installation

Shulker is composed of multiple components, some of them being optional. By design, only the **Shulker Operator** is required to be installed as it contains the core logic.

## Shulker Operator

The **Shulker Operator** can be installed using **Kustomize**:

```bash
$ git clone https://github.com/IamBlueSlime/Shulker
$ kubectl apply -k Shulker/config/default -n shulker-system
```

After this, a `shulker-operator` Pod should be scheduled and work immediately.

:::note

The operator Pod requires a certificate from cert-manager to be provisionned, it may take some seconds/minutes to generate. If the certificate is still not available after some minutes, check your cert-manager logs. There is no special configuration expected, a default installation should work out-of-the-box.

:::
