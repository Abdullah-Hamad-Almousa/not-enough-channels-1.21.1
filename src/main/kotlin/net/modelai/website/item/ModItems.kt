package net.modelai.website.item

import java.util.function.Supplier
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.SnowballItem
import net.modelai.website.NotEnoughChannels
import net.modelai.website.block.ModBlocks
import net.modelai.website.item.custom.ChannelAppleItem
import net.modelai.website.item.custom.ChannelStickItem
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(NotEnoughChannels.MODID)

    val CHANNEL_STICK: DeferredItem<ChannelStickItem> =
        ITEMS.register("channel_stick", Supplier { ChannelStickItem(1, Item.Properties().stacksTo(1).fireResistant()) })

    val CHANNEL_STICK_TIER_2: DeferredItem<ChannelStickItem> =
        ITEMS.register("channel_stick_tier_2", Supplier { ChannelStickItem(2, Item.Properties().stacksTo(1).fireResistant()) })

    val CHANNEL_STICK_TIER_3: DeferredItem<ChannelStickItem> =
        ITEMS.register("channel_stick_tier_3", Supplier { ChannelStickItem(3, Item.Properties().stacksTo(1).fireResistant()) })

    val CHANNEL_APPLE: DeferredItem<ChannelAppleItem> =
        ITEMS.register("channel_apple", Supplier {
            ChannelAppleItem(
                Item.Properties().food(
                    net.minecraft.world.food.FoodProperties.Builder()
                        .nutrition(4)
                        .saturationModifier(1f)
                        .alwaysEdible()
                        .build()
                )
            )
        })

    val SOLAR_MULTIBLOCK_MACHINE_ITEM: DeferredItem<BlockItem> =
        ITEMS.register("solar_multiblock_machine", Supplier {
            BlockItem(ModBlocks.SOLAR_MULTIBLOCK_MACHINE.get(), Item.Properties())
        })

    val SIDE_STRUCTURE_BLOCK_ITEM: DeferredItem<BlockItem> =
        ITEMS.register("side_structure_block", Supplier {
            BlockItem(ModBlocks.SIDE_STRUCTURE_BLOCK.get(), Item.Properties())
        })

    fun register(bus: IEventBus) {
        ITEMS.register(bus)
    }

}
