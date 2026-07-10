# Alchemist Dialogue Scope Design

## Goal

Keep Harbinger NPC expertise distinct: Artificiers teach the Hematic Armature and Vicars teach blood-structure crafting; Alchemists teach refinement, centrifuging, memory weaving, and incubation only.

## Decision

Remove the Alchemist dialogue-tree options and nodes that explain the Hematic Armature, basic blood structures, and Grand-tier blood crafting. Remove their now-unreferenced English localization entries. Do not add redirect dialogue: the Alchemist will not speak on either specialty.

This was chosen over (a) keeping redirect lines to the Artificier or Vicar, which would still have the Alchemist discuss the subjects, and (b) duplicating the instruction in the specialist trees, which already contain the authoritative content.

## Implementation Boundaries

- Modify `HarbingerAlchemistDialogueTrees.java` to remove Armature and blood-structure options and nodes from both Votary variants and remove the Illuminatus blood-crafting branch.
- Modify `en_us.json` to delete the removed option and dialogue keys, and reword any remaining Alchemist prose that frames world-shaping structures as their teaching domain.
- Update `docs/LORE_REFERENCE.md` to identify the Artificier, not the Alchemist, as the Armature teacher. No mechanics, recipes, rewards, or specialist dialogue will change.

## Verification

Search Alchemist tree source and Alchemist localization keys for Armature and blood-structure teaching remnants. Parse `en_us.json` as JSON and compile the changed Java source with the normal Gradle build gate if practical.
