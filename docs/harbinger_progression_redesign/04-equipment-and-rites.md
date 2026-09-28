# Phase 4: equipment, Living Staff, gourds, and rites

[Index](../HARBINGER_PROGRESSION_REDESIGN.md) · Depends on the early assignment structure; coordinate Forge text with Phase 3.

## Outcome

The Artificer teaches the implements needed for the Vicar's rites before those rites demand them. Armor supports a chosen practice. Gourds expand on a clear, existing schedule.

## Read before editing

- [Living Staff recipe](../../src/main/resources/data/hemomancy/recipe/blood_structure/living_staff.json).
- [Artificer assignments](../../src/main/java/com/vincenthuto/hemomancy/common/mission/artificer/ArtificerAssignments.java), ArtificerProgressionRules, Artificer/Vicar dialogue, and DialogueEventHandler.
- [Pallid Vessel](../../src/main/resources/data/hemomancy/recipe/cardinal_rite/pallid_vessel_rite.json), rooted_vein, crimson_vessel_rite, ashen_vessel_rite, horn_of_culmination_rite, and scarlet_vanity recipes.
- [Armature upgrade rites](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/ArmatureUpgradeRites.java), ArmatureUpgradeRules, HematicArmatureBlock, relevant menu/recipe handling.
- ItemInit.vicars_consecration_kit and every reference to its claim key, display name, inquiry, material atlas entry, recipe and reward event.

## Instructions

1. At D1 give the Artificer the practical Hematic Iron lesson. Keep the Vicar's core projection instruction and First Bloodcraft return.
2. At D2 teach The Worn Vow through the placed Armature and first Hematic Iron upgrade. Encourage a complete set as Vocation work; do not make it a degree prerequisite.
3. Teach staff construction and basic use at D2. Preserve the D1 recipe gate, already owned staffs, bonds, forms, fittings, and inventories.
4. Have the Vicar introduce rite grammar after the staff lesson. Rooted Vein is an existing D2 example. Teach only that rite's authored floor/medium/anchor/offering requirements, not the full late-game ritual vocabulary at once.
5. At D3 offer one of Barbed, Chitinite, or Prismatic through The Three Answers. Keep the chosen-family proof and branch rewards; neither all three sets nor all relevant specimen research is required for a rank.
6. Keep Scarlet Vanity and Pallid Vessel D3. Follow with Crimson Vessel D4, Ashen Vessel D5, and Horn of Culmination D6. Teach reserve, filling, equipping and withdrawal in gameplay terms. Do not silently move these recipes to match the superseded initial D2 gourd proposal.
7. Make D5 kit teaching/granting an Artificer responsibility. Display name: Armature Consecration Kit. Preserve the vicars_consecration_kit registry ID and the existing reward claim state for old saves.
8. Remove the Vicar's active grant option after the Artificer path works; leave a referral. Server event dispatch must validate the actual Artificer and range, not just change the option's location.
9. The existing kit handler primarily checks degree and claim state; verify teacher validation in the new call path explicitly. A forged old event sent to another NPC must not grant it.
10. A prior claimant must not receive another free kit under a newly named claim key. Existing kits remain usable. Explain the supported replacement/recovery route without resetting past claims.
11. At D5 connect consecrated Armature work, Blood Lust armor, mask choices, and the prior biological fork. Preserve machine contents and upgrade state. Do not make Fane completion a new requirement to obtain the equipment needed while founding.
12. At D7 teach Weight of the Frame and the existing final lineages. Retain actual ending gates for Silent Archon pieces; verify their serialized choice flags against the real completed-ending state before changing any check.
13. Keep Vicar authority over degree rites and doctrine. Equipment ownership moving to the Artificer does not require renaming rank rites or making the Artificer a promotion authority.

## Acceptance

- [ ] D2 can build and use a Living Staff before the D3 gourd lesson.
- [ ] Early staff ownership bypasses no rank or blood eligibility requirement.
- [ ] First armor piece progresses The Worn Vow; no full-set requirement leaks into Main.
- [ ] All four gourd stages enforce their real gates and are described consistently.
- [ ] Only the valid Artificer grants the kit; old Vicar claims suppress duplicate grants.
- [ ] Existing kits, Armatures, armor components, staff forms and bonds survive save/load.
- [ ] Cancelled or interrupted equipment rites preserve the current documented recovery contract.
- [ ] Undecided D7 and Apotheos players cannot obtain Silent Archon-exclusive outcomes through the equipment lesson.

Use ArtificerProgressionGameTests, armor/rite resource tests, Living Weapon coverage if staff behavior changes, and real machine upgrade tests. Observe inventory and in-world presentation for renamed items and changed teaching.
