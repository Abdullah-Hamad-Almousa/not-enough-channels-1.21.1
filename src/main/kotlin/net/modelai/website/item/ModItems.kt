package net.modelai.website.item

import java.util.function.Supplier
import net.minecraft.world.item.Item
import net.minecraft.world.item.SnowballItem
import net.modelai.website.NotEnoughChannels
import net.modelai.website.item.custom.ChannelAppleItem
import net.modelai.website.item.custom.ChannelStickItem
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(NotEnoughChannels.MODID)

    val CHANNEL_SNOWBALL: DeferredItem<SnowballItem> =
        ITEMS.register("channel_snowball", Supplier { SnowballItem(Item.Properties()) })

    val CHANNEL_ROCK: DeferredItem<SnowballItem> =
        ITEMS.register("channel_rock", Supplier { SnowballItem(Item.Properties()) })

    val CHANNEL_STICK: DeferredItem<ChannelStickItem> =
        ITEMS.register("channel_stick", Supplier { ChannelStickItem(Item.Properties().stacksTo(1)) })

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

    fun register(bus: IEventBus) {
        ITEMS.register(bus)
    }

}