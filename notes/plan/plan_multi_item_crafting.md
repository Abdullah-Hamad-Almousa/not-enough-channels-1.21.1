# Implementation Plan: Universal Multi-Item Lightning Crafting with Item Counts

## 📌 Features Supported
- **1-to-1 Recipes**: `LightningCrafting.register(1, input, output)`
- **Multi-Item Recipes with Counts**:
  ```kotlin
  LightningCrafting.register(
      tier = 3,
      inputs = listOf(
          ingredient(Items.APPLE, count = 1),
          ingredient(Items.HONEYCOMB, count = 2)
      ),
      outputs = listOf(
          output(Items.GOAT_HORN, count = 1),
          output(Items.HONEYCOMB, count = 1)
      )
  )
  ```
- **Mod Items with Counts**:
  `ingredient(ModItems.CHANNEL_APPLE, count = 2)`
- **Raw Suppliers (Count defaults to 1)**:
  `inputs = listOf(Supplier { Items.STICK }, Supplier { Items.CONDUIT })`

---

## 🛠️ Implementation Details

### Helper Functions in `net.modelai.website.crafting`:
- `ingredient(item: ItemLike, count: Int = 1)`
- `ingredient(supplier: Supplier<out Item>, count: Int = 1)`
- `output(item: ItemLike, count: Int = 1)`
- `output(supplier: Supplier<out Item>, count: Int = 1)`

### Imports needed:
```kotlin
import net.modelai.website.crafting.ingredient
import net.modelai.website.crafting.output
```
Or call directly via `LightningCrafting.ingredient(...)` and `LightningCrafting.output(...)`.
