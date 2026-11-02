# HutosLib Effect Source Attribution Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Attribute every active `MobEffectInstance` applied by Hemomancy to Hemomancy and its concrete applying class in HutosLib's Effect Source Lens.

**Architecture:** Keep HutosLib's existing central `MobEffectEvent.Added` capture and add a process-local package-prefix ownership registry used before JAR inference. Hemomancy registers its root package once during mod construction, so all existing and future status-effect callers are covered without wrappers or gameplay edits.

**Tech Stack:** Java 21, NeoForge 21.1.219, Minecraft 1.21.1, Gradle 8.8, JUnit 5, HutosLib/Hemomancy composite build.

**Spec:** `docs/superpowers/specs/2026-08-29-hutoslib-effect-source-attribution-design.md`

## Global Constraints

- Cover only active `MobEffectInstance` entries shown by Minecraft's inventory effect list.
- Do not display or alter attributes, blood mechanics, damage modifiers, cooldowns, capability state, effect duration, amplifiers, particles, icons, or gameplay.
- Do not wrap Hemomancy's individual effect calls and do not hard-code Hemomancy inside HutosLib.
- Preserve HutosLib's existing ledger NBT and packet shape.
- Preserve unrelated dirty changes in both repositories.
- Do not stage, reset, clean, commit, or modify runtime worlds.

---

### Task 1: Add package-owner attribution to HutosLib

**Files:**
- Modify: `../HutosLib/src/test/java/com/vincenthuto/hutoslib/common/effectsource/EffectSourceInferenceTest.java`
- Create: `../HutosLib/src/test/java/com/vincenthuto/hemomancy/viewer/HemomancyEffectCaller.java`
- Modify: `../HutosLib/src/main/java/com/vincenthuto/hutoslib/common/effectsource/EffectSourceInference.java`

**Interfaces:**
- Consumes: Existing `EffectSourceInference.capture()`, `CapturedCaller`, `Frame`, and `Origin` records.
- Produces: `public static void EffectSourceInference.registerPackageOwner(String packagePrefix, String modId)`.

- [ ] **Step 1: Add the real-stack test helper**

```java
package com.vincenthuto.hemomancy.viewer;

import com.vincenthuto.hutoslib.common.effectsource.EffectSourceInference;

public final class HemomancyEffectCaller {

	public static EffectSourceInference.CapturedCaller capture() {
		return EffectSourceInference.capture();
	}

	private HemomancyEffectCaller() {
	}
}
```

- [ ] **Step 2: Add failing registration tests**

Add these imports and tests to `EffectSourceInferenceTest`:

```java
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.vincenthuto.hemomancy.viewer.HemomancyEffectCaller;

@Test
void registeredPackageOwnerAttributesDevelopmentCallerToMod() {
	EffectSourceInference.registerPackageOwner("com.vincenthuto.", "broad_owner");
	EffectSourceInference.registerPackageOwner("com.vincenthuto.hemomancy.", "hemomancy");

	var caller = HemomancyEffectCaller.capture();

	assertEquals(HemomancyEffectCaller.class.getName(), caller.frame().className());
	assertEquals("hemomancy", caller.origin().modId());
}

@Test
void packageOwnerRegistrationRejectsUnsafePrefixesAndBlankModIds() {
	assertThrows(IllegalArgumentException.class,
			() -> EffectSourceInference.registerPackageOwner("", "hemomancy"));
	assertThrows(IllegalArgumentException.class,
			() -> EffectSourceInference.registerPackageOwner("com.vincenthuto.hemomancy", "hemomancy"));
	assertThrows(IllegalArgumentException.class,
			() -> EffectSourceInference.registerPackageOwner("com.vincenthuto.hemomancy.", ""));
}
```

The first test catches missing registration lookup, wrong-prefix selection, and development-class ownership falling back to unknown. The second catches package-boundary ambiguity and invalid startup metadata.

- [ ] **Step 3: Run the focused tests and verify RED**

Run from `../HutosLib`:

```powershell
.\gradlew.bat test --tests com.vincenthuto.hutoslib.common.effectsource.EffectSourceInferenceTest --no-daemon
```

Expected: `compileTestJava` fails because `registerPackageOwner(String, String)` does not exist.

- [ ] **Step 4: Implement the minimal package-owner registry**

Add the imports, field, public registration method, lookup, and registered-origin branch to `EffectSourceInference`:

```java
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

private static final Map<String, String> PACKAGE_OWNERS = new ConcurrentHashMap<>();

public static void registerPackageOwner(String packagePrefix, String modId) {
	if (packagePrefix == null || packagePrefix.isBlank() || !packagePrefix.endsWith(".")) {
		throw new IllegalArgumentException("Package prefix must be nonblank and end with '.': " + packagePrefix);
	}
	if (modId == null || modId.isBlank()) {
		throw new IllegalArgumentException("Mod ID must be nonblank");
	}
	PACKAGE_OWNERS.put(packagePrefix, modId);
}

private static Optional<String> registeredModId(String className) {
	return PACKAGE_OWNERS.entrySet().stream()
			.filter(entry -> className.startsWith(entry.getKey()))
			.max((left, right) -> Integer.compare(left.getKey().length(), right.getKey().length()))
			.map(Map.Entry::getValue);
}
```

At the start of `originFor(Class<?> type)`, after obtaining `jar`, resolve registered ownership before the Minecraft and JAR branches:

```java
Optional<String> registeredModId = registeredModId(type.getName());
if (registeredModId.isPresent()) {
	String modId = registeredModId.get();
	ModList modList = ModList.get();
	if (modList != null) {
		return modList.getModContainerById(modId)
				.map(container -> new Origin(modId, container.getModInfo().getDisplayName(),
						container.getModInfo().getVersion().toString(), jar))
				.orElse(new Origin(modId, modId, "", jar));
	}
	return new Origin(modId, modId, "", jar);
}
```

Keep the current Minecraft, HutosLib, mod-JAR, and unknown fallbacks unchanged.

- [ ] **Step 5: Run focused and full HutosLib tests and verify GREEN**

```powershell
.\gradlew.bat test --tests com.vincenthuto.hutoslib.common.effectsource.EffectSourceInferenceTest --no-daemon
.\gradlew.bat test --no-daemon
```

Expected: both builds report `BUILD SUCCESSFUL`; the real captured frame is `HemomancyEffectCaller` with owner `hemomancy`.

- [ ] **Step 6: Check only the intended HutosLib diff**

```powershell
git diff --check -- src/main/java/com/vincenthuto/hutoslib/common/effectsource/EffectSourceInference.java src/test/java/com/vincenthuto/hutoslib/common/effectsource/EffectSourceInferenceTest.java src/test/java/com/vincenthuto/hemomancy/viewer/HemomancyEffectCaller.java
```

Expected: no output. Do not stage or commit.

---

### Task 2: Register Hemomancy and verify the composite integration

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/Hemomancy.java`

**Interfaces:**
- Consumes: `EffectSourceInference.registerPackageOwner(String packagePrefix, String modId)` from Task 1.
- Produces: Startup registration of `com.vincenthuto.hemomancy.` as owner `hemomancy`.

- [ ] **Step 1: Compile before integration and confirm the API is not yet consumed**

Run from Hemomancy:

```powershell
.\gradlew.bat compileJava --no-daemon
```

Expected: `BUILD SUCCESSFUL`, establishing the dirty checkout's pre-integration compile baseline against the sibling HutosLib build.

- [ ] **Step 2: Register Hemomancy's package during mod construction**

Add the import:

```java
import com.vincenthuto.hutoslib.common.effectsource.EffectSourceInference;
```

Add this as the first statement in `Hemomancy(IEventBus modEventBus)`:

```java
EffectSourceInference.registerPackageOwner("com.vincenthuto.hemomancy.", MOD_ID);
```

- [ ] **Step 3: Compile Hemomancy against the local HutosLib API**

```powershell
.\gradlew.bat compileJava --no-daemon
```

Expected: `BUILD SUCCESSFUL`; Gradle's composite substitution compiles Hemomancy against `../HutosLib`.

- [ ] **Step 4: Audit the central coverage boundary**

```powershell
rg -l "\.(addEffect|forceAddEffect)\(" src/main/java -g '*.java'
rg -n "activeEffects|getActiveEffectsMap|removeEffectNoUpdate" src/main/java -g '*.java'
```

Expected: Hemomancy status-effect applications use `addEffect`; no production code mutates the active-effect map directly. The number and identity of application files may change as unrelated dirty work evolves and is not a test constant.

- [ ] **Step 5: Run Hemomancy JVM tests**

```powershell
.\gradlew.bat test --no-daemon
```

Expected: `BUILD SUCCESSFUL`. If unrelated dirty-work failures remain, record their exact names and keep them separate from effect-source attribution.

- [ ] **Step 6: Run the loaded GameTest server gate**

```powershell
.\gradlew.bat runGameTestServer --no-daemon
```

Expected: the server summary reports all required tests passed. This validates loaded mod construction, including package-owner registration, without changing gameplay.

- [ ] **Step 7: Check the cross-repo diff and protected boundaries**

```powershell
git diff --check -- src/main/java/com/vincenthuto/hemomancy/Hemomancy.java docs/superpowers/specs/2026-08-29-hutoslib-effect-source-attribution-design.md docs/superpowers/plans/2026-08-29-hutoslib-effect-source-attribution.md
git status --short
git -C ..\HutosLib status --short
```

Expected: no whitespace errors; only the named integration files are attributable to this task, while all pre-existing dirty files remain untouched. Do not stage or commit.

- [ ] **Step 8: Record the live-client boundary**

Automated verification proves event capture, caller ownership, composite compilation, persistence compatibility, and loaded startup. It does not prove tooltip width, readability, or hover behavior; verify those later in a live client while holding the Effect Source Lens.
