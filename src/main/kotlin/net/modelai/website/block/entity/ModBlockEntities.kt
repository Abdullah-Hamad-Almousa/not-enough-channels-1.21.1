package net.modelai.website.block.entity

import java.util.function.Supplier
import net.minecraft.Util
import net.minecraft.core.registries.Registries
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.level.block.entity.BlockEntityType
import net.modelai.website.NotEnoughChannels
import net.modelai.website.block.ModBlocks
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object ModBlockEntities {
    val BLOCK_ENTITIES: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NotEnoughChannels.MODID)

    val SOLAR_MULTIBLOCK_TILE: DeferredHolder<BlockEntityType<*>, BlockEntityType<SolarMultiblockTileEntity>> =
        BLOCK_ENTITIES.register("solar_multiblock_machine", Supplier { createSolarMultiblockTileType() })

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun createSolarMultiblockTileType(): BlockEntityType<SolarMultiblockTileEntity> {
        return BlockEntityType.Builder.of(
            BlockEntityType.BlockEntitySupplier { pos, state -> SolarMultiblockTileEntity(pos, state) },
            ModBlocks.SOLAR_MULTIBLOCK_MACHINE.get()
        ).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "solar_multiblock_machine"))
    }

    fun register(bus: IEventBus) {
        BLOCK_ENTITIES.register(bus)
    }
}
