# Hemomancy alpha release checklist

Use this checklist for an alpha candidate. A checked box should mean the stated evidence exists for the exact commit being packaged.

## Automated gate

- [ ] Run `./gradlew.bat alphaCheck --console=plain`.
- [ ] Confirm every required GameTest passed.
- [ ] Confirm the runtime resource-log gate passed.
- [ ] Run `./gradlew.bat build --console=plain`.
- [ ] Install the resulting jar with the declared required dependencies in a clean instance.

## Critical playthroughs

- [ ] Run the isolated Harbinger journey from Mortal Display through Archon.
- [ ] Verify each rank-up rite is discoverable, buildable, completes once, and grants the intended degree/advancement.
- [ ] Begin the Unstained path through Podium suppression and Lethean Baptism.
- [ ] Accept, return, and fulfill every visible Observance; relog once with an active assignment.
- [ ] Reach full Purity, prepare Consecrated Copper at the Podium, and complete Clarity Ascension.
- [ ] Verify the Book of Observances, Self-Reflection Mirror, Still Arts radial, Stillwater Condenser, and Verdigris Lattice.

## Runtime quality

- [ ] Test dedicated-server join, death/respawn, dimension change, and relog with both path capabilities.
- [ ] Test two simultaneous players on different paths.
- [ ] Inspect `latest.log` for missing models, textures, recipes, tags, advancements, serializers, and packet errors.
- [ ] Verify JEI categories and catalysts with JEI installed, including an initial world join.
- [ ] Verify no survival recipe, loot table, inquiry, or guide unlock exposes retired content.

## Presentation and balance

- [ ] Check GUI scaling at small, normal, and large scales.
- [ ] Check keybind conflicts and accessibility of color-coded state.
- [ ] Smoke-test major bosses, structures, worldgen, and Blood Moon behavior.
- [ ] Review progression costs, reward duplication, and obvious farming exploits.
- [ ] Confirm post-alpha/WIP content remains isolated and clearly labeled.
- [ ] Confirm every Saint encounter—including Hemorath—remains outside natural world generation and launch progression.

## Packaging

- [ ] Update `mod_version`, changelog, known issues, and supported dependency versions.
- [ ] Confirm the release jar excludes GameTest classes and development-only resources.
- [ ] Archive the successful `alphaCheck` report and exact commit hash with the candidate.
