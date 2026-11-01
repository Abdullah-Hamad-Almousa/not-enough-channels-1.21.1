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

        // 3-block horizontal and vertical radius around strike
        val area = AABB.ofSize(lightning.position(), 6.0, 6.0, 6.0)
        val items = level.getEntitiesOfClass(ItemEntity::class.java, area)

        for (itemEntity in items) {
            val currentStack = itemEntity.item
            val resultItem = LightningCrafting.getResult(currentStack.item) ?: continue

            // Transform item in-place preserving stack count
            itemEntity.item = ItemStack(resultItem, currentStack.count)

            // Visual and audio effects
            level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                itemEntity.x, itemEntity.y + 0.2, itemEntity.z,
                25, 0.25, 0.25, 0.25, 0.1
            )
            level.playSound(
                null,
                itemEntity.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                1.5f,
                1.0f
            )
        }
    }

    @SubscribeEvent
    fun onEntityStruckByLightning(event: EntityStruckByLightningEvent) {
        val itemEntity = event.entity as? ItemEntity ?: return
        if (LightningCrafting.hasRecipe(itemEntity.item.item)) {
            event.isCanceled = true // Prevent burning/destruction from natural lightning
        }
    }

}