# Plan: 5x5x5 Multiblock Pattern Migration

Completely remove the legacy loose random counting method and replace it with an **exact 5x5x5 geometric pattern matcher** for the Floating Solar Machine (`SolarMultiblockTileEntity`), featuring 16 precisely positioned `SideStructureBlock` locations with bidirectional ($0^\circ$ and $90^\circ$) rotational alignment.

---

## 📐 The Exact 5x5x5 Multiblock Layout

The Controller block **B** (`SolarMultiblockMachineBlock`) floats at the center of Layer 3 at relative coordinates $(0, 0, 0)$. The 16 side structure blocks **A** (`SideStructureBlock`) are arranged across Layers 1 through 4. Layer 5 directly above is open to the sky.

### Layer-by-Layer Coordinate Mapping

```text
Layer 1 (Y = -2, Bottom)         Layer 2 (Y = -1)                Layer 3 (Y = 0, Center)
Z\X -2 -1  0 +1 +2               Z\X -2 -1  0 +1 +2              Z\X -2 -1  0 +1 +2
-2   x  x  x  x  x               -2   x  x  A  x  x              -2   A  x  x  x  A
-1   A  x  x  x  A               -1   x  x  x  x  x              -1   x  x  x  x  x
 0   x  A  x  A  x                0   x  x  x  x  x               0   x  x  B  x  x
+1   A  x  x  x  A               +1   x  x  x  x  x              +1   x  x  x  x  x
+2   x  x  x  x  x               +2   x  x  A  x  x              +2   A  x  x  x  A
(6 Side Blocks)                  (2 Side Blocks)                 (4 Side Blocks + Controller B)

Layer 4 (Y = +1)                 Layer 5 (Y = +2, Top)
Z\X -2 -1  0 +1 +2               Z\X -2 -1  0 +1 +2
-2   x  x  A  x  x               -2   x  x  x  x  x
-1   x  x  x  x  x               -1   x  x  x  x  x
 0   A  x  x  x  A                0   x  x  x  x  x  <-- Open sky above Controller B
+1   x  x  x  x  x               +1   x  x  x  x  x
+2   x  x  A  x  x               +2   x  x  x  x  x
(4 Side Blocks)                  (0 Blocks / Skylight)
```

**Total Required Structure Blocks**: $6 + 2 + 4 + 4 = 16$ blocks of **A**.

---

## 🧭 Rotational Symmetry & Invariant Math

Because every layer in this design possesses natural **$180^\circ$ point reflection symmetry** ($(-x, -z) \equiv (x, z)$):
- **Orientation 0 (North-South / Primary)**: Exactly as defined in the schematic above.
- **Orientation 1 (East-West / Rotated $90^\circ$)**: Coordinate transformation: $(x' = -z, z' = x)$.

By checking both orientations during pattern validation:
- Players can build the multiblock facing **North-South OR East-West** without failure.
- If neither orientation is $100\%$ complete, the controller tracks the orientation with the higher match count (e.g. `14/16 blocks placed`) for the GUI status badge and tooltip.
- Non-structure spaces (`x`) are unconstrained so players can run cables, fluid pipes, or automation through the empty spaces of the $5 \times 5 \times 5$ volume.

---

## 🛠️ Code Changes to Apply

### 1. `src/main/kotlin/net/modelai/website/block/entity/SolarMultiblockTileEntity.kt`
- **Remove** the old loose bounding box loop (`worldPosition.offset(-4, -4, -4)` to `worldPosition.offset(4, 4, 4)`).
- **Add** `PATTERN_OFFSETS_NS` and `PATTERN_OFFSETS_EW` to the `companion object`:
  ```kotlin
  companion object {
      val PATTERN_OFFSETS_NS = listOf(
          // Layer 1 (dy = -2)
          BlockPos(-2, -2, -1), BlockPos(2, -2, -1),
          BlockPos(-1, -2, 0),  BlockPos(1, -2, 0),
          BlockPos(-2, -2, 1),  BlockPos(2, -2, 1),
          // Layer 2 (dy = -1)
          BlockPos(0, -1, -2),  BlockPos(0, -1, 2),
          // Layer 3 (dy = 0)
          BlockPos(-2, 0, -2),  BlockPos(2, 0, -2),
          BlockPos(-2, 0, 2),   BlockPos(2, 0, 2),
          // Layer 4 (dy = 1)
          BlockPos(0, 1, -2),
          BlockPos(-2, 1, 0),   BlockPos(2, 1, 0),
          BlockPos(0, 1, 2)
      )

      val PATTERN_OFFSETS_EW = PATTERN_OFFSETS_NS.map { pos ->
          BlockPos(-pos.z, pos.y, pos.x)
      }
  }
  ```
- **Update** `updateStructure(lvl: Level)` to evaluate both orientations against `ModBlocks.SIDE_STRUCTURE_BLOCK.get()`, select the orientation with the highest match count, set `isStructureValid = (matched.size == 16)`, and store the matched positions in `cachedSidePositions`.

### 2. `src/main/kotlin/net/modelai/website/block/custom/SideStructureBlock.kt`
- **Update** `notifyNearbyControllers` from $\pm 4$ bounds to the tight $5 \times 5 \times 5$ controller relative bounds:
  ```kotlin
  for (checkPos in BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 2, 2))) {
      val be = level.getBlockEntity(checkPos)
      if (be is SolarMultiblockTileEntity) {
          be.markStructureDirty()
      }
  }
  ```

### 3. `src/main/kotlin/net/modelai/website/client/gui/SolarStatusScreenAddon.kt`
- Fix the foreground coordinate translation (`val x = posX; val y = posY`) so the status pill badge renders inside the GUI header box.
- Update the structure hover tooltip to indicate `5x5 Pattern Formed (16/16)` or `Incomplete (X/16 Blocks) - Requires exact 5x5 layout`.

---

## ✅ Verification Gate
- Run `.\gradlew.bat compileKotlin` and `.\gradlew.bat build -x test` with 0 errors.
- Verify random side block placement does not form the multiblock, while exact North-South and East-West 5x5x5 placements form `16/16`.
