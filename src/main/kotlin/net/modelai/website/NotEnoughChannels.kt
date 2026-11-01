package net.modelai.website

import com.mojang.logging.LogUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Blocks
import net.modelai.website.crafting.LightningCrafting
import net.modelai.website.crafting.LightningCraftingHandler
import net.modelai.website.item.ModItems
import net.modelai.website.tab.ModCreativeTabs
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartingEvent
import org.slf4j.Logger
import java.util.function.Supplier
import net.minecraft.world.item.Items
import net.modelai.website.item.ModItems.CHANNEL_APPLE

@Mod(NotEnoughChannels.MODID)
class NotEnoughChannels(modEventBus: IEventBus, modContainer: ModContainer) {

    companion object {
        const val MODID: String = "notenoughchannels"
        val LOGGER: Logger = LogUtils.getLogger()
    }

    init {
        // Register the commonSetup method for modloading
        modEventBus.addListener(::commonSetup)

        // Register items and creative tabs to the mod event bus
        ModItems.register(modEventBus)
        ModCreativeTabs.register(modEventBus)

        // Register ourselves for server events
        NeoForge.EVENT_BUS.register(this)
        NeoForge.EVENT_BUS.register(LightningCraftingHandler)

        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)

        // Register Crafting recipe (no .get()!)
        LightningCrafting.register(1, ModItems.CHANNEL_SNOWBALL, ModItems.CHANNEL_ROCK)
        LightningCrafting.register(1, Supplier { Items.APPLE }, ModItems.CHANNEL_APPLE)

        // Example Tier 2 / Tier 3 recipes can be added here whenever needed:
        // LightningCrafting.register(2, Supplier { Items.GOLD_INGOT }, Supplier { Items.DIAMOND })
        LightningCrafting.register(2, Supplier { Items.GOLDEN_APPLE }, Supplier { Items.ENCHANTED_GOLDEN_APPLE})
        // LightningCrafting.register(3, Supplier { Items.NETHER_STAR }, ...)
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("HELLO FROM COMMON SETUP")

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT))
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt())
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        LOGGER.info("HELLO from server starting")
    }
}
