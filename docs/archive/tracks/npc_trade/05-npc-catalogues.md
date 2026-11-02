# Track 5: what each NPC trades

**Status (2026-10-31): built, uncommitted.** 49 cards across 29 speakers under `data/hemomancy/npc_trade/<speaker>/`,
written with their lines in `en_us.json` (`hemomancy.trade.<speaker>.<offer>.title/.line`, `.opens` on Court cards).
What later tracks code against:
- **Per-speaker lines**: `hemomancy.trade.<speaker>.thanks` for every speaker with cards; `.thanks_fine` where a card can
  grade FINE; `.appraise.poor` where a card's grader can refuse (durability, tendency, specimen); `.refuse_harbinger` and
  `.stained` for all four Unstained speakers. `DialogueTradeView` now voices a refusal: `REFUSED_POOR` shows
  `.appraise.poor` and `REFUSED_STAINED` shows `.stained` when the key exists (else the plain reason).
- **Stained goods**: `NpcTradeSides.STAINED` (`#hemomancy:trade/stained`, new tag of outpost goods) and
  `NpcTradeSides.refusesStained(speaker, stack)`. `NpcTradeService` refuses an Unstained card with
  `REFUSED_STAINED` (`hemomancy.trade.refused.stained`, in `REASON_KEYS`) when a chosen slot that would pay the card holds
  a stained stack, before anything is taken. `Party.stained(speaker, slot)` is a new default method (false), so fakes
  needn't change; `PlayerTradeParty` reads the tag. The Trade box grades a stained stack POOR for an Unstained board, so it
  is never pre-picked.
- **Graders**: `NpcTradeAppraisals.TENDENCY` (`appraisal_tendency`, required, an `EnumBloodTendency` name): an empty
  vessel or blood without that tendency is POOR, a matching sample FAIR, one already identified under the microscope FINE.
  `NpcTradeAppraisals.SPECIMEN`: an empty Specimen Jar is POOR, an ordinary creature FAIR, a `hemomancy:` creature FINE.
  `Grader` now takes a `clientSide` flag (the tendency grader reads `BloodProfileData`'s client or server copy);
  `grade(stack, appraisal)` is the server's grade and `grade(stack, appraisal, true)` is the Trade box preview. `Spec`
  gained a `check` of its param values, which the loader runs (a bad or missing `appraisal_tendency` fails the file).
- **Tags**: `#hemomancy:trade/curiosity` (the Ringmaster's want: shells, horns, heads, mob trophies, wreck logs) and
  `#hemomancy:trade/never_sold` grew to cover the Mycophant Tendril, the Ringmaster's topper, the Vein-Mason's Scar Blank,
  Consecrated Copper (the Podium pledge), and the Saint residuum, recollections and relics.
- **Tests**: `NpcTradeCatalogueDataTest` parses every shipped card through `NpcTradeLoader.parse` (Hemomancy ids against
  the frozen registry golden, vanilla ids trusted), runs `NpcTradeRegistry.validate`, rejects a want/give loop at one
  NPC, checks the per-speaker keys and that Court cards are customs on roles that take one. The tendency/specimen grades and
  the `appraisal_tendency` check are in `NpcTradeLoaderTest`; the stained refusal is in `NpcTradeServiceTest`.

The catalogue as built (wants, then gives; `fine` is `give_fine`):

| Speaker | Cards |
| --- | --- |
| Alchemist | 4 recycled enzyme → enzyme primer; 2 Ferric `bloody_vial` (tendency, D2, locked carrot, 3/2d) → 2 hematic iron powder, fine 4; 3 spore sac → 2 fermented spider eye; 250 mB blood (1/day) → 2 bleeding bulb; 2 frigid enzyme (rapport 30, locked carrot) → incandescent enzyme |
| Artificer | 8 scrap → 2 powder; worn iron/diamond tool (durability) → ferric binder, fine 2; bundle 4 string + blood crystal shard (1/3d) → suture needle; 4 chitinous husk (D3, locked carrot) → crimson lacquer |
| Mnemonist | 2 faded memory → memory thread; filled specimen jar (specimen, 2/2d) → memory thread, fine 2; 2 book → 3 glow ink sac |
| Vicar | 500 mB blood (1/day) → 2 sanguine salve; 3 blood crystal shard (D4, locked carrot) → vascular poultice |
| Hermit | 3 bread → candle (rapport 2) |
| Vein-Mason | 8 bone → 4 bone bricks; 6 clay ball → 4 polished venous stone |
| Voyager | salt-stained voyager log → 3 erythrocoral fragment; filled map → 4 cooked pelagic herring |
| Votary Wayfarer | 12 kelp → 2 raw pelagic herring; 6 prismarine shard → 2 erythrocoral fragment; nautilus shell → calcified erythrocoral |
| Zealot | 6 copper ingot → 2 Lethean Dew; 3 amethyst shard (Unstained stage 1, locked carrot) → Draught of Still Waters |
| Acolyte | 4 `#minecraft:wool` → 2 Lethean Poppy; 6 bread → 2 Lethean Dew |
| Guardian | 6 iron ingot (2/day) → Lethean Brew; 16 `#minecraft:arrows` → 3 bread (Blood Bolts are stained); worn iron/mail armour (durability) → 6 iron nugget, fine 12 |
| Scout | 4 paper → 2 Lethean Dew (rapport 2) |
| Court | One `custom` card each for the eight roles that take their house's custom (gardener, tidewarden, nursery_tender, linesman, host, bellwarden, marrow_tender, offering_bearer) |
| Ringmaster | 1 `#hemomancy:trade/curiosity` (rarity, 3/day) → 2 circus curtain, fine laughing mask + 2 curtain; 16 `#minecraft:planks` → 2 troupe rigging; bell (D4, locked carrot) → 4 puppeteering thread |
| Performers | acrobat 8 string → potion of slow falling; fire-eater 4 blaze powder → potion of fire resistance; knife-thrower worn `#minecraft:swords` (durability) → 8 arrows, fine 16; stilt-walker 16 sticks → 4 scaffolding; strongman 4 iron ingot → potion of strength; beast tamer 8 wheat (1/3d) → name tag; threadkeeper 6 paper → 2 puppeteering thread; understudy 3 book → mourning mask |

Deviations from the plan, with reasons:
- **Alchemist "Fresh blood" gives 2 Bleeding Bulbs, not 2 Bloody Flasks.** A flask refills 2,500 mL when used, so 250 mL for
  two would be a 5,000 mL loop.
- **"Ferric draw" wants filled Bloody Vials, not Ferric Enzymes.** An enzyme's tendency is fixed by its item, so a
  tendency grader on it reads nothing; a vial's blood profile is what the Alchemist can appraise. The `tendency` grader
  reads that profile and treats an identified sample as FINE.
- **Artificer**: the suture bundle takes a blood crystal shard instead of scrap (the scrap card already wants scrap, and
  two cards may not want the same item); "Worn tools" pays Ferric Binder, not scrap or iron (scrap would loop with the
  scrap card, and there is no hematic iron ingot). "Offcuts" is dropped: no Vein-Mason offcut item exists. A D3 husk
  → lacquer card replaces it.
- **Mnemonist**: "Thread back" (memory thread → paper goods) is dropped because the same NPC gives memory thread, which
  breaks the no-loop rule; 2 books → 3 glow ink sacs takes its place. `freshness` became `specimen`: the jar stores the
  captured creature but no capture time, so there is no age to read. Graded instead by what it holds.
- **Vicar**: "Misbegotten leavings" is dropped (no such item); the D4 carrot is blood crystal for the chapel.
- **Vein-Mason**: no scar or effigy gives. The Scar Blank is the Vein-Mason's lesson reward (`VeinMasonDialogueEvents`),
  so it is now `never_sold`; bone and clay pay in building stone.
- **Voyager** doesn't want the Brine-Sealed Letter: it is a Court referral (`CourtDiscoveryProgress`), and taking it
  back would undo the referral. A filled map takes its place.
- **Zealot**: no salt item exists, so there is no salt card. Consecrated Copper is never sold (it is the Podium pledge).
- **Scout**: she is dying in her geode, so one card: paper to finish her notes.
- **Court**: only the eight roles that take a custom get a card. `CourtHospitality.customRefusal` refuses a custom from a
  role without `Offer.CUSTOM`, so a card on the other ten would always be refused, and the one-want rule allows one custom
  card per speaker. The welcome gift, the Lightkeeper's lesson and a lead's welcome keep their own dialogue triggers; the
  card's `opens` line names what standing leads to (guest standing, then joining at the fifth Degree). No `thanks_fine`,
  since a custom never grades.
- **Ringmaster**: "Ring repair" is a supply card for planks; the route repair event is unchanged. "The finale's leavings"
  is dropped: the finale's own drop (the topper) is never sold, and a card can't switch its fine pay by degree. A D4 bell
  → thread card is the carrot instead.
- **Faculty**: no tendency-graded curiosity. The `tendency` grader reads blood samples, which aren't Circus goods, so each
  of the eight Circus performers has one act-themed card instead (acrobat, fire-eater, knife-thrower, stilt-walker,
  strongman, beast tamer, threadkeeper, understudy). None touches `CircusSchoolDialogue`'s lessons.
- **`rank` grader** isn't shipped: no first-pass card wants a memory or engram.
- Not in this track: the docs in `docs/HEMOMANCY_REFERENCE.md` (it still says the Artificer, Wayfarer and Voyager vessel
  have no trades) and the wiki, which Track 7 owns; give counts against stack sizes and the potion components are
  checked only by the live loader, so Track 7's loaded GameTest must confirm `NpcTradeRegistry.errors() == 0`.

**Goal:** every hub NPC gets a small, distinct catalogue (four to seven cards in the first pass) that says something about who they are. Each card wants something different. What a side pays with fits its lore.

Item ids marked *(check)* are guesses at what the catalogue needs. Confirm each against `ItemInit` / `BlockInit` before writing JSON, and swap in the real id or drop the row. Ids without the mark exist today.

## How each side pays

### Harbingers: blood, specimens, and what's left over
The outpost runs on blood and on what the work leaves behind. Harbinger NPCs take enzymes, scrap, spore sacs and specimens, and **one card each may take blood itself** (a `blood` want through `BloodAccounting.pay` and the `npc_tithe` source). They pay in reagents, parts and know-how goods. They appraise hard: the Alchemist reads a draw's tendency, the Mnemonist reads a memory's rank.

### The Unstained: clean goods, and no deals with Harbingers (Q-2)
Copper, clean water, salt, wool, honest materials. They pay in antiseptic, Lethean and still-water goods. They **won't trade with a Harbinger at all**: from D1 on, their Trade box opens on a refusal line and nothing else. That isn't saintliness. It's a closed door, and the refusal line should sound like one person's contempt or caution, not policy. A D0 player (or an Unstained one) trades normally. Stained goods (anything tagged `hemomancy:trade/stained` *(new tag)*) are still refused at the card, with a line, so an undecided player carrying outpost goods hears about it.

Each Unstained speaker needs a `refuse_harbinger` line in their own voice: the Zealot cold, the Acolyte uneasy, the Guardian flat, the Scout wary.

### The Brined Court: customs, not prices (Q-1)
The Court keeps its gift economy (`CourtHospitality`). Court cards are `custom` wants: the house's custom (`court_offering/<house>` tags), recorded with `CourtLife.recordAct`, paid back in **standing**, not goods. What a Court card "gives" is shown as what that standing opens: the next welcome gift, a lesson, a lead's welcome. Those keep their existing triggers; the card only shows the way there. There is no stock and no appraisal.

### The Circus: curiosities and spectacle (Q-4)
The Crimson Troupe doesn't want blood (Q-3) or money. They want **curiosities** (odd finds from the world: mausoleum relics, wreck finds, strange specimens, mob trophies) and things that feed the show. They pay in Circus props and performer goods. The pavilion is open to D0 and inactive-blood visitors, so no Circus card needs active blood or a degree, except as a carrot.

## First-pass catalogues

### Alchemist
| Card | Wants | Gives | Notes |
| --- | --- | --- | --- |
| Spent enzyme | 4 × `recycled_enzyme` | 1 × `enzyme_primer` | Always open. |
| Ferric draw | 2 × `ferric_enzyme`, `tendency` (Ferric) | reagents *(pick)*, `give_fine` adds more | D2+. |
| Spore stock | 3 × `spore_sac` | brewing ingredients *(pick)* | |
| Fresh blood | 250 mB blood | `bloody_flask` ×2 | `blood` want; Harbinger, active volume. |
| Cold reagent | 2 × `frigid_enzyme` | `incandescent_enzyme` | One enzyme for its opposite, a small swap. `rapport_at_least` 30. |

### Artificer (the Redwright)
| Card | Wants | Gives |
| --- | --- | --- |
| Scrap in | 8 × `hematic_iron_scrap` | 2 × `hematic_iron_powder` |
| Worn tools | 1 iron or better tool, `durability` | hematic iron *(check)*; `give_fine` more |
| Suture work | 4 × string + 1 × `hematic_iron_scrap` (`bundle`) | 1 × `hematic_suture_needle` |
| Offcuts | Vein-Mason offcut *(check)* | station part *(pick; not a kit)* |

### Mnemonist
| Card | Wants | Gives |
| --- | --- | --- |
| Faded memory | 2 × `faded_memory` | 1 × `memory_thread` |
| Specimen | a filled specimen jar *(check)*, `freshness` | `memory_thread`, `give_fine` ×2 |
| Thread back | 4 × `memory_thread` | paper/ink goods *(pick)* |

Never a `memory_*` give (the loader bans it).

### Vicar
Few trades: the Vicar teaches. Two cards: a tithe of blood for a blessing consumable *(check)*, and Misbegotten leavings *(check)* for hematic goods. `show_locked` on a D4 card as a carrot.

### Cicatrix Anchorite (the Vein-Mason)
Stone, bone and mortar for scar and effigy materials (`zombie_husk_effigy` and its kin are rewards here only if crafting them is not a lesson proof; check `VeinMasonScarLesson` first).

### Voyager and Votary Wayfarer
Coastal and Reef goods. The Voyager wants map and route knowledge (`salt_stained_voyager_log`, `brine_sealed_letter`) and pays in Reef/Brined materials. The Wayfarer wants kelp, prismarine, and wreck finds.

### Hermit
One card at most. The Hermit has little and wants little: food for a scrap of advice (a lore line unlocked through Track 6, no item).

### Zealot, Acolyte, Guardian, Scout
Every card here is for D0 and Unstained players only (the side rule; no `requires` needed).

| NPC | Wants | Gives |
| --- | --- | --- |
| Zealot | copper, salt *(check)*; `stained` goods refused | antiseptic/Lethean consumables *(check)* |
| Acolyte | wool, paper, clean water bottles | still-water goods *(check)* |
| Guardian | iron, arrows, worn armour (`durability`) | Unstained gear repairs or supplies *(pick)* |
| Scout | map items, Misbegotten sightings *(item check)* | trail supplies |

### Court roles
One `custom` card per role, for its house's custom (`CourtHospitality.customAct(house)`), plus the welcome gift shown as the first standing step. The Lightkeeper's lesson and a lead's welcome show as later steps.

### Ringmaster
| Card | Wants | Gives | Notes |
| --- | --- | --- | --- |
| A curiosity | one item tagged `hemomancy:trade/curiosity` *(new tag)* | Circus props *(pick)* | `rarity` grader (Track 2 adds it with this row): rarer finds grade `FINE`. |
| Ring repair | building blocks for the ring *(check against the repair event)* | a ticket or favour item *(check)* | Must not stand in for the existing repair event; it's a separate supply card. |
| The finale's leavings | a trophy from the finale *(check)* | `give_fine` only for D4+ | `show_locked` carrot. |

### Performers and faculty
Each performer wants one thing tied to their act, and pays in what that act uses:
| Speaker | Wants | Gives |
| --- | --- | --- |
| Acrobat | rope/string, slime *(check)* | light-footed consumables *(pick)* |
| Fire-eater | blaze powder, fuel | heat-themed props *(pick)* |
| Knife-thrower | worn blades (`durability`) | throwing goods *(pick)* |
| Stilt-walker | sticks, wool | height props *(pick)* |
| Faculty | curiosities of their school's tendency (`tendency` grader) | school reagents *(pick)* |

None of these replaces the school lessons in `CircusSchoolDialogue`; a trade never counts as a lesson.

## Lines
Every card has a `line` and every speaker has `thanks`, `thanks_fine`, `appraise.poor` and a refusal per reason. Write one speaker at a time against `docs/NPC_VOICES.md` (rhythm, contractions, questions, vocabulary) and keep each inside the contrast map. Roughly 6 to 10 keys per NPC.

## Balance rules
- A trade is never a faster route to a progression proof than doing the lesson.
- A trade's give is worth a bit less than its want at `FAIR`. The value is in convenience, appraisal and rapport.
- No loop: the same item never appears as a want on one card and a give on another card from one NPC. The loader can check this; Track 7 adds it.
