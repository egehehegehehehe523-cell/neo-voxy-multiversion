# Neo Voxy · Multiversion

[简体中文](README.md)

Neo Voxy is maintained by **JohnSnow**, continuing [NHblock714/voxy](https://github.com/NHblock714/voxy) with multiversion support, client optimizations, and optional mod integrations.

> [!IMPORTANT]
> If problems occur after updating Neo Voxy, first delete the Neo Voxy configuration file and the Voxy cache in the affected world.
>
> **Building**: GitHub Actions build artifacts have been manually deleted. Fork this repository, run `manual-artifact` in your fork, and download the resulting artifacts; alternatively, follow the manual build instructions below.
>
> **Server data sharing**: Neo Voxy is compatible with [voxy-server-side](https://www.curseforge.com/minecraft/mc-mods/voxy-server-side-forge-neoforge), which can be installed on the server to share LOD data.
>
> **Dependencies**: NeoForge uses Sodium, and Forge uses Embeddium. Sinytra Connector and Forgified Fabric API are not project dependencies, and installing them is not recommended.

## Supported Editions

| Edition | Install side | Renderer dependency | Java | Release file |
|---|---|---|---:|---|
| Minecraft 1.21.1 · NeoForge | Client required, server optional | Sodium 0.8.x / Iris 1.8.12+ | 21 | `neo-voxy-0.5.0-beta-mc1.21.1-neoforge.jar` |
| Minecraft 1.20.1 · Forge | Client only | Embeddium / Oculus | 17 | `neo-voxy-0.4.0-beta-mc1.20.1-forge-client.jar` |
| Minecraft 26.1.2 · NeoForge | Client only | Sodium 0.9.2 / Iris 1.11.4+ | 25 | `neo-voxy-0.4.0-beta-mc26.1.2-neoforge-client.jar` |

## Experimental Optimizations

The experimental page includes stationary optimizations, GPU workload optimizations, memory caching, terrain transitions, and simplified shading. Options cover stationary reuse, occlusion precision, batched Hi-Z calculations, and cache limits. Their descriptions explain tradeoffs in frame rate, visuals, latency, and memory use. Hi-Z compilation failures trigger a fallback and a warning. Compare the options individually; performance gains are not guaranteed on all hardware.

## Vanilla Features

| Vanilla feature | 1.21.1 | 1.20.1 | 26.1.2 | Description |
|---|:---:|:---:|:---:|---|
| Iris / Oculus | ✅ | ✅ | ✅ | Shader support |
| Crossed ground-plant models | ✅ | ✅ | ✅ | Displays plants using lightweight crossed models |
| Leaf LOD modes | ✅ | ✅ | ✅ | Provides Fast, Balanced, and Quality modes |
| Extended chunk requests | ✅ | ✅ | ✅ | Requests farther chunks in single-player; disabled by default |
| LOD biome water colours | ✅ | ✅ | ✅ | Handles water-colour transitions between biomes |
| LOD build-pressure control | ✅ | ✅ | ✅ | Adjusts the priority of frame rate versus build speed |
| World curvature | ✅ | ✅ | ✅ | Curves distant terrain in the GPU vertex stage |
| LOD beacon beams | ✅ | — | — | Displays beacon beams at long range |

`✅` means supported, `❌` means unavailable, and `—` means not applicable. If both circular LOD handoff and a shader pack's own handoff feature are enabled, disabling one of them is recommended.

## Mod Integrations

Integrations activate only when the corresponding mod is installed. The version column lists the Minecraft and mod versions used as the current integration verification baseline.

| Mod (Chinese / English) and version | Status | Integration |
|---|:---:|---|
| [机械动力 / Create](https://www.curseforge.com/minecraft/mc-mods/create)<br>MC 1.21.1 + Create 6.0.10 | ✅ | LOD trains, tracks, contraptions, and kinetic components |
| [机械动力：航空学 / Create Aeronautics](https://www.curseforge.com/minecraft/mc-mods/create-aeronautics)<br>MC 1.21.1 + Aeronautics 1.3.2 | 🧪 | LOD laser-pointer beams |
| [机械动力：交错电网 / Create: Power Grid](https://www.curseforge.com/minecraft/mc-mods/power-grid)<br>MC 1.21.1 + Power Grid 0.6.1 | ✅ | LOD hanging wires, cords, and string-light wires |
| [机械动力：伪装方块+ / Create: Copycats+](https://www.curseforge.com/minecraft/mc-mods/copycats)<br>MC 1.21.1 + Copycats+ 3.0.9 | ✅ | Dedicated LOD models and materials for slopes, slices, doors, shafts, and cogwheels |
| [Sable / Sable](https://www.curseforge.com/minecraft/mc-mods/sable)<br>MC 1.21.1 + Sable 2.0.5 | ✅ | LOD vehicles |
| [节气 / Ecliptic Seasons](https://www.curseforge.com/minecraft/mc-mods/ecliptic-seasons)<br>MC 1.21.1 + Ecliptic Seasons 0.15.0-rc-3-1 | ✅ | LOD seasonal snow, frozen water, seasonal models, and colours |
| [模拟殖民地 / Domum Ornamentum](https://www.curseforge.com/minecraft/mc-mods/domum-ornamentum)<br>MC 1.21.1 + Domum Ornamentum 1.0.236-snapshot | ✅ | LOD Domum Ornamentum models and materials |
| [框架方块 / FramedBlocks](https://www.curseforge.com/minecraft/mc-mods/framedblocks)<br>MC 1.21.1 + FramedBlocks 10.6.1 | ✅ | LOD framed blocks and camouflage materials |
| [小方块 / LittleTiles](https://www.curseforge.com/minecraft/mc-mods/littletiles)<br>MC 1.21.1 + LittleTiles 1.6.0-pre226 | 🧪 | Experimental functionality only; poor performance |

### GemRender 0.1.20 / Golem Core 1.0.4 on Minecraft 1.21.1

Compatibility safeguard for [upstream issue #57](https://github.com/NHblock-Johnsnow/neo-voxy-multiversion/issues/57):

GemRender's optional Voxy lighting mixin rewrites the GLSL returned by
`ShaderLoader.parse` for `quads3.vert` and `quads.frag`. That patch expects the
older `vec4 pos = MVP * vec4(point, 1.0f);` statement, but Neo Voxy 0.5.0
uses `applyWorldCurvature(getQuadCornerPoint(...))` instead. The old patch
also expects location 2 for its varying and the upper bits of `interData.w`
for its light ID; Neo Voxy uses those for boundary distance and leaf-hash data.
Applying the old patch unchanged can fail GLSL source replacement, shader
compilation, or rendering.

When GemRender is installed, **only these two Voxy 0.5.0 GLSL sources** are
loaded without GemRender's older source-rewrite hook. GemRender itself,
Golem Core, NTM, and every other Voxy shader remain enabled. This is a
crash-prevention fallback rather than a full dynamic-lighting port:
**GemRender's lighting on distant Voxy LOD terrain is not provided by this
compatibility fallback.** No changes are made to Voxy's internal cache,
render distance, memory limits, or ingestion queue.

The issue's three crash reports all terminate in vanilla code after the
client player has already become null (two in MultiPlayerGameMode, one in
GameRenderer). They show shader packs off and do not contain the first
exception that caused the world to be discarded. Therefore the source
mismatch above is independently verified, but the **complete root cause
of those three crashes is not yet proven**. Test with GemRender/Golem Core
in a fresh world and share `logs/latest.log` if it still disconnects; that
log preserves preceding renderer-initialization errors.

## Building

Build individual editions on Windows:

```powershell
.\scripts\build.ps1 neoforge-1.21.1
.\scripts\build.ps1 client-1.20.1
.\scripts\build.ps1 client-26.1.2
```

Build all editions:

```powershell
.\scripts\build-all.ps1
```

The scripts prefer `JAVA_HOME_17`, `JAVA_HOME_21`, and `JAVA_HOME_25`. On Linux/macOS, use `scripts/build.sh` and `scripts/build-all.sh`. Final artifacts are copied to `dist/`.

## License

See the license files shipped with each edition.
