package net.modelai.website.crafting

import com.hrznstudio.titanium.component.fluid.FluidTankComponent
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Supplier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.material.Fluid
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.items.IItemHandler

/**
 * Lazy item ingredient wrapper.
 * Defers [item.get] resolution until recipe matching to prevent premature registry access crashes.
 */
class SolarItemIngredient(val item: Supplier<out Item>) {
    fun test(stack: ItemStack): Boolean {
        return !stack.isEmpty && stack.item == item.get()
    }

    fun getItem(): Item = item.get()
}

/**
 * Lazy fluid ingredient wrapper.
 * [amountMb] must be between 1 and 1000 mB.
 */
class SolarFluidIngredient(val fluid: Supplier<out Fluid>, val amountMb: Int) {
    init {
        require(amountMb in 1..1000) { "Fluid ingredient amount must be between 1 and 1000 mB, got $amountMb" }
    }

    fun test(stack: FluidStack): Boolean {
        return !stack.isEmpty && stack.fluid == fluid.get() && stack.amount >= amountMb
    }

    fun getFluid(): Fluid = fluid.get()
}

/**
 * Lazy item output wrapper.
 * [count] must be between 1 and 64.
 */
class SolarItemOutput(val item: Supplier<out Item>, val count: Int = 1) {
    init {
        require(count in 1..64) { "Item output count must be between 1 and 64, got $count" }
    }

    fun createStack(): ItemStack = ItemStack(item.get(), count)

    fun getItem(): Item = item.get()
}

/**
 * Lazy fluid output wrapper.
 * [amountMb] must be between 1 and 1000 mB.
 */
class SolarFluidOutput(val fluid: Supplier<out Fluid>, val amountMb: Int) {
    init {
        require(amountMb in 1..1000) { "Fluid output amount must be between 1 and 1000 mB, got $amountMb" }
    }

    fun createStack(): FluidStack = FluidStack(fluid.get(), amountMb)

    fun getFluid(): Fluid = fluid.get()
}

// DSL Builder Functions
fun solarItem(item: Supplier<out Item>): SolarItemIngredient = SolarItemIngredient(item)
fun solarItem(item: Item): SolarItemIngredient = SolarItemIngredient(Supplier { item })

fun solarFluid(fluid: Supplier<out Fluid>, amountMb: Int): SolarFluidIngredient = SolarFluidIngredient(fluid, amountMb)
fun solarFluid(fluid: Fluid, amountMb: Int): SolarFluidIngredient = SolarFluidIngredient(Supplier { fluid }, amountMb)

fun solarOutput(item: Supplier<out Item>, count: Int = 1): SolarItemOutput = SolarItemOutput(item, count)
fun solarOutput(item: Item, count: Int = 1): SolarItemOutput = SolarItemOutput(Supplier { item }, count)

fun solarFluidOutput(fluid: Supplier<out Fluid>, amountMb: Int): SolarFluidOutput = SolarFluidOutput(fluid, amountMb)
fun solarFluidOutput(fluid: Fluid, amountMb: Int): SolarFluidOutput = SolarFluidOutput(Supplier { fluid }, amountMb)

/**
 * Result of matching input slots and tanks against a [SolarRecipe].
 * Contains exact slot indices to allow O(1) ingredient consumption during craft completion.
 */
data class MatchResult(
    val recipe: SolarRecipe,
    val matchedItemSlots: List<Int>,
    val matchedFluidTanks: List<Int>
)

/**
 * Definition of a craftable recipe in the Floating Multiblock Solar Machine.
 */
data class SolarRecipe(
    val itemInputs: List<SolarItemIngredient> = emptyList(),
    val fluidInputs: List<SolarFluidIngredient> = emptyList(),
    val itemOutputs: List<SolarItemOutput> = emptyList(),
    val fluidOutput: SolarFluidOutput? = null,
    val durationTicks: Int,
    val energyPerTick: Int
) {
    init {
        require(itemInputs.isNotEmpty() || fluidInputs.isNotEmpty()) {
            "Recipe must have at least one input (item or fluid)"
        }
        require(itemInputs.size <= 10) {
            "Recipe can have at most 10 item inputs, got ${itemInputs.size}"
        }
        require(fluidInputs.size <= 3) {
            "Recipe can have at most 3 fluid inputs, got ${fluidInputs.size}"
        }
        require(itemOutputs.size <= 2) {
            "Recipe can have at most 2 item outputs, got ${itemOutputs.size}"
        }
        require(itemOutputs.isNotEmpty() || fluidOutput != null) {
            "Recipe must have at least one output (item or fluid)"
        }
        require(durationTicks > 0) {
            "durationTicks must be > 0, got $durationTicks"
        }
        require(energyPerTick >= 0) {
            "energyPerTick must be >= 0, got $energyPerTick"
        }
    }

    /**
     * Checks if the given raw input item stacks and fluid stacks match this recipe.
     * Enforces exact shapeless matching for items (all non-empty item slots must be consumed 1-to-1).
     * Enforces that each fluid ingredient finds a distinct tank with sufficient fluid.
     */
    fun matchesInputs(inputStacks: List<ItemStack>, inputFluids: List<FluidStack>): MatchResult? {
        // 1. Exact Item Matching
        val nonEmptyIndices = mutableListOf<Int>()
        for (i in inputStacks.indices) {
            if (!inputStacks[i].isEmpty) {
                nonEmptyIndices.add(i)
            }
        }

        if (nonEmptyIndices.size != itemInputs.size) {
            return null
        }

        val matchedItemSlots = if (itemInputs.isEmpty()) {
            emptyList()
        } else {
            val assigned = IntArray(itemInputs.size) { -1 }
            val used = BooleanArray(inputStacks.size)

            fun backtrackItem(ingredientIndex: Int): Boolean {
                if (ingredientIndex == itemInputs.size) return true
                val ingredient = itemInputs[ingredientIndex]
                for (slotIdx in nonEmptyIndices) {
                    if (!used[slotIdx] && ingredient.test(inputStacks[slotIdx])) {
                        used[slotIdx] = true
                        assigned[ingredientIndex] = slotIdx
                        if (backtrackItem(ingredientIndex + 1)) return true
                        used[slotIdx] = false
                    }
                }
                return false
            }

            if (!backtrackItem(0)) return null
            assigned.toList()
        }

        // 2. Fluid Matching
        val matchedFluidTanks = if (fluidInputs.isEmpty()) {
            emptyList()
        } else {
            val assigned = IntArray(fluidInputs.size) { -1 }
            val used = BooleanArray(inputFluids.size)

            fun backtrackFluid(fluidIndex: Int): Boolean {
                if (fluidIndex == fluidInputs.size) return true
                val ingredient = fluidInputs[fluidIndex]
                for (tankIdx in inputFluids.indices) {
                    if (!used[tankIdx] && ingredient.test(inputFluids[tankIdx])) {
                        used[tankIdx] = true
                        assigned[fluidIndex] = tankIdx
                        if (backtrackFluid(fluidIndex + 1)) return true
                        used[tankIdx] = false
                    }
                }
                return false
            }

            if (!backtrackFluid(0)) return null
            assigned.toList()
        }

        return MatchResult(this, matchedItemSlots, matchedFluidTanks)
    }

    /**
     * Checks whether the machine's output slots and fluid output tank can accommodate the recipe's products.
     */
    fun canAcceptOutputs(
        outputStacks: List<ItemStack>,
        currentFluidOutput: FluidStack,
        fluidTankCapacity: Int = 1000
    ): Boolean {
        // Verify fluid output capacity
        if (fluidOutput != null) {
            val reqFluid = fluidOutput.fluid.get()
            val reqAmount = fluidOutput.amountMb
            if (currentFluidOutput.isEmpty) {
                if (reqAmount > fluidTankCapacity) return false
            } else {
                if (currentFluidOutput.fluid != reqFluid) return false
                if (currentFluidOutput.amount + reqAmount > fluidTankCapacity) return false
            }
        }

        // Simulate item output insertion into the available output slots
        if (itemOutputs.isNotEmpty()) {
            val simulated = outputStacks.map { it.copy() }.toMutableList()
            for (out in itemOutputs) {
                var remaining = out.count
                val outItem = out.item.get()
                val maxStack = minOf(64, ItemStack(outItem).maxStackSize)

                // Pass 1: add to slots already holding the same item
                for (i in simulated.indices) {
                    if (remaining <= 0) break
                    val stack = simulated[i]
                    if (!stack.isEmpty && stack.item == outItem) {
                        val space = maxStack - stack.count
                        if (space > 0) {
                            val toAdd = minOf(space, remaining)
                            stack.grow(toAdd)
                            remaining -= toAdd
                        }
                    }
                }

                // Pass 2: add to empty slots
                for (i in simulated.indices) {
                    if (remaining <= 0) break
                    val stack = simulated[i]
                    if (stack.isEmpty) {
                        val toAdd = minOf(maxStack, remaining)
                        simulated[i] = ItemStack(outItem, toAdd)
                        remaining -= toAdd
                    }
                }

                if (remaining > 0) return false
            }
        }

        return true
    }

    // Interop helpers for Titanium components
    fun matchesInputs(inventory: IItemHandler, fluidTanks: List<FluidTankComponent<*>>): MatchResult? {
        val inputStacks = (0 until inventory.slots).map { inventory.getStackInSlot(it) }
        val inputFluids = fluidTanks.map { it.fluid }
        return matchesInputs(inputStacks, inputFluids)
    }

    fun canAcceptOutputs(outputInventory: IItemHandler, outputTank: FluidTankComponent<*>): Boolean {
        val outputStacks = (0 until outputInventory.slots).map { outputInventory.getStackInSlot(it) }
        return canAcceptOutputs(outputStacks, outputTank.fluid, outputTank.capacity)
    }
}

/**
 * Central registry for all Floating Multiblock Solar Machine recipes.
 */
object SolarMachineCrafting {
    private val RECIPES = CopyOnWriteArrayList<SolarRecipe>()

    fun register(recipe: SolarRecipe): SolarRecipe {
        RECIPES.add(recipe)
        sortRecipes()
        return recipe
    }

    fun register(
        itemInputs: List<SolarItemIngredient> = emptyList(),
        fluidInputs: List<SolarFluidIngredient> = emptyList(),
        itemOutputs: List<SolarItemOutput> = emptyList(),
        fluidOutput: SolarFluidOutput? = null,
        durationTicks: Int,
        energyPerTick: Int
    ): SolarRecipe {
        val recipe = SolarRecipe(
            itemInputs = itemInputs,
            fluidInputs = fluidInputs,
            itemOutputs = itemOutputs,
            fluidOutput = fluidOutput,
            durationTicks = durationTicks,
            energyPerTick = energyPerTick
        )
        return register(recipe)
    }

    private fun sortRecipes() {
        val sorted = RECIPES.sortedWith(
            compareByDescending<SolarRecipe> { it.itemInputs.size + it.fluidInputs.size }
                .thenByDescending { it.itemInputs.size }
                .thenByDescending { it.durationTicks }
        )
        RECIPES.clear()
        RECIPES.addAll(sorted)
    }

    fun getRecipes(): List<SolarRecipe> = RECIPES.toList()

    fun findMatchingRecipe(
        inputStacks: List<ItemStack>,
        inputFluids: List<FluidStack>
    ): MatchResult? {
        for (recipe in RECIPES) {
            val match = recipe.matchesInputs(inputStacks, inputFluids)
            if (match != null) return match
        }
        return null
    }

    fun findMatchingRecipe(
        inputInventory: IItemHandler,
        inputTanks: List<FluidTankComponent<*>>
    ): MatchResult? {
        val inputStacks = (0 until inputInventory.slots).map { inputInventory.getStackInSlot(it) }
        val inputFluids = inputTanks.map { it.fluid }
        return findMatchingRecipe(inputStacks, inputFluids)
    }
}
