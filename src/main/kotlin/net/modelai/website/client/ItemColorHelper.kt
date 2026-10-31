package net.modelai.website.client

import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent

fun RegisterColorHandlersEvent.Item.registerColor(color: Long, vararg items: ItemLike) {
    this.register(
        { _: ItemStack, tintIndex: Int ->
            if (tintIndex == 0) color.toInt() else -1
        },
        *items
    )
}

fun RegisterColorHandlersEvent.Item.registerColor(color: Int, vararg items: ItemLike) {
    this.registerColor(color.toLong(), *items)
}

fun RegisterColorHandlersEvent.Item.registerColor(item: ItemLike, color: Long) {
    this.registerColor(color, item)
}

fun RegisterColorHandlersEvent.Item.registerColor(item: ItemLike, color: Int) {
    this.registerColor(color.toLong(), item)
}
