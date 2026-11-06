# The Sanguine Monolith's dialogue hub

Approved in chat, 2026-11-05. The user set the four boxes and the blood-only trade, "for things similar to the Vicar"; everything marked **(my call)** is overturnable.

## Why

The Monolith stands in for the Vicar's counsel from the fifth Degree (Lore §6.5a), but it speaks through a flat option list, a held item replaces the whole conversation, and it has no Trade box. The user wants it structured like a traditional NPC. This also answers two open playthrough decisions: D-40 [D5-6] (no D5 guidance, a held item hides the tree) and D-76 [D7-5] (no D7 line names the shatter or the Seed).

## What the player sees

Below Degree 5 the stone still gives its one silent refusal, and a cracked or broken stone still gives only its hint or "nothing more to say". From Degree 5, any right click, empty-handed or holding an item (cell hands still pass through to their own use), opens the same four-box hub as the Vicar:

**Quest Work**
- Counsel card, every Degree: what to do next, read from the same checks as the Vicar's Degree 5 and 6 guidance, in the stone's words. At Degree 7 it names pressing further and the Qliphoth Seed; at Degree 8, or with a Seed already taken, it says so instead.
- Degree 7: the Cornerstone card shows Turn In when claimable, Active when not yet, Complete once claimed **(my call)**.

**Press further** (user, 2026-11-05): not a card. From Degree 7 it sits in a glowing **Important** tab centred above Leave, because it is a special action. The tab replaces, for every NPC, the old jump into the first unread lore card: a hub always opens on its boxes, and the tab holds special actions and any card with a sigil or a ready Turn In; unread lore never counts. Press further opens the existing warning, whose button glows too; only that second press breaks the stone. Below Degree 7 the button does not appear at all **(my call)**, so it can't spoil what the stone becomes.

**Inquiries**
- One card per pack item the stone has an answer for, using the existing `dialogue_inquiry/monolith/` answers, exactly as the Vicar's box works.

**Conversation**
- "What are you?" as a lore card, plus three new lore questions per Degree **(my call on the text)** and two rapport lines (Known, Trusted). Sub-nodes go Back to the hub. "[Understood.]" is gone; "[Depart]" is the exit.

**Trade**: every card is a blood price, paid through `BloodAccounting` under `npc_tithe`.

| File | Degree | Blood | Gives | Stock |
|---|---|---|---|---|
| `monolith/measure` | 5+ | 500 mL | 2 Sanguine Salves | 1 use, 1 day |
| `monolith/weight` | 5+ | 750 mL | 1 Vascular Poultice | 2 uses, 3 days |
| `monolith/deep` | 6+, shown locked | 1,000 mL | 2 Vascular Poultices | 1 use, 3 days |

Numbers are **(my call)**; goods match the Vicar's.

**Screen**: its own crest and a portrait per Degree: cold stone at 5, skin-like at 6, fever-hot at 7 **(my call)**.

## Rules this changes

- `NpcTradeRegistry` rejects a second blood card on one speaker. The rule stays for everyone but the stone: `NpcTradeSides` gets a blood-only speaker list (`monolith`), those speakers' blood cards claim nothing exclusive, and the loader refuses any other want kind from them.
- A held item no longer gives a one-line answer or the "unknown" reply. `hemomancy.monolith.item_inquiry.unknown` and `hemomancy.dialogue.monolith.option.understood` are deleted.
- The open conversation lasts 6000 ticks from its last use instead of 1200 from opening **(my call)**: a hub is read and traded in, and a stale Press further would silently do nothing after a minute.
- A stone that has begun to break gives no Cornerstone to a conversation opened before the press.
- The Cornerstone event id becomes `monolith_cornerstone_claim`, so the hub's existing state rule reads it as a claim.

## How a block trades

Trade and topic payloads carry the tree's entity id, already `-1` for the stone. `NpcTradeSpeaker.near(player, id, reach)` resolves `-1` through `MonolithDialogueContext` (the stone this player's conversation is open on, in reach, same dimension, same block entity) and anything else as a living entity. `SanguineMonolithBlockEntity` implements `NpcTradeSpeaker` and refuses while breaking or broken, or below Degree 5. Payload layouts don't change, so there is no protocol bump.

## Non-goals

No new dialogue screen, no theme change, no Archon path acknowledgment lines (`withArchonAcknowledgment` stays the four NPCs), no change to the walk, the gateway or who may press further (D-73 stays open). Fragments and the Seed are never traded.

## Copy

All NPC lines follow `docs/NPC_VOICES.md` (Monolith sheet): short and heavy, no contractions, third person at D5, "I" from D6, questions only at D7, temperature in every bracket line, no plain statement of the infection before D7. The exact text is in `en_us.json`.
