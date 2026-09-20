# Phlebotomist’s Cabinet

A Bloodwood specimen cabinet with nine pigeonholes. Each holds **64 exactly matching full Blood Vials**, for **576 vials total**. Craft it with Bloodwood Planks in the centre and four edge positions, with Hematic Iron Scraps in the four corners. The Alchemist teaches its recipe in late Degree 1 after you personally identify three distinct creature sources and learn Borrowed Physiology. See [Clinical Blood Tools](Clinical-Blood-Tools.md).

## Store and retrieve

Right-click to open the cabinet. Each cell shows one specimen, its quantity and source. Hover for the full vial tooltip and capacity. Unknown specimens show a question mark and “Properties Unknown”; identified specimens show their tendency colors and full properties in the tooltip.

- Click a held full vial into an empty or matching cell to deposit it.
- Either mouse button withdraws one vial onto an empty cursor.
- Shift-click a player vial to fill matching capacity first, then an empty cell.
- Shift-click a cabinet cell to withdraw as many individual vials as your inventory can hold.
- Different specimens never exchange places through a cell click. Swap keys, throwing, creative cloning, dragging and double-click collection do not extract virtual quantities.

Every component must match, including custom names and identification. Independently examined otherwise identical vials merge; examined and unknown samples do not. An extra empty cell can hold the same specimen when its first cell is full. Racks, empty vials and unrelated containers cannot be stored. Loose vials remain unstackable.

## Glazing

Use a clear glass pane to fit a viewing panel. Sneak-use a Knapper to remove it without damaging the tool. Survival consumes one pane on fitting and returns one on removal, dropping it in front if your inventory is full. Creative fitting costs nothing and removal returns nothing. The door opens while anyone is viewing the menu, then closes shortly after the last viewer leaves.

## Automation and dismantling

Hoppers can insert and withdraw individual full vials from every side. A comparator measures total quantity: empty is 0, half-full is 8, completely full is 15.

**Remove all specimens before breaking the cabinet in survival.** A filled cabinet withstands ordinary explosions and cannot be moved by pistons. An empty cabinet drops itself, preserving its glass panel. The carried block has no specimen inventory.

**Intentional creative destruction discards all contents without spilling vials.** Commands and forced removals by other mods are outside the ordinary survival protection.

[Developer validation and live checklist](Phlebotomists-Cabinet-Validation.md).

For portable storage, rebuild an empty cabinet into a [Phlebotomist’s Field Case](Phlebotomists-Field-Case.md). The Field Case holds nine distinct identities, sixteen vials each, and opens only while placed.

With a glass front fitted, aim at an occupied compartment in the world to see the blood source and current sample count beside the crosshair. Empty compartments and wooden dividers show no tooltip.

Each occupied compartment contains a modeled glass blood vial with an iron stopper. These are baked parts of the placed block model: adding the first sample fills that compartment visually, and removing the last clears it. One modeled vial represents the compartment, not its exact stored quantity.
