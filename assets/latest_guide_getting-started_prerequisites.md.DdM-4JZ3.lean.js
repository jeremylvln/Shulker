import{_ as t,c as o,a2 as s,o as r}from"./chunks/framework.c3OWTk-0.js";const p=JSON.parse('{"title":"Prerequisites","description":"","frontmatter":{},"headers":[],"relativePath":"latest/guide/getting-started/prerequisites.md","filePath":"latest/guide/getting-started/prerequisites.md"}'),a={name:"latest/guide/getting-started/prerequisites.md"};function l(n,e,i,d,u,c){return r(),o("div",null,e[0]||(e[0]=[s('<h1 id="prerequisites" tabindex="-1">Prerequisites <a class="header-anchor" href="#prerequisites" aria-label="Permalink to &quot;Prerequisites&quot;">​</a></h1><h2 id="kubernetes-cluster" tabindex="-1">Kubernetes Cluster <a class="header-anchor" href="#kubernetes-cluster" aria-label="Permalink to &quot;Kubernetes Cluster&quot;">​</a></h2><p>Shulker should be able to be installed on any Kubernetes cluster meeting the following criterias:</p><ul><li>The minimum known working Kubernetes version is <strong>1.27</strong>, but it way work on older versions as well</li><li>At least one <strong>Linux node</strong> is needed for the Shulker operators to work (amd64 or arm64 architectures are supported)</li></ul><div class="info custom-block"><p class="custom-block-title">INFO</p><p>By default, any <code>ProxyFleet</code> will create automatically a Kubernetes Service of <code>LoadBalancer</code> kind. For this behavior to work properly, your cloud provider should support load balancer provisioning. While this is a non-issue for almost all cloud providers, it may be one if you are self-provisioning your own Kubernetes Cluster.</p></div><div class="info custom-block"><p class="custom-block-title">INFO</p><p>The node requirements are those for the Shulker operators to work. It may not reflect those of containers created by Shulker. While there is no such restrictions by default, a custom configuration from you may prevent some pods to schedule properly.</p></div><p>All Shulker components should be installed in the same namespace, <code>shulker-system</code> by default.</p><h2 id="mandatory-softwares" tabindex="-1">Mandatory softwares <a class="header-anchor" href="#mandatory-softwares" aria-label="Permalink to &quot;Mandatory softwares&quot;">​</a></h2><h3 id="agones" tabindex="-1">Agones <a class="header-anchor" href="#agones" aria-label="Permalink to &quot;Agones&quot;">​</a></h3><p>Shulker delegates the management of game servers (proxies and servers) to Agones.</p><ul><li>Website: <a href="https://agones.dev/site/" target="_blank" rel="noreferrer">https://agones.dev/site/</a></li><li>Installation guide: <a href="https://agones.dev/site/docs/installation/" target="_blank" rel="noreferrer">https://agones.dev/site/docs/installation/</a></li></ul><p>Shulker requires that you to configure Agones to work properly:</p><ul><li>Add your Shulker deployment&#39;s namespace (<code>shulker-system</code> by default) to Agones&#39;s list of <code>GameServer</code> namespaces. This will make Agones create the secret containing the gRPC credentials Shulker will use to interact with its API. Add the namespace to the <code>gameservers.namespaces</code> Helm value</li><li>Enable Agones Allocator component. It is used to summon manually a new <code>GameServer</code> when needed (mostly used in Shulker addons). Set the <code>agones.allocator.install=true</code> Helm value. Optionally make its <code>Service</code> be of type <code>ClusterIP</code> so it will be only used internally by setting the <code>agones.allocator.service.serviceType=ClusterIP</code> value</li></ul><div class="warning custom-block"><p class="custom-block-title">WARNING</p><p>Watch out that while Shulker does not need heavy scaling to handle production workload, Agones sure does. Please consider your needs when installing and configuring Agones.</p></div><h2 id="optional-softwares" tabindex="-1">Optional softwares <a class="header-anchor" href="#optional-softwares" aria-label="Permalink to &quot;Optional softwares&quot;">​</a></h2><p><strong><a href="https://github.com/prometheus-operator/prometheus-operator" target="_blank" rel="noreferrer">Prometheus</a></strong> metrics are also exposed by some components. Monitor manifests can be optionally installed.</p>',16)]))}const m=t(a,[["render",l]]);export{p as __pageData,m as default};
