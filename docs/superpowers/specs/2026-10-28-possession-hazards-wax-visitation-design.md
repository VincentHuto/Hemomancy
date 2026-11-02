# Abhorrent Gaze and Grab, Adipocere, Visitation (Section 4b) — Design

Date: 2026-10-28. Source: `docs/Endgame Path Asymmetry …_V2.md` ("Concentration", "Wax tiers", the offerings table) and the user's overnight request ("abhorrent thoughts concentration breaking gaze and grab, corpse wax item and scrapping mechanic", "cardinal rites for possession and visitation"). Overnight run, so every decision below is a ruling made on the user's behalf.

## D1. Abhorrent Thought: gaze and grab
- **Gaze.**
  - Every 20 ticks, an Abhorrent Thought drains **1 Concentration** from each possessing player within **10 blocks** it has line of sight to. The shell does not stop a gaze.
  - The readable edge: the gazed player gets Darkness for 40 ticks (a pulsing vignette) and a low heartbeat sound.
  - Players who are not possessing are unaffected, so the thought behaves as before outside possession.
- **Grab.** An Abhorrent Thought's melee hit on a possessing player breaks Concentration outright: ejection with **Mind Broken for 12000 ticks**, double the zero-Concentration ejection.
- **Rules** live in `AbhorrentGazeRules`.

## D2. Adipocere (corpse wax), wax tier 2
- **Item** `hemomancy:adipocere`.
- **Scraping.** Use an axe on an unharvested **bog body**: the body is marked harvested (it regrows as before), drops 1 adipocere, and the axe takes 1 durability. It plays the axe-scrape sound with wax-off particles.
- **Bog revenants** drop adipocere 35% of the time when killed by a player.
- **Ghastly Alembic distillation:** honeycomb with a Ruinous Enzyme catalyst (consumed) gives 1 adipocere. The doc's third input, a bleeding bulb, is dropped because distillation takes one ingredient plus one catalyst.
- **Possession offering.** An adipocere lying in the possession rite space adds **+3 max Concentration**.
- **Loot.** Mausoleum chests are wired in Section 5.

## D3. Rite of Visitation (Apotheos)
- **`cardinal_rite/visitation_rite`:** bond, `communion_lesser` floor, D8, no braziers.
- **Refused** unless the caster is an Apotheos and is not already in the Gardens.
- **Completion** carries the Apotheos physically into the Gardens: onto their heart-node if they have one, otherwise to the Spine landing.
  - It stores the usual Spine return point, so the Spine or a Podium brings them back.
  - It is the rite-space counterpart of Spine travel, for an Apotheos who has no Spine on hand or who wants to arrive at the sanctum.
