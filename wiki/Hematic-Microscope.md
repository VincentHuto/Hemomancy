# Hematic Microscope

The cellular field is slightly transparent, with a pale blue-green translucent glass backing around the brass lens. The world remains partially visible through both surfaces.

Hold the microscope in your main hand and one filled Blood Vial in your off hand. Hold use for two seconds to identify that vial. Keep holding to study the slide; release to return immediately to ordinary vision. The microscope does not magnify the world or consume blood.

The player raises the microscope and inserts the broad head of the offhand vial into its recessed side socket, leaving the pointed end in the vial hand. The microscope reaches the eye after 16 ticks. This two-hand pose is visible in third person and mirrors with the player's main-hand setting. In first person, the lens fades in over ticks 12–16; third person keeps an unobstructed world view.

A quiet focusing loop starts once an unidentified, readable sample is inserted. It stops when the server confirms identification at 40 ticks. Known samples and unreadable specimens do not play the loop. Releasing use immediately removes the overlay and sound, followed by six ticks of vial withdrawal and lowering. Switching items or starting another action cancels that cosmetic recovery.

Craft it with two copper ingots, an iron ingot, a glass pane and leather. Ask the Alchemist about the microscope or Blood Vial for sampling guidance. The vial rack is not an examination container.

Identification reveals the vial's current tendencies, biological properties, and injection prediction in its tooltip. Unexamined blood remains injectable. Identification changes neither potency nor other vials of the same species, and grants no bestiary surrender rewards.

The slide displays up to three tendencies and two biological motifs. Additional identified traits remain in the readout (which pages when necessary) and tooltip. Paired budding cells distinguish Animus; crystalline discs distinguish Congeatio. Ferric platelets form chains, Ductilis cells branch and pulse, Lux discs glow in waves, Flammeus coronas rise, Mortem membranes collapse, and Tenebris silhouettes fade and shift. Equivalent specimens share a deterministic composition.

Client settings under `microscope` offer reduced motion and low density. They do not change examination time or traits. A filled vial with unreadable provenance shows a murky slide and retains its data. Switching either held stack, changing specimen components, interrupted use, death, spectator mode, or a data reload cancels examination. Hold use again after a reload.

## Resource packs and addons

All screen decoration uses editable PNG sprites under `textures/gui/microscope/`: the lens frame, plasma field, readout panel and border, screen dimming, progress track/fill, cells, property motifs and Ductilis connections. PNG alpha controls static surface transparency. The game does not run the asset generator; Java handles dynamic text, cell movement, clipping, and progress. [Texture editing guide and icon filenames](../src/main/resources/assets/hemomancy/textures/gui/microscope/README.md).

The eight tendency icons are separate 32 x 32 PNGs under `textures/gui/microscope/tendencies/`; the twelve property icons are under `textures/gui/microscope/properties/`. Edit those named files directly. The original two atlases remain as references and are no longer rendered.

These textures reload through the ordinary resource manager; the overlay retains no composition cache. Tendency tint comes from the existing enum palette. Built-in biological motifs use `hemomancy:blood_properties/*` and `hemomancy:fungal`. Additional namespaced `blood_properties/*` entity tags synchronize independently of injection definitions and display as stable IDs when no translation exists. Unrecognized motifs use neutral motes.

Editable item source: `assets/hemomancy/models/item/bbmodel/hematic_microscope.bbmodel`. The model generator is `tools/model_export/hematic_microscope.py`; use `--model-only` to rebuild the socket, editable Blockbench model, and mirrored viewing model without changing any item or GUI textures. Without that option it preserves existing GUI PNGs unless explicitly invoked with `--reset-gui-textures`. The original focusing loop can be regenerated with `tools/audio/author_microscope_loop.py`; its sound event is `hemomancy:item.microscope.examine_loop`.

## Verification (2026-09-16)

`assemble`, focused microscope pose/playback/composition and vial-animation JVM checks, and all 70 required tests in `runBloodInjectionGameTestServer` passed. New coverage verifies both physical hands, a single completion at exactly 40 ticks, continued viewing, release, identical-stack cancellation during viewing/recovery, silent known/murky samples, late-observer session snapshots, and packet serialization. Geometry checks cover head seating, outward needle direction, eyepiece alignment, both hand grips, wide/slim arms, mirroring, and delayed overlay timing. All 10 existing microscope texture hashes remain unchanged; the 21-element JSON and editable Blockbench model match, with a corresponding mirrored model. The custom sound is mono Vorbis at 44.1 kHz with a one-second period.

The full JVM suite ran 2,037 tests with two failures outside this feature: the legacy discovery count expects 361 but finds 360, and a resource scan cannot decode `docs/phlegethontic-nether-worldgen/orcadian-horseman/README.md` as UTF-8. These files were not changed for the microscope.

The initial isolated client loaded the assets successfully, but Windows capture failed twice with `SetIsBorderRequired` (`0x80004002`), preventing an in-world visual/audio check. A later startup retry failed in the separate HutosLib dependency while loading `com.vincenthuto.hutoslib.common.karma.Karma`; that class is present in the dependency jar, whose ZIP integrity check passes. No HutosLib source was changed. Live client acceptance remains open: held poses, normal FOV, GUI scales and aspect ratios, motion/readability, reduced motion, low density, resource reload, and interaction with other overlays. Source review and asset checks do not replace those in-game checks.
