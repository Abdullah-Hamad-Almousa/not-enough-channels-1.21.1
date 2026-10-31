package net.modelai.website

import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = NotEnoughChannels.MODID, dist = [Dist.CLIENT])
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = NotEnoughChannels.MODID, value = [Dist.CLIENT])
class NotEnoughChannelsClient(container: ModContainer) {

    init {
        // Allows NeoForge to create a config screen for this mod's configs.
        container.registerExtensionPoint(IConfigScreenFactory::class.java, IConfigScreenFactory { modContainer, parent ->
            ConfigurationScreen(modContainer, parent)
        })
    }

    companion object {
        @SubscribeEvent
        @JvmStatic
        fun onClientSetup(event: FMLClientSetupEvent) {
            // Some client setup code
            NotEnoughChannels.LOGGER.info("HELLO FROM CLIENT SETUP")
            NotEnoughChannels.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
        }

        @SubscribeEvent
        @JvmStatic
        fun registerItemColors(e: RegisterColorHandlersEvent.Item) {
            e.register(
                { _: ItemStack, tintIndex: Int ->
                    if (tintIndex == 0) {
                        0xFF2121BB.toInt()
                    } else {
                        -1
                    }
                },
                NotEnoughChannels.CHANNEL_SNOWBALL.get(

                )
            )
        }
    }
}