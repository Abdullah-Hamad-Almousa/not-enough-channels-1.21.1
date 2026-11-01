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

---

## 🏗️ Architecture & Project Structure

```
not-enough-channels-1.21.1/
├── build.gradle                               # ModDevGradle, Kotlin & localRuntime configuration
├── gradle.properties                          # Project properties & JVM target settings
├── libs/                                      # Local runtime test mod JARs (loaded in runClient)
├── notes/                                     # Planning, design docs, and architecture notes
│   └── plan/
├── src/
│   ├── main/
│   │   ├── kotlin/net/modelai/website/        # Mod Kotlin source code
│   │   │   ├── NotEnoughChannels.kt           # Main mod class (@Mod) & common setup
│   │   │   ├── NotEnoughChannelsClient.kt     # Client-only setup & screens (@EventBusSubscriber)
│   │   │   ├── Config.kt                      # ModConfigSpec definition
│   │   │   ├── crafting/                      # In-world crafting mechanics
│   │   │   │   ├── LightningCrafting.kt       # Tiered & Multi-Item recipe registry & DSL helpers
│   │   │   │   └── LightningCraftingHandler.kt# Event bus listener for lightning transformations
│   │   │   ├── item/                          # Deferred items & custom item classes
│   │   │   │   ├── ModItems.kt                # Item registry (sticks, snowballs, apple, rock)
│   │   │   │   └── custom/
│   │   │   │       ├── ChannelStickItem.kt    # Tiered stick item (lightning summoner with tags)
│   │   │   │       └── ChannelAppleItem.kt    # Custom food item
│   │   │   └── tab/                           # Creative mode tabs
│   │   │       └── ModCreativeTabs.kt
│   │   ├── resources/                         # Assets, data, textures, lang files
│   │   │   └── assets/notenoughchannels/
│   │   │       ├── lang/en_us.json
│   │   │       └── models/item/               # Item JSON models (sticks, apple, snowballs)
│   │   └── templates/META-INF/
│   │       └── neoforge.mods.toml             # Mod metadata template processed at build
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
   - Dependency: `implementation 'thedarkcolour:kotlinforforge-neoforge:5.12.0'`
   - Metadata version: KFF 5.12.0 uses Kotlin `2.4.0` bytecode/metadata. Keeping compiler at `2.4.0` prevents metadata mismatch errors.

3. **Mod Loader in `neoforge.mods.toml`:**
   - Must use `modLoader="kotlinforforge"` and `loaderVersion="[5.12.0,)"`.
   - Main `@Mod` classes must be registered properly with Kotlin syntax.

---

## 🧪 Runtime Testing Mods (`libs/` & `localRuntime`)

To test interactions in-game with target mods (like JEI, AE2, Sodium, Pipez, Functional Storage, Just Dire Things) without declaring them as hard dependencies of `not-enough-channels`:

1. **Configuration in `build.gradle`:**
   ```groovy
   dependencies {
       implementation 'thedarkcolour:kotlinforforge-neoforge:5.12.0'
       localRuntime fileTree(dir: 'libs', include: ['*.jar'])
   }
   ```
2. **Behavior:**
   - Any `.jar` placed in the `libs/` folder will be active during `./gradlew.bat runClient`.
   - Test mod JARs are **never** packaged into your release JAR and are **never** added to your mod's POM or `neoforge.mods.toml`.
3. **Target Test Mods for this Project:**
   - **JEI** (`jei-1.21.1-neoforge-19.57.0.449`)
   - **Sodium** (`sodium-neoforge-0.8.13+mc1.21.1`)
   - **Applied Energistics 2** (`appliedenergistics2-19.2.17`)
   - **Just Dire Things** (`justdirethings-1.5.7`)
   - **Pipez** (`pipez-neoforge-1.21.1-1.2.31`)
   - **Functional Storage** (`functionalstorage-1.21.1-1.5.8`) + **Titanium** (dependency)

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

5. **Item Color Tinting & ARGB Byte Order (Minecraft 1.21.1+):**
   - Client item color handlers (`RegisterColorHandlersEvent.Item`) use **ARGB** format (`0xAARRGGBB`), **NOT** RGBA.
   - Bits 24–31: **Alpha** (`0xFF` = 100% opaque solid, `0x00` = transparent).
   - For 100% solid color, put `0xFF` at the start: `0xFF2121BB.toInt()` in Kotlin, or use `net.minecraft.util.FastColor.ARGB.opaque(0x2121BB)`.

6. **DeferredItem & Registry Resolution Timing (`Supplier` vs `.get()`):**
   - In NeoForge 1.21.1, `DeferredItem<T>` is a `DeferredHolder` implementing `Supplier<T>`.
   - **Never call `.get()` on a `DeferredItem` inside the `@Mod` class constructor (`init`) or static initializers.** At this phase, the registries are not yet frozen.
   - Calling `.get()` early throws `java.lang.NullPointerException: Trying to access unbound value: ResourceKey[...]`.
   - Systems like custom recipe registries (`LightningCrafting.register`) must accept `Supplier<out Item>` so that items are resolved lazily at runtime when in-game events occur.

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
     - Examples:
       ```kotlin
       // 1:1 recipe (defaults to tier 1)
       LightningCrafting.register(1, ModItems.CHANNEL_SNOWBALL, ModItems.CHANNEL_ROCK)

       // Multi-input to multi-output with item counts:
       LightningCrafting.register(
           tier = 3,
           inputs = listOf(
               ingredient(Items.APPLE, count = 1),
               ingredient(Items.HONEYCOMB, count = 2)
           ),
           outputs = listOf(
               output(Items.GOAT_HORN, count = 1),
               output(Items.HONEYCOMB, count = 1)
           )
       )
       ```
   - **Batch Crafting:**
     - The crafting engine calculates `maxCrafts = min(availableCount / requiredCount)`. Dropping multiple stacks executes multiple crafts in a single strike.
     - Output items are spawned at the strike coordinates, automatically split into stacks of up to `maxStackSize` (64).
   - **Built-in Minecraft Sound Profiles:**
     - **Tier 1 / Natural:** `SoundEvents.AMETHYST_BLOCK_CHIME` (pitch 4.0f)
     - **Tier 2:** `SoundEvents.RESPAWN_ANCHOR_CHARGE` (pitch 1.0f)
     - **Tier 3:** `SoundEvents.CONDUIT_ACTIVATE` (pitch 1.2f)
   - **Ingredient Burn Protection:**
     - `LightningCraftingHandler.onEntityStruckByLightning` checks `LightningCrafting.isIngredient(item)`. If true, the event is canceled to prevent fire or lightning damage from destroying ingredients.

9. **Preserving Documentation & User Directives:**
   - Maintain comments and existing code structure.
   - The user prefers to make code changes directly with clear instructions or diffs when guided.

---

## 🚀 Key Commands

All commands should be executed from the project root (`A:\IDE\Intellija\not-enough-channels-1.21.1`):

### Compilation & Build
```powershell
# Compile Kotlin sources
.\gradlew.bat compileKotlin

# Full build (compiles, processes resources, builds JAR)
.\gradlew.bat build

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

- **`IllegalArgumentException: 25.0.4`:** Occurs if Gradle uses Java 25. Ensure `org.gradle.java.home` in `gradle.properties` points to JDK 21. Run `.\gradlew.bat --stop` to terminate any rogue daemons running on higher JDKs.
- **`Module was compiled with an incompatible version of Kotlin`:** Occurs if the Kotlin Gradle plugin version is below `2.4.0`. KFF 5.12.0 bundles Kotlin 2.4.0 dependencies.
- **`Argument type mismatch: actual type is 'Dist', but 'Array<Dist>' was expected`:** Caused by omitting brackets on `@Mod(dist = [Dist.CLIENT])`.
- **`'enum class Bus : Enum<EventBusSubscriber.Bus!>' is deprecated`:** In NeoForge 1.21.1, the `bus` parameter on `@EventBusSubscriber` is deprecated and marked for removal. Migrate mod bus listeners to `modEventBus.addListener(...)` in constructor instead.
- **`NullPointerException: Trying to access unbound value: ResourceKey[...]`:** Occurs when `.get()` is called on a `DeferredItem` or `DeferredHolder` during mod constructor (`init`) before registry freeze. Accept `Supplier<out Item>` and evaluate lazily.
- **`EntityStruckByLightningEvent` not triggering:** Occurs when lightning is marked `lightning.setVisualOnly(true)`. Vanilla suppresses damage and strike events for visual lightning bolts. Use `EntityJoinLevelEvent` instead.
- **Duplicate Entity Spawning:** Ensure `level.addFreshEntity(entity)` is invoked only once per entity. Invoking it multiple times causes ghost entities and desync warnings.
- **Item Invisible or Glassy/Mirror-like when using `RegisterColorHandlersEvent`:** Minecraft 1.21+ interprets colors as ARGB (`0xAARRGGBB`). Alpha is the first byte: `0x00` makes items 100% invisible, and values like `0x77` make them translucent. Always prefix solid colors with `0xFF` (e.g. `0xFF2121BB.toInt()` or `FastColor.ARGB.opaque(...)`).
- **`Caused by: java.net.SocketTimeoutException: Read timed out` during `runClient`:** Originates from `YggdrasilMinecraftSessionService` attempting to fetch skins for the offline dev profile `Dev` from Mojang servers. Completely harmless and can be safely ignored.
- **Transitive Dependencies for Test Mods:** When dropping test mods like Functional Storage into `libs/`, ensure any required library mods (e.g. Titanium) are also in `libs/` to avoid runtime startup crashes.
