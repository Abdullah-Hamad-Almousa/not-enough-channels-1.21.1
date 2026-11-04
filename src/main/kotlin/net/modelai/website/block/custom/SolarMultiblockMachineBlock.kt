package net.modelai.website.block.custom

import com.hrznstudio.titanium.block.BasicTileBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.modelai.website.block.entity.SolarMultiblockTileEntity

class SolarMultiblockMachineBlock(properties: Properties) :
    BasicTileBlock<SolarMultiblockTileEntity>(properties, SolarMultiblockTileEntity::class.java) {

    override fun getTileEntityFactory(): BlockEntityType.BlockEntitySupplier<*> {
        return BlockEntityType.BlockEntitySupplier { pos, state ->
            SolarMultiblockTileEntity(pos, state)
        }
    }

    override fun propagatesSkylightDown(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return true
    }
}
