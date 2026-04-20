Hemomancy Modding Agent Instructions (Minecraft Forge 1.20.1)
1) Project identity

   Repository: VincentHuto/Hemomancy
   Mod ID: hemomancy
   MC/Forge: 1.20.1 / 47.2.20
   Java: 17
   Root package: com.vincenthuto.hemomancy

2) Source of truth

Before making claims or changes, check in this order:

    Code (authoritative when docs conflict)
    /home/runner/work/Hemomancy/Hemomancy/HEMOMANCY_REFERENCE.md
    /home/runner/work/Hemomancy/Hemomancy/LORE_REFERENCE.md
    /home/runner/work/Hemomancy/Hemomancy/MNA_COMPATIBILITY_BRAINSTORM.md

3) Lore/tone constraints (mandatory)

   Preserve morally gray framing.
   Do not frame Harbingers as pure villains or Unstained as pure heroes.
   Keep Harbinger language archaic/ecclesiastical/Latinate.
   Keep Unstained language clean/sacramental/plain.

4) Engineering constraints

   Make minimal, surgical changes.
   Respect mutual exclusivity of Harbinger vs Unstained progression.
   Do not break optional compat loading (ModList.get().isLoaded(...) guards).
   Use existing naming patterns (snake_case IDs, XxxInit, tendency vocabulary consistency).
   Use Hemomancy.rloc(...) for resource locations.

5) System-specific gotchas

   IBloodVolume.active == false means blood systems are not enabled yet.
   Memory items require both overlay texture + model/provider wiring.
   Some skills have math but incomplete event wiring; verify before claiming behavior.
   Entity loot tables are hand-authored JSON; don’t re-enable disabled data-gen providers casually.

6) Workflow

   Inspect relevant files first.
   If changing code, run existing build/tests:
   ./gradlew build
   use existing run configs (runClient, runServer, runData) only as needed.
   Implement smallest complete fix/feature.
   Re-run relevant validation.
   If mechanics/lore behavior changed, update reference docs accordingly.

7) Security and compatibility

   No hard dependencies on optional mods from core paths.
   Avoid introducing secrets, unsafe network behavior, or unnecessary new dependencies.
   Preserve backwards behavior unless change request explicitly requires otherwise.

8) Response style for this repository

   Be concise, concrete, and file-path specific.
   Distinguish clearly between: implemented behavior, documented intent, and WIP systems.
   When uncertain, state uncertainty and verify in code before asserting.

