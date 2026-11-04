package net.modelai.website.tab

import java.util.function.Supplier
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.ItemStack
import net.modelai.website.NotEnoughChannels
import net.modelai.website.item.ModItems
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object ModCreativeTabs {
    val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NotEnoughChannels.MODID)

    val MOD_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> =
        CREATIVE_MODE_TABS.register("main", Supplier {
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.notenoughchannels"))
                .icon(Supplier { ItemStack(ModItems.CHANNEL_STICK.get()) })
                .displayItems { _, output ->
                    output.accept { ModItems.CHANNEL_APPLE.get() }
                    output.accept(ModItems.CHANNEL_STICK.get())
                    output.accept(ModItems.CHANNEL_STICK_TIER_2.get())
                    output.accept(ModItems.CHANNEL_STICK_TIER_3.get())
                    output.accept(ModItems.SOLAR_MULTIBLOCK_MACHINE_ITEM.get())
                    output.accept(ModItems.SIDE_STRUCTURE_BLOCK_ITEM.get())
                }
                .build()
        })

    fun register(bus: IEventBus) {
        CREATIVE_MODE_TABS.register(bus)
    }
}
