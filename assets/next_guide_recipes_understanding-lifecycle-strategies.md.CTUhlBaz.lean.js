import{_ as a,c as e,a2 as i,o as n}from"./chunks/framework.c3OWTk-0.js";const k=JSON.parse('{"title":"Understanding Lifecycle Strategies","description":"","frontmatter":{},"headers":[],"relativePath":"next/guide/recipes/understanding-lifecycle-strategies.md","filePath":"next/guide/recipes/understanding-lifecycle-strategies.md"}'),t={name:"next/guide/recipes/understanding-lifecycle-strategies.md"};function l(r,s,p,h,o,c){return n(),e("div",null,s[0]||(s[0]=[i(`<h1 id="understanding-lifecycle-strategies" tabindex="-1">Understanding Lifecycle Strategies <a class="header-anchor" href="#understanding-lifecycle-strategies" aria-label="Permalink to &quot;Understanding Lifecycle Strategies&quot;">​</a></h1><p>Your proxies and servers lifecycles are <em>described</em> by Shulker but are actually managed by <strong>Agones</strong>.</p><p>You may want to customize the lifecycle of your servers so Agones does not disturb your players at innapropriate times.</p><div class="info custom-block"><p class="custom-block-title">EXAMPLE</p><p>For instance, you may not want Agones to update your fleet of servers because you upgraded a plugin while some players are in a mini-game.</p></div><p>While Shulker&#39;s server agent marks your server as <code>Ready</code> when the agent is fully loaded, you may want to also mark your server as <code>Allocated</code> when an interrupted game session is needed (a mini-game for instance).</p><p>Shulker allows you to choose a <strong>Lifecycle Strategy</strong> that will change the automatic behaviors of Agones. It can be changed on the <code>MinecraftServer</code> and <code>MinecraftServerFleet</code>:</p><div class="language-yaml vp-adaptive-theme line-numbers-mode"><button title="Copy Code" class="copy"></button><span class="lang">yaml</span><pre class="shiki shiki-themes github-light github-dark has-focused-lines vp-code" tabindex="0"><code><span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">apiVersion</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">: </span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;">shulkermc.io/v1alpha1</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">kind</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">: </span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;">MinecraftServerFleet</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">metadata</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">:</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">  name</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">: </span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;">dropper-game</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">spec</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">:</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">  clusterRef</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">:</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">    name</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">: </span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;">getting-started</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">  replicas</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">: </span><span style="--shiki-light:#005CC5;--shiki-dark:#79B8FF;">1</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">  template</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">:</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">    spec</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">:</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">      clusterRef</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">:</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">        name</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">: </span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;">getting-started</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">      tags</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">:</span></span>
<span class="line"><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">        - </span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;">lobby</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">      version</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">:</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">        channel</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">: </span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;">Paper</span></span>
<span class="line"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">        name</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">: </span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;">&#39;1.20.4&#39;</span></span>
<span class="line has-focus"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">      config</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">:  </span></span>
<span class="line has-focus"><span style="--shiki-light:#22863A;--shiki-dark:#85E89D;">        lifecycleStrategy</span><span style="--shiki-light:#24292E;--shiki-dark:#E1E4E8;">: </span><span style="--shiki-light:#032F62;--shiki-dark:#9ECBFF;">AllocateWhenNotEmpty</span></span></code></pre><div class="line-numbers-wrapper" aria-hidden="true"><span class="line-number">1</span><br><span class="line-number">2</span><br><span class="line-number">3</span><br><span class="line-number">4</span><br><span class="line-number">5</span><br><span class="line-number">6</span><br><span class="line-number">7</span><br><span class="line-number">8</span><br><span class="line-number">9</span><br><span class="line-number">10</span><br><span class="line-number">11</span><br><span class="line-number">12</span><br><span class="line-number">13</span><br><span class="line-number">14</span><br><span class="line-number">15</span><br><span class="line-number">16</span><br><span class="line-number">17</span><br><span class="line-number">18</span><br><span class="line-number">19</span><br></div></div><h2 id="allocatewhennotempty-strategy" tabindex="-1"><code>AllocateWhenNotEmpty</code> strategy <a class="header-anchor" href="#allocatewhennotempty-strategy" aria-label="Permalink to &quot;\`AllocateWhenNotEmpty\` strategy&quot;">​</a></h2><p>With this strategy, Shulker&#39;s server agent will mark your server as <code>Allocated</code> when at lease one player is connected on the server.</p><p>This will disable any automatic reschedule of Agones that may be due to a plugin upgrade. For your server to be updated, you&#39;ll have to either:</p><ol><li>Have all the players disconnected for the server is set back to <code>Ready</code></li><li>Shutdown yourself the server (with the <code>/server</code> command for instance)</li></ol><div class="info custom-block"><p class="custom-block-title">INFO</p><p>Shulker will still mark your server as <code>Ready</code> once the agent is fully loaded.</p></div><h2 id="manual-strategy" tabindex="-1"><code>Manual</code> strategy <a class="header-anchor" href="#manual-strategy" aria-label="Permalink to &quot;\`Manual\` strategy&quot;">​</a></h2><p>With this strategy, no extra work is done apart marking the server as <code>Ready</code> after loading. It is up to you and custom implementation to manage the lifecycle of your server.</p><div class="warning custom-block"><p class="custom-block-title">WARNING</p><p>Using this strategy with no custom implementation will keep the <code>Ready</code> state forever. Thus, Agones will always think that the server is not used and it may be recreated at any time for any reason.</p></div>`,15)]))}const g=a(t,[["render",l]]);export{k as __pageData,g as default};
