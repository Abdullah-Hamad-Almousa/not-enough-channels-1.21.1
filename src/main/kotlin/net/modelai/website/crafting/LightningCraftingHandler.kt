package net.modelai.website.crafting

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent

object LightningCraftingHandler {

    @SubscribeEvent
    fun onEntityJoinLevel(event: EntityJoinLevelEvent) {
        val level = event.level as? ServerLevel ?: return
        val lightning = event.entity as? LightningBolt ?: return

        // Tier detection from lightning tags
        val tier = when {
            lightning.tags.contains("channel_tier_3") -> 3
            lightning.tags.contains("channel_tier_2") -> 2
            else -> 1 // Tier 1 for "channel_tier_1" or natural lightning
        }

        val area = AABB.ofSize(lightning.position(), 6.0, 6.0, 6.0)
        val itemEntities = level.getEntitiesOfClass(ItemEntity::class.java, area) { !it.isRemoved && !it.item.isEmpty }
        if (itemEntities.isEmpty()) return

        var craftedAny = false

        for (recipe in LightningCrafting.getRecipesForTier(tier)) {
            // Count available items in the area for this recipe
            val available = mutableMapOf<net.minecraft.world.item.Item, Int>()
            for (entity in itemEntities) {
                if (entity.isRemoved || entity.item.isEmpty) continue
                val item = entity.item.item
                available[item] = (available[item] ?: 0) + entity.item.count
            }

            // Calculate how many times this recipe can craft
            var maxCrafts = Int.MAX_VALUE
            for (input in recipe.inputs) {
                val reqItem = input.item.get()
                val count = available[reqItem] ?: 0
                val possible = count / input.count
                if (possible < maxCrafts) {
                    maxCrafts = possible
                }
            }

            if (maxCrafts <= 0) continue

            // Consume required ingredients from item entities
            for (input in recipe.inputs) {
                val reqItem = input.item.get()
                var remainingToDeduct = input.count * maxCrafts

                for (entity in itemEntities) {
                    if (remainingToDeduct <= 0) break
                    if (entity.isRemoved || entity.item.isEmpty) continue
                    if (entity.item.item != reqItem) continue

                    val stack = entity.item
                    if (stack.count <= remainingToDeduct) {
                        remainingToDeduct -= stack.count
                        entity.discard()
                    } else {
                        stack.shrink(remainingToDeduct)
                        entity.item = stack
                        remainingToDeduct = 0
                    }
                }
            }

            // Spawn output items
            val strikePos = lightning.position()
            for (output in recipe.outputs) {
                val outItem = output.item.get()
                var totalToSpawn = output.count * maxCrafts
                val maxStack = outItem.defaultInstance.maxStackSize

                while (totalToSpawn > 0) {
                    val count = minOf(totalToSpawn, maxStack)
                    totalToSpawn -= count
                    val spawned = ItemEntity(level, strikePos.x, strikePos.y, strikePos.z, ItemStack(outItem, count))
                    spawned.setDefaultPickUpDelay()
                    level.addFreshEntity(spawned)
                }
            }

            craftedAny = true
        }

        if (craftedAny) {
            // Visual sparks
            level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                lightning.x, lightning.y + 0.2, lightning.z,
                50, 0.75, 0.75, 0.75, 0.3
            )

            // Tier-specific sound
            val (sound, pitch) = when (tier) {
                2 -> SoundEvents.RESPAWN_ANCHOR_CHARGE to 1.0f
                3 -> SoundEvents.CONDUIT_ACTIVATE to 1.2f
                else -> SoundEvents.AMETHYST_BLOCK_CHIME to 4.0f
            }
            level.playSound(null, lightning.blockPosition(), sound, SoundSource.BLOCKS, 50f, pitch)
        }
    }

    @SubscribeEvent
    fun onEntityStruckByLightning(event: EntityStruckByLightningEvent) {
        val itemEntity = event.entity as? ItemEntity ?: return
        if (LightningCrafting.isIngredient(itemEntity.item.item)) {
            event.isCanceled = true // Protect recipe ingredients from burning
        }
    }
}
