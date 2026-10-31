package net.modelai.website

import java.util.function.Supplier
import net.minecraft.world.item.ItemStack
import com.mojang.logging.LogUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.item.SnowballItem
import net.minecraft.world.level.block.Blocks
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import org.slf4j.Logger

@Mod(NotEnoughChannels.MODID)
class NotEnoughChannels(modEventBus: IEventBus, modContainer: ModContainer) {

    companion object {
        const val MODID: String = "notenoughchannels"
        val LOGGER: Logger = LogUtils.getLogger()

        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)

        val CHANNEL_SNOWBALL: DeferredItem<SnowballItem> =
            ITEMS.register("channel_snowball", Supplier { SnowballItem(Item.Properties()) })
        val CHANNEL_ROCK: DeferredItem<SnowballItem> =
            ITEMS.register("channel_rock", Supplier { SnowballItem(Item.Properties()) })

        private fun createModTab(): CreativeModeTab {
            return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.notenoughchannels"))
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(Supplier { ItemStack(CHANNEL_SNOWBALL.get()) })
                .displayItems { _, output ->
                    output.accept(CHANNEL_SNOWBALL.get())
                    output.accept(CHANNEL_ROCK.get())
                }
                .build()
        }

        val MOD_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> =
            CREATIVE_MODE_TABS.register("main", Supplier(::createModTab))
    }

    init {
        // Register the commonSetup method for modloading
        modEventBus.addListener(::commonSetup)

        // Register Deferred Registers to the mod event bus
        ITEMS.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus)

        // Register ourselves for server events
        NeoForge.EVENT_BUS.register(this)

        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
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