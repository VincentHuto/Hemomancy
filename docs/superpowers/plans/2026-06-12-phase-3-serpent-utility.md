# Phase 3 Serpent Utility Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add two serpent-scale utility items: Constrictor Cords for short field control and Scale Grip for one-stack death retention.

**Architecture:** Keep the gameplay small and additive. Constrictor Cords use a throwable item/projectile/effect path following existing projectile patterns; Scale Grip uses death/respawn events plus a pure slot-selection helper so the risky inventory behavior can be tested without a Minecraft runtime.

**Tech Stack:** Java 21, NeoForge 1.21.1, Minecraft item/entity/effect registries, plain Java sentinel tests, JSON resources, PowerShell-generated PNG textures.

---

## File Map

- Create `src/main/java/com/vincenthuto/hemomancy/common/effect/ConstrictorCordRules.java`: pure constants for cord duration, boss duration, cooldown, velocity, and movement multiplier.
- Create `src/main/java/com/vincenthuto/hemomancy/common/effect/ConstrictedEffect.java`: harmful movement-suppression effect.
- Create `src/main/java/com/vincenthuto/hemomancy/common/item/shared/ConstrictorCordItem.java`: throwable item that spawns the projectile.
- Create `src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/ConstrictorCordEntity.java`: projectile that applies `constricted`.
- Create `src/main/java/com/vincenthuto/hemomancy/common/item/shared/ScaleGripRules.java`: pure selector for the stack protected by Scale Grip.
- Create `src/main/java/com/vincenthuto/hemomancy/common/item/shared/ScaleGripEvents.java`: death/respawn inventory retention behavior.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java`: register `constrictor_cord` and `scale_grip`.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`: register `constrictor_cord_projectile`.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/init/EffectInit.java`: register `constricted`.
- Add item/effect textures, item models, recipes, lang entries, docs, and sentinel tests.

---

### Task 1: Add RED Rule And Resource Tests

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/common/effect/ConstrictorCordRulesTest.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/item/shared/ScaleGripRulesTest.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/resource/PhaseThreeSerpentUtilityResourceTest.java`

- [ ] **Step 1: Add Constrictor Cord rule test**

Create `src/test/java/com/vincenthuto/hemomancy/common/effect/ConstrictorCordRulesTest.java`:

```java
package com.vincenthuto.hemomancy.common.effect;

public final class ConstrictorCordRulesTest {
	private ConstrictorCordRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals(100, ConstrictorCordRules.DURATION_TICKS, "normal duration");
		assertEquals(50, ConstrictorCordRules.BOSS_DURATION_TICKS, "boss duration");
		assertEquals(20, ConstrictorCordRules.USE_COOLDOWN_TICKS, "use cooldown");
		assertEquals(1.25D, ConstrictorCordRules.PROJECTILE_VELOCITY, "projectile velocity");
		assertTrue(ConstrictorCordRules.MOVEMENT_SPEED_MULTIPLIER > 0.0D, "movement multiplier stays above zero");
		assertTrue(ConstrictorCordRules.MOVEMENT_SPEED_MULTIPLIER < 0.15D, "movement multiplier strongly constricts");
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertEquals(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.001D) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertTrue(boolean value, String label) {
		if (!value) {
			throw new AssertionError(label);
		}
	}
}
```

- [ ] **Step 2: Add Scale Grip selector test**

Create `src/test/java/com/vincenthuto/hemomancy/common/item/shared/ScaleGripRulesTest.java`:

```java
package com.vincenthuto.hemomancy.common.item.shared;

import java.util.List;

public final class ScaleGripRulesTest {
	private ScaleGripRulesTest() {
	}

	public static void main(String[] args) {
		selectsFirstEligibleNormalStack();
		neverSelectsScaleGrip();
		returnsNoSelectionForEmptyInventory();
	}

	private static void selectsFirstEligibleNormalStack() {
		List<ScaleGripRules.Candidate> candidates = List.of(
				new ScaleGripRules.Candidate(0, "hemomancy:scale_grip", false, true),
				new ScaleGripRules.Candidate(1, "minecraft:diamond_pickaxe", false, false),
				new ScaleGripRules.Candidate(2, "minecraft:iron_ingot", false, false));
		assertEquals(1, ScaleGripRules.selectProtectedSlot(candidates).orElse(-1), "first eligible slot");
	}

	private static void neverSelectsScaleGrip() {
		List<ScaleGripRules.Candidate> candidates = List.of(
				new ScaleGripRules.Candidate(0, "hemomancy:scale_grip", false, true),
				new ScaleGripRules.Candidate(1, "hemomancy:scale_grip", false, true));
		assertFalse(ScaleGripRules.selectProtectedSlot(candidates).isPresent(), "scale grip cannot protect itself");
	}

	private static void returnsNoSelectionForEmptyInventory() {
		List<ScaleGripRules.Candidate> candidates = List.of(
				new ScaleGripRules.Candidate(0, "minecraft:air", true, false),
				new ScaleGripRules.Candidate(1, "hemomancy:scale_grip", false, true));
		assertFalse(ScaleGripRules.selectProtectedSlot(candidates).isPresent(), "empty inventory");
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertFalse(boolean value, String label) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}
```

- [ ] **Step 3: Add resource coverage test**

Create `src/test/java/com/vincenthuto/hemomancy/common/resource/PhaseThreeSerpentUtilityResourceTest.java`:

```java
package com.vincenthuto.hemomancy.common.resource;

import java.nio.file.Files;
import java.nio.file.Path;

public final class PhaseThreeSerpentUtilityResourceTest {
	private static final Path ROOT = Path.of("src/main/resources");
	private static final Path DATA = ROOT.resolve("data/hemomancy");
	private static final Path ASSETS = ROOT.resolve("assets/hemomancy");

	private PhaseThreeSerpentUtilityResourceTest() {
	}

	public static void main(String[] args) {
		for (String id : new String[] { "constrictor_cord", "scale_grip" }) {
			require(ASSETS.resolve("models/item/" + id + ".json"));
			require(ASSETS.resolve("textures/item/" + id + ".png"));
			require(DATA.resolve("recipe/" + id + ".json"));
		}
		require(ASSETS.resolve("textures/mob_effect/constricted.png"));
	}

	private static void require(Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
	}
}
```

- [ ] **Step 4: Run RED compile**

Run:

```powershell
./gradlew.bat testClasses --no-daemon
```

Expected: compilation fails because `ConstrictorCordRules` and `ScaleGripRules` do not exist.

---

### Task 2: Implement Pure Rules And Registrations

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/effect/ConstrictorCordRules.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/item/shared/ScaleGripRules.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/EffectInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`

- [ ] **Step 1: Create ConstrictorCordRules**

Create `src/main/java/com/vincenthuto/hemomancy/common/effect/ConstrictorCordRules.java`:

```java
package com.vincenthuto.hemomancy.common.effect;

public final class ConstrictorCordRules {
	public static final int DURATION_TICKS = 100;
	public static final int BOSS_DURATION_TICKS = DURATION_TICKS / 2;
	public static final int USE_COOLDOWN_TICKS = 20;
	public static final double PROJECTILE_VELOCITY = 1.25D;
	public static final double PROJECTILE_INACCURACY = 1.0D;
	public static final double MOVEMENT_SPEED_MULTIPLIER = 0.08D;

	private ConstrictorCordRules() {
	}
}
```

- [ ] **Step 2: Create ScaleGripRules**

Create `src/main/java/com/vincenthuto/hemomancy/common/item/shared/ScaleGripRules.java`:

```java
package com.vincenthuto.hemomancy.common.item.shared;

import java.util.List;
import java.util.OptionalInt;

public final class ScaleGripRules {
	private ScaleGripRules() {
	}

	public static OptionalInt selectProtectedSlot(List<Candidate> candidates) {
		for (Candidate candidate : candidates) {
			if (!candidate.empty() && !candidate.scaleGrip()) {
				return OptionalInt.of(candidate.slot());
			}
		}
		return OptionalInt.empty();
	}

	public record Candidate(int slot, String itemId, boolean empty, boolean scaleGrip) {
	}
}
```

- [ ] **Step 3: Add registry placeholders**

Modify `ItemInit`, `EffectInit`, and `EntityInit` after the relevant existing registrations:

```java
// ItemInit near serpent_scale
public static final DeferredHolder<Item, Item> constrictor_cord = BASEITEMS.register("constrictor_cord",
		() -> new ConstrictorCordItem(new Item.Properties()));
public static final DeferredHolder<Item, Item> scale_grip = BASEITEMS.register("scale_grip",
		() -> new Item(new Item.Properties()));
```

```java
// EffectInit near other utility effects
public static final DeferredHolder<MobEffect, MobEffect> constricted = EFFECTS.register("constricted",
		() -> new ConstrictedEffect(MobEffectCategory.HARMFUL, 0x6D4731));
```

```java
// EntityInit near projectiles
public static final DeferredHolder<EntityType<?>, EntityType<ConstrictorCordEntity>> constrictor_cord_projectile =
		ENTITY_TYPES.register("constrictor_cord_projectile",
				() -> EntityType.Builder.<ConstrictorCordEntity>of(ConstrictorCordEntity::new, MobCategory.MISC)
						.sized(0.25F, 0.25F)
						.clientTrackingRange(4)
						.updateInterval(4)
						.build(Hemomancy.rloc("constrictor_cord_projectile").toString()));
```

At this point the project still will not compile until the runtime classes in Task 3 exist.

---

### Task 3: Implement Constrictor Cord Runtime

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/effect/ConstrictedEffect.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/item/shared/ConstrictorCordItem.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/ConstrictorCordEntity.java`

- [ ] **Step 1: Create ConstrictedEffect**

Create `src/main/java/com/vincenthuto/hemomancy/common/effect/ConstrictedEffect.java`:

```java
package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ConstrictedEffect extends MobEffect {
	public ConstrictedEffect(MobEffectCategory category, int color) {
		super(category, color);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
				Hemomancy.rloc("constricted_movement_speed"),
				ConstrictorCordRules.MOVEMENT_SPEED_MULTIPLIER - 1.0D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}
```

If the attribute modifier signature differs in this NeoForge mapping, keep the same ID, amount, and operation while adapting only the method signature.

- [ ] **Step 2: Create ConstrictorCordItem**

Create `src/main/java/com/vincenthuto/hemomancy/common/item/shared/ConstrictorCordItem.java`:

```java
package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.common.effect.ConstrictorCordRules;
import com.vincenthuto.hemomancy.common.entity.projectile.ConstrictorCordEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ConstrictorCordItem extends Item {
	public ConstrictorCordItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEAD_KNOT_PLACE,
				SoundSource.PLAYERS, 0.6F, 0.8F);
		if (!level.isClientSide) {
			ConstrictorCordEntity projectile = new ConstrictorCordEntity(level, player);
			projectile.setItem(stack);
			projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
					(float) ConstrictorCordRules.PROJECTILE_VELOCITY,
					(float) ConstrictorCordRules.PROJECTILE_INACCURACY);
			level.addFreshEntity(projectile);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		player.getCooldowns().addCooldown(this, ConstrictorCordRules.USE_COOLDOWN_TICKS);
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}
}
```

- [ ] **Step 3: Create ConstrictorCordEntity**

Create `src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/ConstrictorCordEntity.java`:

```java
package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.effect.ConstrictorCordRules;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ConstrictorCordEntity extends ThrowableItemProjectile {
	public ConstrictorCordEntity(EntityType<? extends ConstrictorCordEntity> type, Level level) {
		super(type, level);
	}

	public ConstrictorCordEntity(Level level, LivingEntity owner) {
		super(EntityInit.constrictor_cord_projectile.get(), owner, level);
	}

	@Override
	protected Item getDefaultItem() {
		return ItemInit.constrictor_cord.get();
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!level().isClientSide && result.getEntity() instanceof LivingEntity target && target != getOwner()) {
			int duration = target instanceof Mob mob && mob.getType().is(net.minecraft.tags.EntityTypeTags.BOSSES)
					? ConstrictorCordRules.BOSS_DURATION_TICKS
					: ConstrictorCordRules.DURATION_TICKS;
			target.addEffect(new MobEffectInstance(EffectInit.constricted, duration, 0, false, true, true), getOwner());
		}
	}

	@Override
	protected void onHit(HitResult result) {
		if (!level().isClientSide) {
			if (level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 12, 0.2D, 0.2D, 0.2D, 0.03D);
			}
			level().playSound(null, blockPosition(), SoundEvents.LEAD_BREAK, SoundSource.PLAYERS, 0.7F, 0.75F);
		}
		this.remove(RemovalReason.KILLED);
	}
}
```

If `EntityTypeTags.BOSSES` is not available in this mapping, use `target.canChangeDimensions()` only as a compile fallback is not acceptable. Instead, remove the boss branch and apply normal duration; the effect is short enough to remain safe. Record that fallback in docs.

- [ ] **Step 4: Compile after Constrictor Cord runtime**

Run:

```powershell
./gradlew.bat testClasses --no-daemon
```

Expected: compile errors only if mapped method signatures need adjustment. Fix signatures without changing Phase 3 behavior.

---

### Task 4: Implement Scale Grip Runtime

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/item/shared/ScaleGripEvents.java`

- [ ] **Step 1: Create ScaleGripEvents**

Create `src/main/java/com/vincenthuto/hemomancy/common/item/shared/ScaleGripEvents.java`:

```java
package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ScaleGripEvents {
	private static final Map<UUID, ItemStack> PENDING_RETURNS = new ConcurrentHashMap<>();

	private ScaleGripEvents() {
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			return;
		}
		int gripSlot = findScaleGripSlot(player);
		if (gripSlot < 0) {
			return;
		}

		OptionalInt protectedSlot = ScaleGripRules.selectProtectedSlot(toCandidates(player));
		if (protectedSlot.isEmpty()) {
			return;
		}

		ItemStack saved = player.getInventory().getItem(protectedSlot.getAsInt()).copy();
		if (saved.isEmpty()) {
			return;
		}
		player.getInventory().setItem(protectedSlot.getAsInt(), ItemStack.EMPTY);
		player.getInventory().getItem(gripSlot).shrink(1);
		PENDING_RETURNS.put(player.getUUID(), saved);
		player.displayClientMessage(Component.literal("Scale Grip coils around " + saved.getHoverName().getString() + ".")
				.withStyle(ChatFormatting.DARK_GREEN), true);
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		ItemStack saved = PENDING_RETURNS.remove(player.getUUID());
		if (saved == null || saved.isEmpty()) {
			return;
		}
		if (!player.getInventory().add(saved)) {
			player.drop(saved, false);
		}
	}

	private static int findScaleGripSlot(ServerPlayer player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			if (player.getInventory().getItem(slot).is(ItemInit.scale_grip.get())) {
				return slot;
			}
		}
		return -1;
	}

	private static List<ScaleGripRules.Candidate> toCandidates(ServerPlayer player) {
		List<ScaleGripRules.Candidate> candidates = new ArrayList<>();
		for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
			ItemStack stack = player.getInventory().items.get(slot);
			candidates.add(new ScaleGripRules.Candidate(slot, itemId(stack), stack.isEmpty(),
					stack.is(ItemInit.scale_grip.get())));
		}
		return candidates;
	}

	private static String itemId(ItemStack stack) {
		if (stack.isEmpty()) {
			return "minecraft:air";
		}
		ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
		return id == null ? "minecraft:air" : id.toString();
	}
}
```

- [ ] **Step 2: Compile after Scale Grip runtime**

Run:

```powershell
./gradlew.bat testClasses --no-daemon
```

Expected: compile succeeds after adapting any mapped method signatures.

---

### Task 5: Add Resources, Textures, Lang, And Docs

**Files:**
- Add: `src/main/resources/assets/hemomancy/models/item/constrictor_cord.json`
- Add: `src/main/resources/assets/hemomancy/models/item/scale_grip.json`
- Add: `src/main/resources/assets/hemomancy/textures/item/constrictor_cord.png`
- Add: `src/main/resources/assets/hemomancy/textures/item/scale_grip.png`
- Add: `src/main/resources/assets/hemomancy/textures/mob_effect/constricted.png`
- Add: `src/main/resources/data/hemomancy/recipe/constrictor_cord.json`
- Add: `src/main/resources/data/hemomancy/recipe/scale_grip.json`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Modify: `docs/HEMOMANCY_REFERENCE.md`

- [ ] **Step 1: Add item models**

Create `constrictor_cord.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "hemomancy:item/constrictor_cord"
  }
}
```

Create `scale_grip.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "hemomancy:item/scale_grip"
  }
}
```

- [ ] **Step 2: Add recipes**

Create `src/main/resources/data/hemomancy/recipe/constrictor_cord.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "tools",
  "key": {
    "S": { "item": "hemomancy:serpent_scale" },
    "T": { "item": "hemomancy:puppeteering_thread" },
    "D": { "item": "hemomancy:dicentra_sap" }
  },
  "pattern": [
    "TST",
    "SDS",
    "TST"
  ],
  "result": {
    "count": 4,
    "id": "hemomancy:constrictor_cord"
  }
}
```

Create `src/main/resources/data/hemomancy/recipe/scale_grip.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "tools",
  "key": {
    "S": { "item": "hemomancy:serpent_scale" },
    "D": { "item": "hemomancy:dicentra_sap" },
    "I": { "item": "hemomancy:hematic_iron_scrap" }
  },
  "pattern": [
    " S ",
    "SDS",
    " I "
  ],
  "result": {
    "count": 1,
    "id": "hemomancy:scale_grip"
  }
}
```

- [ ] **Step 3: Generate textures**

Use PowerShell/.NET to generate 16x16 PNGs for:
- `textures/item/constrictor_cord.png`: coiled dark red cord with scale flecks.
- `textures/item/scale_grip.png`: hooked crimson/bronze scale clasp.
- `textures/mob_effect/constricted.png`: tight cord loop icon.

- [ ] **Step 4: Add lang entries**

Add to `src/main/resources/assets/hemomancy/lang/en_us.json`:

```json
"effect.hemomancy.constricted": "Constricted",
"item.hemomancy.constrictor_cord": "Constrictor Cord",
"item.hemomancy.scale_grip": "Scale Grip"
```

- [ ] **Step 5: Update docs**

Update `docs/HEMOMANCY_REFERENCE.md` material notes:

```markdown
| ![](../src/main/resources/assets/hemomancy/textures/item/serpent_scale.png) Serpent Scale | Drops from Scarlet Serpents in desert/badlands, swamp, and jungle biome families. Crafts Constrictor Cords for short field control and Scale Grip for one-stack death retention. |
```

Add a short Phase 3 note near the Phase 1/2 utility additions:

```markdown
**Phase 3 serpent utility additions:** `constrictor_cord` is a throwable Serpent Scale utility that applies short `constricted` movement suppression without damage. `scale_grip` is a single-use token that consumes itself on death to preserve one non-Scale-Grip inventory stack for respawn when `keepInventory` is false.
```

---

### Task 6: Verify And Fix

**Files:**
- Tests and any compile-fix files from previous tasks.

- [ ] **Step 1: Run compile/tests**

Run:

```powershell
./gradlew.bat testClasses --no-daemon
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run explicit sentinel tests**

Run:

```powershell
java -cp "build/classes/java/test;build/classes/java/main" com.vincenthuto.hemomancy.common.effect.ConstrictorCordRulesTest
java -cp "build/classes/java/test;build/classes/java/main" com.vincenthuto.hemomancy.common.item.shared.ScaleGripRulesTest
java -cp build/classes/java/test com.vincenthuto.hemomancy.common.resource.PhaseThreeSerpentUtilityResourceTest
```

Expected: each command exits 0 with no output.

- [ ] **Step 3: Run full build**

Run:

```powershell
./gradlew.bat build --no-daemon
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Check worktree scope**

Run:

```powershell
git status --short
```

Expected: Phase 3 files are present alongside the already-uncommitted Phase 1/2 files. Do not revert or stage unrelated changes.
