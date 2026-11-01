package net.modelai.website.crafting

import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import java.util.function.Supplier

data class CraftingItem(
    val item: Supplier<out Item>,
    val count: Int = 1
)

fun ingredient(item: ItemLike, count: Int = 1): CraftingItem =
    CraftingItem(Supplier { item.asItem() }, count)

fun ingredient(supplier: Supplier<out Item>, count: Int = 1): CraftingItem =
    CraftingItem(supplier, count)

fun output(item: ItemLike, count: Int = 1): CraftingItem =
    CraftingItem(Supplier { item.asItem() }, count)

fun output(supplier: Supplier<out Item>, count: Int = 1): CraftingItem =
    CraftingItem(supplier, count)

object LightningCrafting {

    data class Recipe(
        val tier: Int,
        val inputs: List<CraftingItem>,
        val outputs: List<CraftingItem>
    )

    private val RECIPES = mutableListOf<Recipe>()

    /**
     * Multiple inputs with count to multiple outputs with count
     */
    fun register(
        tier: Int,
        inputs: List<CraftingItem>,
        outputs: List<CraftingItem>
    ) {
        RECIPES.add(Recipe(tier, inputs, outputs))
        RECIPES.sortByDescending { it.inputs.size }
    }

    /**
     * Overload for raw Suppliers (count defaults to 1)
     */
    @JvmName("registerSuppliers")
    fun register(
        tier: Int,
        inputs: List<Supplier<out Item>>,
        outputs: List<Supplier<out Item>>
    ) {
        register(
            tier,
            inputs.map { CraftingItem(it, 1) },
            outputs.map { CraftingItem(it, 1) }
        )
    }

    /**
     * Multiple inputs to single output
     */
    fun register(
        tier: Int,
        inputs: List<Supplier<out Item>>,
        output: Supplier<out Item>
    ) {
        register(tier, inputs, listOf(output))
    }

    /**
     * Single input to single output with tier
     */
    fun register(
        tier: Int = 1,
        input: Supplier<out Item>,
        output: Supplier<out Item>
    ) {
        register(tier, listOf(input), listOf(output))
    }

    /**
     * Backwards-compatible single input to single output (defaults to Tier 1)
     */
    fun register(
        input: Supplier<out Item>,
        output: Supplier<out Item>
    ) {
        register(1, listOf(input), listOf(output))
    }

    // Helper functions on the object itself as well
    fun ingredient(item: ItemLike, count: Int = 1): CraftingItem = net.modelai.website.crafting.ingredient(item, count)
    fun ingredient(supplier: Supplier<out Item>, count: Int = 1): CraftingItem = net.modelai.website.crafting.ingredient(supplier, count)
    fun output(item: ItemLike, count: Int = 1): CraftingItem = net.modelai.website.crafting.output(item, count)
    fun output(supplier: Supplier<out Item>, count: Int = 1): CraftingItem = net.modelai.website.crafting.output(supplier, count)

    fun getRecipesForTier(tier: Int): List<Recipe> {
        return RECIPES.filter { it.tier == tier }
    }

    fun isIngredient(item: Item): Boolean {
        return RECIPES.any { r -> r.inputs.any { it.item.get() == item } }
    }

    fun hasRecipe(item: Item): Boolean {
        return isIngredient(item)
    }
}
