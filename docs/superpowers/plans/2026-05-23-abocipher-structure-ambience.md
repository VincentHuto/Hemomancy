# Abocipher Structure Ambience Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add hidden, structure-placed Abocipher particle emitters to Blood Temple and Harbinger Outpost worldgen.

**Architecture:** A hidden `abocipher_emitter` block hosts a lightweight block entity with a persisted profile and variant seed. The emitter ticks only on the client, spawns existing `ParticleInit.abocipher` particles near nearby players, and is placed from each target structure's `afterPlace(...)` hook with randomized interior offsets.

**Tech Stack:** Minecraft 1.21.1, NeoForge 21.1.x registries, Java 21, Hemomancy block/block-entity patterns, existing Abocipher `SimpleParticleType`, PowerShell source guards, `./gradlew.bat build`.

---

### File Map

**Create:**
- `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/AbocipherEmitterBlock.java` - invisible, no-collision technical block with client ticker hookup.
- `src/main/java/com/vincenthuto/hemomancy/common/tile/functional/AbocipherEmitterBlockEntity.java` - stores profile/seed and emits Abocipher particles client-side.
- `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/AbocipherEmitterPlacement.java` - shared worldgen helper for randomized emitter placement.

**Modify:**
- `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java` - register `abocipher_emitter` and skip auto block item registration.
- `src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java` - register `abocipher_emitter` block entity type.
- `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/BloodTempleStructure.java` - place one Blood Temple emitter from `afterPlace(...)`.
- `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/HarbingerOutpostStructure.java` - place multiple Harbinger Outpost emitters from `afterPlace(...)`.
- `docs/HEMOMANCY_REFERENCE.md` - document the hidden structure ambience marker.

---

### Task 1: Add A Source Guard For Expected Changes

**Files:**
- Verify: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java`
- Verify: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java`
- Verify: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/BloodTempleStructure.java`
- Verify: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/HarbingerOutpostStructure.java`

- [ ] **Step 1: Run the failing source guard**

```powershell
@'
$checks = @()
$blockInit = Get-Content 'src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java' -Raw
$blockEntityInit = Get-Content 'src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java' -Raw
$temple = Get-Content 'src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/BloodTempleStructure.java' -Raw
$outpost = Get-Content 'src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/HarbingerOutpostStructure.java' -Raw
$checks += @{ Name = 'block registered'; Pass = $blockInit -match 'abocipher_emitter' }
$checks += @{ Name = 'hidden block item skipped'; Pass = $blockInit -match 'BlockInit\.abocipher_emitter\.get\(\)' -and $blockInit -match 'shouldSkipAutoBlockItem' }
$checks += @{ Name = 'block entity registered'; Pass = $blockEntityInit -match 'BlockEntityType<AbocipherEmitterBlockEntity>' }
$checks += @{ Name = 'temple places emitter'; Pass = $temple -match 'AbocipherEmitterPlacement\.placeBloodTempleEmitters' }
$checks += @{ Name = 'outpost places emitters'; Pass = $outpost -match 'AbocipherEmitterPlacement\.placeHarbingerOutpostEmitters' }
$failed = $checks | Where-Object { -not $_.Pass }
$checks | ForEach-Object { Write-Host "$($_.Name): $($_.Pass)" }
if ($failed) { exit 1 }
'@ | powershell -NoProfile -ExecutionPolicy Bypass
```

Expected: FAIL because no emitter block, block entity, or structure placement exists yet.

### Task 2: Implement The Hidden Emitter Block And Block Entity

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/AbocipherEmitterBlock.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/functional/AbocipherEmitterBlockEntity.java`

- [ ] **Step 1: Create the invisible block**

`AbocipherEmitterBlock` should extend `BaseEntityBlock`, return `RenderShape.INVISIBLE`, return empty shape/collision/occlusion shapes, and expose only a client-side ticker.

```java
package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.functional.AbocipherEmitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class AbocipherEmitterBlock extends BaseEntityBlock {
    public static final MapCodec<AbocipherEmitterBlock> CODEC = simpleCodec(AbocipherEmitterBlock::new);

    public AbocipherEmitterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbocipherEmitterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, BlockEntityInit.abocipher_emitter.get(), AbocipherEmitterBlockEntity::clientTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
```

- [ ] **Step 2: Create the block entity**

`AbocipherEmitterBlockEntity` should persist `Profile` and `VariantSeed`, use only common-side `Level` APIs, and call `level.addParticle(ParticleInit.abocipher.get(), ...)` from `clientTick`.

```java
package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class AbocipherEmitterBlockEntity extends BlockEntity {
    private static final String TAG_PROFILE = "Profile";
    private static final String TAG_VARIANT_SEED = "VariantSeed";

    private Profile profile = Profile.BLOOD_TEMPLE;
    private long variantSeed;

    public AbocipherEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.abocipher_emitter.get(), pos, state);
        this.variantSeed = pos.asLong();
    }

    public void configure(Profile profile, long variantSeed) {
        this.profile = profile;
        this.variantSeed = variantSeed;
        setChanged();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AbocipherEmitterBlockEntity emitter) {
        Profile profile = emitter.profile;
        long gameTime = level.getGameTime();
        if (Math.floorMod(gameTime + emitter.cadenceOffset(), profile.cadenceTicks) != 0) {
            return;
        }
        if (level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                profile.viewDistance, false) == null) {
            return;
        }

        int count = profile.baseParticles;
        if (level.random.nextFloat() < profile.extraParticleChance) {
            count++;
        }
        for (int i = 0; i < count; i++) {
            emitter.spawnParticle(level, pos, profile);
        }
    }

    private int cadenceOffset() {
        return (int) Math.floorMod(variantSeed, profile.cadenceTicks);
    }

    private void spawnParticle(Level level, BlockPos pos, Profile profile) {
        double angle = level.random.nextDouble() * Mth.TWO_PI;
        double distance = Math.sqrt(level.random.nextDouble()) * profile.radius;
        double x = pos.getX() + 0.5D + Math.cos(angle) * distance;
        double y = pos.getY() + 0.35D + level.random.nextDouble() * profile.verticalSpread;
        double z = pos.getZ() + 0.5D + Math.sin(angle) * distance;
        double driftX = (level.random.nextDouble() - 0.5D) * profile.horizontalDrift;
        double driftY = profile.upwardDriftMin + level.random.nextDouble() * profile.upwardDriftRange;
        double driftZ = (level.random.nextDouble() - 0.5D) * profile.horizontalDrift;

        level.addParticle(ParticleInit.abocipher.get(), x, y, z, driftX, driftY, driftZ);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.profile = tag.contains(TAG_PROFILE, Tag.TAG_STRING) ? Profile.byId(tag.getString(TAG_PROFILE)) : Profile.BLOOD_TEMPLE;
        this.variantSeed = tag.contains(TAG_VARIANT_SEED, Tag.TAG_LONG) ? tag.getLong(TAG_VARIANT_SEED) : worldPosition.asLong();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(TAG_PROFILE, profile.id);
        tag.putLong(TAG_VARIANT_SEED, variantSeed);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        loadAdditional(tag, registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        if (pkt.getTag() != null) {
            handleUpdateTag(pkt.getTag(), registries);
        }
    }

    public enum Profile {
        BLOOD_TEMPLE("blood_temple", 4.0D, 2.7D, 8, 1, 0.25F, 42.0D, 0.015D, 0.010D, 0.020D),
        HARBINGER_OUTPOST("harbinger_outpost", 5.5D, 3.8D, 6, 1, 0.55F, 48.0D, 0.020D, 0.010D, 0.025D);

        private final String id;
        private final double radius;
        private final double verticalSpread;
        private final int cadenceTicks;
        private final int baseParticles;
        private final float extraParticleChance;
        private final double viewDistance;
        private final double horizontalDrift;
        private final double upwardDriftMin;
        private final double upwardDriftRange;

        Profile(String id, double radius, double verticalSpread, int cadenceTicks, int baseParticles,
                float extraParticleChance, double viewDistance, double horizontalDrift,
                double upwardDriftMin, double upwardDriftRange) {
            this.id = id;
            this.radius = radius;
            this.verticalSpread = verticalSpread;
            this.cadenceTicks = cadenceTicks;
            this.baseParticles = baseParticles;
            this.extraParticleChance = extraParticleChance;
            this.viewDistance = viewDistance;
            this.horizontalDrift = horizontalDrift;
            this.upwardDriftMin = upwardDriftMin;
            this.upwardDriftRange = upwardDriftRange;
        }

        private static Profile byId(String id) {
            for (Profile profile : values()) {
                if (profile.id.equals(id)) {
                    return profile;
                }
            }
            return BLOOD_TEMPLE;
        }
    }
}
```

### Task 3: Register The Hidden Block And Block Entity

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java`

- [ ] **Step 1: Register the block in `BlockInit`**

Add the emitter near other special technical blocks:

```java
public static final DeferredHolder<Block, Block> abocipher_emitter = SPECIALBLOCKS.register("abocipher_emitter",
        () -> new AbocipherEmitterBlock(BlockBehaviour.Properties.of()
                .replaceable()
                .noCollission()
                .noOcclusion()
                .noLootTable()
                .pushReaction(PushReaction.DESTROY)
                .sound(SoundType.EMPTY)));
```

- [ ] **Step 2: Skip the automatic block item**

Add the emitter to `shouldSkipAutoBlockItem`:

```java
|| block == BlockInit.abocipher_emitter.get()
```

- [ ] **Step 3: Register the block entity type**

Add to `BlockEntityInit`:

```java
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AbocipherEmitterBlockEntity>> abocipher_emitter = TILES
        .register("abocipher_emitter", () -> BlockEntityType.Builder
                .of(AbocipherEmitterBlockEntity::new, BlockInit.abocipher_emitter.get()).build(null));
```

### Task 4: Add Randomized Structure Placement

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/AbocipherEmitterPlacement.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/BloodTempleStructure.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/HarbingerOutpostStructure.java`

- [ ] **Step 1: Create the placement helper**

```java
package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.functional.AbocipherEmitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class AbocipherEmitterPlacement {
    private static final int MAX_ATTEMPTS = 48;

    private AbocipherEmitterPlacement() {
    }

    public static void placeBloodTempleEmitters(WorldGenLevel level, BoundingBox fullBox, RandomSource random,
            BlockPos center) {
        BlockPos origin = new BlockPos(center.getX(), Math.max(fullBox.minY() + 2, center.getY()), center.getZ());
        placeNear(level, fullBox, random, origin, 4, 3, AbocipherEmitterBlockEntity.Profile.BLOOD_TEMPLE);
    }

    public static void placeHarbingerOutpostEmitters(WorldGenLevel level, BoundingBox fullBox, RandomSource random,
            int centerX, int centerZ, int floorY, int maxY) {
        int quarterX = Math.max(2, (fullBox.maxX() - fullBox.minX()) / 4);
        int quarterZ = Math.max(2, (fullBox.maxZ() - fullBox.minZ()) / 4);
        int y = Math.min(maxY, floorY + 3);

        placeNear(level, fullBox, random, new BlockPos(centerX, y, centerZ), 4, 4,
                AbocipherEmitterBlockEntity.Profile.HARBINGER_OUTPOST);
        placeNear(level, fullBox, random, new BlockPos(centerX - quarterX, y, centerZ + quarterZ), 3, 4,
                AbocipherEmitterBlockEntity.Profile.HARBINGER_OUTPOST);
        placeNear(level, fullBox, random, new BlockPos(centerX + quarterX, y, centerZ - quarterZ), 3, 4,
                AbocipherEmitterBlockEntity.Profile.HARBINGER_OUTPOST);
    }

    private static boolean placeNear(WorldGenLevel level, BoundingBox fullBox, RandomSource random, BlockPos origin,
            int horizontalSpread, int verticalSpread, AbocipherEmitterBlockEntity.Profile profile) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int dx = attempt == 0 ? 0 : random.nextInt(horizontalSpread * 2 + 1) - horizontalSpread;
            int dy = attempt == 0 ? 0 : random.nextInt(verticalSpread * 2 + 1) - verticalSpread;
            int dz = attempt == 0 ? 0 : random.nextInt(horizontalSpread * 2 + 1) - horizontalSpread;
            BlockPos candidate = origin.offset(dx, dy, dz);
            if (!fullBox.isInside(candidate.getX(), candidate.getY(), candidate.getZ())) {
                continue;
            }
            if (!level.getBlockState(candidate).isAir()) {
                continue;
            }

            level.setBlock(candidate, BlockInit.abocipher_emitter.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(candidate) instanceof AbocipherEmitterBlockEntity emitter) {
                emitter.configure(profile, random.nextLong());
            }
            return true;
        }
        return false;
    }
}
```

- [ ] **Step 2: Place the Blood Temple emitter**

In `BloodTempleStructure.afterPlace(...)`, after the two `DiscoveryInscriptionPlacement` calls and before NPC spawning, add:

```java
AbocipherEmitterPlacement.placeBloodTempleEmitters(level, fullBox, random,
        new BlockPos(centerX, centerY + 1, centerZ));
```

- [ ] **Step 3: Place the Harbinger Outpost emitters**

In `HarbingerOutpostStructure.afterPlace(...)`, after the two `DiscoveryInscriptionPlacement` calls and before NPC spawning, add:

```java
AbocipherEmitterPlacement.placeHarbingerOutpostEmitters(level, fullBox, random,
        centerX, centerZ, floorY, maxY);
```

### Task 5: Document And Verify

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`
- Verify: all modified Java files

- [ ] **Step 1: Document the ambience marker**

In the particle/worldgen documentation, add a short note:

```markdown
- **Abocipher Structure Ambience** - Blood Temple and Harbinger Outpost generation places hidden `abocipher_emitter` technical blocks during `afterPlace`. These invisible, non-colliding block entities emit client-side Abocipher glyph particles with Blood Temple and Harbinger Outpost profiles. Existing explored structures are not retroactively migrated.
```

- [ ] **Step 2: Re-run the source guard**

Run the Task 1 PowerShell guard again.

Expected: PASS for all five checks.

- [ ] **Step 3: Run formatting/whitespace guard**

```powershell
git diff --check
```

Expected: no output and exit code 0.

- [ ] **Step 4: Run the build**

```powershell
./gradlew.bat build
```

Expected: build exits 0.

