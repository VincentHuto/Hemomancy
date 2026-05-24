# Memory Auto-Equip Design

## Goal

When a player consumes a new manipulation memory, the memory should automatically equip while the player still has open normal manipulation slots. The Mnemonic Reliquary should only become necessary once those normal slots are full and the player needs to choose which learned memories remain equipped.

## Behavior

- Blood memory use still requires active blood magic and the proper initiatory degree for the memory rank.
- When a new manipulation is learned, it is added to the player's known manipulations as before.
- If the learned manipulation is not a fixed mechanical manipulation and the player has an open normal manipulation slot, it is equipped immediately.
- If normal manipulation slots are full, the memory is learned but not equipped.
- Blood absorption and blood projection remain fixed mechanical manipulations. They are not treated as normal learned-slot entries.

## Player Feedback

- If a memory is learned and auto-equipped, show a short actionbar message: `Memorized and equipped: <name>`.
- If a memory is learned but normal slots are full, show a short actionbar message: `Memory learned. Use a Mnemonic Reliquary to change equipped memories.`
- Duplicate memories and rank-gated memories keep their existing failure behavior.

## Implementation Notes

The change belongs in `BloodMemoryItem.use(...)`, because that is the path where memory items are consumed and learned. It should reuse `ManipulationEquipHelper.equipNameIfPossible(...)` and `ManipSlotHelper.getMaxSlots(player)` rather than duplicating slot counting logic.

## Testing

- Extend the pure manipulation helper test to cover auto-equip capacity behavior if helper changes are needed.
- Add a source-level regression test for `BloodMemoryItem` to verify it calls `ManipSlotHelper.getMaxSlots(...)`, attempts `known.equipManip(...)` after learning, and shows separate feedback for auto-equipped versus learned-but-full memories.
