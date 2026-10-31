# Step-by-Step Refactoring Plan: ModItems & ModCreativeTabs

This document explains exactly which lines to remove and which lines to add across each file.

---

## 1. `src/main/kotlin/net/modelai/website/item/ModItems.kt`

### Status
Currently incomplete:
```kotlin
package net.modelai.website.item

import java.util.function.Supplier
import net.minecraft.world.item.Item
import net.minecraft.world.item.SnowballItem
import net.modelai.website.NotEnoughChannels
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {

    val ITEMS:

}
```

### Lines to Add
Replace the incomplete `object ModItems` body with:
```kotlin
package net.modelai.website.item

import java.util.function.Supplier
import net.minecraft.world.item.Item
import net.minecraft.world.item.SnowballItem
import net.modelai.website.NotEnoughChannels
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {
    // 1. DeferredRegister for Items
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(NotEnoughChannels.MODID)

    // 2. Item definitions moved from NotEnoughChannels.kt
    val CHANNEL_SNOWBALL: DeferredItem<SnowballItem> =
        ITEMS.register("channel_snowball", Supplier { SnowballItem(Item.Properties()) })

    val CHANNEL_ROCK: DeferredItem<SnowballItem> =
        ITEMS.register("channel_rock", Supplier { SnowballItem(Item.Properties()) })

    // 3. Helper to register on the mod event bus
    fun register(bus: IEventBus) {
        ITEMS.register(bus)
    }
}
```

---

## 2. `src/main/kotlin/net/modelai/website/tab/ModCreativeTabs.kt`

### Status
Currently an empty class:
```kotlin
package net.modelai.website.tab

class ModCreativeTabs {
}
```

### Lines to Remove
- Remove `class ModCreativeTabs { }`.

### Lines to Add
Change `class` to `object`, add the imports, the `CREATIVE_MODE_TABS` register, the tab definition with `output.accept(...)`, and the `register(bus)` function:
```kotlin
package net.modelai.website.tab

import java.util.function.Supplier
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.ItemStack
import net.modelai.website.NotEnoughChannels
import net.modelai.website.item.ModItems
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object ModCreativeTabs {
    // 1. DeferredRegister for CreativeModeTab
    val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NotEnoughChannels.MODID)

    // 2. Mod tab definition moved from NotEnoughChannels.kt
    val MOD_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> =
        CREATIVE_MODE_TABS.register("main", Supplier {
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.notenoughchannels"))
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon { ItemStack(ModItems.CHANNEL_SNOWBALL.get()) }
                .displayItems { _, output ->
                    output.accept(ModItems.CHANNEL_SNOWBALL.get())
                    output.accept(ModItems.CHANNEL_ROCK.get())
                }
                .build()
        })

    // 3. Helper to register on the mod event bus
    fun register(bus: IEventBus) {
        CREATIVE_MODE_TABS.register(bus)
    }
}
```

---

## 3. `src/main/kotlin/net/modelai/website/NotEnoughChannels.kt`

### Lines to Remove

#### A. In `companion object` (Lines 34 to 57):
Remove:
```kotlin
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)

        val CHANNEL_SNOWBALL: DeferredItem<SnowballItem> =
            ITEMS.register("channel_snowball", Supplier { SnowballItem(Item.Properties()) })
        val CHANNEL_ROCK: DeferredItem<SnowballItem> =
            ITEMS.register("channel_rock", Supplier { SnowballItem(Item.Properties()) })

        private fun createModTab(): CreativeModeTab {
            return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.notenoughchannels"))
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(Supplier { ItemStack(CHANNEL_SNOWBALL.get()) })
                .displayItems { _, output ->
                    output.accept(CHANNEL_SNOWBALL.get())
                    output.accept(CHANNEL_ROCK.get())
                }
                .build()
        }

        val MOD_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> =
            CREATIVE_MODE_TABS.register("main", Supplier(::createModTab))
```

#### B. In `init` block (Lines 64 to 65):
Remove:
```kotlin
        // Register Deferred Registers to the mod event bus
        ITEMS.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus)
```

#### C. Unused Imports at the top:
Remove:
```kotlin
import java.util.function.Supplier
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.item.SnowballItem
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
```

### Lines to Add

#### A. New Imports at the top:
Add:
```kotlin
import net.modelai.website.item.ModItems
import net.modelai.website.tab.ModCreativeTabs
```

#### B. In `init` block:
Add:
```kotlin
        // Register items and creative tabs to the mod event bus
        ModItems.register(modEventBus)
        ModCreativeTabs.register(modEventBus)
```

---

## 4. `src/main/kotlin/net/modelai/website/NotEnoughChannelsClient.kt`

Because `CHANNEL_SNOWBALL` and `CHANNEL_ROCK` moved from `NotEnoughChannels` to `ModItems`:

### Lines to Remove (Lines 40-41):
```kotlin
            e.registerColor(NotEnoughChannels.CHANNEL_SNOWBALL, 0xFF2121BB)
            e.registerColor(NotEnoughChannels.CHANNEL_ROCK, 0xFF949494)
```

### Lines to Add
1. Import at top:
```kotlin
import net.modelai.website.item.ModItems
```
2. In `registerItemColors`:
```kotlin
            e.registerColor(ModItems.CHANNEL_SNOWBALL, 0xFF2121BB)
            e.registerColor(ModItems.CHANNEL_ROCK, 0xFF949494)
```

---

## 5. Verification Checklist (Once Implemented)
- [ ] No compilation errors when running:
  ```bash
  .\gradlew compileKotlin
  ```
- [ ] In-game check: Items show up in the custom creative tab and display their tinted colors correctly.
