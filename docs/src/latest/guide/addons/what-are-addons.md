# Addons

While Shulker has strong and well defined objectives to solve, Shulker
is also present in every part of your Minecraft infrastructure: at the
Kubernetes level of course, by administrating the different resources, but
also in your proxies and servers thanks to the built-in agent plugins.

This makes Shulker capable of providing additional features that are not
strictly required for your Minecraft cluster to operate, but are not easy
to implement by yourself.

Shulker's **addons** are optional and opt-in components that can provide
additional functionalities to your Minecraft clusters. They are not meant
for everyone but for those who are interested in these specific features.

:::warning

It is important to note that Shulker is an infrastructure management tool
before all. This means that some addons may need further work from you
(deployment of required infrastructure, plugin development, etc...).

Providing a ready-to-use SDK will be preferred for you to manipulate
addons allowing third-party manipulation, but will be not required.

:::

##
