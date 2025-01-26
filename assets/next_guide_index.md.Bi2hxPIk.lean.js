import{_ as t,c as o,a2 as a,o as n}from"./chunks/framework.CaW-x5sm.js";const p=JSON.parse('{"title":"Introduction","description":"","frontmatter":{},"headers":[],"relativePath":"next/guide/index.md","filePath":"next/guide/index.md"}'),r={name:"next/guide/index.md"};function i(s,e,l,h,u,d){return n(),o("div",null,e[0]||(e[0]=[a('<h1 id="introduction" tabindex="-1">Introduction <a class="header-anchor" href="#introduction" aria-label="Permalink to &quot;Introduction&quot;">​</a></h1><p>Shulker aims to be your default choice to managing Minecraft infrastructures in the cloud with Kubernetes.</p><blockquote><p>Everybody with some knowledge about Kubernetes should be able to connect to a Minecraft Network within 10 minutes. <strong>That&#39;s the goal.</strong></p></blockquote><h2 id="the-idea" tabindex="-1">The Idea <a class="header-anchor" href="#the-idea" aria-label="Permalink to &quot;The Idea&quot;">​</a></h2><p>Hosting a Minecraft server to play with some friends is fairly easy. And if you do not want to do it yourself, they are plenty of hosting providers that will fee you a couple of money to handle this for you.</p><p><strong>However</strong>, once you&#39;ll need some custom architecture, like having multiple Minecraft servers a player can connect to, or if you have the ambition of handling a bigger number of players, <strong>things gets complicated at best</strong>.</p><p>Managing proxies like Velocity or BungeeCord is still manageable by hand, while their number is still reasonable. The same apply for servers that are persistent as they should only be started once and then kept alive. This will only <strong>cost you money and time</strong>, things that you&#39;ll not be able to invest on something else.</p><p>Things gets really difficult when your Minecraft servers are ephemeral, as they should be destroyed and cleaned up on each reboot. Doing this by hand is impossible, and scripts would only cover the basic cases while struggling on any error.</p><p>Containers is part of the solution here, they provide a way of running isolated programs in a descriptive way, without any human management (apart from launching the containers, of course). However you&#39;ll still have to automate the creation and destruction of containers.</p><p>Let&#39;s consider that you have a perfect solution to run your containers automatically on your server fleet. The last, and maybe the most important, point you&#39;ll have to figure out is the <strong>cost</strong>: having dedicated servers running servers is fine, however this is profitable only if they are running at 100% usage every hour, every day. That&#39;s where cloud computing shows some benefits: having a dynamic infrastructure is not only starting Minecraft servers when needed, but also redeeming only the compute power you need, when you need it.</p><p><strong>This is where Kubernetes and Shulker comes in.</strong></p><h2 id="boundaries" tabindex="-1">Boundaries <a class="header-anchor" href="#boundaries" aria-label="Permalink to &quot;Boundaries&quot;">​</a></h2><p>To be everybody <em>go-to</em>, we should pay close attention to what arbitrary choices we make in order to simplify your life while not constraining you from custom development. To achieve that we had to define strong principles that define the limits of what Shulker should do:</p><ul><li><strong>Schedule automatically Minecraft proxies and servers from a specification you describe</strong>: to allow you configure your softwares without having to manage multiple separate files by hand.</li><li><strong>Keep up-to-date the server registry of your proxies</strong>: as Shulker already has the list and availability of all the servers in the Cluster, it should also keep the proxies up-to-date about the creation and deletion of servers.</li><li><strong>Try to save the players as most as possible</strong>: while errors can occur, Shulker should try as most as possible to avoid the disconnection of the player.</li></ul><p>Everything beyond this should be opt-in at the discretion of the user.</p>',15)]))}const y=t(r,[["render",i]]);export{p as __pageData,y as default};
