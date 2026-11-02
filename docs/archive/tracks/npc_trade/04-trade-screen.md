# Track 4: the Trade box

**Status (2026-10-31): built, uncommitted.** What later tracks code against:
- **View:** `client/screen/dialogue/DialogueTradeView` (package-private) draws the `TRADE` category: the rapport bar (`hemomancy.trade.ui.rapport` + tier name + fill), the cards, or the speaker's refusal line centred in `textColor`, or `hemomancy.trade.ui.empty` when the board has no cards. `DialogueScreen.renderCategory` branches to it for `TRADE`; it reads `DialogueScreen.tradeBoard()`, so a fresh board from any `NpcTradeResultPacket` redraws the box. The hub's Trade card is enabled by a `TRADE` topic **or** a present board. A `TRADE` topic opened by `openTopic` (target `DialogueHubFactory.TRADE_TARGET`) opens the Trade box.
- **Layout:** `DialogueTradeLayout.calculate(content, cardCount, expandedIndex, wellCount, note)` gives `rapport()`, `cards()` (`Card(frame, body, note, wells, offer)`; `body` is always 54 px, the strip grows under it), `contentHeight()`, `maxScroll(viewport)`; `columns(width)` and `rapportFill(rapport)` are static.
- **Picks:** `DialogueTradePicks` (pure): `Stack(slot, count, Grade[] byPart)`, `prePick` (best grade, then largest, then lowest slot; never a POOR stack), `predict` (mirrors `NpcTradeService.plan`: slots in pick order, one part per slot, lowest grade wins), `carried` (`NONE`/`SOME`/`ENOUGH`), `toggle`, `retain`. Cards carried `ENOUGH` sort first within their `order`.
- **Sending:** Offer sends `NpcTradeRequestPacket(entityId, offerId, slots)`: picked slots for items/bundles, none for blood, all 36 main slots for a Court custom (the server takes the first that holds the house custom). The card greys until a reply for that offer, or `DialogueTradeView.PENDING_TIMEOUT_TICKS` (40) game ticks, since a rate-limited request gets no reply.
- **Replies:** `DialogueScreen.receiveTradeResult` now also calls the view. `DONE`/`DONE_FINE` collapse the card, play `ITEM_PICKUP` and put the speaker's `hemomancy.trade.<speaker>.thanks[_fine]` in the header (falling back to `thanks`, then to the reply's reason key, through `I18n.exists`, so **Track 5 only has to add the keys**). `REFUSED` keeps the card open and draws the reason key in `DialogueThemeStyle.dangerColor()` in the strip's note row.
- **Screen API for review/tests:** `DialogueScreen.entityId()`, `openCategory(DialogueCategory)`, `expandTradeCard(offerId)`.
- **Theme:** `DialogueThemeStyle` gained `dangerColor()`, `wellDarkColor()`, `wellBackgroundColor()`, `wellLightColor()` (derived from the theme's colours). Wells use `ScreenDrawUtils.drawSlotWell`, the pick ring `drawSimpleBorder`.
- **Sprites (`dialogue/icons/`, 32 px):** `grade_poor`, `grade_fair`, `grade_fine` (corner triangles; `grade_fine` doubles as the give-fine pip), `carried_full`, `carried_some`, `blood_drop`, `trade_arrow`. The Trade hub cards `categories[_material]/trade[_selected]` were redrawn as claret versions of the Conversation frames. `icons/trade` (Track 1's balance glyph) is kept. `DialogueSpriteResourceTest` lists the new icons.
- **Lang:** `hemomancy.trade.ui.{empty,rapport,fine_pays,give_count,blood,custom,uses_left,restocks_one,restocks_in,carried.enough,carried.some,grade,blood_confirm,custom_confirm,offer,offering}`.
- **Review client:** `npcTradeReviewClient` (row in `gradle/review-clients.gradle`, driver `src/gameTest/.../NpcTradeClientReview`). Drop `{"op":"create","name":"NpcTradeReview","seed":1}` into `build/npc-trade-review/operator/inbox/` after startup. It captures each hub, its Conversation box, the server-built Trade box, then a sample board shown through `receiveTradeResult` (every card state, the appraised strip with three graded swords, a refusal, a thanks), and the Zealot at D0 and at D1 (PASS/FAIL on the server's refusal board). Shots land in `build/npc-trade-review/screenshots`.
- **Tests:** `DialogueTradeLayoutTest`, `DialogueTradePicksTest`, `DialogueNavigationStateTest` (TRADE), `DialogueSpriteResourceTest`. No wire change; `PROTOCOL_VERSION` stays 29.

- **Verified in a client:** `runNpcTradeReviewClient` ran end to end on 2026-10-31 (29 shots, every PASS line green): the cards sort carried-first, the strip pre-picks the fine sword, the refusal note, the `Traded` header fallback, the D1 Zealot refusal and the Circus hub all draw. The speaker voice keys (`thanks`, `refuse_harbinger`) still show their fallback or raw key until Track 5.
- **Crest fallback:** `DialogueScreen.crest()` uses the default crest when the speaker has no crest sprite (the Circus speakers had a missing-texture square in the header since Track 1).

Deviations from the plan:
- There is no Circus `DialogueTheme` (themes are BLOOD, UNSTAINED, FUNGAL, STILL), so the Ringmaster's box is captured in the theme his tree already uses. Adding a Circus theme is a separate art job.
- No dialogue sounds exist in `SoundInit`, so a finished trade plays vanilla `ITEM_PICKUP` through the UI sound channel.
- The review client shows sample boards through the reply path because no offers ship until Track 5; its titles and lines reuse the Trade category keys. Once Track 5 lands, the `*-trade-server` shots show the real catalogue.
- The card's want and give rows share the top line (want icons, arrow, gives, fine pip, then the status on the right edge); title and carried dot on the second line; the NPC line takes up to two lines under it.
- Wells show every matching stack (POOR ones included, with their red corner) so the player sees why a stack wasn't picked; Offer lights only when `predict` returns a non-POOR grade.

**Goal:** the fourth hub card opens a list of want cards. The player sees at once what each NPC wants, whether they're carrying it, what it would grade, and what they'd get. One click picks the stacks, a second confirms.

## Where it lives
Inside `DialogueScreen`, as the `CATEGORY` view for `DialogueCategory.TRADE` (`renderCategory` already branches for `INQUIRIES`; add a branch for `TRADE`). Draw it in a new `DialogueTradeView` (client, `client/screen/dialogue/`) so the 640-line screen doesn't grow by another 300. Layout maths go in a pure `DialogueTradeLayout`, like `DialogueInquiryGridLayout`, so a unit test can pin it.

## A card
```
┌──────────────────────────────────────────────────────────┐
│ [want icons ×count]  →  [give icons ×count]     3 left   │
│ Ferric draw                                    ● carried │
│ "Two of the iron ones. Not the cloudy kind."             │
└──────────────────────────────────────────────────────────┘
```
- **Want side:** the ingredient's display stacks cycle every second (tag wants), with the count. A blood want draws the blood-drop icon and the amount in mB. A custom draws the house's crest.
- **Give side:** the gives. A card with `give_fine` shows a small "fine" pip; hovering it shows the better result.
- **Right edge:** uses left, or "Restocks in N days" when sold out, or the lock reason.
- **Carried marker:** the client scans its own main inventory for stacks that match the want. Filled dot when enough is carried, hollow when some, none when nothing. Cards with enough carried sort first within their `order` band.
- The NPC's `line` sits under the title in the speaker's text colour. It's their voice, not a description.

Card height 54 px (the quest height), in the theme's frame. Theme colours come from `DialogueThemeStyle`. Borders and slot wells come from `ScreenDrawUtils` (`ScreenChromeGuardrailTest`).

## Choosing and confirming
1. Click a card: it expands into a **selection strip**: the matching stacks from the player's inventory, each in a slot well with its predicted grade (Poor / Fair / Fine as a coloured corner). The client pre-picks the best-graded stacks that cover the count. The player can click wells to swap stacks.
   - The client grades with the same `NpcTradeAppraisals` graders (they're pure functions of the stack, in `common/`, so the client can call them). The server grades again; the client's grade is only a preview.
   - A blood or custom card has no strip; it goes straight to confirm.
2. Click **Offer**: send `NpcTradeRequestPacket` with the chosen slots, and grey the card out until the result comes back (the `pendingClaims` pattern the screen already uses for reward claims).
3. On `NpcTradeResultPacket`:
   - `DONE` / `DONE_FINE`: the NPC's reaction line in the header area (`hemomancy.trade.<speaker>.thanks` / `.thanks_fine`), the card list replaced, a short sound from the existing dialogue sounds.
   - `REFUSED`: the reason key under the card, in the danger colour. Nothing changed server side.

## A refused customer
When the presentation carries a trade refusal (an Unstained speaker and a Harbinger player), the Trade box shows only the speaker's refusal line, centred in the content area in their text colour. No empty card frame, no rapport bar.

## Rapport readout
A thin bar at the top of the Trade box: the tier name and progress to the next tier. No numbers, just the tier and the fill.

## Assets
- Category card sprites `dialogue/categories[_material]/trade[_selected]` in every theme folder, and `dialogue/icons/trade`. Draw them to match the existing four. Track 1 leaves placeholders.
- Grade corners and the carried dots as sprites in `dialogue/icons/`.

## Review client
`npcTradeReviewClient` (see the README): Conversation with lore glyphs, the Trade box empty, with matches, with a card expanded and three grades in the strip, sold out, locked, a refused trade, the Zealot refusing a Harbinger, and the Ringmaster's box in the Circus theme.

## Tests
- `DialogueTradeLayoutTest`: card rects, the strip wrap at narrow widths, scroll bounds.
- `DialogueNavigationStateTest` gains the TRADE category.
