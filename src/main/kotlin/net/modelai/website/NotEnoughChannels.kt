package net.modelai.website

import com.mojang.logging.LogUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Blocks
import net.modelai.website.crafting.LightningCrafting
import net.modelai.website.crafting.LightningCraftingHandler
import net.modelai.website.crafting.ingredient
import net.modelai.website.crafting.output
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
import net.modelai.website.crafting.ingredient
import net.modelai.website.crafting.output

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

//        LightningCrafting.register(
//            tier = 3,
//            inputs = listOf(
//                ingredient(Items.APPLE, count = 1),
//                ingredient(Items.HONEYCOMB, count = 2)
//            ),
//            outputs = listOf(
//                output(Items.GOAT_HORN, count = 2),
//                output(Items.HONEYCOMB, count = 1)
//            )
//        )
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
