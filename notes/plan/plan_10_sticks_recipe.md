# Implementation Plan: 10 Sticks -> Apple + Golden Apple Recipe

Add a custom recipe to the Floating Multiblock Solar Machine that consumes 10 sticks (1 in each of the 10 input slots) and produces 1 Apple and 1 Golden Apple.

## Goal Description
Provide an easy, accessible early-game/testing recipe for players to test the Floating Multiblock Solar Machine without needing rare materials (like Netherite or Channel Apples). 
The player places 1 stick into each of the 10 item input slots (`.setSlotLimit(1)`), provides daylight, clear weather, and solar FE, and receives 1 vanilla Apple and 1 vanilla Golden Apple in the 2 output slots.

---

## User Review Required

> [!IMPORTANT]
> **Proposed Recipe Parameters**:
> - **Inputs**: 10 $\times$ `Items.STICK` (1 stick placed in each of the 10 input slots).
> - **Fluid Inputs**: None (purely item-based).
> - **Outputs**: 
>   - Output Slot 1: 1 $\times$ `Items.APPLE`
>   - Output Slot 2: 1 $\times$ `Items.GOLDEN_APPLE`
> - **Fluid Outputs**: None.
> - **Duration**: 100 ticks (5.0 seconds).
> - **Energy**: 100 FE/tick (10,000 FE total).

---

## Open Questions

> [!NOTE]
> 1. **Stick Type**: Should this recipe use standard vanilla `Items.STICK` or the mod's custom `ModItems.CHANNEL_STICK`? (The plan defaults to vanilla `Items.STICK` for easy testing).
> 2. **Energy Cost**: Does 100 FE/t (10,000 FE total) sound good, or would you prefer a free craft (0 FE/t, running entirely off raw daylight)?
> 3. **Output Quantities**: Is 1 Apple and 1 Golden Apple the desired quantity, or should either output produce more?

---

## Proposed Changes

### Recipe Registration

#### [MODIFY] `src/main/kotlin/net/modelai/website/NotEnoughChannels.kt`
- Add recipe registration inside the `init` block of `NotEnoughChannels`:
```kotlin
        // 10 Sticks -> Apple + Golden Apple
        SolarMachineCrafting.register(
            itemInputs = List(10) { solarItem(Items.STICK) },
            itemOutputs = listOf(
                solarOutput(Items.APPLE, 1),
                solarOutput(Items.GOLDEN_APPLE, 1)
            ),
            durationTicks = 100,
            energyPerTick = 100
        )
```

---

## How It Functions In-Game

1. The player opens the $264 \times 180\text{ px}$ Blue GUI of the formed 5x5x5 Solar Machine.
2. The player puts 1 stick in each of the 10 input slots (due to `.setSlotLimit(1)`, all 10 slots will hold 1 stick).
3. Under daytime and clear skies (`canSeeSky`), the machine detects all 10 slots matched:
   - Cyan crafting arrow begins filling.
   - Lodestone plasma wisps hover in the core and ribbons stream inward from the 16 side structure blocks.
   - Start audio (`BEACON_ACTIVATE` + `CONDUIT_ACTIVATE`) plays.
4. Upon reaching 100% (5 seconds):
   - 1 stick from each of the 10 slots is consumed.
   - 1 Apple appears in Output Slot 1.
   - 1 Golden Apple appears in Output Slot 2.
   - Craft completion chime (`CRAFTER_CRAFT` + `VAULT_ACTIVATE`) plays.

---

## Verification Plan

### Automated Tests
1. **Kotlin Compilation**:
   ```powershell
   $env:JAVA_HOME = 'C:/Program Files/Eclipse Adoptium/jdk-21.0.12.8-hotspot'
   .\gradlew.bat compileKotlin
   ```
2. **Project Build**:
   ```powershell
   $env:JAVA_HOME = 'C:/Program Files/Eclipse Adoptium/jdk-21.0.12.8-hotspot'
   .\gradlew.bat build -x test
   ```

### Manual Verification
1. Run `.\gradlew.bat runClient`.
2. Place 1 stick in each of the 10 input slots.
3. Verify that crafting starts under daylight and produces 1 Apple and 1 Golden Apple in the output slots.
