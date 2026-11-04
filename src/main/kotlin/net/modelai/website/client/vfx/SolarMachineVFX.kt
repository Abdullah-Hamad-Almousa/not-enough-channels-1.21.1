package net.modelai.website.client.vfx

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.modelai.website.block.entity.SolarMultiblockTileEntity
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import team.lodestar.lodestone.systems.particle.render_types.LodestoneWorldParticleRenderType
import java.awt.Color

/**
 * High-performance client-side Lodestone particle spawner for the Floating Multiblock Solar Machine.
 *
 * Emits glowing plasma wisps, solar sparkles, and converging energy ribbons from the
 * 16 side structure blocks during active crafting operations.
 */
object SolarMachineVFX {

    private val CYAN_COLOR = Color(0, 240, 255)
    private val DEEP_BLUE_COLOR = Color(0, 85, 255)
    private val WHITE_CYAN_COLOR = Color(180, 255, 255)
    private val AZURE_COLOR = Color(30, 144, 255)

    /**
     * Called on client tick when [SolarMultiblockTileEntity.isCrafting] is true.
     */
    fun spawnCraftingParticles(level: Level, pos: BlockPos, tile: SolarMultiblockTileEntity) {
        val gameTime = level.gameTime
        val center = Vec3.atCenterOf(pos)

        // 1. Core Plasma Wisps (every 2 ticks)
        if (gameTime % 2L == 0L) {
            spawnCoreWisp(level, center)
        }

        // 2. Core Sparkle / Twinkle (every 4 ticks)
        if (gameTime % 4L == 0L) {
            spawnCoreTwinkle(level, center)
        }

        // 3. Converging Energy Ribbons from Side Structure Blocks (every 3 ticks)
        if (gameTime % 3L == 0L) {
            spawnConvergingRibbons(level, center, tile.cachedSidePositions)
        }
    }

    private fun spawnCoreWisp(level: Level, center: Vec3) {
        WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
            .setScaleData(GenericParticleData.create(0.25f, 0.05f).build())
            .setTransparencyData(GenericParticleData.create(0.75f, 0.0f).build())
            .setColorData(ColorParticleData.create(CYAN_COLOR, DEEP_BLUE_COLOR).build())
            .setLifetime(20)
            .setGravity(-0.01f) // Gently rises upward toward the sun
            .setRandomMotion(0.02, 0.03, 0.02)
            .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
            .enableNoClip()
            .spawn(level, center.x, center.y + 0.1, center.z)
    }

    private fun spawnCoreTwinkle(level: Level, center: Vec3) {
        WorldParticleBuilder.create(LodestoneParticleTypes.TWINKLE_PARTICLE)
            .setScaleData(GenericParticleData.create(0.2f, 0.0f).build())
            .setTransparencyData(GenericParticleData.create(0.9f, 0.0f).build())
            .setColorData(ColorParticleData.create(WHITE_CYAN_COLOR, CYAN_COLOR).build())
            .setLifetime(10)
            .setRandomOffset(0.2, 0.2, 0.2)
            .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
            .enableNoClip()
            .spawn(level, center.x, center.y + 0.15, center.z)
    }

    private fun spawnConvergingRibbons(level: Level, coreCenter: Vec3, sidePositions: List<BlockPos>) {
        if (sidePositions.isEmpty()) return

        // Sample up to 2-3 random side blocks per cycle for throttled, silky performance
        val sampleCount = minOf(3, sidePositions.size)
        val sampled = sidePositions.shuffled().take(sampleCount)

        for (sidePos in sampled) {
            val start = Vec3.atCenterOf(sidePos)
            val dx = coreCenter.x - start.x
            val dy = (coreCenter.y + 0.1) - start.y
            val dz = coreCenter.z - start.z
            val dist = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
            if (dist < 0.1) continue

            // Travel takes ~12 ticks
            val travelTicks = 12
            val vx = dx / travelTicks
            val vy = dy / travelTicks
            val vz = dz / travelTicks

            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                .setScaleData(GenericParticleData.create(0.18f, 0.04f).build())
                .setTransparencyData(GenericParticleData.create(0.85f, 0.1f).build())
                .setColorData(ColorParticleData.create(WHITE_CYAN_COLOR, AZURE_COLOR).build())
                .setLifetime(travelTicks)
                .setMotion(vx, vy, vz)
                .setFriction(1.0f) // Keep constant flight speed without drag
                .setGravity(0f)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .enableNoClip()
                .spawn(level, start.x, start.y, start.z)
        }
    }
}
