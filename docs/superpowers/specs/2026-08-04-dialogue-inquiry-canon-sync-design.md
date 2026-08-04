# Dialogue Inquiry Canon Synchronization Design

## Status

Approved 2026-08-04 for implementation on `fix/dialogue-inquiry-canon-sync`.

## Goal

Make every shipped inventory inquiry mechanically truthful, canonically consistent, technically valid, and recognizably voiced by the NPC answering it.

## Authority

1. `docs/LORE_REFERENCE.md` governs narrative canon.
2. `docs/HEMOMANCY_REFERENCE.md` governs implemented mechanics where it does not contradict lore.
3. Live recipes, item classes, capabilities, and registries verify implementation details.
4. Inquiry JSON and `en_us.json` are downstream presentation and must be corrected when they disagree.
5. Proposed or dormant behavior must be labelled as belief, hypothesis, or future work rather than presented as implemented fact.

## Scope

### Functional corrections

- Validate every inquiry JSON localization key against the main runtime `en_us.json`.
- Repair the twelve missing shared inquiry keys currently referenced by 32 mappings.
- Align documentation and prompts with the implemented inventory inquiry hub rather than the obsolete held-main-hand description.
- Make refusal states enforce the instructional limits claimed by their dialogue.
- Add stable Clarity/path condition support where required.
- Add stack-aware inquiry resolution for Mnemonic Blueprints, dynamic Scar Patterns, and Specimen Jars.
- Preserve characterful unknown responses through an explicit unsupported-item path rather than silently claiming they are automatic fallbacks.

### Canon synchronization

Correct the factual conflicts identified in `hemomancy_dialogue_inquiry_audit_2026-08-04.md`, especially:

- D1 Centrifuge / D2 Alembic sequence.
- Eight canonical tendencies and current Ductilis meaning.
- Current syringe, vial-rack, centrifuge, enzyme, and Somatic Loom workflows.
- Hematic Iron projection/salvage rather than obsolete smelting claims.
- Scar learning and loadout management through the Cerebral Scarring Station, Iron Brazier, Mason's Effigy, and dynamic Scar Pattern.
- Mortal Display identity.
- Blood Projection rather than a blood key.
- Personal memory learning versus Reliquary loadout management.
- Qliphoth as an Archon revelation sequence rather than ordinary Order practice.
- Peaceful and hostile Hallowed Residuum acquisition.
- Podium suppression, Lethean Baptism initiation, later Altar work, and Clarity Ascension.
- Pale Humor as White Humor/lymph.
- Pallid Infusion as a Blood Loss-clearing restorative.
- Pale Silver as refined Consecrated Copper, never natural ore.
- Unstained blade doctrine, Absolution Dagger treatment, and Silthmere Glaive reach rationale.
- Sporitic Thurible ownership and Guardian purification status.
- Tears of Silthmere as crafted/distilled material named for Our Lady's title.

### Speaker ownership and voice

- **Alchemist:** processing, reagents, specimens, and machine preparation; measured laboratory language with uncertainty where evidence is incomplete.
- **Mnemonist:** memory learning, manipulation ownership, loadouts, and Loom meaning; patient and recursive.
- **Artificer:** Armature, armour, Living Staff grafts, and fittings; terse, physical, vocational.
- **Vicar:** doctrine, history, degrees, institutions, and ritual meaning; formal rather than procedural.
- **Guardian:** containment, weapons, patrol safety, and defensive fieldcraft; short commands and embodied priorities.
- **Zealot:** purification doctrine, rites, Our Lady, and sacred materials; sacramental language without balance-point recitation.
- **Voyager:** mature field ecology and evidence; qualified, ethical, observational.
- **Wayfarer:** junior observation and admitted uncertainty.
- **Monolith:** compressed implication and high-degree thresholds.

Duplicate item coverage is allowed only when speakers answer different questions.

## Architecture

### Inquiry context

Introduce an immutable `ItemInquiryContext` carrying the stable player state needed during resolution:

- degree;
- purity;
- clarity;
- clarity-unlocked flag;
- active Harbinger blood flag;
- purifying flag;
- Silent Archon flag;
- Apotheos flag.

JSON conditions gain only broadly reusable fields: `min_clarity`, `max_clarity`, `clarity_unlocked`, `requires_active_blood`, and `requires_purifying`. Highly specific quest and stack predicates remain Java-owned.

### Stack-aware providers

Add a focused provider layer ahead of ordinary registry-ID lookup:

1. exact stack-aware provider;
2. exact item-ID JSON entry;
3. existing item-family Java exception;
4. no result.

Initial providers:

- Mnemonic Blueprint: blank, rite plan, or blood-structure plan; include target display name.
- Scar Pattern: template versus prepared loadout; include contained scar names/count.
- Specimen Jar: identify contained specimen and Morphling layers when present.

The inventory hub uses a stable presentation key based on item ID plus relevant stack state so distinct data-bearing stacks do not collapse into one topic.

### Refusal policy

- Alchemist at Clarity: no Harbinger operational inquiries.
- Purifying Alchemist: neutral identification may remain, but machine operation and blood-practice instruction are filtered.
- Purifying/Clarity Artificer: curt identification may remain, but no procedural Armature or graft instruction.
- Other speakers keep their canonically appropriate access rules.

The policy is explicit in code rather than inferred from prose.

### Unknown items

The inventory hub continues to show known mapped items. Add one explicit generic topic such as “Ask about another object” that routes to the speaker's unknown response. Do not create one menu topic for every unsupported inventory stack.

## Testing

- Resource test parses every inquiry JSON and asserts all referenced localization keys exist in the main runtime language file.
- Condition tests cover new Clarity/path fields, invalid ranges, and first-match ordering.
- Hub tests cover distinct stateful stacks, unknown topic behavior, and refusal filtering.
- Canon drift test rejects the audit's forbidden stale phrases in current inquiry/localization surfaces.
- Existing focused tests remain green.
- Final verification runs `./gradlew.bat test`, then `./gradlew.bat alphaCheck --console=plain` when the available environment supports NeoForge GameTests.

## Non-goals

- No rewrite of the entire dialogue framework.
- No new NPCs, quests, recipes, or progression systems.
- No promotion of dormant mechanics into canon.
- No broad rewrite of non-inquiry dialogue except directly adjacent stale lines required for consistency.
- No merge into `neo-1.21.1`; the completed work remains on the correction branch for review.

## Acceptance criteria

- No shipped inquiry JSON can display a raw translation key.
- Documentation describes the inventory inquiry hub and all nine speaker roles.
- Dynamic stacks receive content-specific explanations.
- Refusal branches behave as their prose claims.
- All audit-listed hard factual contradictions are removed.
- Duplicate mappings respect expertise boundaries.
- Each speaker remains identifiable without portrait or speaker name.
- Focused and full available test gates pass.