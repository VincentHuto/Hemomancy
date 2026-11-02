# HutosLib Effect Source Attribution for Hemomancy

## Goal

Make every active Minecraft potion/status effect applied to a player by Hemomancy identify Hemomancy and the concrete applying class in HutosLib's Effect Source Lens.

## Scope

- Cover active `MobEffectInstance` entries shown by Minecraft's inventory effect list.
- Cover effects applied by all Hemomancy systems, including skills, Morphlings, armor, items, manipulations, rites, entities, and world events.
- Preserve HutosLib's existing latest-application record, persistence, synchronization, removal, and tooltip behavior.
- Work in packaged mod JARs and the local composite development build where Hemomancy consumes the sibling HutosLib checkout.

## Non-goals

- Do not display attribute modifiers, blood income or drain, damage modifiers, cooldowns, capability state, or other mechanics that are not `MobEffectInstance`s.
- Do not add viewer-specific wrappers around Hemomancy's effect calls.
- Do not alter effect duration, amplifier, particles, icon visibility, source entity, or gameplay behavior.
- Do not hard-code Hemomancy knowledge inside HutosLib.

## Existing Behavior

HutosLib listens for `MobEffectEvent.Added` on server players. NeoForge posts that event directly inside both `LivingEntity.addEffect` overloads, so the listener already observes every normal Hemomancy status-effect application. HutosLib walks past its own listener, the event bus, and `LivingEntity.addEffect` to retain the first gameplay caller.

The remaining gap is caller ownership in development. Packaged classes can be associated with their containing mod JAR, but classes loaded from Gradle output directories report `development classes`; JAR-name matching therefore cannot reliably resolve them to Hemomancy.

## Design

### HutosLib package ownership registration

`EffectSourceInference` will expose:

```java
public static void registerPackageOwner(String packagePrefix, String modId)
```

The method will reject blank prefixes and mod IDs. Package prefixes must end with `.` so `com.example.mod.` cannot accidentally match `com.example.modular`. Registrations are process-local metadata and are neither persisted nor sent over the network.

Ownership lookup will select the longest registered prefix matching the captured caller class. This permits a mod to register a broad root while allowing a more specific nested package to override it. A thread-safe standard-library map is sufficient because registrations occur during mod construction and reads occur during gameplay events.

### Hemomancy registration

Hemomancy will register one mapping during normal mod construction:

```java
EffectSourceInference.registerPackageOwner("com.vincenthuto.hemomancy.", Hemomancy.MOD_ID);
```

No individual skill, Morphling, armor class, or effect call will change.

### Attribution flow

1. Hemomancy calls either `LivingEntity.addEffect` overload.
2. NeoForge posts `MobEffectEvent.Added`.
3. HutosLib records the active effect ID and explicit source entity, when supplied.
4. HutosLib captures the first caller outside its own effect plumbing, the event bus, and `LivingEntity.addEffect`.
5. HutosLib first resolves the caller through registered package ownership.
6. When registered, it reads display name and version from `ModList` using the registered mod ID and retains the existing class, method, file, line, and code-source fields.
7. Unregistered callers retain the current Minecraft, HutosLib, JAR-based, or unknown fallback behavior.
8. The existing packet synchronizes the record to the client, and the lens adds it to the hovered active-effect tooltip.

## Error Handling and Compatibility

- Invalid registration arguments fail immediately with `IllegalArgumentException` during mod startup.
- Re-registering the same package prefix replaces its mod ID, avoiding duplicate state during development reloads or tests.
- Missing `ModList` metadata still records the registered mod ID and caller details; display name, version, and JAR fall back to the existing safe values.
- Existing serialized ledgers and packets do not change shape, so no data migration or protocol expansion is required.
- HutosLib remains independent of Hemomancy and can support the same one-line registration from other development mods.

## Verification

Implementation will follow test-first development:

1. Add a failing HutosLib test that captures a real stack frame from a test helper under a registered Hemomancy package and expects `callerModId` to be `hemomancy`.
2. Add failing validation and longest-prefix tests for the public registration contract.
3. Implement the minimal registration and lookup behavior in `EffectSourceInference`.
4. Register Hemomancy's package during mod construction and compile Hemomancy against the sibling HutosLib composite build.
5. Run HutosLib's full JVM tests.
6. Run Hemomancy's focused tests, `compileJava`, and a source audit confirming active effects are applied through `addEffect` rather than direct active-effect-map mutation.
7. Run the relevant Hemomancy GameTest server coverage if the focused compatibility test requires loaded registries.

Automated checks establish event capture, ownership resolution, compilation, persistence, and packet compatibility. A live client remains required to confirm the expanded tooltip is readable while holding the Effect Source Lens.

## Protected Boundaries

- Preserve unrelated dirty changes in both repositories.
- Do not stage, reset, clean, commit, or modify runtime worlds.
- Do not change Hemomancy gameplay or the set of effects visible in Minecraft's inventory.
