package net.modelai.website

import net.minecraft.client.Minecraft
import net.modelai.website.client.registerColor
import net.modelai.website.item.ModItems
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

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
            e.registerColor(ModItems.CHANNEL_STICK, 0xFF949494)
            e.registerColor(ModItems.CHANNEL_STICK_TIER_2, 0xFF119454)
            e.registerColor(ModItems.CHANNEL_STICK_TIER_3, 0xFF99BB26)
            e.registerColor(ModItems.CHANNEL_APPLE, 0xFF2551BB)
        }
    }
}
