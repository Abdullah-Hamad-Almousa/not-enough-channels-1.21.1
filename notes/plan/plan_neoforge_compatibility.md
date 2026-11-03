# Plan: NeoForge Version Compatibility (Supporting 21.1.x / 21.1.250+)

## 📌 Context
Currently, the mod requires `21.1.265` at runtime because in `src/main/templates/META-INF/neoforge.mods.toml`, the NeoForge dependency specifies:
```toml
versionRange="[${neo_version},)"
```
Since `neo_version=21.1.265` in `gradle.properties`, the built JAR tells NeoForge to reject any NeoForge version below `21.1.265`.

---

## ❓ Can the mod work on older versions like `21.1.250` or `21.1.x`?
**Yes! 100%.**
The mod only uses standard NeoForge items, events, and Minecraft registries. Nothing in the code is exclusive to 21.1.265.

---

## 💡 How Version Ranges Work in NeoForge:
- `[21.1.0, 21.2.0)`: Matches **any** 1.21.1 NeoForge build (`21.1.1` to `21.1.999`), but stops at 1.21.2. This is the **standard recommended range** for 1.21.1 mods.
- `[21.1.250,)`: Matches `21.1.250` and newer.
- `[21.1.0,)`: Matches `21.1.0` and newer.

---

## 🛠️ The 3 Small Steps to Apply (When Ready)

### 1. `gradle.properties`:
Add:
```properties
neo_version_range=[21.1.0, 21.2.0)
```

### 2. `build.gradle`:
In the `generateModMetadata` task, add:
```groovy
neo_version_range: neo_version_range,
```

### 3. `src/main/templates/META-INF/neoforge.mods.toml`:
Change line 72 from:
```toml
versionRange="[${neo_version},)"
```
to:
```toml
versionRange="${neo_version_range}"
```
