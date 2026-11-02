# Advanced Potion Brewing — Concept and Contracts

> **Scope:** What the player learns at D3 and D5, and the boundaries every implementation stage preserves.  
> **Source:** `HEMOMANCY_ADVANCED_POTION_BREWING_DESIGN.md`.

## 1. Play loop

```text
Brewing Stand → finished drinkable potion
Living Syringe / Vial Centrifuge → tendency enzyme
Ghastly Alembic with heat + reservoir blood
    D3 Condenser: refine one potion OR combine two into one bottle
    D5 Athanor: stronger refinements OR bind/refill a three-effect vessel
```

The Brewing Stand remains familiar. The Alembic rewards sample work and blood management without replacing ordinary brewing. The principal reward of D3 compounding is **two effects in one slot**, with both original bottles spent. D5 binds the base effect into a durable vessel: three potions establish its identity and first dose, then two matching catalyst potions and blood add each further dose. A filled vessel occasionally spends the user's blood instead of a prepared dose.

## 2. Progression

| Stage | Access | New capability | Station appearance |
| --- | --- | --- | --- |
| Base | Existing Alembic unlock | Current distillation, blood/flask/gourd handling | Current model |
| D3 | Alchemist lesson → Hematic Condenser Kit → safe Lesser Cardinal Rite | Enzyme refinement and two-effect compound bottles | Coil, two return tubes, raised receiving bulb |
| D5 | Alchemist mastery lesson → Sanguine Athanor Kit → safe Greater Cardinal Rite on D3 station | Catalyst 2, three-effect vessel binding/refill, stronger refinements | Taller crown, three asymmetric bulbs, three blood channels |

Lessons grant each kit once. Rite escrow preserves the kits on a harmless abort, and breaking a successful upgraded station drops the installed kits once for reuse. Degree gates apply to performing the rite; a shared already-upgraded machine is usable by other players under normal machine access rules.

## 3. Distinct operation verbs

| Verb | Recipe | Output contract |
| --- | --- | --- |
| **Refine** | Finished potion + authored enzyme | One fixed named potion with an authored effect/attached behavior |
| **Compound** | Finished base + finished catalyst + blood | One drinkable bottle with both independent duration effects |
| **Bind** | Base potion + two catalyst potions + blood | The base potion becomes a named bound vessel with 1/3 doses |
| **Refill** | Same bound vessel + two matching catalyst potions + blood | Same formula, one additional dose up to 3/3 |
| **Substitute** | Use a filled vessel; if blood is available, roll 20% | On success pay 1,000 mL from user's volume and keep dose; otherwise spend one dose |

The Alembic selects operations by valid input types and tier, without a mode toggle. The flask slot keeps its existing blood-handling role and has no binding role.

## 4. Non-negotiable invariants

1. **One output per recipe:** Neither a refined bottle nor a compound recipe duplicates original potion counts.
2. **Single source of truth:** Component data stores identity, effects, drawbacks, exact refill signature, and doses; server validation decides actions. Client preview reads that result.
3. **Bound base costs once:** A D5 vessel's base potion is spent only at binding. No zero-dose blood-only activation.
4. **No recursive mixing:** D3 compounds do not become D5 vessel ingredients; a vessel does not enter another compound. Three primary effects is the cap.
5. **Safe upgrade rites:** No combat, timer, or lost one-time kit. The station remains at its old tier until atomic culmination.
6. **Existing effect restrictions:** Mnemonic Whispers redrinking still applies if Whispers is the secondary effect. Event and rebound behavior accompanies an eligible refined component.
7. **One physical machine:** Changes to the Alembic's registry ID and functional block footprint are unnecessary; the renderer may extend beyond one block visually.

## 5. First-release boundaries

- Drinkable potions with one duration-based primary effect. Reject instant and empty-effect inputs.
- D3 accepts one refined component only where its attached behavior is supported; initially this is Ferric Poise.
- D5 pilot Nerve Surge and Fervent Reprisal are standalone outputs for the first release. Their rebound/retaliation integration into compounds is a separate tested expansion.
- No automatic tendency × potion matrix; recipes are individually authored.
- No splash, lingering, tipped-arrow, or modded-potion compatibility promise yet. Vanilla and Hemomancy source effects can be included when their use-time behavior is verified.

## 6. Step checklist

- [ ] Locate the current 1.21.1 Alembic, potion registration, Vial Centrifuge, rite, dialogue, renderer, and test code. Write down true paths and signatures before modifying them.
- [ ] Confirm the existing six-slot layout, hopper faces, heat condition, reservoir units, active recipe serialization, and shared Pallid Retort serializer in code.
- [ ] Keep this document's invariants in the acceptance notes for documents 02–05; if current code contradicts one, update the plan before changing the code.
- [ ] Run the smallest existing Alembic/Retort smoke tests as baseline and record their command and result for comparison at each gate.
