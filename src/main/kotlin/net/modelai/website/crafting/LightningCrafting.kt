package net.modelai.website.crafting

import net.minecraft.world.item.Item
import java.util.function.Supplier

object LightningCrafting {

    private val RECIPES = mutableListOf<Pair<Supplier<out Item>, Supplier<out Item>>>()

    fun register(input: Supplier<out Item>, output: Supplier<out Item>) {
        RECIPES.add(input to output)
    }

    fun getResult(input: Item): Item? {
        return RECIPES.firstOrNull { it.first.get() == input }?.second?.get()
    }

    fun hasRecipe(input: Item): Boolean {
        return RECIPES.any { it.first.get() == input }
    }
}