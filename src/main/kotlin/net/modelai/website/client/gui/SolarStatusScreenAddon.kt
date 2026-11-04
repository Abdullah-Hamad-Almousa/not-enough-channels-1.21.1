package net.modelai.website.client.gui

import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.modelai.website.block.entity.SolarMultiblockTileEntity

/**
 * Screen addon providing a compact status pill badge in the GUI header bar.
 *
 * Displays live sunlight and multiblock structure formation status, with rich
 * hover tooltips for real-time diagnostics.
 */
class SolarStatusScreenAddon(
    private val tile: SolarMultiblockTileEntity,
    posX: Int = 208,
    posY: Int = 5
) : BasicScreenAddon(posX, posY) {

    companion object {
        const val BADGE_WIDTH: Int = 50
        const val BADGE_HEIGHT: Int = 11

        const val COLOR_PILL_BG: Int = 0xFF050C16.toInt()
        const val COLOR_BORDER_ACTIVE: Int = 0xFF00D2FF.toInt()
        const val COLOR_BORDER_INACTIVE: Int = 0xFF1B3A57.toInt()

        const val COLOR_SUN_ACTIVE: Int = 0xFFFFD700.toInt()
        const val COLOR_SUN_BLOCKED: Int = 0xFFCC3333.toInt()

        const val COLOR_MB_VALID: Int = 0xFF00FF66.toInt()
        const val COLOR_MB_INVALID: Int = 0xFFFFA500.toInt()
    }

    override fun isBackground(): Boolean = false

    override fun getXSize(): Int = BADGE_WIDTH

    override fun getYSize(): Int = BADGE_HEIGHT

    override fun drawBackgroundLayer(
        guiGraphics: GuiGraphics,
        screen: Screen,
        provider: IAssetProvider,
        xCenter: Int,
        yCenter: Int,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        // No background layer needed; the status badge renders on the foreground pass
    }

    override fun drawForegroundLayer(
        guiGraphics: GuiGraphics,
        screen: Screen,
        provider: IAssetProvider,
        xCenter: Int,
        yCenter: Int,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        val x = posX
        val y = posY

        val hasSun = tile.hasSunlight()
        val isValid = tile.isStructureValid

        // 1. Pill Background
        guiGraphics.fill(x, y, x + BADGE_WIDTH, y + BADGE_HEIGHT, COLOR_PILL_BG)

        // 2. Pill Border
        val borderColor = if (hasSun && isValid) COLOR_BORDER_ACTIVE else COLOR_BORDER_INACTIVE
        guiGraphics.renderOutline(x, y, BADGE_WIDTH, BADGE_HEIGHT, borderColor)

        val font = Minecraft.getInstance().font

        // 3. Sunlight Indicator (Dot/Sun at left)
        val sunColor = if (hasSun) COLOR_SUN_ACTIVE else COLOR_SUN_BLOCKED
        guiGraphics.drawString(font, "☀", x + 3, y + 2, sunColor, false)

        // 4. Structure Count Text (at right)
        val countText = if (isValid) "16/16" else "${tile.cachedSidePositions.size}/16"
        val countColor = if (isValid) COLOR_MB_VALID else COLOR_MB_INVALID
        guiGraphics.drawString(font, countText, x + 16, y + 2, countColor, false)
    }

    override fun getTooltipLines(): List<Component> {
        val lines = mutableListOf<Component>()
        lines.add(Component.literal("☀️ Solar Diagnostics").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))

        // Sunlight & Sky Access
        if (tile.hasSunlight()) {
            lines.add(Component.literal("  • Sunlight: Active (Sky Access OK)").withStyle(ChatFormatting.GREEN))
        } else {
            lines.add(Component.literal("  • Sunlight: Blocked / Night").withStyle(ChatFormatting.RED))
        }

        // Weather
        val raining = tile.level?.isRaining == true
        if (raining) {
            lines.add(Component.literal("  • Weather: Rain/Storm (Blocking Sun)").withStyle(ChatFormatting.RED))
        } else {
            lines.add(Component.literal("  • Weather: Clear Sky").withStyle(ChatFormatting.AQUA))
        }

        // Multiblock Structure
        if (tile.isStructureValid) {
            lines.add(Component.literal("  • Multiblock: 5x5 Pattern Formed (16/16)").withStyle(ChatFormatting.GREEN))
        } else {
            lines.add(
                Component.literal("  • Multiblock: Incomplete (${tile.cachedSidePositions.size}/16 Blocks)")
                    .withStyle(ChatFormatting.GOLD)
            )
            lines.add(Component.literal("    Requires exact 5x5x5 layout").withStyle(ChatFormatting.DARK_GRAY))
        }

        // Crafting Engine
        if (tile.isCrafting) {
            lines.add(Component.literal("  • Engine: Crafting Active").withStyle(ChatFormatting.AQUA))
        } else {
            lines.add(Component.literal("  • Engine: Idle / Waiting").withStyle(ChatFormatting.GRAY))
        }

        return lines
    }
}
