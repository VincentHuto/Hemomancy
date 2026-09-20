# Antecedent Inquiry

This optional investigation follows sculk, Ancient Cities, and evidence of an earlier incursion. It adds no blood tendency and does not gate the main Harbinger path.

## Collect and examine

Use a Living Syringe with a loaded Vial Rack on a living Warden or a Sculk Catalyst. Either source gives the separate **Unidentified Dark Sample** item. After a 40-tick Hematic Microscope examination, it identifies as **Ahaematic Colloid**: no hematic content, tendency, or enzyme response, and classification **Incertae**. Catalyst sampling leaves the block in place.

Creative, JEI, and plain command-created Colloid stacks default to a Warden specimen and can be examined immediately. The Warden default is fixed; Catalyst provenance comes from actual Catalyst sampling or explicitly supplied specimen data. The microscope's inclusions and signal marks are editable PNGs (`colloid_inclusion.png`, `colloid_signal.png`, and `colloid_direction.png`) under `assets/hemomancy/textures/gui/microscope/`.

The sample contracts near vibration and changes during the Severed Record. It cannot be injected, centrifuged into an enzyme, carved into a creature recording, or counted toward the clinical three-blood-source lesson. An analyzed Colloid is consumed only when filling a Listening Scar housing, which returns a normal empty Bloody Vial. Existing component-backed Warden vials migrate to the separate item without losing provenance. Ordinary blood is unchanged.

## Recordings and evidence

New Ancient Cities can have a self-contained Vigil entrance beside a road or walkway anywhere in the city leading to the **Vigil Beneath**. The city generates normally first; if the annex cannot fit without damaging the existing structure or obstructing its walkways, that city has no Vigil. The entrance chest always contains the **Severed Record**. The gallery holds **Lower District Survey** and **The Quieting**. Ordinary Ancient City chests have a 12% supplemental chance to contain one of those two records. Existing explored cities are not retrofitted.

Ancient cylinders use the normal Clairaudiograph cylinder slot. Bring the machine because the Vigil does not supply one. Keep an analyzed sample in the specimen slot or in a nearby listener's hand. Playback preserves both the specimen and cylinder.

The Severed Record lasts 73 seconds. Its recorded audio is silent from 55 to 68 seconds. During that silence, a separate unresolved trace continues and the sample moves in a coordinated way. A physical tap at 63 seconds comes from the specimen, not the recording. The final click at 72 seconds occurs behind each nearby listener and is absent from the recorded waveform. Captions still identify speech and environmental events when audio is muted.

The evidence belongs to the player. The sample and archive interval needs continuous presence from 56 to 65 seconds within eight blocks. A traded identified vial does not grant personal examination evidence. Interrupted observation can be repeated. Facts persist through death, dimension changes, and saves, and can be found out of order.

## Teachers

- At Degree 3, the Alchemist and Vicar discuss an examined dark sample.
- After hearing the Severed Record, the Alchemist requests a controlled replay. Keep the Alchemist, analyzed sample, and yourself near the machine through the decisive interval.
- At Degree 4, bring the controlled result to the Vicar. His recognition gives the clue: **"Pass where the wool ends."**
- At Degree 5, the Vicar compares the two incursions' strategies.
- At Degree 7, the optional Fungal Entity epilogue waits for a player who completed the investigation.

The evidence points to a repeated intelligence using a changed strategy. It does not make the Warden a primitive Apotheos or sculk into crimson mycelium.

## The Vigil Beneath

The upper passage ends its wool at an intact architectural boundary. Beyond it, the corridor has operational records and a bell test. Phantom clicks are authored sounds and make no vibration events. Environmental ambience fades at the boundary while footsteps remain audible, then returns down the containment stair.

The gallery overlooks ordinary vanilla sculk. Its three original Sensors answer late and inconsistently. Its original Shrieker fails without darkness, warning escalation, or a Warden summon. Its Catalyst consumes nearby death experience and grows slowly within the specimen bed. Breaking and replacing those fixtures removes their special behavior. Sculk elsewhere remains vanilla.

Read **Lower Vigil: Record 7**, then carry a Clairaudiograph into the archive. The contained ancient vessel answers only during the Severed Record's unresolved interval, forming an upward geometric pattern. Ordinary sound does not wake it. Observe this yourself. Vicar recognition and archive evidence complete the inquiry.

## Listening Scar

After completion, the Vicar exchanges **four iron nuggets and one echo shard** for a reconstructible housing. Hold it in one hand with an analyzed Colloid vial in the other, then use it. It consumes the sample and returns the original empty vial. Hold the loaded housing through the Severed Record's decisive interval to awaken it.

At a **Scarlet Vanity**, place an awakened Listening Scar in the socket beside an equipped Charm of Vascularium. Remove it there when needed. The talisman stays inside the charm, so moving the charm preserves it.

When an actual Sculk Sensor activates within 16 blocks, the equipped talisman gives a brief cyan directional pulse and quiet tactile sound. Responses are limited to one per five ticks. It does not locate Wardens, prevent detection, silence footsteps, or react to the authored phantom clicks.

## Content and maintenance

The Vigil can attach to a suitable walkway throughout the city; it is no longer restricted to the portal area. Its pieces do not contribute their bounding boxes to Ancient City terrain carving. Its template clears only the authored interior, preserving surrounding rock instead of excavating a rectangular cavern that exposes deep lava. Natural lava can still occur nearby. This generation change applies to new chunks; it does not repair existing excavations. The city generation test compares the vanilla pieces for identical seeds with and without the annex, checks protected block positions and terrain density, and covers every center variant and rotation.

Four enclosed modules form the Vigil: an entrance hut, listening passage, balcony gallery, and archive. Six arrangements let the passage and archive connect to different sides of the gallery. Deepslate brick walls, the stepped roof, specimen bed, and complete record sequence remain. Existing Vigils retain their original layout and behavior; they are not rebuilt.

The three recordings use original synthetic performances from the locally installed Microsoft David and Zira voices, with captions and authored acoustic beds. Their editable scripts, cue timing, structure generator, and native 16-pixel art sources are in `tools/oneoff/antecedent/`. They regenerate without touching saved worlds.

The [fresh Markdown rewrites](../docs/antecedent-fresh-draft/README.md) are editorial drafts and have not replaced runtime text or audio. Five lectern documents and two chest books are embedded in the generated NBT; editing their exported Markdown alone does not update the game. See the [lore reference](../docs/LORE_REFERENCE.md#the-antecedent-the-first-incursion) for each record's role and the [developer reference](../docs/HEMOMANCY_REFERENCE.md#antecedent-inquiry-and-audio-analysis) for evidence gates and authoring sources.

Server checks: `./gradlew.bat runAntecedentGameTestServer`. Audio math checks: `./gradlew.bat test --tests '*SpectrogramAnalysisTest' --tests '*AntecedentResearchTest'`. The opt-in `runAntecedentReviewClient` fixture uses only `build/antecedent-client` and is excluded from release sources.
