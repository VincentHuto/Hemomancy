# Hemomancy mob drop consolidation handoff

Compiled 2026-10-06 from the decisions in this conversation. This is a continuation brief: accepted design does not mean every loot table, recipe or rename has already been implemented. Source observations below are a focused spot-check, not a complete mob/drop audit.

## Goal and boundaries

Give the growing Overworld, Pelagic, Nether and End mob rosters useful material roles without creating a unique ingredient for every ambient creature. Share ordinary materials through biological function: armor, barbs, color, restraint, nerves, feathers, venom, food and cleansing fluids. Keep each mob's habitat and behavior distinct.

Keep special anatomical drops rare and special, including the Scorpion's Telson, Fargone's Proboscis and the Chthonian Queen's egg organ (called the “prostogysm egg organ” in the conversation; verify its canonical spelling and registry ID). Shared materials must not erase these unique organs or make their progression roles trivial.

## Accepted shared material decisions

| Shared material / name | Intended sources | Decision and rationale |
| --- | --- | --- |
| **Chitinous Husk** | Chthonians, aquatic Chitons, Chitinite, Bombardier Beetles; Peacock Spiders rarely | General armored arthropod/exoskeleton material. The initial suggestion was “Chitonous Husk”; current source already uses **Chitinous Husk**, the working spelling for continuation. Audit related armored mobs for suitable additions. |
| **Chromatophores** | Prism Cuttle/cuttlefish, Bloody-Belly Comb Jellies, Luminal Cicadas | Share the color/light material beyond cuttlefish. A source-neutral display name is the intended direction; exact final wording beyond “Chromatophores” was not separately settled. |
| **Barbed Splinters** | Barbed Urchins, Excoriated, Venous Striders, Vampire Bats, Vampire Squids | Consolidate calcified blood spines into a general sharp/barbed material. Excoriated supply arrow-like barbs, striders have grasping talons, bats supply fangs, squid supply spikes on their membranes. Vampire Bats should supply this rather than a membrane. Current singular item display name is **Barbed Splinter**. |
| **Enthralling Filament** | Peacock Spiders, Hemojellies; relevant possession/restraint mobs after audit | Explicit replacement name for “Binding Filament,” covering both mental enthrallment and physical ensnaring. Peacock Spiders supply filament as their normal material and husks rarely. Hemojellies should have longer capturing tentacles to support this drop. No complete possession-mob roster was settled in the visible conversation. |
| **Ganglions** | Siphonophores, alongside appropriate existing neural sources | Siphonophores should have a Ductilis theme and zapping tentacles, providing a shared neural material. Confirm whether ordinary `ganglion_cluster` is the correct existing item; do not substitute the unique Naeglerophaeon organ without a separate decision. |
| **Venous Pinions** | Venous Striders, Choir Keepers, Osteophage Vultures | Shared feather material across three different bird niches and dimensions. Add Choir Keepers to the pinion sources. |
| **Sanguine Formation** | Crimson Does, alongside existing sources | Passive deer should provide an ordinary sanguine material plus usable food. |
| **Cleansing Hemolymph** | Hemolymphopoda, Ice Fish | Ice Fish use the existing cleansing material **for now**. Their hemoglobin-free blood supports the Unstained/purity theme rather than requiring another unique resource. |
| **Congetio blood samples** | Goats, Polar Bears, Foxes, Strays, Rabbits and suitable cold-associated mobs | Provide more accessible cold-associated blood sources. Preserve the request as blood-profile/sample work; delivery through death loot versus the existing blood-sampling system was not resolved. Check the project's exact tendency/sample spelling before editing IDs. |

### Venom consolidation: direction accepted, details open

Add **Bloodworms** to tidepools and other shallow/low-level aquatic environments as an accessible venom source when Venom-Rib Centipedes are unavailable. The proposed consolidation is one shared venom material obtainable from bloodworms, scorpions and centipedes, potentially with processing routes from Telsons and Toxicognaths.

The shared venom item's name, direct drops, processing yields and exact recipes are **not settled**. Preserve rare Telsons and other special organs. Do not silently replace every Telson or Toxicognath drop with generic venom; decide whether ordinary venom drops coexist with rare organs, whether organs can be processed, and which recipes really need a unique organ.

### Sclerite progression

Chalybeate Sclerites have become rarer. Review their material value, consumption amounts and progression gates rather than treating them as a plentiful general ingredient. Certain recipes should be refactored around the new availability. No exact price, recipe quantities or approved recipe migration list was agreed.

## Food roles

| Mob | Accepted food decision |
| --- | --- |
| Pelagic Herring | Drop a raw fish food item with a cooked counterpart/recipe. |
| Crimson Doe | Drop raw cookable deer meat, alongside Sanguine Formation; comparable resource role to cows/pigs. |

Exact food names, nutrition, saturation, drop quantities and cooking experience remain open. Prefer a small coherent food set rather than extra ingredient families without a purpose.

## Shared plant drops and crafts

| Input family | Intended shared output | Status |
| --- | --- | --- |
| New algal blocks | **Befouling Ash** through a regular furnace | Accepted direction; enumerate eligible blocks and yields. |
| Most Hemomancy plants and fungi | **Befouling Ash** through a regular furnace | Accepted direction; explicitly catalogue eligible plant items/blocks rather than granting every organic-looking block a recipe. |
| Nether lichen blocks from the Phlegethontic Basins | **Smouldering Ash** through a regular furnace | Explicit exception to the general Befouling Ash route. Verify exact biome/block IDs and spelling. |

Purpose: give otherwise low-use vegetation a common crafting role and make the ashes required by many recipes renewable through exploration and gathering.

No complete per-plant shared drop list was settled in the visible conversation. The accepted shared craft is furnace conversion to ash; existing harvested plant identities need not be replaced automatically. Determine per-input yield, smelting time, experience, and whether these are the base ashes rather than the separate **Active** ash variants. Smoking/blasting, bulk conversions and special-flora exceptions require a separate decision.

## New mobs and appearance/behavior changes

| Mob | Purpose and accepted requirements | Continuation status |
| --- | --- | --- |
| **Vampire Squid** | Deep swimming life in the lowest two added ocean layers; membrane spikes supply Barbed Splinters. Model/animation can borrow the broad silhouette and structure of the larger Naeglerophaeon. | Implemented in this conversation. Loot uses the existing spine ID displayed as Barbed Splinter. Whole body now smoothly follows actual 3D swimming, including diving and retreat. |
| **Bloodworm** | Shallow/tidepool venom alternative to rarer centipedes. | Proposed; implementation and exact shared venom material still to be completed/verified. |
| **Ice Fish** | Cold oceans, frozen rivers, Ice Spikes and related cold aquatic habitats; natural purity from lacking hemoglobin; cleansing/blood-breakdown role. | Implemented in this conversation; existing Cleansing Hemolymph is the temporary shared drop. |
| **Osteophage Vulture** | Skeletal bearded vulture in Soul Sand Valleys. Perch on the large bone-block fossils; hunt skeletons with Choir Keeper-like climbing/swooping/navigation; eat loose bone items offered by players or dropped by mobs; supply Venous Pinions. | Implemented in this conversation. Latest balance decision: spawn weight **8 → 2**, one quarter of its former weight, single-mob groups. Existing mobs are not removed. |
| **Choir Keeper** | Approved owl–peacock appearance: broad feathered head, facial discs, forward-facing eyes, short beak, ear tufts and compact feathered neck; retain the fifteen Eye-of-Ender tail feathers. Supply Venous Pinions. | Owl–peacock model implemented on 2026-10-07. Low-light hunting, silent swoops, flocking and hooting remain proposals; this visual revision does not implement them. |
| **Hemojelly** | Longer tentacles visibly ensnare and capture prey, supporting Enthralling Filament. | Accepted tweak; completion must be checked. |
| **Siphonophore** | Ductilis-themed colonial swimmer with zapping tentacles and a ganglion drop. | Accepted direction; behavior/drop completion must be checked. |
| **Bloody-Belly Comb Jelly** | Color/light material source and convincing swimming posture. | Shared chromatophore drop is accepted; movement-aligned whole-body tilt implemented in this conversation. |

The birds should retain distinct ecological roles: **Venous Striders** are Overworld water birds, **Osteophages** are Nether bone scavengers, and **Choir Keepers** are End nocturnal hunters with flocking/songbird and peacock traits.

## Source spot-check at handoff

The following were read directly from the current checkout when writing this handoff:

- `chitinous_husk` is already registered and localized as **Chitinous Husk**. Broader source distribution still needs auditing.
- `calcified_blood_spine` is still the registry ID, but its display name is already **Barbed Splinter**. Barbed Urchin and Vampire Squid loot tables both use that ID.
- `cuttlefish_chromatophores` is still registered and displayed as **Cuttlefish Chromatophores**. Prism Cuttle loot uses it.
- Bloody-Belly Comb Jelly, Siphonophore and Pelagic Herring entity loot tables currently have **empty pools**. Their agreed shared materials/food have not been added to those tables.
- Crimson Doe loot includes `sanguine_formation`; its accepted raw/cooked food role needs a fuller audit.
- Ice Fish and Hemolymphopoda loot use `cleansing_hemolymph`.
- Osteophage and Venous Strider loot use `venous_pinion`; Choir Keeper's intended addition still needs checking.
- Both `ganglion_cluster` and `naeglerophaeon_ganglion` exist. Preserve the distinction between ordinary neural material and the unique boss organ.
- Osteophage's biome modifier now has weight **2**, with `minCount = maxCount = 1`.

Key files: `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java`, `src/main/resources/assets/hemomancy/lang/en_us.json`, `src/main/resources/data/hemomancy/loot_table/entities/`, `src/main/resources/data/hemomancy/recipe/`, and `src/main/resources/data/hemomancy/neoforge/biome_modifier/`. Mechanics authority is `docs/HEMOMANCY_REFERENCE.md`; lore authority is `docs/LORE_REFERENCE.md`.

## Work to continue

1. Catalogue every registered mob, its actual current loot, blood profile, habitat and nonlethal harvest paths. Include mobs with empty loot tables. This complete catalogue was requested originally and is not supplied by the focused spot-check above.
2. Map ordinary drops onto the accepted shared families. Keep rare organs listed separately. Record exact old item IDs, current display names, intended names and affected recipes.
3. Implement the confirmed missing sources and food roles, then decide the venom name/processing scheme and the exact Sclerite recipe changes.
4. Catalogue plants, fungi, algae and basin lichen; add the approved furnace ash routes with explicit exceptions and sensible yields.
5. Preserve existing registry IDs for display-only renames where practical. Any true ID replacement needs compatibility planning for inventories, saved stacks, recipes, tags, books and integrations; an ID migration was not authorized as a design decision here.
6. Align loot, recipes, tags, localization, datagen, books/dialogue, mechanics reference and wiki. Check progression after materials become available in several dimensions; preserve reasons to seek rare organs and harvest live snails.
7. Validate the changed drop/recipe contracts and relevant loaded behavior. Review actual spawn abundance where balance is changed: spawn weight is relative to competing entries, so a quarter weight does not guarantee exactly a quarter of observed population.

Do not treat unspecified extra mob assignments, venom recipes, plant yields, Sclerite costs or new ingredient names as previously approved decisions. Continue within this checkout's autonomous execution instructions and preserve unrelated work.
