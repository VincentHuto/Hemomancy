# Alchemist Dialogue Scope Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Artificer the sole Armature teacher and the Vicar the sole blood-structure teacher by removing those subjects from Alchemist dialogue.

**Architecture:** The Alchemist tree will retain only refinement and biological-processing lessons. Its removed Votary and Illuminatus nodes will no longer be reachable or localized, while the existing Artificer and Vicar trees remain unchanged. A source-level JUnit contract will prevent the role boundaries from regressing.

**Tech Stack:** Java 21, NeoForge 21.1.219, Gradle, JUnit 5, Minecraft JSON language resources.

## Global Constraints

- Keep the change limited to player-facing dialogue ownership; do not alter Armature or blood-structure mechanics, recipes, rewards, or specialist dialogues.
- Use existing NPC terminology: `Hematic Artificer / Redwright` and `Harbinger Vicar`.
- Treat `src/main/resources/assets/hemomancy/lang/en_us.json` as authoritative over generated resources.
- Keep Alchemist language focused on refinement, centrifuging, memory weaving, taxonomy, and incubation.

---

### Task 1: Add a specialist-ownership regression contract

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/AlchemistDialogueScopeTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/AlchemistDialogueScopeTest.java`

**Interfaces:**
- Consumes: Alchemist, Artificer, and Vicar dialogue-tree source plus `en_us.json`.
- Produces: A JUnit test proving the Alchemist lacks Armature/blood-structure branches and the specialist trees retain their corresponding teaching branches.

- [ ] **Step 1: Write the failing test**

```java
@Test
void alchemistDoesNotTeachArtificerOrVicarSubjects() throws IOException {
    String alchemist = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java");
    String artificer = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerArtificerDialogueTrees.java");
    String vicar = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java");
    String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");

    assertFalse(alchemist.contains("tell_me_about_armature"));
    assertFalse(alchemist.contains("blood_structure_intro"));
    assertFalse(alchemist.contains("blood_crafting_lore"));
    assertFalse(lang.contains("hemomancy.alchemist.votary.armature_lore"));
    assertFalse(lang.contains("hemomancy.alchemist.votary.blood_structure_intro"));
    assertFalse(lang.contains("hemomancy.alchemist.illuminatus.blood_crafting_lore"));
    assertTrue(artificer.contains("teach_armature"));
    assertTrue(vicar.contains("ask_about_blood_crafting"));
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests com.vincenthuto.hemomancy.common.entity.npc.dialogue.AlchemistDialogueScopeTest`

Expected: FAIL because the current Alchemist source and language file still contain the prohibited branches and keys.

- [ ] **Step 3: Commit the regression contract**

```powershell
git add -- src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/AlchemistDialogueScopeTest.java
git commit -m "test: cover alchemist dialogue ownership"
```

### Task 2: Remove prohibited Alchemist dialogue branches and references

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java:326-446,458-498,576-593`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json:653,684,701,863,868-871,1014-1017`
- Modify: `docs/LORE_REFERENCE.md:282`

**Interfaces:**
- Consumes: The ownership contract from Task 1.
- Produces: An Alchemist tree without Armature or blood-structure entries, English localization with no corresponding Alchemist instruction, and lore documentation that assigns base-rack instruction to the Artificer.

- [ ] **Step 1: Remove Votary and Illuminatus routing**

Delete both Votary variants' `tell_me_about_blood_structures` and `tell_me_about_armature` greeting options plus their `blood_structure_intro` and `armature_lore` nodes. Delete the Illuminatus `tell_me_about_blood_crafting` option and `blood_crafting_lore` node. Update the two degree comments so they describe only retained subject matter.

- [ ] **Step 2: Remove and re-scope English dialogue text**

Delete the eight unreferenced option and lore keys:

```text
hemomancy.dialogue.alchemist.option.tell_me_about_armature
hemomancy.dialogue.alchemist.option.tell_me_about_blood_crafting
hemomancy.dialogue.alchemist.option.tell_me_about_blood_structures
hemomancy.alchemist.votary.armature_lore.line1
hemomancy.alchemist.votary.armature_lore.line2
hemomancy.alchemist.votary.armature_lore.line3
hemomancy.alchemist.votary.blood_structure_intro
hemomancy.alchemist.illuminatus.blood_crafting_lore
```

Reword the remaining Alchemist item-inquiry and Sanctified lines so they describe refinement materials and the machine chain without explaining or claiming blood-structure crafting. Preserve their item keys and gameplay-neutral usefulness.

- [ ] **Step 3: Correct the lore-role ownership statement**

In section 6.4c of `docs/LORE_REFERENCE.md`, replace the claim that the Alchemist teaches the base rack with the Hematic Artificer / Redwright. Preserve the Vicar's Degree 5 consecration role.

- [ ] **Step 4: Run the ownership test to verify it passes**

Run: `./gradlew.bat test --tests com.vincenthuto.hemomancy.common.entity.npc.dialogue.AlchemistDialogueScopeTest`

Expected: PASS.

- [ ] **Step 5: Run focused resource and source checks**

Run:

```powershell
Get-Content -Raw src/main/resources/assets/hemomancy/lang/en_us.json | ConvertFrom-Json | Out-Null
rg -n -i 'tell_me_about_armature|blood_structure_intro|blood_crafting_lore|hemomancy\.alchemist.*(armature|blood.craft|blood.structure|conduit)' src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java src/main/resources/assets/hemomancy/lang/en_us.json
```

Expected: JSON parse succeeds; `rg` returns exit code 1 with no matches.

- [ ] **Step 6: Run the project regression gate**

Run: `./gradlew.bat test`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit the implementation**

```powershell
git add -- src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java src/main/resources/assets/hemomancy/lang/en_us.json docs/LORE_REFERENCE.md src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/AlchemistDialogueScopeTest.java
git commit -m "fix: narrow alchemist dialogue scope"
```
