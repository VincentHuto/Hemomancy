# Track 1: the Conversation merge

**Status (2026-10-31): built, uncommitted.** What later tracks code against:
- `DialogueCategory { QUESTS, INQUIRIES, CONVERSATION, TRADE }`; `DialoguePresentation.categories()` returns them in that order. `TRADE` has no topics yet, so its card draws disabled. Track 4 adds `TRADE` topics to the hub presentation; the screen already draws `dialogue/categories[_material]/trade[_selected]` and `dialogue/icons/trade` (placeholder art) and reads `hemomancy.dialogue.category.trade[.summary]`.
- `DialogueOptionPresentation` gained `boolean lore, boolean quest` (wire), with `withLore()`, `withQuest()` and static `loreLine()` / `questWork()`. `DialogueOption.withLore()` / `withQuest()` copy an option with the flag set; that is how tree classes tag options.
- `DialogueTopic` gained `boolean lore` (wire), `readPath()` (a lore card is read under the old `lore/<slug>`), and the static helpers `conversationId(slug)` and `loreReadPath(slug)`. `DialogueHubFactory.LORE_ICON` is the in-list glyph (`dialogue/icons/lore`).
- `DialogueHubFactory.decorate`: an option is Quest Work when it carries `quest` or its ids hold a mission keyword (the quest keyword list is unchanged); every other root option is one `conversation/<slug>` card in authored order. Options with an event or no next node get a `__hub_conversation_<n>` confirm node. `__hub_conversation`, `conversation/speak_freely`, `hemomancy.dialogue.topic.speak_freely` and `hemomancy.dialogue.conversation.prompt` are gone. `classify` and its lore keywords are gone.
- `common/entity/npc/trade/NpcTradeSides` (Track 2 extends it): `Side { HARBINGER, UNSTAINED, COURT, CIRCUS }`, `side(id)`, `is(id, side)`, `all()`, `CIRCUS_RINGMASTER`. Speakers: the eight Harbinger ids, `acolyte`/`zealot`/`guardian`/`scout`, every `CourtRole.entityId()` (`court_<role>`), `circus_ringmaster` and the eight performer role ids (`circus_acrobat`, `circus_fire_eater`, `circus_knife_thrower`, `circus_stilt_walker`, `circus_strongman`, `circus_beast_tamer`, `circus_threadkeeper`, `circus_understudy`).
- The Circus goes through the hub: the Ringmaster as `circus_ringmaster`, each performer as its `facultyRoleId()`. Newcomer "ask the Vicar" guidance is skipped for the `CIRCUS` side. Ringmaster trial/accept/reject/finale/repair and the school's testimony/lesson/practical/guest/inspect/report/claim are `withQuest()`; the Ringmaster's `school` card and the school's history and colleagues cards are `withLore()`.
- Golden `src/test/resources/dialogue/lore-options.txt` (`<speaker> <option text key>`), checked by `DialogueLoreFlagTest`. Add a line when you write a lore option.
- `PacketHandler.PROTOCOL_VERSION` is 28; `payload-registry.txt` rewritten.

Deviations from the plan:
- The golden was seeded by a source scan, not a one-off JUnit test: most root options are only reachable through decorators that need a live `ServerPlayer`, so a test could not build them. The scan applied the old `classify` (leave and inquiry skips included) to every `new DialogueOption(...)` in the hub tree classes, resolving constants; the 30 sites with computed arguments were resolved by hand. `DialogueLoreFlagTest` then sweeps every primitive/record-argument tree builder of the hub NPCs with seeded samples and checks flags against the golden both ways (66 of the 74 Harbinger/Unstained lines are reachable that way; the floor is pinned).
- The golden keeps the old classifier's keyword accidents, as the plan asked (no drift): every Cicatrix Anchorite option without a quest keyword was "lore" because "anchorite" contains "rite", so `replace_pattern` and `accept_referral` carry the lore flag. `SuccessionDialogue`'s `explain` option is not flagged: it is added after the hub is built, so the old classifier never saw it. Worth a later pass.
- Circus golden lines use the speaker `circus_performer` for the shared school options, since every performer builds the same keys.
- The static factories are `loreLine()` / `questWork()`, not `lore()` / `quest()`: those names are the record accessors.
- Category card sprites are not per theme: they live in `categories/` and `categories_material/` only. The lore cards there were renamed to `trade` and hue-shifted to amber as placeholders. `icons/trade` is 32 px like its siblings (drawn at 16).
- `hemomancy.dialogue.category.trade.summary` reads "What they want, and what they give for it."; the glyph tooltip key is `hemomancy.dialogue.topic.lore_marker`.

**Goal:** Lore and Conversation become one box, Conversation. Every root option is its own card. Lore cards carry a lore glyph and an unread dot. The freed fourth slot becomes `TRADE`, empty until Track 4 fills it.

## Current state
- `DialogueCategory { QUESTS, INQUIRIES, LORE, CONVERSATION }`, sent by ordinal in `DialogueTopic.toNetwork`.
- `DialoguePresentation.CATEGORIES` fixes the four hub cards in that order.
- `DialogueHubFactory.decorate(tree, npcId, knowledge)` walks the root options. It skips leave and inquiry options, and `classify(option)` sorts the rest by keyword: a quest list, a lore list (`about`, `lore`, `history`, `order`, `rite`, `ritual`, `machine`, `degree`, `truth`, `explain`, `what_`, `why_`, `teach`, `blood_crafting`, `annetta`), else conversation.
  - Each quest or lore option becomes its own `DialogueTopic`. An option with an event or no next node gets a synthetic `__hub_<category>_<n>` confirm node.
  - Every conversation option is folded into one `__hub_conversation` node behind a single `conversation/speak_freely` topic.
  - Lore topics are `unread` when `DialogueKnowledge.hasRead(readId(npcId, "lore/<slug>"))` is false, and get `DialogueAttention.NOTICE`.
- `DialogueScreen.openTopic` sends `DialogueTopicOpenedPacket` for an unread `LORE` topic and marks it locally read. `renderTopicStatus` draws `statusIcon("unread")`.
- Sprites: `dialogue/icons/<category>`, `dialogue/categories/<category>[_selected]` and the `categories_material/` twins, per theme folder (`blood`, `fungal`, `still`, `unstained` and their `_material` sets).
- Lang: `hemomancy.dialogue.category.{lore,conversation}[.summary]`, `hemomancy.dialogue.topic.speak_freely`.
- Callers: 13 `ProgressionDialogueNpc` entities (eight Harbinger, four Unstained, `CourtMemberEntity`) all go through `DialogueHubFactory.decorate`. The Circus doesn't (see below).

## Target

### The enum and the box list
- `DialogueCategory { QUESTS, INQUIRIES, CONVERSATION, TRADE }`. `LORE` is gone. `fromOrdinal` still falls back to `CONVERSATION`.
- `DialoguePresentation.CATEGORIES` lists them in that order.
- Protocol bump (the ordinals moved).

### An explicit lore flag
- `DialogueOptionPresentation` gains `boolean lore`, with a `withLore()` copy and a `lore()` factory. It goes on the wire, so it rides the same protocol bump.
- Today's classification must not drift while the flag goes in. Before changing `classify`, write a one-off test that records, for every NPC tree the hub builds, which root options land in `LORE` now. Save it as a golden, `src/test/resources/dialogue/lore-options.txt` (`<npc> <option text key>` per line).
- Tag every option in the golden with `.withLore()` in its tree class (`HarbingerAlchemistDialogueTrees`, `HarbingerVicarDialogueTrees` and the rest).
- `DialogueLoreFlagTest` then asserts the flagged set equals the golden. Delete the lore keyword branch from `classify`. The quest branch stays for now; making quests explicit is a separate cleanup, not this track.
- A later lore line is flagged when it is written. The golden is rewritten with it.

### Cards
- `decorate` builds one `CONVERSATION` topic per non-quest root option, in the tree's own order (lore and talk interleaved as authored). It stops folding them into `__hub_conversation`.
  - Topic id: `conversation/<slug>`.
  - A lore option sets `topic.icon` to `dialogue/icons/lore`. It is `unread` (with `NOTICE`) by the same rule as today, computed from the **old** read id `readId(npcId, "lore/<slug>")`, so existing read records still match.
  - An option with an event, or no next node, gets the same synthetic confirm node quest topics get.
- `conversation/speak_freely` and its lang key go away. The `__hub_conversation` node goes with it, unless a test shows something else opens it by id (check `DialogueAttentionResolver`, which picks the first attention topic).
- `DialogueScreen.openTopic` sends `DialogueTopicOpenedPacket` for any unread topic with the lore flag, not for any `LORE` topic. Since the category is gone, the topic needs to carry the flag: add `boolean lore` to `DialogueTopic` (wire change, same bump), or test `icon` equality. Prefer the field; an icon is presentation, not meaning.
- Conversation cards use the 44-px height lore cards use now.

### Sprites and lang
- Rename the `lore` category card sprites to `trade` art in every theme folder (Track 4 draws the final trade art; a recoloured placeholder is fine here). Keep `dialogue/icons/lore` as the in-list glyph and draw a 16-px `dialogue/icons/trade`.
- Lang: drop `hemomancy.dialogue.category.lore[.summary]`. Rewrite `hemomancy.dialogue.category.conversation.summary` to cover both ("Talk, and what they know of the old work."; check it against the rules shared by all speakers in `NPC_VOICES.md`). Add `hemomancy.dialogue.category.trade` ("Trade") and its summary. Add `hemomancy.dialogue.topic.lore_marker` for the glyph's tooltip ("Lore").

### The Circus joins the hub (Q-4)
Today `CircusRingmasterEntity.mobInteract` sends a focused tree from `dialogue(player)`, and every `CircusPerformerEntity` subclass (acrobat, fire-eater, knife-thrower, stilt-walker, `CircusFacultyEntity`) sends `CircusSchoolDialogue.tree(...)`. Neither goes through `DialogueHubFactory`.
- Both call `DialogueHubFactory.decorate(tree, speakerId, player)` before sending, as `CourtMemberEntity` does. Speaker ids: `circus_ringmaster`, and `circus_<performer>` from each performer's role. Confirm the existing role names in `CircusPerformerRules` and reuse them.
- Skip newcomer guidance for Circus speakers. `decorate` adds an "ask the Vicar" option at D0, which needs a `hemomancy.dialogue.newcomer.<npc>` key and doesn't fit the pavilion. The pavilion allows ordinary D0 and inactive-blood visits (AGENTS.md), and the Circus is not the Vicar's errand. Skip by the `CIRCUS` side in `NpcTradeSides`, not by an id check.
- Keyword guessing would sort the Ringmaster's options wrong: `succession_warning`, `liberation_warning`, the trial, the finale and the repair would land in Conversation. Before the merge, mark them explicitly as quest options. The cleanest fix is a `quest` flag on `DialogueOptionPresentation` next to `lore`; that starts the explicit-quest cleanup for these options alone. The DANGER style the confirm options use stays as it is.
- `CircusReferralDialogueTest` and the `circus_*` GameTests must still find the trial, accept/reject and finale options, now inside Quest Work.
- The school's `school` node is talk with lore lines: flag the curriculum and faculty lines as lore.

### An empty Trade box
Until Track 4 lands, `TRADE` has no topics, so its card draws disabled (`hasTopics` is false). Nothing else is needed.

## Tests to touch
`DialogueHubFactoryTest`, `DialoguePresentationTest`, `DialogueNavigationStateTest`, `DialogueThemeStyleConfigTest`, `DialogueAttentionResolverTest`, and every `*DialogueTest` that asserts a topic's category: `CircusReferral`, `DeepDarkCommission`, `MorphlingHandling`, `OverworldFungalSurvey`, `PhlegethonticCommission`, `VagrantMindInquiry`, `VoyagerIntroduction`. The GameTest `ClinicalBloodProgressionGameTests` also names a category. Rewrite their expectations where `LORE` becomes a flagged `CONVERSATION` topic. Don't delete them.

## Done when
- The hub shows Quest Work, Inquiries, Conversation and a disabled Trade, on every Harbinger, Unstained, Court and Circus speaker.
- Every option in `lore-options.txt` shows the glyph, and the unread dot clears on open and stays cleared after relog.
- `classify` has no lore keywords.
- Payload registry and protocol are bumped; `alphaCheck` passes.
