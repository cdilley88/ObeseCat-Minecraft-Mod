# Tabbyrium Energy & Harvesting System

The energy system in ObeseCat is completely overhauled with the **Tabbyrium** cycle—a Command & Conquer-inspired harvesting and refining progression rooted in Cold War nuclear fallout and feline mutation.

Universal Forge Energy (FE) is the standard power format, completely replacing the former Cat Food Energy (CF) mechanic.

---

## 1. Fat Man & The Trinity Spark

Plutonium Cat Food and Lithium Deuteride Cat Food are **no longer energy fuels or machine inputs**. They exist exclusively to feed **Fat Man**.

- **Feeding Fat Man:**
  - Standard Plutonium Cat Food: **+1 Reactive Meal**
  - Lithium Deuteride Cat Food: **+3 Reactive Meals** (Instant Detonation)
- **Critical Mass:** Reached at **3 meals**. When critical mass is achieved, an emergency klaxon sounds and a **10-second countdown** begins before a thermonuclear detonation.
- **Detonation Aftermath:** The blast excavates an enormous crater and vitrifies sand/soil into **Trinitite** glass at ground zero (mineable with a Netherite Pickaxe).

---

## 2. The Tabbyrium Blossom Tree

Using Trinitite harvested from the Trinity crater, players craft the **Tabbyrium Blossom Sapling**:

```
Crafting Recipe:
  [Trinitite]   [Rotten Flesh]   [Trinitite]
  [Rotten Flesh] [Oak Sapling]   [Rotten Flesh]
  [Trinitite]   [Rotten Flesh]   [Trinitite]
-> 1 Tabbyrium Blossom Sapling
```

- When planted and grown (or bonemealed), it forms an ominous mutated tree with a trunk composed of **Tabbyrium Stalk** (`obesecat:tabbyrium_stalk`), crowned with a pulsating, subtly breathing **Tabbyrium Spore Pod** (`obesecat:tabbyrium_spore_pod`, emitting light level 15).
- **Tabbyrium Stalk:** A rotatable mutated log block that can be chopped with an axe and crafted into 4 **Tabbyrium Planks** (`obesecat:tabbyrium_planks`).
- **Organic Spreading Lifecycle:**
  - The **Tabbyrium Stalk** leaches natural soil and stone within a 4-block radius into **Tabbyrium Crust** (`obesecat:tabbyrium_crust`).
  - The **Tabbyrium Crust** actively sprouts new **Tabbyrium Clusters** (`obesecat:tabbyrium_cluster`, Stage 0) and nurtures existing clusters to full maturity (Stage 3).
  - The **Tabbyrium Spore Pod** emits continuous airborne spores and provides maximum ambient illumination.

---

## 3. Environmental Hazards & Tabbyrium Growth

- **Biological Hazard (Vanilla Wither):**
  - **Tabbyrium Crust:** Stepping on the leached crust inflicts vanilla **Wither** (`MobEffects.WITHER`, 2 seconds).
  - **Tabbyrium Clusters:** Brushing against or walking on clusters inflicts vanilla **Wither** (3 seconds) plus magic laceration damage.
- **Growth Stages & Visuals:**
  - Tabbyrium Clusters grow through 4 distinct stages (`age` 0 to 3) with dedicated cutout models:
    - **Stage 0:** Small budding crystalline sprouts.
    - **Stage 1:** Growing multi-spike formation.
    - **Stage 2:** Prominent angular crystal cluster.
    - **Stage 3:** Towering, faceted mature Tiberium crystals laced with dark feline tabby stripes.
  - Mining mature clusters with a pickaxe drops 2–4 **Tabbyrium Shards**.
  - 9 Tabbyrium Shards can be crafted into a dense **Tabbyrium Crystal**.

---

## 4. Refining & Storage (Forge Energy)

### Tabbyrium Refinery (`obesecat:tabbyrium_refinery`)
The industrial processor that converts raw Tabbyrium into Forge Energy:
- **Internal Buffer:** 100,000 FE
- **Maximum Output:** Up to 1,000 FE/tick
- **Fuel Values:**
  - **Tabbyrium Shard:** 10,000 FE (processed over 40 ticks / 2 seconds)
  - **Tabbyrium Crystal:** 45,000 FE (processed over 120 ticks / 6 seconds)
- **6-Face Automation:** Full configurable side matrix for both `ITEM` input and `POWER` output layers.
- **Easter Egg:** Sneak-right-clicking the refinery with an empty hand triggers an EVA voice alert:
  > `[EVA] Refinery operational. Establishing battlefield control.`

### Tabbyrium Silo (`obesecat:tabbyrium_silo`)
The high-capacity power grid reservoir:
- **Storage Capacity:** 1,000,000 FE
- **Max Transfer Rate:** 10,000 FE/tick across all faces.
- **Interoperability:** Connects seamlessly with standard NeoForge FE cables, Pipez, Mekanism Universal Cables, and RFTools power cells.
- **Easter Egg:** Sneak-right-clicking the silo or clicking it when full triggers the iconic C&C klaxon:
  > `[EVA] WARNING: SILOS NEEDED.`

### Creative Tabbyrium Silo (`obesecat:creative_tabbyrium_silo`)
- A creative-only infinite energy reservoir supplying unlimited FE to any connected power consumer.

---

## 5. Consumer Tech

- **Cat Charger (`obesecat:cat_charger`):** Buffers 500,000 FE and charges items at 200 FE/tick.
- **Portable Cat Charger (`obesecat:portable_cat_charger`):** 500,000 FE inventory battery that charges held weapons and items at up to 1,000 FE/tick.
- **Skill & Summon Items:** Holy Sword, Mighty Sword, Dark Sword skills, and Magicite summons consume Forge Energy directly from player FE items.
