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
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(Supplier { ItemStack(ModItems.CHANNEL_SNOWBALL.get()) })
                .displayItems { _, output ->
                    output.accept(ModItems.CHANNEL_SNOWBALL.get())
                    output.accept(ModItems.CHANNEL_ROCK.get())
                    output.accept(ModItems.CHANNEL_STICK.get())
                    output.accept (ModItems.CHANNEL_APPLE.get())
                }
                .build()
        })

    fun register(bus: IEventBus) {
        CREATIVE_MODE_TABS.register(bus)
    }
}
