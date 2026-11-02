# Dictation and field notes verification

Verified 2026-09-26 in the current HutosLib/Hemomancy composite checkout.

- HutosLib book JVM suite: 48 tests passed (`../HutosLib/build/dictation-library-tests.log`).
- Dedicated Book Kit server suite: 6 tests passed (`build/dictation-gametest-final.log`), including placed-guide transfer/retrieval, inventory NBT round-trip, legacy inventory loading, blood/XP failure retention, successful charges/unlocks, exhausted-note fallback, and Escritoire crafting from the shared table.
- Updated `VirtualFieldNotesSourceTest` compiled and ran successfully.
- Migrated table model references, texture files, recipes and loot JSON validated. Both checkout diffs passed whitespace checks.

Disposable `BookKitReview` client: inspected Escritoire book placement, pending-note red tint versus idle, the single shared inventory badge, reading its placed Liber with an empty inventory, and rendering/reading the HutosLib guide on the shared table. Screenshots are under `build/book-kit-client/screenshots/`: `dictation-pending.png`, `dictation-idle-world.png`, `dictation-reading.png`, `field-notes-inventory.png`, `hutoslib-dictation-table.png`, `hutoslib-placed-reading.png`. The review client was shut down normally afterward.

This was a Hemomancy-enabled integrated client plus dedicated-server tests, not a separate HutosLib-only client launch or two-player visual session. HutosLib's badge is opt-in through `FieldNotes.Provider`; it registers no provider itself. Payment and knowledge synchronization remain in Hemomancy's existing server-side path.
