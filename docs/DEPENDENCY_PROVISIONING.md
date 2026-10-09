# Dependency provisioning and packaged acceptance

The Windows CI runner retains provisioned ignored JARs, but now verifies their exact SHA-256 digests before running the gate. Run the same preflight locally:

```powershell
pwsh -NoProfile -File gradle/verify-dependency-bundle.ps1
./gradlew.bat alphaCheck build
```

`gradle/dependency-bundle.json` records all nine active local JAR inputs, including the previously unchecked Create and Mana and Artifice artifacts. Provision those exact filenames under `libs/` from the project's dependency bundle. A version-looking filename alone is insufficient. Do not use unrelated ignored JARs as substitutes.

HutosLib is a sibling composite build at `../HutosLib`. Its pinned input includes both the Git revision and a digest of source/build files, including non-ignored untracked source. This captures the existing modified library source and the intentional build-file packaging exclusions without altering the retained authoring resources. The recorded revision alone does not reproduce those modifications: distribute the matching source snapshot to the runner. This is a snapshot lock, not a claim that upstream HEAD is clean. Intentional dependency changes require reviewing the artifacts/source and regenerating the manifest with `-Update`.

The current composite builds **HutosLib 7.4.0**, although Hemomancy's fallback coordinate remains 7.3.5. Maven fallback has not been independently accepted and is not the pinned CI input. Minecraft is 1.21.1, NeoForge 21.1.251, Java 21; Maven development libraries remain pinned in `gradle.properties`/`build.gradle` (GeckoLib 4.9.2, Curios 9.5.1+1.21.1).

Packaged installation uses the mod metadata, not the development classpath: install NeoForge, Hemomancy, HutosLib and TerraBlender. JEI is optional. The clean dedicated-server smoke test under `build/priority-clean-server/` contains only those three mod JARs; its startup reached `Done`. Record each JAR's digest and preserve the startup log with any release evidence. Development/GameTest launches do not establish clean installation.

For repeatable clean server acceptance, install the configured NeoForge 21.1.251 into a new directory, copy only the three required mods into `mods/`, accept the Minecraft server EULA, and launch Java 21 with the installer's generated arguments. Use a disposable world and an unused port. Stop through the console and confirm saving/shutdown. Use the official NeoForge Maven installer artifact. The earlier packaged acceptance below used 21.1.219; it does not establish packaged acceptance for 21.1.251. Never replace an existing player world for this check.

The current minimal packaged client/server replay passed connected render/recipe setup, full dimension saving and normal exits with only Hemomancy, HutosLib 7.4.0 and TerraBlender 4.1.0.8 (`build/priority-minimal-corrected-accepted.log`). HutosLib packaging excludes two unreferenced invalid-name backup PNGs while preserving their sources; the corresponding source digest is updated in the manifest. Optional-JEI checks, visual/reload/locale coverage, additional inventory/reward recovery modes and natural Survival progression remain separate acceptance steps. Actual two-player ownership and reward cases are qualified in the repair record. See [the repair record](validation/PRIORITY_FINDINGS_2026_10_02.md).
