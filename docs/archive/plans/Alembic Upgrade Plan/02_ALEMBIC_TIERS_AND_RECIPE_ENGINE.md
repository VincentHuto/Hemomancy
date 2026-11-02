# Alembic Tiers and Recipe Engine — Implementation Steps

> **Depends on:** `01_ADVANCED_BREWING_CONCEPT.md`.  
> **Deliverable:** A backward-compatible three-tier Alembic and deterministic, transactional advanced-recipe routing. No new potion combat effect is required for this gate.

## 1. Code map before edits

The current `GhastlyAlembicBlockEntity` has six slots: input 0, flask 1, result 2, catalyst 3, flask output 4, and tincture blood 5. `GhastlyAlembicRenderer` draws the reservoir over resource-backed block models; there is no `GhastlyAlembicModel` Java class. Inspect the Pallid Retort because it uses the shared distillation serializer. Do not change Retort recipe semantics for Alembic-only brewing.

## 2. Target state and inventory

Define one persisted tier field:

```text
BASE = 0
CONDENSER = 1
ATHANOR = 2
```

On old saves with no field, use BASE. Reject out-of-range values by reading as BASE and recording a warning once. Keep the same block ID. The two kit items are **not** duplicated into the block's ordinary drop: break handling returns the normal Alembic item plus each installed kit exactly once; restoring a block item does not smuggle a second tier tag back into placement.

Keep slot indices 0–5. Append Catalyst 2 at index 6; expose it to the GUI and side automation only at ATHANOR. Recover unexpected slot-6 contents in lower-tier saves without deleting them. When breaking a machine, empty the new slot through normal inventory-drop rules without duplication. The flask slot remains dedicated to its existing cured-clay flask and blood handling; binding does not use it.

## 3. Route recipes explicitly

Resolve from a snapshot of server-side inputs, tier, tank amount, heat, and output fit:

```text
if an exact existing non-Pallid distillation recipe matches:
    resolve that recipe first
else if machine is BASE:
    remain idle or perform existing blood filling
else if ATHANOR and input is bound vessel and both catalysts are potions:
    try REFILL
else if ATHANOR and input is base potion and both catalyst slots have valid potions:
    try BIND
else if input is potion and catalyst 1 is potion and catalyst 2 is empty:
    try D3 COMPOUND
else if input is potion and catalyst 1 is enzyme and catalyst 2 is empty:
    try authored REFINEMENT for exact tier/base/catalyst
else:
    remain idle with a specific blocked reason
```

Do not make an invalid advanced combination silently fall into an existing recipe that consumes its potion or vessel. The existing distillation path keeps priority for any exact old recipe collision. Build a single `BrewingMatch`-style result containing operation, required items, required blood, duration, and exact output snapshot. Actual class names may follow existing repository conventions.

### Costs for the first implementation

| Operation | Blood | Time |
| --- | ---: | ---: |
| D3 Ferric Poise refinement | 250 mL | 100 ticks |
| D3 two-effect compounding | 500 mL | 160 ticks |
| D5 three-effect vessel binding | 1,000 mL | 200 ticks |
| D5 vessel refill | 750 mL | 160 ticks |
| D5 Nerve Surge refinement | 750 mL | 120 ticks |
| D5 Fervent Reprisal refinement | 750 mL | 160 ticks |

Store costs in the authored advanced recipe definition or its configuration, not as one unconditional machine constant. Existing distillation values remain theirs.

## 4. Transaction boundary

Use a server-authoritative progression state: `IDLE → RUNNING(snapshot) → COMMIT`. Validate all requirements before starting and revalidate input identity, tier, heat, reservoir, and output capacity before commit. If any changes during processing, stop and leave unspent inputs and blood; never generate partial output. At commit, consume the exact input stacks and reservoir amount and install **one** result as one atomic server-side operation. Persist enough progress to resume safely or restart cleanly after chunk reload; no route may commit twice.

If the result slot is occupied by a dissimilar potion or a full stack, do not start. If automation removes a catalyst while running, abort without consuming resources. A recipe with an unsupported potion effect cannot be accepted simply because its item is a potion. Ordinary gourd filling and blood seep remain independent of the advanced operation and must not race its commit.

## 5. Data and API contracts for later documents

Create a registry-backed component representing:

```text
RefinedBrewData:
  recipeId
  one primary effect specification
  optional attached behavior and rebound policy

CompoundBrewData:
  base registered potion/effect snapshot
  catalyst registered potion/effect snapshot
  optional eligible refined attachment

VesselFormulaData (on the transformed base potion):
  base effect specification
  unordered pair of catalyst effect specifications
  current doses (0..3)
  max doses = 3
```

Do not serialize translated display names. Decode with bounds: no duplicate primary effects, no fourth effect, no negative or greater-than-3 doses, no missing registry references. The component decoder should reject invalid states safely without consuming a potion. Use the platform's 1.21.1 potion contents system for ordinary effect application; carry Hemomancy provenance and attached behaviors separately.

The server recipe resolver produces a preview summary. The client displays a truthful name, two/three effects, time, blood cost, and blocked reason without inventing a result locally. Text for blocked states: upgrade required, no heat, insufficient blood, wrong refill formula, vessel full, unsupported effect, or output occupied.

## 6. Work packages and evidence

### A. Tier persistence and slot safety

- [ ] Add tier enum/persistence with old-save BASE default and clear invalid-state handling.
- [ ] Append and gate Catalyst 2; update menu, slot validators, sided handlers, break drops, and relevant synchronization.
- [ ] Verify save/reload on all three tiers; break/reinstall gives one machine and exactly the installed kits, and no hidden slot item disappears.
- [ ] Verify base Alembic and Pallid Retort process the same old recipes and container outputs as before.

### B. Match and commit

- [ ] Implement exact route precedence and immutable `BrewingMatch` snapshot.
- [ ] Add authored advanced-recipe serializer separate from shared `distillation_recipe`; load registered potions and enzymes by IDs, reject unknown and ambiguous recipes at data reload.
- [ ] Prove invalid inputs, insufficient reservoir, missing heat, full output, catalyst removal, and chunk unload consume nothing and produce nothing.
- [ ] Prove a valid one-item recipe produces one output and pays the configured blood once, even across save/reload or hopper access.

### C. Client feedback

- [ ] Add tier-specific slot/widget visibility and preview/blocked tooltips. The visual second catalyst at D5 maps to the appended real slot.
- [ ] Verify the GUI does not advertise a valid result for an unsupported source potion; confirm server denial matches the client reason.

**Gate to documents 03/04:** Run the repository's existing Alembic and Pallid Retort tests plus focused tier/transaction tests, then record commands and results. Do not begin combat-effect work while the new recipe commit can duplicate inputs.
