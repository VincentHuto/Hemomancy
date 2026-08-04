# Dialogue Inquiry Canon Synchronization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Repair the Hemomancy inventory-inquiry system so its data is valid, its state handling matches the current UI, and every response agrees with canonical lore, implemented mechanics, and speaker voice.

**Architecture:** Preserve the existing datapack-backed registry and dialogue hub. Add a reusable inquiry context, narrow stack-aware providers for data-bearing items, explicit refusal/unknown policies, and resource tests that prevent localization and canon drift. Rewrite only the inquiry and directly adjacent stale localization identified by the approved audit.

**Tech Stack:** Java 21, NeoForge 1.21.1, JUnit 5, Gson, Minecraft `ItemStack` data components, datapack JSON, `en_us.json`.

## Global Constraints

- Branch: `fix/dialogue-inquiry-canon-sync`, based on commit `68293ad6c6400b04b9b98a1c598a382e6e8f8b04`.
- `docs/LORE_REFERENCE.md` governs narrative canon.
- `docs/HEMOMANCY_REFERENCE.md` governs implemented mechanics where it does not contradict lore.
- Use British English for authored prose except official Minecraft names.
- Do not add new progression, recipes, NPCs, or dormant mechanics.
- Do not merge into `neo-1.21.1`.
- Preserve the existing strong voices of the Artificer, Mnemonist, Voyager, Wayfarer, and Monolith.
- Test-first for code and resource contracts; commit each independently reviewable task.

---

## File map

### New production files

- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryContext.java` — immutable stable player-state input for inquiry resolution.
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/StackAwareInquiryProvider.java` — provider interface for data-bearing stacks.
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/StackAwareInquiryRegistry.java` — ordered provider dispatch and stable presentation keys.
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/InquiryAccessPolicy.java` — explicit refusal-state filtering by speaker and inquiry class.

### Modified production files

- `ItemInquiryCondition.java`, `ItemInquiryEntry.java`, `ItemInquiryLoader.java`, `ItemInquiryRegistry.java` — context-based matching and new JSON fields.
- `DialogueItemInquiryNodes.java`, `DialogueHubFactory.java` — stack-aware topics, unknown topic, and access filtering.
- NPC entity interaction classes for the nine inquiry speakers — build `ItemInquiryContext` and pass it to decoration.
- `src/main/resources/assets/hemomancy/lang/en_us.json` — missing keys, factual corrections, and voice rewrites.
- `src/main/resources/data/hemomancy/dialogue_inquiry/**` — corrected gates and mappings where the localization key or ownership changes.
- `docs/DIALOGUE_ITEM_QUERY_GUIDE.md` — inventory-hub contract, all nine roles, stack-aware and refusal behaviour.

### New/modified tests

- `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/ItemInquiryResourceValidationTest.java`
- `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryConditionTest.java`
- `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueItemInquiryNodesTest.java`
- `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueInquiryCanonTest.java`
- Existing scope/ownership tests updated only when their assertions intentionally change.

---

### Task 1: Inquiry resource validation and missing localization repair

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/ItemInquiryResourceValidationTest.java`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`

**Interfaces:**
- Consumes: inquiry JSON shape documented by `ItemInquiryLoader`.
- Produces: a JUnit gate that fails when any shipped inquiry line key is absent from the main runtime language file.

- [ ] **Step 1: Write the failing localization-reference test**

Create a test that walks `src/main/resources/data/hemomancy/dialogue_inquiry`, parses root `lines` and every conditional branch `lines`, parses `src/main/resources/assets/hemomancy/lang/en_us.json`, and reports all missing keys together.

```java
@Test
void everyInquiryLineExistsInRuntimeLanguageFile() throws IOException {
    Set<String> languageKeys = loadLanguageKeys();
    Map<Path, List<String>> missingByFile = new TreeMap<>();
    try (Stream<Path> files = Files.walk(INQUIRY_ROOT)) {
        files.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
            for (String key : inquiryLineKeys(path)) {
                if (!languageKeys.contains(key)) {
                    missingByFile.computeIfAbsent(path, ignored -> new ArrayList<>()).add(key);
                }
            }
        });
    }
    assertTrue(missingByFile.isEmpty(), () -> formatMissing(missingByFile));
}
```

- [ ] **Step 2: Run the focused test and verify the known failure**

Run:

```powershell
./gradlew.bat test --tests "*ItemInquiryResourceValidationTest"
```

Expected: failure listing the twelve shared missing keys used by 32 JSON files.

- [ ] **Step 3: Add the twelve shared keys with speaker-appropriate prose**

Add:

```text
hemomancy.alchemist.item_inquiry.minecraft_reagent.line1/.line2
hemomancy.guardian.item_inquiry.minecraft_arms.line1/.line2
hemomancy.vicar.item_inquiry.minecraft_relic.line1/.line2
hemomancy.votary_wayfarer.item_inquiry.minecraft_fieldkit.line1/.line2
hemomancy.voyager.item_inquiry.minecraft_salvage.line1/.line2
hemomancy.zealot.item_inquiry.minecraft_ritegoods.line1/.line2
```

Keep each pair general enough for its mapped group while retaining the speaker's voice. Do not invent item-specific mechanics in a shared line.

- [ ] **Step 4: Add structural checks to the same test**

Assert that every entry has exactly one of `lines` or `conditions`, every branch has non-empty lines, and every conditional entry has a final unconstrained catch-all branch. Report all violations in one assertion.

- [ ] **Step 5: Run the focused test**

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/ItemInquiryResourceValidationTest.java src/main/resources/assets/hemomancy/lang/en_us.json
git commit -m "test(dialogue): validate inquiry localization resources"
```

---

### Task 2: Context-based condition model

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryContext.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryCondition.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryEntry.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryLoader.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryRegistry.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryConditionTest.java`

**Interfaces:**
- Produces:

```java
public record ItemInquiryContext(
    int degree,
    float purity,
    float clarity,
    boolean clarityUnlocked,
    boolean activeBlood,
    boolean purifying,
    boolean silentArchon,
    boolean apotheos
) {
    public static ItemInquiryContext legacy(int degree, float purity) { ... }
}
```

```java
Optional<List<String>> ItemInquiryRegistry.resolve(
    String npcId, ResourceLocation itemId, ItemInquiryContext context)
```

- [ ] **Step 1: Write condition tests before changing production code**

Cover inclusive degree/purity/clarity bounds, nullable Boolean constraints, unconstrained branches, and first-match ordering.

```java
@Test
void clarityGateRequiresUnlockedClarityAndMinimumValue() {
    ItemInquiryCondition condition = new ItemInquiryCondition(
        -1, -1, -1f, -1f, 50f, -1f,
        true, null, null, List.of("key"));
    assertFalse(condition.matches(new ItemInquiryContext(0, 100, 49, true, false, true, false, false)));
    assertFalse(condition.matches(new ItemInquiryContext(0, 100, 75, false, false, true, false, false)));
    assertTrue(condition.matches(new ItemInquiryContext(0, 100, 75, true, false, true, false, false)));
}
```

- [ ] **Step 2: Run the focused test and verify compilation failure**

Run:

```powershell
./gradlew.bat test --tests "*ItemInquiryConditionTest"
```

Expected: FAIL because `ItemInquiryContext` and the expanded constructor do not exist.

- [ ] **Step 3: Implement `ItemInquiryContext` and expand conditions**

Add JSON fields:

```text
min_clarity
max_clarity
clarity_unlocked
requires_active_blood
requires_purifying
```

Use nullable `Boolean` values so omission is distinct from `false`. Retain the old two-argument registry overload as a compatibility wrapper calling `ItemInquiryContext.legacy(...)` until all callers migrate.

- [ ] **Step 4: Add loader range validation**

Reject or warn and skip a branch when:

```text
min_degree > max_degree
min_purity > max_purity
min_clarity > max_clarity
purity or clarity is outside 0..100
```

Do not silently convert invalid authored values into unconstrained conditions.

- [ ] **Step 5: Run focused and existing inquiry tests**

```powershell
./gradlew.bat test --tests "*ItemInquiryConditionTest" --tests "*DialogueItemInquiry*"
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry
git commit -m "feat(dialogue): add inquiry progression context"
```

---

### Task 3: Stack-aware dynamic inquiry resolution

**Files:**
- Create: `StackAwareInquiryProvider.java`
- Create: `StackAwareInquiryRegistry.java`
- Modify: `DialogueItemInquiryNodes.java`
- Modify: `DialogueHubFactory.java`
- Read and reuse: `MnemonicBlueprintItem`, `MnemonicBlueprintTarget`, `DataComponentInit`, `ScarPatternData`/current scar component helper, `SpecimenJarData`.
- Create/modify: `DialogueItemInquiryNodesTest.java`
- Modify: `en_us.json` for dynamic line templates.

**Interfaces:**

```java
public interface StackAwareInquiryProvider {
    boolean supports(String speakerId, ItemStack stack);
    Optional<ResolvedStackInquiry> resolve(
        String speakerId, ItemStack stack, ItemInquiryContext context);
}

public record ResolvedStackInquiry(
    String presentationKey,
    List<Component> lines
) {}
```

If `DialogueNode` requires translation-key strings rather than `Component`, use a small serializable line descriptor already supported by the dialogue packet; do not concatenate localized names server-side into raw English.

- [ ] **Step 1: Extend hub tests for duplicate data-bearing stacks**

Create two Mnemonic Blueprint stacks with different targets and assert that two distinct inquiry node IDs/topics are produced. Add equivalent tests for two Scar Patterns with different scar ID sets.

- [ ] **Step 2: Run the focused test and verify current deduplication failure**

Expected: one topic per registry ID rather than one per meaningful stack state.

- [ ] **Step 3: Implement the provider registry and stable presentation keys**

Provider order:

1. Mnemonic Blueprint provider;
2. Scar Pattern provider;
3. Specimen Jar provider;
4. ordinary item-ID JSON resolution;
5. existing Vicar `HematicMemoryItem` family exception.

Stable keys should contain only deterministic, packet-safe text, for example:

```java
"hemomancy/mnemonic_blueprint/rite/" + target.id()
"hemomancy/scar_pattern/" + sha1(sortedScarIds)
"hemomancy/specimen_jar/" + specimenId
```

- [ ] **Step 4: Add dynamic response templates**

Provide speaker-specific templates:

- Vicar: blank versus remembered rite/structure and its institutional meaning.
- Mnemonist: Scar Pattern template/loadout and the named routes it carries.
- Voyager/Alchemist: specimen identity and observable stored layers without claiming unimplemented biology.

- [ ] **Step 5: Preserve fallback to JSON and family exceptions**

A provider that does not support a speaker/stack must return empty without suppressing the exact JSON mapping.

- [ ] **Step 6: Run focused tests**

Expected: distinct dynamic topics, ordinary items unchanged, no duplicate topics for identical state.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue src/main/resources/assets/hemomancy/lang/en_us.json
git commit -m "feat(dialogue): inspect stateful inquiry items"
```

---

### Task 4: Refusal policy, unknown topic, and NPC context wiring

**Files:**
- Create: `InquiryAccessPolicy.java`
- Modify: `DialogueItemInquiryNodes.java`
- Modify: `DialogueHubFactory.java`
- Modify: the nine inquiry-speaking entity interaction classes.
- Modify: `en_us.json` prompts and unknown-topic labels.
- Modify: `DialogueItemInquiryNodesTest.java`

**Interfaces:**

```java
public enum InquiryAccess {
    FULL,
    IDENTIFICATION_ONLY,
    NONE
}

public static InquiryAccess accessFor(String speakerId, ItemInquiryContext context)
```

- [ ] **Step 1: Write refusal-policy tests**

Assert:

- Clarity Alchemist: no inquiry topics.
- Purifying Alchemist: only entries explicitly marked identification-safe.
- Purifying/Clarity Artificer: identification-safe entries only, with no Armature/graft procedure.
- Normal states remain unchanged.

Use a small allow-list of neutral item IDs/categories rather than attempting to infer safety from prose.

- [ ] **Step 2: Write unknown-topic test**

Assert that the hub exposes exactly one generic unknown-object topic when an inventory contains unsupported items, routing to `hemomancy.<speaker>.item_inquiry.unknown`; do not create one topic per unsupported stack.

- [ ] **Step 3: Implement policy and context wiring**

Build `ItemInquiryContext` from live capabilities in each entity interaction path and pass it through `withInventoryItemInquiries` and `DialogueHubFactory.decorate`.

- [ ] **Step 4: Replace stale held-item prompts**

Examples:

```text
Alchemist: "Show me what you carry. I will name what belongs to my bench."
Mnemonist: "Open your inventory of objects. I will tell you which ones remember."
Guardian: "Choose the object. I will tell you whether it belongs in the field."
```

Update every inquiry speaker, not only the four with conflicts.

- [ ] **Step 5: Run focused tests**

Expected: refusal and unknown policies match the design.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/vincenthuto/hemomancy/common/entity/npc src/test/java/com/vincenthuto/hemomancy/common/entity/npc src/main/resources/assets/hemomancy/lang/en_us.json
git commit -m "fix(dialogue): enforce inquiry access policy"
```

---

### Task 5: Canon-sync Alchemist, Vicar, and Monolith inquiry prose

**Files:**
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Modify as needed: `dialogue_inquiry/alchemist/**`, `dialogue_inquiry/vicar/**`, `dialogue_inquiry/monolith/**`
- Create: `DialogueInquiryCanonTest.java`
- Update: `AlchemistDialogueScopeTest.java`

**Interfaces:**
- Produces: current-facing prose free of the audit's stale Harbinger mechanics.

- [ ] **Step 1: Write failing stale-phrase assertions**

The test reads only current runtime inquiry/localization surfaces and rejects exact or case-insensitive patterns including:

```text
Scar Binder
wear the scar
blood key
integrate it into your bloodline
Fungal, Umbral, Incandescent
smelting environment itself is blood-charged
fill it at the Ghastly Alembic
recycled through the centrifuge
```

Keep an explicit allow-list for historical docs outside runtime resources.

- [ ] **Step 2: Run the canon test and capture failures**

- [ ] **Step 3: Rewrite Alchemist factual content and cadence**

Correct:

- D1 Centrifuge / D2 Alembic.
- Living Syringe + Vial Rack sampling.
- Eight enzymes and current tendency meanings.
- Loom blank memory, catalyst, stored enzyme units, projected blood, and orb retrieval.
- Reliquary as active known-memory arrangement only.
- Scar station material role without claiming worn plates.
- Hematic Iron projection/salvage route.
- Morphlings as fungal symbionts/parasites.
- Graft entries defer procedure to the Artificer.

Each response should usually contain one operational fact and one laboratory observation.

- [ ] **Step 4: Rewrite Vicar factual content and cadence**

Correct:

- Mortal Display as a Hermit's offered heart and initiation threshold.
- Blood Projection, not blood key.
- Personal memory learning versus Reliquary loadout.
- Current scar doctrine and Vein-Mason ownership.
- Qliphoth as Archon-only revelation progression.
- peaceful/hostile Hallowed Residuum routes.
- provisional metaphysics labelled as Lodge speculation.

Keep procedure broad and defer station details to specialists.

- [ ] **Step 5: Correct directly adjacent Monolith D6 guidance**

Replace organ-plus-engram Loom guidance with current high-degree guidance grounded in adopted systems. Preserve the Monolith's compressed voice.

- [ ] **Step 6: Run canon and ownership tests**

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/resources/assets/hemomancy/lang/en_us.json src/main/resources/data/hemomancy/dialogue_inquiry src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue
git commit -m "fix(lore): synchronize Harbinger inquiries"
```

---

### Task 6: Canon-sync Guardian and Zealot inquiry prose

**Files:**
- Modify: `en_us.json`
- Modify as needed: `dialogue_inquiry/guardian/**`, `dialogue_inquiry/zealot/**`
- Extend: `DialogueInquiryCanonTest.java`

- [ ] **Step 1: Add failing Unstained stale-phrase assertions**

Reject:

```text
smelt the ore directly
Pale Humor Flask. Blood
Pallid Infusion initiates
bring your hemolytic solution ... to the Altar
Guardians are not expected to walk
Guardian detachments use it to smoke
harvested ... at dawn
five points of purity
five points of clarity
```

- [ ] **Step 2: Rewrite Guardian entries**

Correct Pale Silver origin, Guardian purification status, blade doctrine, Absolution Dagger treatment, Glaive reach rationale, and Sporitic Thurible ownership. Remove unimplemented claims about arrows, bolts, shield projectile disruption, universal coatings, and exact coating durability unless verified in code.

Target voice: short field commands, generally no more than two compact sentences per line pair.

- [ ] **Step 3: Rewrite Zealot entries**

Correct:

1. Podium suppression.
2. Lethean Baptism path initiation.
3. later Altar role.
4. Consecrated Copper preparation and Clarity Ascension.
5. Pallid Infusion restorative effect.
6. White Humor/lymph terminology.
7. Pale Silver refinement from Consecrated Copper.
8. crafted/distilled Tears of Silthmere and Silthmere as Our Lady's title.
9. peaceful/hostile Hallowed Residuum routes.
10. removal of spoken balance-point values.

- [ ] **Step 4: Correct affected JSON gates**

Remove the false `min_purity: 75` Pallid Infusion gate. Use unconditional restorative explanation, or a real `clarity_unlocked` branch only if the response genuinely changes without misrepresenting function.

- [ ] **Step 5: Run canon and resource tests**

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/assets/hemomancy/lang/en_us.json src/main/resources/data/hemomancy/dialogue_inquiry src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueInquiryCanonTest.java
git commit -m "fix(lore): synchronize Unstained inquiries"
```

---

### Task 7: Documentation, complete regression suite, and branch review

**Files:**
- Rewrite: `docs/DIALOGUE_ITEM_QUERY_GUIDE.md`
- Update if implementation status changed: `docs/HEMOMANCY_REFERENCE.md`
- Update test documentation only if a new command or suite category is introduced: `docs/TESTING.md`

- [ ] **Step 1: Rewrite the inquiry guide**

Document:

- inventory inquiry hub rather than held-main-hand flow;
- all nine speaker IDs;
- context fields and JSON examples;
- stack-aware provider priority;
- unknown topic behaviour;
- refusal filtering;
- main runtime `en_us.json` as the localization source of truth;
- resource-test requirements for new entries;
- expertise ownership matrix from the approved design.

- [ ] **Step 2: Run formatting and focused test suite**

```powershell
./gradlew.bat test --tests "*ItemInquiry*" --tests "*DialogueInquiry*" --tests "*AlchemistDialogueScopeTest" --tests "*PuppeteeringThreadInquiryOwnershipTest"
```

Expected: all PASS.

- [ ] **Step 3: Run the full JVM suite**

```powershell
./gradlew.bat test --console=plain
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Run the project alpha gate**

```powershell
./gradlew.bat alphaCheck --console=plain
```

Expected: JVM tests and required GameTests pass. If the environment cannot launch the NeoForge server, record the exact environmental failure and do not claim the gate passed.

- [ ] **Step 5: Review branch diff**

```bash
git diff --check neo-1.21.1...HEAD
git diff --stat neo-1.21.1...HEAD
git log --oneline neo-1.21.1..HEAD
```

Confirm no unrelated files, generated-language copies, recipes, or progression systems changed.

- [ ] **Step 6: Commit documentation**

```bash
git add docs/DIALOGUE_ITEM_QUERY_GUIDE.md docs/HEMOMANCY_REFERENCE.md docs/TESTING.md
git commit -m "docs(dialogue): document inventory inquiry contract"
```

- [ ] **Step 7: Final compare**

Compare `neo-1.21.1` to `fix/dialogue-inquiry-canon-sync` and report commit list, changed files, tests executed, and any known limitations. Leave the branch unmerged.
