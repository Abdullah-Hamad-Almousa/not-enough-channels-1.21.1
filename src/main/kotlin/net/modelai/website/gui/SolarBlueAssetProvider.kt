package net.modelai.website.gui

import com.hrznstudio.titanium.api.client.AssetTypes
import com.hrznstudio.titanium.api.client.IAsset
import com.hrznstudio.titanium.api.client.IAssetType
import com.hrznstudio.titanium.api.client.assets.types.IBackgroundAsset
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider
import net.minecraft.resources.ResourceLocation
import net.modelai.website.NotEnoughChannels
import java.awt.Point
import java.awt.Rectangle

/**
 * Custom Titanium Asset Provider for the Floating Multiblock Solar Machine.
 *
 * Configures the expanded 264x180 GUI container bounds and centered player inventory layout.
 * Safe to reference across both logical client and logical server environments.
 */
object SolarBlueAssetProvider : IAssetProvider {

    val EMPTY_BG_LOCATION: ResourceLocation = ResourceLocation.fromNamespaceAndPath(
        NotEnoughChannels.MODID, "textures/gui/empty.png"
    )

    private val BACKGROUND_ASSET: IBackgroundAsset = object : IBackgroundAsset {
        override fun getResourceLocation(): ResourceLocation = EMPTY_BG_LOCATION

        override fun getArea(): Rectangle = Rectangle(0, 0, 264, 180)

        override fun getInventoryPosition(): Point = Point(51, 98)

        override fun getHotbarPosition(): Point = Point(51, 156)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : IAsset> getAsset(assetType: IAssetType<T>): T? {
        if (assetType == AssetTypes.BACKGROUND) {
            return BACKGROUND_ASSET as T
        }
        // Returns null so Titanium automatically delegates unhandled assets to DefaultAssetProvider.DEFAULT_PROVIDER
        return null
    }
}
