package net.modelai.website

import com.mojang.logging.LogUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluids
import net.modelai.website.block.ModBlocks
import net.modelai.website.block.entity.ModBlockEntities
import net.modelai.website.crafting.LightningCrafting
import net.modelai.website.crafting.LightningCraftingHandler
import net.modelai.website.crafting.SolarMachineCrafting
import net.modelai.website.crafting.ingredient
import net.modelai.website.crafting.output
import net.modelai.website.crafting.solarFluid
import net.modelai.website.crafting.solarFluidOutput
import net.modelai.website.crafting.solarItem
import net.modelai.website.crafting.solarOutput
import net.modelai.website.item.ModItems
import net.modelai.website.tab.ModCreativeTabs
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartingEvent
import org.slf4j.Logger
import java.util.function.Supplier

@Mod(NotEnoughChannels.MODID)
class NotEnoughChannels(modEventBus: IEventBus, modContainer: ModContainer) {

    companion object {
        const val MODID: String = "notenoughchannels"
        val LOGGER: Logger = LogUtils.getLogger()
    }

    init {
        // Register the commonSetup method for modloading
        modEventBus.addListener(::commonSetup)
        modEventBus.addListener(::registerCapabilities)

        // Register blocks, block entities, items, and creative tabs to the mod event bus
        ModBlocks.register(modEventBus)
        ModBlockEntities.register(modEventBus)
        ModItems.register(modEventBus)
        ModCreativeTabs.register(modEventBus)

        // Register ourselves for server events
        NeoForge.EVENT_BUS.register(this)
        NeoForge.EVENT_BUS.register(LightningCraftingHandler)

        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)

        LightningCrafting.register(1, Supplier { Items.APPLE }, ModItems.CHANNEL_APPLE)
        LightningCrafting.register(
            tier = 1,
            inputs = listOf(
                ingredient(Items.BREEZE_ROD, count = 64)
            ),
            outputs = listOf(
                output(ModItems.CHANNEL_STICK as Supplier<out Item>, count = 1)
            )
        )

        // Register sample recipes for the Floating Multiblock Solar Machine
        SolarMachineCrafting.register(
            itemInputs = listOf(
                solarItem(Items.DIAMOND),
                solarItem(ModItems.CHANNEL_APPLE)
            ),
            fluidInputs = listOf(
                solarFluid(Fluids.WATER, 1000)
            ),
            itemOutputs = listOf(
                solarOutput(Items.NETHERITE_INGOT, 16)
            ),
            fluidOutput = solarFluidOutput(Fluids.LAVA, 1000),
            durationTicks = 200,
            energyPerTick = 1000
        )

        SolarMachineCrafting.register(
            itemInputs = listOf(
                solarItem(Items.GOLD_INGOT),
                solarItem(Items.GOLD_INGOT),
                solarItem(Items.GOLD_INGOT),
                solarItem(Items.GOLD_INGOT),
                solarItem(ModItems.CHANNEL_STICK)
            ),
            itemOutputs = listOf(
                solarOutput(Items.NETHERITE_SCRAP, 2)
            ),
            fluidOutput = solarFluidOutput(Fluids.LAVA, 500),
            durationTicks = 100,
            energyPerTick = 500
        )

        SolarMachineCrafting.register(
            itemInputs = listOf(
                solarItem(Items.IRON_INGOT)
            ),
            fluidInputs = listOf(
                solarFluid(Fluids.WATER, 250)
            ),
            itemOutputs = listOf(
                solarOutput(Items.REDSTONE, 4)
            ),
            durationTicks = 60,
            energyPerTick = 250
        )

        SolarMachineCrafting.register(
            itemInputs = List(10) { solarItem(Items.STICK) },
            itemOutputs = listOf(
                solarOutput(Items.APPLE, 1),
                solarOutput(Items.GOLDEN_APPLE, 1)
            ),
            durationTicks = 100,
            energyPerTick = 100
        )
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("HELLO FROM COMMON SETUP")

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT))
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt())
    }

    private fun registerCapabilities(event: RegisterCapabilitiesEvent) {
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntities.SOLAR_MULTIBLOCK_TILE.get()
        ) { tile, _ -> tile.energyStorage }

        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            ModBlockEntities.SOLAR_MULTIBLOCK_TILE.get()
        ) { tile, side -> tile.getFluidHandler(side) }

        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.SOLAR_MULTIBLOCK_TILE.get()
        ) { tile, side -> tile.getItemHandler(side) }
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        LOGGER.info("HELLO from server starting")
    }
}
