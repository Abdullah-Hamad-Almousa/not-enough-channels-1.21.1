package net.modelai.website.crafting

import net.minecraft.world.item.Item
import java.util.function.Supplier

object LightningCrafting {

    data class Recipe(
        val tier: Int,
        val input: Supplier<out Item>,
        val output: Supplier<out Item>
    )

    private val RECIPES = mutableListOf<Recipe>()

    fun register(tier: Int, input: Supplier<out Item>, output: Supplier<out Item>) {
        RECIPES.add(Recipe(tier, input, output))
    }

    fun register(input: Supplier<out Item>, output: Supplier<out Item>) {
        register(1, input, output)
    }

    fun getResult(input: Item, tier: Int): Item? {
        return RECIPES.firstOrNull() { it.tier == tier && it.input.get() == input }?.output?.get()
    }

    fun hasRecipe(input: Item): Boolean {
        return RECIPES.any { it.input.get() == input}
    }

}