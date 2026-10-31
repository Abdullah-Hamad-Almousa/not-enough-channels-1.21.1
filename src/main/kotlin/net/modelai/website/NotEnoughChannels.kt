package net.modelai.website

import java.util.function.Supplier
import net.minecraft.world.item.ItemStack
import com.mojang.logging.LogUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import org.slf4j.Logger

@Mod(NotEnoughChannels.MODID)
class NotEnoughChannels(modEventBus: IEventBus, modContainer: ModContainer) {

    companion object {
        const val MODID: String = "notenoughchannels"
        val LOGGER: Logger = LogUtils.getLogger()

        val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(MODID)
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)

        val EXAMPLE_BLOCK: DeferredBlock<Block> =
            BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE))

        val EXAMPLE_BLOCK_ITEM: DeferredItem<BlockItem> =
            ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK)

        val EXAMPLE_ITEM: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "example_item",
            Item.Properties().food(
                FoodProperties.Builder()
                    .alwaysEdible()
                    .nutrition(1)
                    .saturationModifier(2f)
                    .build()
            )
        )

        private fun createExampleTab(): CreativeModeTab {
            return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.notenoughchannels"))
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(Supplier { ItemStack(EXAMPLE_ITEM.get()) })
                .displayItems { _, output ->
                    output.accept(EXAMPLE_ITEM.get())
                }
                .build()
        }

        val EXAMPLE_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> =
            CREATIVE_MODE_TABS.register("example_tab", Supplier(::createExampleTab))
    }

    init {
        // Register the commonSetup method for modloading
        modEventBus.addListener(::commonSetup)

        // Register Deferred Registers to the mod event bus
        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus)

        // Register ourselves for server events
        NeoForge.EVENT_BUS.register(this)

        // Register the item to a creative tab
        modEventBus.addListener(::addCreative)

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

    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM)
        }
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        LOGGER.info("HELLO from server starting")
    }
}