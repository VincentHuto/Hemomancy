# AGENTS.md — Hemomancy

## Project snapshot
- Hemomancy is a Minecraft **NeoForge mod, not legacy Forge**: Minecraft `1.21.1`, NeoForge `21.1.172` (`21.1.x` range), Java `21` (`mod_id=hemomancy`, package `com.vincenthuto.hemomancy`).
- Entrypoint: `src/main/java/com/vincenthuto/hemomancy/Hemomancy.java`; build/version source of truth: `gradle.properties` and `build.gradle`.
- Source-of-truth docs: `HEMOMANCY_REFERENCE.md` for implementation/status, `LORE_REFERENCE.md` for tone/worldbuilding, `MNA_COMPATIBILITY_BRAINSTORM.md` for Mana and Artifice ideas. Prefer current code when docs disagree.
- Theme matters: morally gray blood magic + fungal cosmic horror. Do not frame Harbingers as simple villains or Unstained as simple heroes.

## Lore guardrails
- Blood magic is publicly a sacred inheritance of the Hematic Order, but biologically it is fungal blood-memory/infection tied to a slow cosmic reproductive cycle.
- Harbingers are taboo, shunned, and sometimes dangerous, but not evil; they value found-family covenant language (`Hematic Order`, `Sanguine Brotherhood`, `Crimson Lodge`).
- The Unstained began as former Harbingers; they see blood magic as infection that can be painfully shed, not as absolute sin. Their imagery is white/silver/oxidized copper, Lethean water, antiseptic ritual, and blunt weapons.
- The Fungal Entity and Pale Lady are amoral forces of nature, not Satan/God analogues: one is alien sporulation, the other a defensive immune response that can drift toward autoimmune danger.
- Common society mostly sees hemomancy as eerie and taboo, like vultures/fungi cleaning nature, not as an apocalypse cult.

## Architecture to understand first
- `Hemomancy.java` wires nearly everything: `DeferredRegister`s from `common/init/*Init.java`, configs, capability registration, packet payload registration, creative tab population, reload listeners (`ItemInquiryLoader`), and HutosLib book serializer setup.
- Main packages: `client/` is client-only rendering/screens/particles; `common/` holds gameplay systems; `compat/` holds optional integrations; `config/` registers NeoForge config specs; `mixin/` backs `hemomancy.mixins.json`.
- Player state uses NeoForge attachments/capabilities: definitions in `HemoAttachmentTypes`/`HemoCapabilityKeys`, registration in `HemoCapabilityRegistrar`, access through `HemoCapabilityAccess`. Do not revive old provider patterns.
- Core progression state includes `IBloodVolume`, `IBloodTendency`, `IVascularSystem`, `IKnownManipulations`, `IInitiatoryDegree`, `IUnstainedProgress`, `IVisceralOrgans`, and scar/morphling capabilities.
- Networking is NeoForge 1.21 payload-based: add packets to `common/network/PacketHandler.java` with `CustomPacketPayload`, static `TYPE`, `STREAM_CODEC`, and the appropriate `playToClient`/`playToServer` registration. Do not use old `SimpleChannel` patterns.

## Workflows
- From the project root on Windows/PowerShell: `./gradlew.bat build`, `./gradlew.bat runClient`, `./gradlew.bat runServer`, `./gradlew.bat runData`.
- `settings.gradle` includes `../HutosLib` as a composite build; Hemomancy also relies on local jars in `libs/` for TerraBlender, GeckoLib, and JEI.
- `runData` writes `src/generated/resources`; `build.gradle` includes that directory but lets `src/main/resources` win on duplicates. `DataGeneration.java` currently only enables blockstates, item models, and language; server recipe/tag/loot providers are intentionally commented out.
- There are no normal files under `src/test` currently; validate code changes with `build` or the relevant NeoForge run config.

## Project conventions
- Use NeoForge 1.21 APIs/imports (`net.neoforged.*`, `DeferredHolder`, payload networking, attachments); do not add old `net.minecraftforge.*` imports or Forge-era registration/networking patterns.
- Registries live in `common/init/*Init.java` and use `snake_case` IDs with `DeferredHolder`s; Java classes are `PascalCase`. Use `Hemomancy.rloc("path")` for mod resource locations.
- Blood manipulations are registered in `ManipulationInit` and implemented under `common/manipulation/<tendency>/`; each records cost, align level, XP cost, type, rank, tendency, vein section, cooldown, and optional Drudge behavior via `setDrudgeAction(...)`.
- Internal tendencies are `ANIMUS`, `FLAMMEUS`, `DUCTILIS`, `LUX`, `MORTEM`, `CONGEATIO`, `FERRIC`, `TENEBRIS`; enzyme item names use different vocabulary (Vivacious/Fervent/Neurotic/etc.), so do not “normalize” them.
- Data paths are current 1.21-style in this repo: main recipes under `data/hemomancy/recipe/`, entity loot under `data/hemomancy/loot_table/entities/`, item inquiry dialogue under `data/hemomancy/dialogue_inquiry/<npc>/<namespace>/<item>.json`.
- Lore text should match existing vocabulary: Latinate/ecclesiastical for Harbinger content (`Liber Sanguinum`, `Crimson Lodge`), cleaner sacramental language for Unstained content (`Lethean Dew`, `Our Lady of Still Waters`).

## Integration and safety notes
- `build.gradle` excludes `compat/mna/**` and `compat/curios/**` because those NeoForge 1.21.1 deps are not available; do not import those compat classes from core code or remove exclusions without adding real deps.
- JEI compat code can compile because a local JEI jar is present; HutosLib is required and supplies shared book/dialogue/particle utilities such as `BookPlaceboReloadListener`.
- Harbinger and Unstained paths are mutually exclusive; new degree/purity code must preserve the reset behavior described in `HEMOMANCY_REFERENCE.md` §5.
- `IBloodVolume` exists on every player, but `volume.isActive()` is the opt-in gate for blood magic; do not treat capability presence as initiation.
- Memory/manipulation additions often require multiple files: manipulation class + `ManipulationInit`, language/model/datagen (`HemoItemModelProvider`), memory overlay texture, and any relevant progression/recipe docs.
