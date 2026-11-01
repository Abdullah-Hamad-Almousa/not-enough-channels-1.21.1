# Implementation Plan: Multi-Item Crafting Recipe Code Syntax

## 📌 Context
In Kotlin and Java, `Items.APPLE && Items.HONEYCOMB` cannot compile because `&&` is a boolean logical operator (true/false) and cannot combine Minecraft `Item` objects.

To specify multiple ingredients (like Apple AND Honeycomb), we use collections (`listOf(...)`), variable arguments (`vararg`), or clean helper functions.

---

## 🎨 Recommended Syntax Designs

### Design 1: Standard `listOf(...)` (Clean & Idiomatic Kotlin)
Using Kotlin's built-in `listOf()`:

```kotlin
// Multi-input to Single output:
LightningCrafting.register(
    tier = 3,
    inputs = listOf(Supplier { Items.APPLE }, Supplier { Items.HONEYCOMB }),
    output = Supplier { Items.CONDUIT }
)

// When using ModItems (ModItems are already Suppliers, no wrapper needed!):
LightningCrafting.register(
    tier = 3,
    inputs = listOf(ModItems.CHANNEL_APPLE, Supplier { Items.HONEYCOMB }),
    output = Supplier { Items.CONDUIT }
)
```

---

### Design 2: Helper Functions `items(...)` (Ultra-Clean, No Boilerplate)
By defining smart helper overloads, we can allow passing vanilla `Item` or `DeferredItem` directly without writing `Supplier { ... }`:

```kotlin
LightningCrafting.register(
    tier = 3,
    inputs = items(Items.APPLE, Items.HONEYCOMB),
    output = item(Items.CONDUIT)
)
```

With mod items:
```kotlin
LightningCrafting.register(
    tier = 3,
    inputs = items(ModItems.CHANNEL_APPLE, Items.HONEYCOMB),
    output = item(Items.CONDUIT)
)
```

---

### Design 3: Supporting Item Counts (e.g. 1 Apple + 2 Honeycombs)
When recipes require specific stack sizes:

```kotlin
LightningCrafting.register(
    tier = 3,
    inputs = listOf(
        ingredient(Items.APPLE, count = 1),
        ingredient(Items.HONEYCOMB, count = 2)
    ),
    outputs = listOf(
        output(Items.CONDUIT, count = 1)
    )
)
```

---

## 💻 Exact Implementation of the Helper Methods

In [`LightningCrafting.kt`](file:///A:/IDE/Intellija/not-enough-channels-1.21.1/src/main/kotlin/net/modelai/website/crafting/LightningCrafting.kt):

```kotlin
package net.modelai.website.crafting

import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import java.util.function.Supplier

object LightningCrafting {

    data class Ingredient(val item: Supplier<out Item>, val count: Int = 1)
    data class Output(val item: Supplier<out Item>, val count: Int = 1)

    data class Recipe(
        val tier: Int,
        val inputs: List<Ingredient>,
        val outputs: List<Output>
    )

    private val RECIPES = mutableListOf<Recipe>()

    // Core registration
    fun register(tier: Int, inputs: List<Ingredient>, outputs: List<Output>) {
        RECIPES.add(Recipe(tier, inputs, outputs))
        RECIPES.sortByDescending { it.inputs.size }
    }

    // Convenience 1: Multiple Suppliers to Single Supplier Output
    fun register(tier: Int, inputs: List<Supplier<out Item>>, output: Supplier<out Item>) {
        register(tier, inputs.map { Ingredient(it, 1) }, listOf(Output(output, 1)))
    }

    // Convenience 2: Single Input to Single Output (Tier optional, defaults to 1)
    fun register(tier: Int = 1, input: Supplier<out Item>, output: Supplier<out Item>) {
        register(tier, listOf(input), output)
    }

    // Helper conversion functions so you never need to type `Supplier { ... }` manually:
    fun item(item: ItemLike): Supplier<out Item> = Supplier { item.asItem() }
    fun items(vararg items: ItemLike): List<Supplier<out Item>> = items.map { item(it) }
    fun items(vararg suppliers: Supplier<out Item>): List<Supplier<out Item>> = suppliers.toList()

    fun ingredient(item: ItemLike, count: Int = 1) = Ingredient(Supplier { item.asItem() }, count)
    fun ingredient(supplier: Supplier<out Item>, count: Int = 1) = Ingredient(supplier, count)

    fun output(item: ItemLike, count: Int = 1) = Output(Supplier { item.asItem() }, count)
    fun output(supplier: Supplier<out Item>, count: Int = 1) = Output(supplier, count)

    fun getRecipesForTier(tier: Int): List<Recipe> = RECIPES.filter { it.tier == tier }
    fun isIngredient(item: Item): Boolean = RECIPES.any { r -> r.inputs.any { it.item.get() == item } }
}
```
