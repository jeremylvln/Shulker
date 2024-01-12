import{_ as s,o as e,c as i,R as a}from"./chunks/framework.YRogKEPu.js";const g=JSON.parse('{"title":"Installation","description":"","frontmatter":{},"headers":[],"relativePath":"latest/guide/getting-started/installation.md","filePath":"latest/guide/getting-started/installation.md"}'),t={name:"latest/guide/getting-started/installation.md"},n=a(`<h1 id="installation" tabindex="-1">Installation <a class="header-anchor" href="#installation" aria-label="Permalink to &quot;Installation&quot;">​</a></h1><p>Shulker is composed of multiple components, some of them being optional. By design, only the <strong>Shulker Operator</strong> is required to be installed as it contains the core logic.</p><h2 id="using-helm" tabindex="-1">Using Helm <a class="header-anchor" href="#using-helm" aria-label="Permalink to &quot;Using Helm&quot;">​</a></h2><p>If you need to fine-tune Shulker and its different components, an exhaustive Helm chart is provided. The default configuration is enough to get started.</p><p>The Helm Chart is available in the <a href="https://github.com/jeremylvln/Shulker/tree/main/kube/helm" target="_blank" rel="noreferrer"><code>kube/helm</code></a> folder of the repository.</p><p>To install Shulker using Helm:</p><div class="language-bash vp-adaptive-theme line-numbers-mode"><button title="Copy Code" class="copy"></button><span class="lang">bash</span><pre class="shiki shiki-themes github-light github-dark vp-code"><code><span class="line"><span style="--shiki-light:#6F42C1;--shiki-dark:#B392F0;">$</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> git</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> clone</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> https://github.com/jeremylvln/Shulker</span></span>
<span class="line"><span style="--shiki-light:#6F42C1;--shiki-dark:#B392F0;">$</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> cd</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> Shulker</span></span>
<span class="line"><span style="--shiki-light:#6F42C1;--shiki-dark:#B392F0;">$</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> git</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> checkout</span><span style="--shiki-light:#D73A49;--shiki-dark:#F97583;"> &lt;</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;">version</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> tag</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> to</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> us</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">e</span><span style="--shiki-light:#D73A49;--shiki-dark:#F97583;">&gt;</span></span>
<span class="line"><span style="--shiki-light:#6F42C1;--shiki-dark:#B392F0;">$</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> cd</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> kube/helm</span></span>
<span class="line"><span style="--shiki-light:#6F42C1;--shiki-dark:#B392F0;">$</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> helm</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> install</span><span style="--shiki-light:#005CC5;--shiki-dark:#79B8FF;"> -n</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> shulker-system</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> .</span></span></code></pre><div class="line-numbers-wrapper" aria-hidden="true"><span class="line-number">1</span><br><span class="line-number">2</span><br><span class="line-number">3</span><br><span class="line-number">4</span><br><span class="line-number">5</span><br></div></div><div class="tip custom-block"><p class="custom-block-title">TIP</p><p>Replace <code>&lt;version tag to use&gt;</code> by the name of the version you want to install, for instance <code>v0.3.0</code>.</p></div><h2 id="using-pre-rendered-manifests" tabindex="-1">Using pre-rendered manifests <a class="header-anchor" href="#using-pre-rendered-manifests" aria-label="Permalink to &quot;Using pre-rendered manifests&quot;">​</a></h2><p>Pre-rendered manifests for common uses are provided and are generated for the Helm charts. It allows you to test Shulker in your cluster without hassle.</p><p>The manifests are available in the <a href="https://github.com/jeremylvln/Shulker/tree/main/kube/manifests" target="_blank" rel="noreferrer"><code>kube/manifests</code></a> folder of the repository.</p><p>There are 4 pre-rendered variants available:</p><ul><li><code>stable.yaml</code>: a default configuration as you would render the Helm chart without modifying the values</li><li><code>stable-with-prometheus.yaml</code>: the same default configuration with Prometheus support, including <code>ServiceMonitor</code> for the different components</li><li><code>next.yaml</code>: the same configuration as for <code>stable.yaml</code>, with the images tagged to <code>next</code> to quickly test the future release</li><li><code>next-with-prometheus.yaml</code>: the combination of <code>next.yaml</code> with Prometheus support</li></ul><p>You can apply them directly with <code>kubectl</code>:</p><div class="language-bash vp-adaptive-theme line-numbers-mode"><button title="Copy Code" class="copy"></button><span class="lang">bash</span><pre class="shiki shiki-themes github-light github-dark vp-code"><code><span class="line"><span style="--shiki-light:#6F42C1;--shiki-dark:#B392F0;">$</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> kubectl</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> apply</span><span style="--shiki-light:#005CC5;--shiki-dark:#79B8FF;"> -f</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> stable.yaml</span><span style="--shiki-light:#005CC5;--shiki-dark:#79B8FF;"> -n</span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;"> shulker-system</span></span></code></pre><div class="line-numbers-wrapper" aria-hidden="true"><span class="line-number">1</span><br></div></div>`,15),l=[n];function h(r,o,p,d,k,c){return e(),i("div",null,l)}const m=s(t,[["render",h]]);export{g as __pageData,m as default};
