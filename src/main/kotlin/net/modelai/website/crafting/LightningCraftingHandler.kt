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
    fun onEntityJoinLevel( event: EntityJoinLevelEvent ) {
        val level = event.level as? ServerLevel ?: return
        val lightning = event.entity as? LightningBolt ?: return

        val tier = when {
            lightning.tags.contains("channel_tier_3") -> 3
            lightning.tags.contains("channel_tier_2") -> 2
            else -> 1
        }

        val area = AABB.ofSize(lightning.position(), 6.0, 6.0, 6.0)
        val items = level.getEntitiesOfClass(ItemEntity::class.java, area)

        for (itemEntity in items) {

            val currentStack = itemEntity.item
            val resultItem = LightningCrafting.getResult(currentStack.item, tier) ?: continue

            itemEntity.item = ItemStack(resultItem, currentStack.count)

            level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                itemEntity.x, itemEntity.y + .2, itemEntity.z,
                50, .75, .75, .75, .3
            )

            val (sound, pitch) = when (tier) {
                2 -> SoundEvents.RESPAWN_ANCHOR_CHARGE to 1.0f
                3 -> SoundEvents.CONDUIT_ACTIVATE to 1.2f
                else -> SoundEvents.AMETHYST_BLOCK_CHIME to 4.0f
            }

            level.playSound(
                null,
                itemEntity.blockPosition(),
                sound,
                SoundSource.BLOCKS,
                50f,
                pitch
            )

        }

    }

    @SubscribeEvent
    fun onEntityStruckByLightning(event: EntityStruckByLightningEvent) {
        val itemEntity = event.entity as? ItemEntity ?: return
        if (LightningCrafting.hasRecipe(itemEntity.item.item)) {
            event.isCanceled = true
        }
    }

}