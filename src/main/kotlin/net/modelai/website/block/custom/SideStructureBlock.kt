package net.modelai.website.block.custom

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.modelai.website.block.entity.SolarMultiblockTileEntity

class SideStructureBlock(properties: Properties) : Block(properties) {

    override fun propagatesSkylightDown(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return true
    }

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, movedByPiston: Boolean) {
        super.onPlace(state, level, pos, oldState, movedByPiston)
        if (!level.isClientSide && !state.`is`(oldState.block)) {
            notifyNearbyControllers(level, pos)
        }
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        if (!level.isClientSide && !state.`is`(newState.block)) {
            notifyNearbyControllers(level, pos)
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    private fun notifyNearbyControllers(level: Level, pos: BlockPos) {
        for (checkPos in BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 2, 2))) {
            val be = level.getBlockEntity(checkPos)
            if (be is SolarMultiblockTileEntity) {
                be.markStructureDirty()
            }
        }
    }
}
