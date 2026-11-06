# The Sanguine Monolith's dialogue hub: implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:executing-plans (the user's standing choice is native execution with one final reviewer). Steps use checkbox syntax.

**Goal:** the Monolith opens the Vicar's four-box hub: Quest Work with counsel, Inquiries, Conversation with new lore, and a blood-only Trade box.

**Architecture:** the tree builder keeps its degree split and gains a `hub` entry that runs the same decorators as the Vicar. Trade payloads resolve the block speaker through `MonolithDialogueContext`, and the block entity implements `NpcTradeSpeaker`. No payload layout changes.

**Tech stack:** NeoForge 1.21.1, Java 21, GameTests, JUnit, Pillow for the art.

**Spec:** `docs/superpowers/specs/2026-11-05-monolith-dialogue-hub-design.md`

## Global constraints

- Leave everything uncommitted in the working tree (standing user rule).
- All player text is an `en_us.json` key, written to the Monolith sheet in `docs/NPC_VOICES.md`.
- Only one Gradle run at a time; another session may share `build/`.
- Update the topic doc, the references and the open-question roll-up with the change (AGENTS.md).

## Review focus

Each is pinned by a test in Task 2.

1. A forged trade naming `-1` with no open conversation is dropped with no reply and no blood taken.
2. The stone starts breaking while a second player has its Trade box open: that player's trade is dropped.
3. A player who walked out of reach with the box open: dropped, nothing taken.
4. A full pack: refused and no blood taken; blood under the reserve: refused and no goods given.
5. Opening an entity's dialogue ends the stone's conversation, so a trade at `-1` afterwards is dropped.

---

### Task 1: Trade rules for a blood-only speaker

**Files:** `NpcTradeSides.java`, `NpcTradeRegistry.java`, `NpcTradeLoader.java`, `NpcTradeLoaderTest.java`, `data/hemomancy/npc_trade/monolith/{measure,weight,deep}.json`, `en_us.json`

**Produces:** `NpcTradeSides.bloodOnly(String speaker) -> boolean`.

- [ ] Add failing tests to `NpcTradeLoaderTest`: `aBloodOnlySpeakerKeepsEveryBloodCard` (two `monolith/*` blood cards both kept), `anOrdinarySpeakerStillKeepsOneBloodCard` (existing alchemist assertion stands), `aBloodOnlySpeakerRefusesOtherWants` (`monolith/x` with an `items` want is rejected).
- [ ] Add `monolith` to `SIDES` as `HARBINGER` and to a `BLOOD_ONLY` set.
- [ ] `NpcTradeRegistry.claims(offer, ...)` skips `exclusiveClaim` for a `bloodOnly` speaker's blood want. `NpcTradeLoader.parse` throws when a `bloodOnly` speaker's want is not `Blood`.
- [ ] Write the three cards from the spec's table (`deep` has `degree_at_least: 6`, `show_locked: true`, `rapport: 2`) and their `title`, `line` and `thanks` keys.
- [ ] Run `./gradlew.bat test --tests "*NpcTrade*"`. Expected: PASS.

### Task 2: A block can be a trade speaker

**Files:** `NpcTradeSpeaker.java`, `NpcTradeService.java`, `PlayerTradeParty.java`, `NpcTradeRequestPacket.java`, `NpcTradeSeenPacket.java`, `DialogueTopicOpenedPacket.java`, `MonolithDialogueContext.java`, `SanguineMonolithBlockEntity.java`, `NpcTradeGuardrailTest.java`, `PlaythroughPlayer.java`, `NpcTradeGameTests.java`

**Produces:**
- `MonolithDialogueContext.speakingTo(ServerPlayer) -> @Nullable SanguineMonolithBlockEntity`; `CONVERSATION_TICKS = 6000`.
- `static @Nullable NpcTradeSpeaker NpcTradeSpeaker.near(ServerPlayer, int id, double reach)`.
- `NpcTradeService.attempt(ServerPlayer, NpcTradeSpeaker, int replyId, ResourceLocation, int[])`; the `Entity` overload delegates, because GameTests call it.
- `PlaythroughPlayer.tradeAtBlock(ResourceLocation offerId, int... slots) -> NpcTradeResultPacket`.

- [ ] Add failing GameTests in `NpcTradeGameTests`: `theStoneTradesForBlood` (D5 pays 500 mL, gets 2 salves, rapport up, reply id -1, second use sold out), `theStoneDropsAForgedTrade`, `aBreakingStoneTakesNoTrade`, `theStoneTradesOnlyInReach`, `aFullPackOrShortBloodTakesNothing`, `anotherSpeakerEndsTheStonesConversation`, one per review-focus line.
- [ ] Implement the interfaces above. `SanguineMonolithBlockEntity implements NpcTradeSpeaker`: id `SanguineMonolithDialogueTrees.SPEAKER_ID`, `tradesWith` false while breaking or broken or below Degree 5. Topic-opened accepts `-1` when `speakingTo` is non-null. Change `pressFurtherAfterTheTalkExpires` to skip `CONVERSATION_TICKS + 1`.
- [ ] Run `./gradlew.bat npcTradeGameTestServer`. Expected: all required tests pass.

### Task 3: The hub

**Files:** `SanguineMonolithDialogueTrees.java`, `SanguineMonolithDialogueEvents.java`, `SanguineMonolithBlock.java`, new `CovenantProgress.java`, `HarbingerVicarDialogueTrees.java`, `HarbingerVicarEntity.java`, `DialogueRapportLore.java`, `en_us.json`, `lore-options.txt`, `DialogueLoreFlagTest.java`, `route-fingerprints.txt`

**Produces:**
- `SanguineMonolithDialogueTrees.SPEAKER_ID = "monolith"`; `hub(ServerPlayer, int degree) -> DialogueTree`; `EVENT_CORNERSTONE = "monolith_cornerstone_claim"`.
- `record CovenantProgress(boolean inBloodline, boolean faneComplete, boolean chamberReturned, boolean throneBound, boolean vigilComplete)` with `static of(ServerPlayer)` and `List<String> steps(int degree)`, returning key suffixes (`found_bloodline`, `found_fane`, `perform_bloodline_rite`, `chamber_return`, `bind_throne`, `complete_vigil`, `perform_hematic_order_rite`). The Vicar's `withCovenantGuidance` keeps its signature and reads `steps`.

- [ ] Rewrite the Playthrough assertions first: `PlaythroughArchonGameTests` (a held stick opens the hub and still offers the shatter), `PlaythroughSanctifiedGameTests` (a held bone keeps the guidance), `monolithAtD5` and `noPointerToTheShatter` (now required, DECISION comments removed).
- [ ] Node shape: root options in order are counsel (`withQuest`), press further (`withQuest`, no attention, because the screen auto-opens attention cards; D7), Cornerstone (`withQuest`, D7), lore cards, ask about item, leave. Sub-nodes Back to root. The Cornerstone's next node is `armature_cornerstone` when `ClaimState.READY`, `armature_cornerstone_unready` otherwise (same lines, still offers the event so the refusal line plays), `armature_cornerstone_claimed` when claimed.
- [ ] `hub` runs `forPlayer`, then `DialogueItemInquiryNodes.withInventoryItemInquiries(tree, player, SPEAKER_ID)`, then `DialogueHubFactory.decorate(tree, SPEAKER_ID, player)`. The block calls it for any held item; `itemInquiry` is deleted.
- [ ] Write the copy; delete the two dead keys; add the rapport row, the golden lines, and update the fingerprint lines.
- [ ] Run `./gradlew.bat test --tests "*Dialogue*" --tests "*NpcTrade*"`, then the playthrough GameTests for the Monolith. Expected: PASS, apart from the 6 known pre-existing playthrough failures.

### Task 4: Crest and portraits

**Files:** new `tools/oneoff/monolith_dialogue_art.py`, `textures/gui/sprites/dialogue/crests/monolith.png`, `textures/entity/sanguine_monolith_{cold,skin,fever}_portrait.png`, `SanguineMonolithDialogueTrees.java`, `DialogueSpriteResourceTest.java`

- [ ] Generate a 32×32 crest and three 48×48 portraits from the stone's own palette. Point each Degree's tree at its icon.
- [ ] View the PNGs, then prove the hub on screen with a review client: all four boxes at D5 and D7, the Trade box, a completed trade.

### Task 5: Docs and full check

**Files:** `docs/NPC_DIALOGUE_AND_TRADE.md`, `docs/HEMOMANCY_REFERENCE.md`, `docs/LORE_REFERENCE.md`, `docs/NPC_VOICES.md`, `docs/OPEN_QUESTIONS.md`, `docs/PLAYTHROUGH_TESTS.md`, `docs/ENDGAME_PATHS.md`

- [ ] Update card and speaker counts, the blood-only exception, the hub's speaker list, and §6.5a. Move D-40 and D-76 to rulings in the topic docs and the roll-up.
- [ ] Run `./gradlew.bat test`, then one fresh reviewer over the working tree.
