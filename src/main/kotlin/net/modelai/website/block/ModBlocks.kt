package net.modelai.website.block

import java.util.function.Supplier
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.modelai.website.NotEnoughChannels
import net.modelai.website.block.custom.SideStructureBlock
import net.modelai.website.block.custom.SolarMultiblockMachineBlock
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredRegister

object ModBlocks {
    val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(NotEnoughChannels.MODID)

    val SOLAR_MULTIBLOCK_MACHINE: DeferredBlock<SolarMultiblockMachineBlock> =
        BLOCKS.register("solar_multiblock_machine", Supplier { createSolarMachineBlock() })

    val SIDE_STRUCTURE_BLOCK: DeferredBlock<SideStructureBlock> =
        BLOCKS.register("side_structure_block", Supplier { createSideStructureBlock() })

    private fun createSolarMachineBlock(): SolarMultiblockMachineBlock {
        return SolarMultiblockMachineBlock(
            BlockBehaviour.Properties.of()
                .strength(4.0f, 8.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .isViewBlocking { _, _, _ -> false }
        )
    }

    private fun createSideStructureBlock(): SideStructureBlock {
        return SideStructureBlock(
            BlockBehaviour.Properties.of()
                .strength(3.0f, 6.0f)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isViewBlocking { _, _, _ -> false }
        )
    }

    fun register(bus: IEventBus) {
        BLOCKS.register(bus)
    }
}
