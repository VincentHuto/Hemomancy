# Track 8: The Living Staff and mining

**Goal:** the Staff is the Harbinger's one tool. It grows with use, takes a pickaxe form, and carries the projection
mining Shapings. It never replaces a full tool set, because it is one object, it must be summoned, and it is in one
form at a time.

**Depends on:** Track 1 (the verb toggle, `VerbReach`), Track 2 (`CrawlSession`).

## What exists

- **Bond and summon.** `LivingStaffBondHelper.unlockFromBloodStructureCraft`; `conjure_staff` (1,000 mL) →
  `LivingArsenalInventoryGuard.summonOrRecoverStaff`. One per inventory.
- **Eight forms** in `LivingStaffWeaponFormRules`: blade, axe, spear, claws, crossbow, torch, flail, sickle. Hot swap
  costs 250 mL, less 50 per Weapons Master level, floor 50. Melee has a 50% chance to cost `attackDamage × 25`.
- **Tools.** `LivingToolItem` is a `DiggerItem` mineable on `WART_BLOCKS` with `EnumModToolTiers.LIVING`
  (netherite level, speed 25). `LivingAxeItem` exists. Mining costs no blood. Durability resets on a form swap. No
  enchantments. `isCorrectToolForDrops` is dead code (Track 0).
- **Growth today** is a skill branch (`LivingStaffSkillBranch`) and grafts (`LivingWeaponForm`,
  `LivingWeaponMemoryUnlocks`). `LivingStaffProgress.bloodHandled` counts every mL the Staff has moved and nothing
  reads it.
- **Vein mining** exists as a fungal scar: Talaromyces Minus plus sneak → `VeinMinerHelper.tryVeinMine` from
  `ScarsEntityEventHandler.onBlockBreak`, unguarded (Track 0).
- **Utilities.** The Staff already carries Projection and Absorption; which one fires is decided by the selected
  utility (`LivingStaffUtilitySelectionRules`, retired by Track 1 in favour of the verb toggle).

## Target design

### 1. The Staff grows from use

`bloodHandled` becomes the Staff's age. Thresholds give **growth rings** (five to start), each a small permanent
gain, read through one pure `LivingStaffGrowthRules`:

| Ring | At (mL handled) | Gives |
|---|---|---|
| 1 | 5,000 | +0.5 verb reach |
| 2 | 25,000 | tool forms mine at iron tier → better speed |
| 3 | 100,000 | +1 verb reach, +10% channel rate |
| 4 | 300,000 | tool forms reach diamond tier |
| 5 | 750,000 | +1 verb reach, hot swap floor drops to 25 mL |

Numbers are starting points. The skill branch and grafts stay as the chosen half of growth; rings are the earned
half. The Staff's model shows its rings (a band per ring on the shaft), so a veteran's Staff looks like one.

### 2. A pickaxe form (`CL-16`)

- `LivingPickaxeItem extends LivingToolItem`, the ninth form.
- **Tier is earned, and capped.** Iron-equal at bond, diamond-equal at ring 4, never netherite. It cannot be
  enchanted: no Fortune, no Silk Touch, no Efficiency. That single rule is what keeps the vanilla pickaxe worth
  making. The axe form follows the same tier rule; today both are netherite-level from the first minute.
- **It costs blood.** Each block broken costs mL by hardness, paid through `BloodAccounting.pay` with a new `Source`.
  Out of blood, it mines at hand speed. Durability becomes meaningless for living tools; remove the reset-on-swap
  trick by removing durability from them.
- **Touch list.** `LivingStaffWeaponFormRules`, `LivingStaffWeaponFormHelper.itemForForm`,
  `LivingArsenalInventoryGuard.isLivingArsenalItem`, `ItemInit`, `ManipulationInit`, `ManipulationTreeInit`,
  `LivingWeaponForm`, `WeaponConjureRules`, `CastPresentation`, `ManipulationIconResolver`,
  `VesperManipulationCoverage`, `ItemClientExtensions`, `MixinLivingEntity`, lang, the frozen registry and creative
  tab lists. The model comes from a generator in `tools/model_export/`, hematic iron and bone, no copper.

### 3. Projection mining

Shapings, usable from the Staff in staff form or from the hand tool at shorter reach. All of them use Track 2's
`CrawlSession` and break through `PlayerBlockEdits.breakBlock`.

- **Vein draw (Ferric).** Hold Projection on an ore: the crawl follows connected blocks of the same ore tag while
  blood flows; release breaks what was reached. Harvest tier is the Staff's current tier, so it cannot pull diamonds
  at ring 1. Drops are plain (no Fortune). Cost per block is higher than the pickaxe form's: it trades blood for
  time.
- **Shaped mining (Ferric, later rank).** Projection cuts a set shape from the struck face: a 1×2 tunnel, a 3×3
  face, a stair. The shape is chosen in the Shaping's radial detail, not by a new key. Only blocks the Staff's tier
  could mine; stops at anything with a block entity.
- **Vascular Dowsing** (from Track 2's second wave) pairs with these: veins run from the struck face toward ore in
  reach, and Vein draw follows them.
- **Talaromyces Minus** keeps its scar-and-sneak vein mine, routed through the same `CrawlSession` so there is one
  vein miner with one set of rules and guards.

### 4. Form and verb together

- Staff form carries the verbs at full reach. Weapon and tool forms carry them at base reach, or not at all for the
  crossbow. Selecting a Shaping in a weapon form returns the Staff to staff form (existing behaviour, kept).
- The hot-swap cost is the price of being a multi-tool. Keep it; it is the reason a player still carries a sword or a
  pickaxe for sustained work.

## What changes, file by file

| File | Change |
| --- | --- |
| New `LivingStaffGrowthRules` (pure); `LivingStaffProgress` | Rings from `bloodHandled`. |
| `VerbReach` | Reads rings. |
| New `LivingPickaxeItem`; the touch list above | The form. |
| `LivingToolItem`, `LivingAxeItem`, `EnumModToolTiers` | Earned tier, blood per block, no durability, fix `isCorrectToolForDrops`. |
| New Shapings under `common/manipulation/ferric/` | Vein draw, shaped mining. |
| `VeinMinerHelper`, `ScarsEntityEventHandler` | Through `CrawlSession` and `PlayerBlockEdits`. |
| Staff model generator and renderer | Rings. |

## Tests

- New: `LivingStaffGrowthRulesTest`, a `staff_mining_validation` GameTest batch (tier by ring; blood per block;
  vein draw respects tier and spawn protection; shaped mining stops at a block entity).
- Rewrite: `LivingArsenalInventoryGuardSourceTest`, `LivingStaffWeaponFormRulesTest`,
  `LivingStaffArmamentSourceTest`, `WeaponConjureRulesTest`.
- Guardrails: `PlayerBlockEditGuardrailTest`, `BloodAccountingGuardrailTest`.

## Hazards

- **Nerf on paper.** The living axe drops from netherite-level to iron-level at bond. It is the right call for the
  mod's place beside vanilla, and the rings give it back, but it changes how early Harbingers feel. Say it in the
  design, not in a patch note.
- **Vein mining is a solved feature in other mods.** The point of doing it as a Shaping is that it costs blood per
  block, respects tier, and looks like the mod. If it is strictly better than a Fortune pickaxe, it has failed.
- **Vesper.** `VesperManipulationCoverage` lists every manipulation Vesper can answer. New Ferric Shapings are
  player-only; list them as such.

## Open questions

`CL-16`.

## Acceptance

- A bonded Harbinger can mine iron with the Staff on day one and diamonds after real use, for blood, without
  enchantments.
- A player with a Fortune III pickaxe still has a reason to carry it.
- The Staff visibly ages.
