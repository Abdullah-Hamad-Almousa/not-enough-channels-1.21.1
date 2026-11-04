package net.modelai.website.block.entity

import com.hrznstudio.titanium.annotation.Save
import com.hrznstudio.titanium.block.tile.PoweredTile
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent
import com.hrznstudio.titanium.component.fluid.FluidTankComponent
import com.hrznstudio.titanium.component.inventory.InventoryComponent
import com.hrznstudio.titanium.component.progress.ProgressBarComponent
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.modelai.website.block.ModBlocks
import net.modelai.website.client.gui.BluePanelScreenAddon
import net.modelai.website.client.gui.SolarStatusScreenAddon
import net.modelai.website.client.vfx.SolarMachineVFX
import net.modelai.website.crafting.MatchResult
import net.modelai.website.crafting.SolarMachineCrafting
import net.modelai.website.gui.SolarBlueAssetProvider
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import net.neoforged.neoforge.items.IItemHandler
import java.awt.Color

class SolarMultiblockTileEntity(pos: BlockPos, state: BlockState) :
    PoweredTile<SolarMultiblockTileEntity>(
        ModBlocks.SOLAR_MULTIBLOCK_MACHINE.get(),
        ModBlockEntities.SOLAR_MULTIBLOCK_TILE.get(),
        pos,
        state
    ) {

    companion object {
        // 16 Exact relative offsets (dx, dy, dz) for North-South (0° / 180°) orientation
        val PATTERN_OFFSETS_NS: List<BlockPos> = listOf(
            // Layer 1 (dy = -2)
            BlockPos(-2, -2, -1), BlockPos(2, -2, -1),
            BlockPos(-1, -2, 0),  BlockPos(1, -2, 0),
            BlockPos(-2, -2, 1),  BlockPos(2, -2, 1),
            // Layer 2 (dy = -1)
            BlockPos(0, -1, -2),  BlockPos(0, -1, 2),
            // Layer 3 (dy = 0, Controller B at 0, 0, 0)
            BlockPos(-2, 0, -2),  BlockPos(2, 0, -2),
            BlockPos(-2, 0, 2),   BlockPos(2, 0, 2),
            // Layer 4 (dy = +1)
            BlockPos(0, 1, -2),
            BlockPos(-2, 1, 0),   BlockPos(2, 1, 0),
            BlockPos(0, 1, 2)
        )

        // 16 Exact relative offsets (dx, dy, dz) for East-West (90° / 270°) orientation
        val PATTERN_OFFSETS_EW: List<BlockPos> = PATTERN_OFFSETS_NS.map { pos ->
            BlockPos(-pos.z, pos.y, pos.x)
        }
    }

    // --- Components ---

    @field:Save
    val inputInventory: InventoryComponent<SolarMultiblockTileEntity> =
        InventoryComponent<SolarMultiblockTileEntity>("inputInventory", 96, 32, 10)
            .setRange(5, 2)
            .setSlotLimit(1)
            .setOutputFilter { _, _ -> false } // Automation cannot extract inputs
            .setColorGuiEnabled(true)
            .setOnSlotChanged { _, _ -> markInputsDirty() }

    @field:Save
    val outputInventory: InventoryComponent<SolarMultiblockTileEntity> =
        InventoryComponent<SolarMultiblockTileEntity>("outputInventory", 216, 32, 2)
            .setRange(1, 2)
            .setSlotLimit(64)
            .setInputFilter { _, _ -> false } // Automation cannot insert into outputs
            .setColorGuiEnabled(true)
            .setOnSlotChanged { _, _ -> markInputsDirty() }

    @field:Save
    val inputTank1: FluidTankComponent<SolarMultiblockTileEntity> =
        FluidTankComponent<SolarMultiblockTileEntity>("inputTank1", 1000, 32, 22)
            .setTankAction(FluidTankComponent.Action.FILL)
            .setOnContentChange { markInputsDirty() }

    @field:Save
    val inputTank2: FluidTankComponent<SolarMultiblockTileEntity> =
        FluidTankComponent<SolarMultiblockTileEntity>("inputTank2", 1000, 52, 22)
            .setTankAction(FluidTankComponent.Action.FILL)
            .setOnContentChange { markInputsDirty() }

    @field:Save
    val inputTank3: FluidTankComponent<SolarMultiblockTileEntity> =
        FluidTankComponent<SolarMultiblockTileEntity>("inputTank3", 1000, 72, 22)
            .setTankAction(FluidTankComponent.Action.FILL)
            .setOnContentChange { markInputsDirty() }

    @field:Save
    val outputTank: FluidTankComponent<SolarMultiblockTileEntity> =
        FluidTankComponent<SolarMultiblockTileEntity>("outputTank", 1000, 234, 22)
            .setTankAction(FluidTankComponent.Action.DRAIN)
            .setOnContentChange { markInputsDirty() }

    @field:Save
    val progressBar: ProgressBarComponent<SolarMultiblockTileEntity> =
        ProgressBarComponent<SolarMultiblockTileEntity>(190, 42, 100)
            .setBarDirection(ProgressBarComponent.BarDirection.ARROW_RIGHT)
            .setColor(DyeColor.CYAN)
            .setCanIncrease { false } // Explicitly managed in serverTick
            .setCanReset { false }

    // --- State Variables ---

    private var tickCounter: Int = 0

    var structureDirty: Boolean = true
        private set

    var isStructureValid: Boolean = false
        private set

    var isCrafting: Boolean = false
        private set

    var inputsDirty: Boolean = true
        private set

    val cachedSidePositions: MutableList<BlockPos> = ArrayList()

    private var activeMatch: MatchResult? = null

    init {
        // Apply Electric Cyan tinting to the 10 item input slots
        for (i in 0 until 10) {
            inputInventory.setSlotToColorRender(i, Color(0x00, 0xD2, 0xFF))
        }
        // Apply Royal Blue tinting to the 2 item output slots
        outputInventory.setSlotToColorRender(0, Color(0x1E, 0x90, 0xFF))
        outputInventory.setSlotToColorRender(1, Color(0x1E, 0x90, 0xFF))

        addInventory(inputInventory)
        addInventory(outputInventory)
        addTank(inputTank1)
        addTank(inputTank2)
        addTank(inputTank3)
        addTank(outputTank)
        addProgressBar(progressBar)
    }

    override fun createEnergyStorage(): EnergyStorageComponent<SolarMultiblockTileEntity> {
        return EnergyStorageComponent(10_000_000, 100_000, 100_000, 12, 22)
    }

    override fun getAssetProvider(): IAssetProvider = SolarBlueAssetProvider

    override fun getTitleColor(): Int = 0xFF00D2FF.toInt()

    override fun initClient() {
        super.initClient()
        addGuiAddonFactory { BluePanelScreenAddon(264, 180) }
        addGuiAddonFactory { SolarStatusScreenAddon(this, 208, 5) }
    }

    fun markStructureDirty() {
        structureDirty = true
    }

    fun markInputsDirty() {
        inputsDirty = true
    }

    fun hasSunlight(): Boolean {
        val lvl = level ?: return false
        return lvl.isDay && !lvl.isRaining && lvl.canSeeSky(worldPosition.above())
    }

    override fun clientTick(
        level: Level,
        pos: BlockPos,
        state: BlockState,
        blockEntity: SolarMultiblockTileEntity
    ) {
        super.clientTick(level, pos, state, blockEntity)
        if (level.isClientSide && isCrafting) {
            SolarMachineVFX.spawnCraftingParticles(level, pos, this)
        }
    }

    override fun serverTick(
        level: Level,
        pos: BlockPos,
        state: BlockState,
        blockEntity: SolarMultiblockTileEntity
    ) {
        super.serverTick(level, pos, state, blockEntity)
        if (level.isClientSide) return

        tickCounter++

        // 1. Structure Verification (Every 20 ticks or when flagged dirty)
        if (tickCounter % 20 == 0 || structureDirty) {
            updateStructure(level)
        }

        // 2. Recipe Matching (Only recomputed when inputsDirty)
        if (inputsDirty) {
            inputsDirty = false
            recheckRecipe()
        }

        val match = activeMatch
        val hasSun = hasSunlight()

        // 3. Daylight & Structure Enforcement: Cancel immediately if conditions fail mid-craft
        if (progressBar.progress > 0) {
            if (!hasSun) {
                // Sunlight lost mid-craft: reset progress and play extinguish sound
                progressBar.progress = 0
                setCraftingState(false)
                level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.9f, 0.8f)
                level.playSound(null, worldPosition, SoundEvents.CONDUIT_DEACTIVATE, SoundSource.BLOCKS, 0.8f, 0.9f)
            } else if (!isStructureValid) {
                progressBar.progress = 0
                setCraftingState(false)
            }
        }

        // 4. Crafting Execution
        if (match != null && isStructureValid && hasSun) {
            val recipe = match.recipe
            val canOutput = recipe.canAcceptOutputs(outputInventory, outputTank)
            val hasEnergy = energyStorage.energyStored >= recipe.energyPerTick

            if (canOutput && hasEnergy) {
                // Ensure crafting state is active and play start audio on initiation
                if (!isCrafting) {
                    setCraftingState(true)
                    level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8f, 1.2f)
                    level.playSound(null, worldPosition, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 0.7f, 1.1f)
                }

                // Drain energy
                energyStorage.extractEnergy(recipe.energyPerTick, false)

                // Advance progress
                progressBar.progress++

                // Ambient crafting sound
                if (progressBar.progress % 40 == 0) {
                    level.playSound(null, worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.5f, 1.4f)
                }

                // 5. Completion at 100% Progress
                if (progressBar.progress >= progressBar.maxProgress) {
                    finishCraft(level, match)
                }
            } else {
                if (isCrafting) {
                    setCraftingState(false)
                }
            }
        } else {
            if (isCrafting) {
                setCraftingState(false)
            }
        }
    }

    private fun updateStructure(lvl: Level) {
        val sideBlock = ModBlocks.SIDE_STRUCTURE_BLOCK.get()

        if (!structureDirty && isStructureValid && cachedSidePositions.size == 16) {
            var valid = true
            for (p in cachedSidePositions) {
                if (!lvl.getBlockState(p).`is`(sideBlock)) {
                    valid = false
                    break
                }
            }
            if (!valid) {
                structureDirty = true
            }
        }

        if (structureDirty || !isStructureValid) {
            val matchesNS = ArrayList<BlockPos>(16)
            for (offset in PATTERN_OFFSETS_NS) {
                val checkPos = worldPosition.offset(offset)
                if (lvl.getBlockState(checkPos).`is`(sideBlock)) {
                    matchesNS.add(checkPos)
                }
            }

            val bestMatches = if (matchesNS.size == 16) {
                matchesNS
            } else {
                val matchesEW = ArrayList<BlockPos>(16)
                for (offset in PATTERN_OFFSETS_EW) {
                    val checkPos = worldPosition.offset(offset)
                    if (lvl.getBlockState(checkPos).`is`(sideBlock)) {
                        matchesEW.add(checkPos)
                    }
                }
                if (matchesEW.size > matchesNS.size) matchesEW else matchesNS
            }

            structureDirty = false
            val prevValid = isStructureValid
            val prevCount = cachedSidePositions.size

            cachedSidePositions.clear()
            cachedSidePositions.addAll(bestMatches)
            isStructureValid = cachedSidePositions.size == 16

            if (prevValid != isStructureValid || prevCount != cachedSidePositions.size) {
                markForUpdate()
            }
        }
    }

    private fun recheckRecipe() {
        val tanks = listOf(inputTank1, inputTank2, inputTank3)
        val match = SolarMachineCrafting.findMatchingRecipe(inputInventory, tanks)

        if (match != null && match.recipe.canAcceptOutputs(outputInventory, outputTank)) {
            if (activeMatch != null && activeMatch?.recipe != match.recipe && progressBar.progress > 0) {
                progressBar.progress = 0
                setCraftingState(false)
            }
            activeMatch = match
            progressBar.maxProgress = match.recipe.durationTicks
        } else {
            if (progressBar.progress > 0) {
                progressBar.progress = 0
                setCraftingState(false)
            }
            activeMatch = null
        }
    }

    private fun finishCraft(lvl: Level, match: MatchResult) {
        val recipe = match.recipe

        // 1. Consume matched items (1 count per slot)
        for (slotIdx in match.matchedItemSlots) {
            val current = inputInventory.getStackInSlot(slotIdx)
            if (!current.isEmpty) {
                current.shrink(1)
                inputInventory.setStackInSlot(slotIdx, current)
            }
        }

        // 2. Consume matched fluids
        val tanks = listOf(inputTank1, inputTank2, inputTank3)
        for (i in match.matchedFluidTanks.indices) {
            val tankIdx = match.matchedFluidTanks[i]
            val fluidReq = recipe.fluidInputs[i]
            tanks[tankIdx].drainForced(fluidReq.amountMb, IFluidHandler.FluidAction.EXECUTE)
        }

        // 3. Insert item outputs
        for (out in recipe.itemOutputs) {
            var toInsert = out.createStack()
            for (slot in 0 until outputInventory.slots) {
                toInsert = outputInventory.insertItem(slot, toInsert, false)
                if (toInsert.isEmpty) break
            }
        }

        // 4. Insert fluid output
        if (recipe.fluidOutput != null) {
            outputTank.fillForced(recipe.fluidOutput.createStack(), IFluidHandler.FluidAction.EXECUTE)
        }

        // 5. Reset progress & play completion audio
        progressBar.progress = 0
        setCraftingState(false)

        lvl.playSound(null, worldPosition, SoundEvents.CRAFTER_CRAFT, SoundSource.BLOCKS, 1.0f, 1.0f)
        lvl.playSound(null, worldPosition, SoundEvents.VAULT_ACTIVATE, SoundSource.BLOCKS, 0.8f, 1.2f)

        markInputsDirty()
        markForUpdate()
    }

    private fun setCraftingState(crafting: Boolean) {
        if (this.isCrafting != crafting) {
            this.isCrafting = crafting
            markForUpdate()
        }
    }

    override fun getItemHandler(side: Direction?): IItemHandler? {
        return super.getItemHandler(side ?: Direction.NORTH)
    }

    override fun getFluidHandler(side: Direction?): IFluidHandler? {
        return super.getFluidHandler(side ?: Direction.NORTH)
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.putBoolean("IsCrafting", isCrafting)
        val posArray = cachedSidePositions.map { it.asLong() }.toLongArray()
        tag.putLongArray("SidePositions", posArray)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        isCrafting = tag.getBoolean("IsCrafting")
        if (tag.contains("SidePositions", Tag.TAG_LONG_ARRAY.toInt())) {
            cachedSidePositions.clear()
            tag.getLongArray("SidePositions").forEach {
                cachedSidePositions.add(BlockPos.of(it))
            }
            isStructureValid = cachedSidePositions.size == 16
        }
        inputsDirty = true
        structureDirty = true
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        val tag = super.getUpdateTag(registries)
        tag.putBoolean("IsCrafting", isCrafting)
        tag.putLongArray("SidePositions", cachedSidePositions.map { it.asLong() }.toLongArray())
        return tag
    }

    override fun handleUpdateTag(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.handleUpdateTag(tag, registries)
        isCrafting = tag.getBoolean("IsCrafting")
        if (tag.contains("SidePositions", Tag.TAG_LONG_ARRAY.toInt())) {
            cachedSidePositions.clear()
            tag.getLongArray("SidePositions").forEach {
                cachedSidePositions.add(BlockPos.of(it))
            }
            isStructureValid = cachedSidePositions.size == 16
        }
    }

    override fun onActivated(
        player: Player,
        hand: InteractionHand,
        facing: Direction,
        hitX: Double,
        hitY: Double,
        hitZ: Double
    ): ItemInteractionResult {
        val superResult = super.onActivated(player, hand, facing, hitX, hitY, hitZ)
        if (superResult == ItemInteractionResult.SUCCESS) {
            return ItemInteractionResult.SUCCESS
        }
        openGui(player)
        return ItemInteractionResult.SUCCESS
    }

    override fun getSelf(): SolarMultiblockTileEntity {
        return this
    }
}
