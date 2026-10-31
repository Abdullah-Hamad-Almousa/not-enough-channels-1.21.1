package net.modelai.website.item

import java.util.function.Supplier
import net.minecraft.world.item.Item
import net.minecraft.world.item.SnowballItem
import net.modelai.website.NotEnoughChannels
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(NotEnoughChannels.MODID)

    val CHANNEL_SNOWBALL: DeferredItem<SnowballItem> =
        ITEMS.register("channel_snowball", Supplier { SnowballItem(Item.Properties()) })

    val CHANNEL_ROCK: DeferredItem<SnowballItem> =
        ITEMS.register("channel_rock", Supplier { SnowballItem(Item.Properties()) })

    fun register(bus: IEventBus) {
        ITEMS.register(bus)
    }
}
