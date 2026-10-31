# Implementation Plan - Right-Click Thunder Stick (No Fire)

## Goal Description
Implement a right-click function for the `CHANNEL_STICK` item that summons a lightning strike (thunderbolt) while preventing it from catching blocks on fire or burning down the environment.

---

## How to Prevent Lightning From Making Fire

In Minecraft, `LightningBolt` entities create fire around the impact point by default. There are **3 clean ways** to prevent fire depending on what you want the strike to do:

### Option 1: Built-in `setVisualOnly(true)` (Simplest & Safest)
Minecraft has a built-in method on `LightningBolt`:
```kotlin
val lightning = EntityType.LIGHTNING_BOLT.create(level) ?: return
lightning.moveTo(pos)
lightning.setVisualOnly(true) // <- Prevents all fire and block destruction!
level.addFreshEntity(lightning)
```
- **Result**: Full lightning flash, visual bolt, and loud thunder sound effect. Zero fire is created, and no blocks are destroyed.
- **Note**: Does not deal damage to mobs. Ideal if this is a cosmetic / effect tool.

---

### Option 2: `setVisualOnly(true)` + Custom Mob Damage (Recommended for Weapons)
If you want the thunderbolt to **hurt mobs** but **NEVER create fire** on the ground:
1. Summon the lightning with `lightning.setVisualOnly(true)` (creates visual & sound, 0 fire).
2. Damage nearby mobs manually using Minecraft's lightning damage source:
```kotlin
// 1. Summon visual & thunder without fire
val lightning = EntityType.LIGHTNING_BOLT.create(level) ?: return
lightning.moveTo(pos)
lightning.setVisualOnly(true)
level.addFreshEntity(lightning)

// 2. Damage mobs in a radius (e.g. 5 blocks) without catching terrain on fire
val area = AABB.ofSize(pos, 5.0, 5.0, 5.0)
val nearbyEntities = level.getEntitiesOfClass(LivingEntity::class.java, area) { it != player }
for (target in nearbyEntities) {
    target.hurt(level.damageSources().lightningBolt(), 5.0f) // 5.0f = 2.5 hearts
}
```
- **Result**: Deals damage to enemies, plays thunder & lightning, zero fire on terrain.

---

### Option 3: Event Interception (`EntityPlaceEvent`)
If you want full vanilla lightning mechanics (including igniting creepers), but want to block fire block creation:
```kotlin
@SubscribeEvent
fun onBlockPlace(event: BlockEvent.EntityPlaceEvent) {
    if (event.entity is LightningBolt && event.placedBlock.block is FireBlock) {
        event.isCanceled = true
    }
}
```
- **Result**: Cancels any fire block placed by lightning bolts in the world.

---

## Proposed Changes (Design Only - No Code Changes Yet)

### 1. Custom Item Class: `src/main/kotlin/net/modelai/website/item/custom/ChannelStickItem.kt`
```kotlin
package net.modelai.website.item.custom

import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

class ChannelStickItem(properties: Properties) : Item(properties) {

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(usedHand)

        if (!level.isClientSide) {
            val hitResult = player.pick(64.0, 1.0f, false)
            if (hitResult.type == HitResult.Type.BLOCK) {
                spawnFirelessLightning(level, hitResult.location, player)
                player.cooldowns.addCooldown(this, 20)
                return InteractionResultHolder.sidedSuccess(itemStack, false)
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide)
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val player = context.player

        if (!level.isClientSide) {
            val strikePos = Vec3.atBottomCenterOf(context.clickedPos.above())
            spawnFirelessLightning(level, strikePos, player)
            player?.cooldowns?.addCooldown(this, 20)
        }

        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    private fun spawnFirelessLightning(level: Level, pos: Vec3, player: Player?) {
        val lightning = EntityType.LIGHTNING_BOLT.create(level) ?: return
        lightning.moveTo(pos)
        // Disables fire creation:
        lightning.setVisualOnly(true)
        level.addFreshEntity(lightning)

        // Optional: damage nearby living entities without creating fire
        val area = AABB.ofSize(pos, 6.0, 6.0, 6.0)
        val targets = level.getEntitiesOfClass(LivingEntity::class.java, area) { it != player }
        for (target in targets) {
            target.hurt(level.damageSources().lightningBolt(), 5.0f)
        }
    }
}
```

### 2. Item Registration in `src/main/kotlin/net/modelai/website/item/ModItems.kt`
Switch `CHANNEL_STICK` registration to use `ChannelStickItem`:
```kotlin
val CHANNEL_STICK: DeferredItem<ChannelStickItem> =
    ITEMS.register("channel_stick", Supplier { ChannelStickItem(Item.Properties().stacksTo(1)) })
```

---

## Verification Plan (When Approved)
1. Run `.\gradlew.bat compileKotlin` to ensure clean build.
2. Launch client via `.\gradlew.bat runClient`.
3. Right-click grass, wood, or leaves with `channel_stick`.
4. Verify thunder sound and bolt appear, but **NO fire blocks** are ignited.
