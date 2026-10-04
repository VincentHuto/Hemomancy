# Mnemonic Whale feeding validation - 2026-10-03

Whales now pursue submerged squid, glow squid, and Prism Cuttles within 16 blocks. Each successful lethal whale bite drops exactly one Mnemonic Ambergris above the whale, without spending or resetting the existing bottle/shedding cooldown. Prey retain their ordinary loot. Reachable bite positions account for the whale's wide footprint; hunting uses tighter horizontal navigation arrival than cruising. Failed hunts pause before searching again. Prism Cuttles now share the reef through their existing biome spawn modifier.

The Liber feeding page unlocks when ambergris is picked up. Item tooltips, Voyager/Wayfarer inquiries, world-content guidance, and mechanics/lore references describe supplying leashed squid or placing and breaking occupied specimen jars. Neither transport mechanic needed production changes.

## Automated evidence

- `./gradlew.bat runMnemonicWhaleGameTestServer build -x test verifyGameTestSuites --console=plain`: all **16 required GameTests passed**, release build and suite registry passed; terminal exit 0. Log: `build/whale-final.log`.
- Tests cover autonomous pursuit of all three prey types, leashed squid, actual occupied-jar breaking/release, consecutive meals during bottle cooldown, exactly one reward above the whale, ordinary squid loot, nonlethal/rejected/repeated bites, other killers, unrelated creatures, dry/removed prey, walls/unreachable prey, return to cruising, shallow water, stronger prey's 20-tick bite interval, loaded reef spawn settings, and the pickup-to-page mapping.
- Direct Java 21 executions of `MnemonicWhaleTuningTest`, `ErythrocoralReefTuningTest`, and `PrismCuttleRulesTest` passed against compiled test/main classes.
- Full JVM run `./gradlew.bat test build verifyGameTestSuites --console=plain` completed **2,423 tests with four failures** and exit 1. The failed test task prevented that invocation's build; the separate final command above built successfully. Log: `build/whale-build.log`.
  - `GameTestHarnessSourceContractTest.isolatedJourneyClientRunAndOperatorGuideRemainAvailable`: malformed text encoding while reading existing guidance.
  - `GameTestHarnessSourceContractTest.devOnlyHarnessProvidesCommandsAndEarlyHarbingerScenarios`: malformed text encoding while reading existing guidance.
  - Legacy `MorphlingLumenlaceRenameResourceTest`: malformed text encoding in an existing resource/source read.
  - Legacy `SporiticThuribleRulesTest`: existing burning-spore item-ID assertion.
  - The full run also logged a missing `com.vincenthuto.hutoslib.common.book.FieldNotes$Provider` during legacy test discovery. These files/classes are outside this change.
- The original loaded red run had 11 failures: missing hunting, absent attack damage, and no guaranteed feeding rewards. Intermediate runs also exposed fixture mistakes and the whale's actual navigation corner/waypoint behavior. Final fixtures preserve normal whale gravity and place cruise water at the whale's preferred depth.

## Live client evidence

Disposable world `MnemonicWhaleReview_20261003` in `build/mnemonic-whale-client`, launched using the existing review profile with an ignored Gradle init script. No original runtime save was used. The scene supplied glass tanks, reef biome coloration, spectator observation, night vision, and summoned creatures; it is not natural-survival acquisition or natural biome-generation acceptance.

The live whale pursued a stationary supplied squid from nine blocks away and killed it with 12 damage. Item inspection reported one ambergris and normal ink sacs. A subsequent normally ticking Prism Cuttle was consumed and another single ambergris appeared. After moving the whale to shallow water near sea level, it caught a freely swimming glow squid and then a naturally present squid. Item inspection showed another one-count ambergris for each kill; whale health remained 38. Screenshots were visually inspected for the whale and floating drops. A position-assisted ordinary item pickup placed one ambergris in the observer's Creative inventory. The final client saved and stopped normally, terminal exit 0.

Evidence: `build/whale-client-verified.log`, `build/mnemonic-whale-client/operator/journal.jsonl`, client/server snapshots, and `build/mnemonic-whale-client/screenshots/whale-repeat-feeding.png`. Jar and lead delivery, exact sampling-cooldown independence, and initial drop height have dedicated server GameTest coverage; ordinary player input for those delivery methods, multiplayer, natural spawning, and cold-process feeding persistence were not accepted in this client pass.

An earlier disposable client crashed because compilation was incorrectly overlapped with its live development classpath, making `AntecedentPlayback` temporarily unavailable. The final client session was restarted after compilation finished; no production workaround was added.
