package net.modelai.website.client.gui

import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

/**
 * Screen addon providing the custom 264x180 deep navy & electric cyan background panel.
 *
 * Renders the sci-fi terminal frame, dark machine and inventory insets, and styled
 * slot backdrops for the centered player inventory.
 */
class BluePanelScreenAddon(
    private val panelWidth: Int = 264,
    private val panelHeight: Int = 180
) : BasicScreenAddon(0, 0) {

    companion object {
        const val COLOR_BG: Int = 0xFF0A192F.toInt()            // Deep Navy #0A192F
        const val COLOR_CYAN_BORDER: Int = 0xFF00D2FF.toInt()   // Electric Cyan #00D2FF
        const val COLOR_CYAN_ACCENT: Int = 0xFF005577.toInt()   // Darker Cyan Accent #005577
        const val COLOR_HEADER_DIV: Int = 0xFF143054.toInt()    // Header Divider #143054
        const val COLOR_INSET_BG: Int = 0xFF060F1D.toInt()      // Dark Navy Inset #060F1D
        const val COLOR_INSET_BORDER: Int = 0xFF10355A.toInt()  // Steel Blue Border #10355A
        const val COLOR_SLOT_BG: Int = 0xFF050C16.toInt()       // Slot Base Inset #050C16
        const val COLOR_SLOT_BORDER: Int = 0xFF1B3A57.toInt()   // Slot Border #1B3A57
        const val COLOR_SLOT_WELL: Int = 0xFF02070E.toInt()     // Slot Inner Well #02070E
        const val COLOR_SLOT_SHADOW: Int = 0xFF000307.toInt()   // Slot Shadow #000307
        const val COLOR_LABEL: Int = 0xFF5A8EA8.toInt()         // Muted Cyan Label #5A8EA8
    }

    override fun isBackground(): Boolean = true

    override fun getXSize(): Int = panelWidth

    override fun getYSize(): Int = panelHeight

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
        val x = xCenter
        val y = yCenter
        val w = panelWidth
        val h = panelHeight

        // 1. Main Outer Navy Panel (with chamfered 2px sci-fi corners)
        guiGraphics.fill(x + 2, y, x + w - 2, y + h, COLOR_BG)
        guiGraphics.fill(x, y + 2, x + 2, y + h - 2, COLOR_BG)
        guiGraphics.fill(x + w - 2, y + 2, x + w, y + h - 2, COLOR_BG)

        // 2. Electric Cyan Outer Frame
        guiGraphics.fill(x + 2, y, x + w - 2, y + 1, COLOR_CYAN_BORDER)
        guiGraphics.fill(x + 2, y + h - 1, x + w - 2, y + h, COLOR_CYAN_BORDER)
        guiGraphics.fill(x, y + 2, x + 1, y + h - 2, COLOR_CYAN_BORDER)
        guiGraphics.fill(x + w - 1, y + 2, x + w, y + h - 2, COLOR_CYAN_BORDER)

        // Corner diagonal pixels
        guiGraphics.fill(x + 1, y + 1, x + 2, y + 2, COLOR_CYAN_BORDER)
        guiGraphics.fill(x + w - 2, y + 1, x + w - 1, y + 2, COLOR_CYAN_BORDER)
        guiGraphics.fill(x + 1, y + h - 2, x + 2, y + h - 1, COLOR_CYAN_BORDER)
        guiGraphics.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, COLOR_CYAN_BORDER)

        // 3. Header Divider
        guiGraphics.fill(x + 4, y + 17, x + w - 4, y + 18, COLOR_HEADER_DIV)

        // 4. Machine Work Area Inset (x=8, y=19, w=248, h=63)
        guiGraphics.fill(x + 8, y + 19, x + w - 8, y + 82, COLOR_INSET_BG)
        guiGraphics.renderOutline(x + 8, y + 19, w - 16, 63, COLOR_INSET_BORDER)

        // 5. Player Inventory Inset (x=47, y=94, w=170, h=82)
        guiGraphics.fill(x + 47, y + 94, x + w - 47, y + 176, COLOR_INSET_BG)
        guiGraphics.renderOutline(x + 47, y + 94, 170, 82, COLOR_INSET_BORDER)

        // 6. Player Inventory Slot Boxes (3 rows x 9 columns) at (51, 98)
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                drawSlotBox(guiGraphics, x + 51 + col * 18, y + 98 + row * 18)
            }
        }

        // 7. Player Hotbar Slot Boxes (1 row x 9 columns) at (51, 156)
        for (col in 0 until 9) {
            drawSlotBox(guiGraphics, x + 51 + col * 18, y + 156)
        }
    }

    private fun drawSlotBox(guiGraphics: GuiGraphics, sx: Int, sy: Int) {
        // Outer slot border
        guiGraphics.renderOutline(sx, sy, 18, 18, COLOR_SLOT_BORDER)
        // Inner slot well
        guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, COLOR_SLOT_WELL)
        // Top and left inner shadows for authentic 3D inset depth
        guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 2, COLOR_SLOT_SHADOW)
        guiGraphics.fill(sx + 1, sy + 1, sx + 2, sy + 17, COLOR_SLOT_SHADOW)
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
        // Player inventory section title
        val font = Minecraft.getInstance().font
        guiGraphics.drawString(font, "Inv", xCenter + 35, yCenter + 85, COLOR_LABEL, false)
    }
}
