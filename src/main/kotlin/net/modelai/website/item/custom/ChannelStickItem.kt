package net.modelai.website.item.custom

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

class ChannelStickItem(properties: Properties) : Item(properties) {

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(usedHand)

        if (!level.isClientSide) {
            val hitResult = player.pick(12.0, 1.0f, false)
            if (hitResult.type == HitResult.Type.BLOCK) {
                spawnLightning(level, hitResult.location, player)
                player.cooldowns.addCooldown(this, 10)
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
            spawnLightning(level, strikePos, player)
            player?.cooldowns?.addCooldown(this, 10)
        }

        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    private fun spawnLightning(level: Level, pos: Vec3, player: Player?) {
        val lightning = EntityType.LIGHTNING_BOLT.create(level) ?: return
        lightning.moveTo(pos)
        lightning.setVisualOnly(true)
        level.addFreshEntity(lightning)
        if (player is ServerPlayer) {
            lightning.cause = player
        }
        val area = net.minecraft.world.phys.AABB.ofSize(pos, 5.0, 5.0, 5.0)
        val targets = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity::class.java, area) { it != player }
        for (target in targets) {
            target.hurt(level.damageSources().lightningBolt(), 5.0f) // 5.0 = 2.5 hearts
        }
        level.addFreshEntity(lightning)
    }

}