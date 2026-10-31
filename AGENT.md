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
├── src/
│   ├── main/
│   │   ├── kotlin/net/modelai/website/        # Mod Kotlin source code
│   │   │   ├── NotEnoughChannels.kt           # Main mod class (@Mod) & common setup
│   │   │   ├── NotEnoughChannelsClient.kt     # Client-only setup & screens (@EventBusSubscriber)
│   │   │   └── Config.kt                      # ModConfigSpec definition
│   │   ├── resources/                         # Assets, data, textures, lang files
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
   - Forge / Game events: use `@EventBusSubscriber` or `NeoForge.EVENT_BUS.register(this)`.

4. **Asset Directory Hierarchy:**
   - All mod assets (models, textures, blockstates, lang, sounds) MUST live strictly under `src/main/resources/assets/<modid>/` (e.g. `assets/notenoughchannels/models/item/channel_snowball.json`).
   - Placing directories directly in `resources/` (e.g. `src/main/resources/models/`) causes Minecraft to fail asset discovery and renders items as missing texture cubes (black/magenta).

5. **Item Color Tinting & ARGB Byte Order (Minecraft 1.21.1+):**
   - Client item color handlers (`RegisterColorHandlersEvent.Item`) use **ARGB** format (`0xAARRGGBB`), **NOT** RGBA.
   - Bits 24–31: **Alpha** (`0xFF` = 100% opaque solid, `0x00` = transparent / completely invisible).
   - Bits 16–23: **Red**, Bits 8–15: **Green**, Bits 0–7: **Blue**.
   - If Alpha is 0 (e.g., standard 6-digit RGB `0x38B6FF`), the item renders **completely invisible**.
   - If Alpha is partial (e.g., `0x772121BB` where Alpha = `0x77`), the item renders **semi-transparent / glassy ("like a mirror")**.
   - For 100% solid color, put `0xFF` at the start: `0xFF2121BB.toInt()` in Kotlin, or use `net.minecraft.util.FastColor.ARGB.opaque(0x2121BB)` or `FastColor.ARGB.color(255, r, g, b)`.
   - When an item inherits from a vanilla model like `minecraft:item/snowball` (`minecraft:item/generated`), `layer0` quads have `tintindex = 0`. Thrown projectile entities (`SnowballItem` -> `Snowball`) render the item stack, so tint is automatically applied to both inventory items and thrown projectiles in flight.

6. **Preserving Documentation & User Directives:**
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
- **Item Invisible or Glassy/Mirror-like when using `RegisterColorHandlersEvent`:** Minecraft 1.21+ interprets colors as ARGB (`0xAARRGGBB`). Alpha is the first byte: `0x00` makes items 100% invisible, and values like `0x77` make them translucent. Always prefix solid colors with `0xFF` (e.g. `0xFF2121BB.toInt()` or `FastColor.ARGB.opaque(...)`).
- **`Caused by: java.net.SocketTimeoutException: Read timed out` during `runClient`:** Originates from `YggdrasilMinecraftSessionService` attempting to fetch skins for the offline dev profile `Dev` from Mojang servers. Completely harmless and can be safely ignored.
- **Transitive Dependencies for Test Mods:** When dropping test mods like Functional Storage into `libs/`, ensure any required library mods (e.g. Titanium) are also in `libs/` to avoid runtime startup crashes.
