# Hemomancy: Sanguine Mastery

Hemomancy is a Minecraft 1.21.1 NeoForge mod about ritual bloodcraft, fungal memory, and the choice between embracing the Harbinger tradition or purging it through the Unstained path.

The current alpha has two playable progression spines:

- Harbinger initiation, degree advancement, blood structures, manipulations, scars, bloodlines, puppeteering, and the rank-up runway through Archon.
- Unstained infection suppression, Lethean Baptism, Purity, directed Observances, Clarity Ascension, Still Arts, pale rites, the Stillwater Condenser, and Verdigris wards.

Development-only and post-alpha systems are kept out of the primary creative tab where practical. Drudges and the Non-Euclidean Hallway remain explicitly experimental.

## Requirements

- Java 21
- Minecraft 1.21.1
- NeoForge 21.1.x
- HutosLib
- The local development jars named in `build.gradle`

This checkout currently resolves several mod dependencies from `libs/`, which is intentionally git-ignored. A sibling `../HutosLib` composite build is used automatically when present; otherwise the configured HutosLib Maven dependency must resolve. See `build.gradle` for the exact local jar filenames.

## Development

On Windows, from the repository root:

```powershell
./gradlew.bat build
./gradlew.bat runClient
./gradlew.bat runServer
./gradlew.bat runData
```

The main implementation is under `src/main`. Server-backed integration tests live in `src/gameTest` and are excluded from release jars.

## Verification

```powershell
./gradlew.bat test
./gradlew.bat runGameTestServer
./gradlew.bat alphaCheck
```

`alphaCheck` is the release gate. It runs JVM contracts, the dedicated NeoForge GameTest server, and a runtime-log check that rejects Hemomancy resource errors. The operator-driven Harbinger journey can also be launched with:

```powershell
./gradlew.bat runAlphaJourneyClient
```

Then use `/hemo test journey start`, `/hemo test journey status`, and `/hemo test journey next`. Full instructions are in [docs/TESTING.md](docs/TESTING.md).

## Project references

- [Mechanical and implementation reference](docs/HEMOMANCY_REFERENCE.md)
- [Lore reference](docs/LORE_REFERENCE.md)
- [Lore consistency decisions](docs/LORE_CONSISTENCY_REVIEW.md)
- [Testing guide](docs/TESTING.md)
- [Alpha release checklist](docs/RELEASE_CHECKLIST.md)
- [Deferred ideas](docs/DEFERRED_IDEAS.md)

## Status

The critical Harbinger and Unstained paths are now guarded by automated progression tests, but this remains alpha software. Final encounter presentation, broader world tuning, balance, accessibility, multiplayer soak testing, and the explicitly marked post-alpha systems still need playtest feedback and polish.

License: All Rights Reserved.
