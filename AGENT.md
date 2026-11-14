# AGENT.md

This document provides project context, guidelines, architecture notes, and operational procedures for autonomous AI agents and coding assistants working on the **Not Enough Channels** codebase.

---

## 📌 Project Overview

- **Mod Name:** `not-enough-channels`
- **Mod ID:** `notenoughchannels`
- **Package Base:** `net.modelai.website`
- **Minecraft Version:** `1.21.1`
- **Mod Loader:** NeoForge (`21.1.265`)
- **Language / Runtime:** Kotlin (targeting Java 21)
- **Kotlin Loader:** [Kotlin for Forge (KFF)](https://github.com/thedarkcolour/KotlinForForge) version `5.12.0` (bundles Kotlin `2.4.0`)
- **Gradle Version:** `9.2.1` with `net.neoforged.moddev:2.0.148`
- **Core Library Dependencies:**
  - **Titanium:** `1.21-4.0.43` (`libs/titanium-1.21-4.0.43.jar`) for advanced machines, `PoweredTile`, custom GUI screens, and addons.
  - **Lodestone:** `curse.maven:lodestone-616457:7264731` for high-performance additive VFX particle effects.

---

## 🏗️ Architecture & Project Structure

```
not-enough-channels-1.21.1/
├── build.gradle                               # ModDevGradle, Kotlin, Titanium & Lodestone dependencies
├── gradle.properties                          # Project properties & JVM target settings
├── libs/                                      # Local runtime test mod JARs (loaded in runClient)
├── notes/                                     # Planning, design docs, and architecture notes
│   └── plan/
│       ├── plan_multiblock_solar_machine.md   # Master multiblock machine specification
│       ├── plan_5x5_multiblock_shape.md       # Exact 5x5x5 geometry and offset pattern
│       ├── plan_phase5_solar_blue_gui.md      # Titanium custom GUI styling specification
│       ├── plan_phase6_lodestone_vfx.md       # Client VFX & particle system specification
│       └── plan_10_sticks_recipe.md           # Sample 10 Sticks testing recipe
├── src/
│   ├── main/
│   │   ├── kotlin/net/modelai/website/        # Mod Kotlin source code
│   │   │   ├── NotEnoughChannels.kt           # Main mod class (@Mod), capabilities & recipe setup
│   │   │   ├── NotEnoughChannelsClient.kt     # Client-only setup & screens (@EventBusSubscriber)
│   │   │   ├── Config.kt                      # ModConfigSpec definition
│   │   │   ├── block/                         # Block registration & custom blocks
│   │   │   │   ├── ModBlocks.kt               # Block DeferredRegister
│   │   │   │   ├── custom/
│   │   │   │   │   ├── SolarMultiblockMachineBlock.kt # Floating controller machine block (BasicTileBlock)
│   │   │   │   │   └── SideStructureBlock.kt  # Skylight-transparent structure block with event notifications
│   │   │   │   └── entity/                    # Block entities & tile logic
│   │   │   │       ├── ModBlockEntities.kt    # BlockEntity DeferredRegister
│   │   │   │       └── SolarMultiblockTileEntity.kt # 10M FE PoweredTile, pattern validator, crafting loop
│   │   │   ├── client/                        # Client-only rendering & GUI
│   │   │   │   ├── gui/
│   │   │   │   │   ├── BluePanelScreenAddon.kt# 264x180 deep navy & electric cyan sci-fi background panel
│   │   │   │   │   └── SolarStatusScreenAddon.kt # Live header status pill badge (Sunlight & 5x5 validation)
│   │   │   │   └── vfx/
│   │   │   │       └── SolarMachineVFX.kt     # Lodestone particle spawner (core wisps, twinkles, ribbons)
│   │   │   ├── crafting/                      # Crafting mechanics & registries
│   │   │   │   ├── LightningCrafting.kt       # Tiered & Multi-Item lightning recipe registry & DSL
│   │   │   │   ├── LightningCraftingHandler.kt# Event bus listener for lightning transformations
│   │   │   │   └── SolarMachineCrafting.kt    # Bipartite shapeless recipe engine (10 items, 3 fluids)
│   │   │   ├── gui/                           # Common GUI definitions
│   │   │   │   └── SolarBlueAssetProvider.kt  # Custom Titanium IAssetProvider with 264x180 dimensions
│   │   │   ├── item/                          # Deferred items & custom item classes
│   │   │   │   ├── ModItems.kt                # Item registry (sticks, snowballs, apple, rock, block items)
│   │   │   │   └── custom/
│   │   │   │       ├── ChannelStickItem.kt    # Tiered stick item (lightning summoner with tags)
│   │   │   │       └── ChannelAppleItem.kt    # Custom food item
│   │   │   └── tab/                           # Creative mode tabs
│   │   │       └── ModCreativeTabs.kt
│   │   ├── resources/                         # Assets, data, textures, lang files
│   │   │   └── assets/notenoughchannels/
│   │   │       ├── blockstates/               # Blockstate definitions for solar machine & side blocks
│   │   │       ├── lang/en_us.json            # English translations
│   │   │       ├── models/block/              # Block models
│   │   │       ├── models/item/               # Item JSON models
│   │   │       └── textures/gui/empty.png     # 1x1 transparent texture preventing Titanium default bleed
│   │   └── templates/META-INF/
│   │       └── neoforge.mods.toml             # Mod metadata template processed at build
│   └── generated/                             # Datagen output directory
└── AGENT.md                                   # This agent reference file
```

---

## ⚙️ Environment & Toolchain Requirements

1. **Java Version:** **Java 21 (LTS)** is mandatory.
   - Mojang ships Java 21 for 1.21.1.
   - Do **NOT** run Gradle using Java 25 or higher due to compatibility issues with older Kotlin tooling (`JavaVersion.parse` failure).
   - `gradle.properties` specifies: `org.gradle.java.home=C:/Program Files/Eclipse Adoptium/jdk-21.0.12.8-hotspot`.
   - Toolchain language version is explicitly `21` in `build.gradle` (`jvmToolchain(21)`).

2. **Kotlin Configuration:**
   - Plugin: `id 'org.jetbrains.kotlin.jvm' version '2.4.0'` in `build.gradle`.
   - Dependency: `implementation 'thedarkcolour:kotlinforforge-neoforge:5.12.0'`.
   - Metadata version: KFF 5.12.0 uses Kotlin `2.4.0` bytecode/metadata. Keeping compiler at `2.4.0` prevents metadata mismatch errors.

3. **Mod Loader in `neoforge.mods.toml`:**
   - Must use `modLoader="kotlinforforge"` and `loaderVersion="[5.12.0,)"`.
   - Main `@Mod` classes must be registered properly with Kotlin syntax.

4. **ModDevGradle Dependency Syntax (No `fg.deobf`):**
   - NeoForge 1.21.1 uses `net.neoforged.moddev` (`2.0.148`) which runs Official Mojang Mappings at runtime.
   - Legacy `fg.deobf(...)` syntax fails with `Could not get unknown property 'fg'`.
   - Use standard Gradle syntax: `implementation 'curse.maven:lodestone-616457:7264731'` or `implementation files('libs/titanium-1.21-4.0.43.jar')`.

---

## 🧪 Runtime Testing Mods (`libs/` & `localRuntime`)

To test interactions in-game with target mods (like JEI, AE2, Sodium, Pipez, Functional Storage, Just Dire Things) without declaring them as hard dependencies of `not-enough-channels`:

1. **Configuration in `build.gradle`:**
   ```groovy
   dependencies {
       implementation 'thedarkcolour:kotlinforforge-neoforge:5.12.0'
       implementation files('libs/titanium-1.21-4.0.43.jar')
       implementation 'curse.maven:lodestone-616457:7264731'

       // Load remaining test mods locally (excluding titanium to prevent duplicate classpath entries)
       localRuntime fileTree(dir: 'libs', include: ['*.jar'], exclude: ['titanium-*.jar'])
   }
   ```
2. **Behavior:**
   - Any `.jar` placed in the `libs/` folder (except Titanium, which is compiled directly) will be active during `./gradlew.bat runClient`.
   - Test mod JARs are **never** packaged into release JARs and are **never** added to your mod's POM or `neoforge.mods.toml`.
3. **Target Test Mods for this Project:**
   - **JEI** (`jei-1.21.1-neoforge-19.57.0.449`)
   - **Sodium** (`sodium-neoforge-0.8.13+mc1.21.1`)
   - **Applied Energistics 2** (`appliedenergistics2-19.2.17`)
   - **Just Dire Things** (`justdirethings-1.5.7`)
   - **Pipez** (`pipez-neoforge-1.21.1-1.2.31`)
   - **Functional Storage** (`functionalstorage-1.21.1-1.5.8`) + **Titanium**

---

## 🧑‍💻 Kotlin & NeoForge Coding Conventions

1. **Array Annotations:**
   - In Kotlin, annotation attributes taking arrays require array brackets `[...]`.
   - Example: `@Mod(value = NotEnoughChannels.MODID, dist = [Dist.CLIENT])` and `@EventBusSubscriber(..., value = [Dist.CLIENT])`. Never omit the brackets.

2. **Deferred Registers & Lambdas:**
   - Avoid deeply nested anonymous lambdas with implicit SAM conversions in `DeferredRegister` registrations, as they may trigger K2 compiler FIR frontend bugs (`source must not be null`).
   - Wrap or extract complex builders into dedicated private helper functions or explicit `java.util.function.Supplier` instances.

3. **Event Listeners:**
   - Mod bus events: use `modEventBus.addListener(...)` in `@Mod` class constructors (inject `modEventBus: IEventBus` into the constructor).
   - `@EventBusSubscriber(bus = Bus.MOD)` is deprecated in NeoForge 1.21.1 and marked for removal. `@EventBusSubscriber` is now reserved strictly for game events on `NeoForge.EVENT_BUS`.
   - Forge / Game events: use `@EventBusSubscriber` or `NeoForge.EVENT_BUS.register(this)` / `NeoForge.EVENT_BUS.register(ListenerObject)`.

4. **Asset Directory Hierarchy:**
   - All mod assets (models, textures, blockstates, lang, sounds) MUST live strictly under `src/main/resources/assets/<modid>/` (e.g. `assets/notenoughchannels/models/item/channel_snowball.json`).
   - Placing directories directly in `resources/` causes Minecraft to fail asset discovery and renders items as missing texture cubes (black/magenta).

5. **Item & Screen Color Tinting & ARGB Byte Order (Minecraft 1.21.1+):**
   - Minecraft and Titanium client renderers use **ARGB** format (`0xAARRGGBB`), **NOT** RGBA.
   - Bits 24–31: **Alpha** (`0xFF` = 100% opaque solid, `0x00` = transparent).
   - For 100% solid color, ALWAYS prefix with `0xFF`: `0xFF0A192F.toInt()` in Kotlin, or use `net.minecraft.util.FastColor.ARGB.opaque(0x0A192F)`.
   - Omitting `0xFF` results in `0x00` alpha, making elements completely invisible.

6. **DeferredItem & Registry Resolution Timing (`Supplier` vs `.get()`):**
   - In NeoForge 1.21.1, `DeferredItem<T>` is a `DeferredHolder` implementing `Supplier<T>`.
   - **Never call `.get()` on a `DeferredItem` inside the `@Mod` class constructor (`init`) or static initializers.** At this phase, the registries are not yet frozen.
   - Calling `.get()` early throws `java.lang.NullPointerException: Trying to access unbound value: ResourceKey[...]`.
   - Custom recipe registries (`LightningCrafting`, `SolarMachineCrafting`) must accept `Supplier<out Item>` so that items are resolved lazily at runtime when in-game events occur.

7. **Custom Lightning Strikes & Visual-Only Behavior:**
   - To prevent lightning from starting fires or destroying terrain, call `lightning.setVisualOnly(true)`.
   - **Gotcha:** Setting `visualOnly = true` causes vanilla `LightningBolt.tick()` to bypass entity damage and **suppress `EntityStruckByLightningEvent`**.
   - **Reliable Crafting/Hit Detection:** Listen to `EntityJoinLevelEvent` on `NeoForge.EVENT_BUS` and check `event.entity is LightningBolt`. This triggers consistently for all lightning strikes.
   - **Manual Damage:** If visual-only lightning should still damage living entities, apply damage explicitly using `level.getEntitiesOfClass(...)` and `target.hurt(level.damageSources().lightningBolt(), damage)`.

8. **⚡ Tiered & Multi-Item Lightning Crafting System:**
   - **Architecture:**
     - `ChannelStickItem(tier: Int)` summons a visual-only lightning bolt tagged with `"channel_tier_$tier"` via vanilla scoreboard tags.
     - Natural thunderstorm strikes have no tag and automatically default to **Tier 1**.
     - `LightningCraftingHandler` listens on `EntityJoinLevelEvent` (`event.entity is LightningBolt`), reads the tier tag, and processes all `ItemEntity` within a 6×6×6 bounding box around the bolt.
   - **Strict Tier Separation:**
     - Each recipe belongs to a specific tier (1, 2, or 3). A stick ONLY crafts recipes of its exact tier.
   - **Multi-Item & Count Support (1:1, 1:N, N:1, N:M):**
     - Supports any combination of inputs and outputs with item stack counts.
     - Helper functions available in `net.modelai.website.crafting`: `ingredient(item, count = 1)` and `output(item, count = 1)`.
   - **Sound Profiles:**
     - Tier 1 / Natural: `SoundEvents.AMETHYST_BLOCK_CHIME` (pitch 4.0f)
     - Tier 2: `SoundEvents.RESPAWN_ANCHOR_CHARGE` (pitch 1.0f)
     - Tier 3: `SoundEvents.CONDUIT_ACTIVATE` (pitch 1.2f)

---

## ☀️ Floating Multiblock Solar Machine Architecture

The Floating Multiblock Solar Machine is an advanced industrial crafting machine powered by daylight, clear skies, and NeoForge Energy (FE), implemented using **Titanium 4.0.43** and **Lodestone**.

```
Layer 1 (Y = -2)    Layer 2 (Y = -1)    Layer 3 (Y = 0)     Layer 4 (Y = +1)    Layer 5 (Y = +2)
x x x x x           x x A x x           A x x x A           x x A x x           x x x x x
A x x x A           x x x x x           x x x x x           x x x x x           x x x x x
x A x A x           x x x x x           x x B x x           A x x x A           x x x x x
A x x x A           x x x x x           x x x x x           x x x x x           x x x x x
x x x x x           x x A x x           A x x x A           x x A x x           x x x x x
(6 blocks)          (2 blocks)          (4 blocks + B)      (4 blocks)          (Empty / Sky)
```

### 1. Exact 5x5x5 Multiblock Pattern & $90^\circ$ Rotational Symmetry
- **Central Controller (`B`)**: Placed at relative coordinate $(0, 0, 0)$ (center of Layer 3).
- **16 Side Structure Blocks (`A`)**: Placed across Layers 1–4 ($dy \in [-2, +1]$).
- **Rotational Symmetry**: Due to $180^\circ$ point reflection symmetry, verifying North-South ($0^\circ$) via `PATTERN_OFFSETS_NS` and East-West ($90^\circ$: `BlockPos(-pos.z, pos.y, pos.x)`) via `PATTERN_OFFSETS_EW` covers all 4 cardinal directions.
- **Unconstrained Non-Structure Blocks (`x`)**: Any non-`A` position can be air, cables, pipes, or structural blocks.
- **Skylight Transparency (`noOcclusion`)**: Floating `SideStructureBlock` instances must use `.noOcclusion()` and `isViewBlocking { _, _, _ -> false }` so structure blocks above the controller do not obstruct daylight access.
- **Event-Driven Validation & Self-Healing**:
  - `SideStructureBlock` triggers `notifyNearbyControllers` on `onPlace`/`onRemove` across its tight relative bounds (`pos.offset(-2, -1, -2)` to `pos.offset(2, 2, 2)`), calling `be.markStructureDirty()`.
  - In `updateStructure(lvl)`: If formed and not dirty, takes an $O(16)$ fast-path. If `structureDirty || !isStructureValid`, it re-scans both orientations, picks the best match count for GUI reporting (`X/16`), and heals automatically within 20 ticks (1s).

### 2. Titanium `PoweredTile` Inventory, Tanks & Automation Rules
- **10 Item Input Slots**: Maximum stack limit of **1** (`.setSlotLimit(1)`). External pipe extraction is prohibited (`.setOutputFilter { _, _ -> false }`).
- **3 Fluid Input Tanks**: Maximum capacity of **1,000 mB** each. Configured with `.setTankAction(FluidTankComponent.Action.FILL)` so external automation can only fill, never extract.
- **2 Item Output Slots**: Stack limit of **64**. External insertion is prohibited (`.setInputFilter { _, _ -> false }`).
- **1 Fluid Output Tank**: Maximum capacity of **1,000 mB** (1 bucket). Configured with `.setTankAction(FluidTankComponent.Action.DRAIN)` so automation can only drain, never insert.
- **10,000,000 FE Buffer**: Provided via `PoweredTile.createEnergyStorage()` (`EnergyStorageComponent(10_000_000, 100_000, 100_000, 12, 22)`).
- **Capability Registration**: Explicitly wired in `NotEnoughChannels.registerCapabilities` for `Capabilities.EnergyStorage.BLOCK`, `Capabilities.FluidHandler.BLOCK`, and `Capabilities.ItemHandler.BLOCK`.

### 3. Shapeless Recipe Engine & Safe 100% Consumption
- **Bipartite Shapeless Matching (`SolarMachineCrafting`)**: Supports up to 10 item inputs, 3 fluid inputs ($\le 1,000\text{ mB}$), 2 item outputs ($\le 64$), and 1 fluid output. Ingredients can be inserted into any slot or tank.
- **Safe 100% Consumption**:
  - Inputs remain in their respective slots and tanks throughout the crafting process.
  - Ingredients are consumed **only when progress reaches 100%**.
  - If daylight is lost (`!level.isDay || level.isRaining || !level.canSeeSky`), if energy drops below `recipe.energyPerTick`, or if an ingredient is removed mid-craft, crafting cancels immediately and progress resets to `0` without consuming any inputs.

### 4. Custom Blue Sci-Fi Titanium GUI
- **Asset Provider (`SolarBlueAssetProvider`)**: Sets GUI dimensions to $264 \times 180\text{ px}$ and centers the player inventory:
  - Main inventory: $(51, 98)$
  - Hotbar: $(51, 156)$
- **Panel Addon (`BluePanelScreenAddon`)**: Renders deep navy (`0xFF0A192F.toInt()`) background, electric cyan (`0xFF00D2FF.toInt()`) border, dark inset paneling, and 36 player slot wells.
- **Status Screen Addon (`SolarStatusScreenAddon`)**:
  - Live header badge rendered at $(208, 5)$.
  - Displays green/cyan pill with hover tooltips for sunlight status (`Active` vs `Blocked / Night - Craft Reset!`) and multiblock status (`5x5 Pattern Formed (16/16)` vs `Incomplete (X/16 Blocks)`).

### 5. Client Lodestone VFX & Sound Profiles (`SolarMachineVFX`)
- **Particle Spawner**: Isolated in `net.modelai.website.client.vfx.SolarMachineVFX` and invoked from `SolarMultiblockTileEntity.clientTick` only when `level.isClientSide && isCrafting`.
  - **Core Wisps** (every 2 ticks): Cyan $\rightarrow$ Deep Blue additive wisps (`LodestoneParticleTypes.WISP_PARTICLE`) hovering and drifting upwards with slight negative gravity (`-0.01f`).
  - **Core Twinkles** (every 4 ticks): White-Cyan sparkles (`LodestoneParticleTypes.TWINKLE_PARTICLE`) in the machine core.
  - **Converging Energy Ribbons** (every 3 ticks): 2–3 random side blocks sampled from synced `cachedSidePositions` emitting constant-velocity (`friction = 1.0f`), non-colliding (`enableNoClip()`) streams toward the core.
  - **Throttling**: Active particle count capped to ~25–35 per active machine, maintaining steady 60+ FPS.
- **Layered Audio Cues (Server-Side)**:
  - **Craft Start**: `SoundEvents.BEACON_ACTIVATE` (0.8f, 1.2f) + `SoundEvents.CONDUIT_ACTIVATE` (0.7f, 1.1f).
  - **Craft Ambient** (every 40 ticks): `SoundEvents.BEACON_AMBIENT` (0.5f, 1.4f).
  - **Craft Complete**: `SoundEvents.CRAFTER_CRAFT` (1.0f, 1.0f) + `SoundEvents.VAULT_ACTIVATE` (0.8f, 1.2f).
  - **Sunlight Lost / Canceled**: `SoundEvents.FIRE_EXTINGUISH` (0.9f, 0.8f) + `SoundEvents.CONDUIT_DEACTIVATE` (0.8f, 0.9f).

---

## 🚀 Key Commands

All commands should be executed from the project root (`A:\IDE\Intellija\not-enough-channels-1.21.1`):

### Compilation & Build
```powershell
# Set Java 21 Home
$env:JAVA_HOME = 'C:/Program Files/Eclipse Adoptium/jdk-21.0.12.8-hotspot'

# Compile Kotlin sources
.\gradlew.bat compileKotlin

# Full build (compiles, processes resources, builds JAR)
.\gradlew.bat build -x test

# Clean build artifacts
.\gradlew.bat clean
```

### Running Minecraft
```powershell
# Launch the client with the mod loaded (and test mods from libs/)
.\gradlew.bat runClient

# Launch dedicated server
.\gradlew.bat runServer
```

### Gradle Daemons & Diagnostics
```powershell
# Stop all running daemons (useful after toolchain or build script edits)
.\gradlew.bat --stop

# Check status of daemons
.\gradlew.bat --status

# Run with stacktrace on failures
.\gradlew.bat compileKotlin --stacktrace
```

---

## ⚠️ Known Gotchas & Troubleshooting

- **`IllegalArgumentException: 25.0.4`:** Occurs if Gradle uses Java 25. Ensure `org.gradle.java.home` in `gradle.properties` points to JDK 21. Run `.\gradlew.bat --stop` to terminate rogue daemons.
- **`Module was compiled with an incompatible version of Kotlin`:** Occurs if the Kotlin Gradle plugin version is below `2.4.0`. KFF 5.12.0 bundles Kotlin 2.4.0 dependencies.
- **`Argument type mismatch: actual type is 'Dist', but 'Array<Dist>' was expected`:** Caused by omitting brackets on `@Mod(dist = [Dist.CLIENT])`.
- **`'enum class Bus : Enum<EventBusSubscriber.Bus!>' is deprecated`:** In NeoForge 1.21.1, the `bus` parameter on `@EventBusSubscriber` is deprecated and marked for removal. Migrate mod bus listeners to `modEventBus.addListener(...)` in constructor instead.
- **`NullPointerException: Trying to access unbound value: ResourceKey[...]`:** Occurs when `.get()` is called on a `DeferredItem` or `DeferredHolder` during mod constructor (`init`) before registry freeze. Accept `Supplier<out Item>` and evaluate lazily.
- **`EntityStruckByLightningEvent` not triggering:** Occurs when lightning is marked `lightning.setVisualOnly(true)`. Vanilla suppresses damage and strike events for visual lightning bolts. Use `EntityJoinLevelEvent` instead.
- **Item/Screen Color Invisible or Translucent:** Minecraft 1.21+ interprets colors as ARGB (`0xAARRGGBB`). Alpha is the first byte: `0x00` makes items 100% invisible. Always prefix solid colors with `0xFF` (e.g. `0xFF0A192F.toInt()` or `FastColor.ARGB.opaque(...)`).
- **Titanium `FluidTankComponent` Constructor:** Capacity is set directly in the constructor: `FluidTankComponent(name, capacity, posX, posY)`. Titanium 4.0.43 does not have a `.setCapacity()` method.
- **Titanium Default 176px Texture Bleeding:** When rendering custom-dimension GUIs (e.g. $264 \times 180$), Titanium's default background sheet may render behind your addon. Override `IAssetProvider.getAsset(AssetTypes.BACKGROUND)` to return a transparent 1x1 PNG (`assets/notenoughchannels/textures/gui/empty.png`).
- **Skylight Blocking by Floating Structure Blocks:** Solid blocks placed above the controller cause `level.canSeeSky(pos.above())` to return `false`. `SideStructureBlock` must configure `.noOcclusion()` and `isViewBlocking { _, _, _ -> false }`.
- **Dedicated Server Crashes from Client VFX:** Never reference Lodestone client rendering classes (`LodestoneWorldParticleRenderType`, particle builders) on dedicated servers. Keep particle dispatch inside `clientTick` and guarded by `level.isClientSide`.
