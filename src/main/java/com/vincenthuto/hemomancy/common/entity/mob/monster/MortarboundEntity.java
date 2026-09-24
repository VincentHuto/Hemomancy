package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.antecedent.VigilSites;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class MortarboundEntity extends Monster {
    private static final int HEARD_MEMORY_TICKS = 160;
    private static final EntityDataAccessor<Integer> FACE = SynchedEntityData.defineId(MortarboundEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK = SynchedEntityData.defineId(MortarboundEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FORM = SynchedEntityData.defineId(MortarboundEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FORM_TICKS = SynchedEntityData.defineId(MortarboundEntity.class, EntityDataSerializers.INT);
    private BlockPos wallCell;
    private MortarboundWallRules.Step travel;
    private Vec3 waypoint;
    private Vec3 heard;
    private long heardAt;
    private int cooldown;
    private int patrolDirection = 1;
    private int forcedEmergenceTicks;

    public MortarboundEntity(EntityType<? extends MortarboundEntity> type, Level level) {
        super(type, level);
        xpReward = 5;
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24).add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.18).add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FACE, Direction.NORTH.get3DDataValue());
        builder.define(ATTACK, 0);
        builder.define(FORM, MortarboundFormRules.Form.EMBEDDED.ordinal());
        builder.define(FORM_TICKS, 0);
    }

    public Direction wallFace() { return Direction.from3DDataValue(entityData.get(FACE)); }
    public int attackTicks() { return entityData.get(ATTACK); }
    @Override public AABB getBoundingBoxForCulling() { return getBoundingBox().inflate(.65, .05, .65); }
    public MortarboundFormRules.State formState() {
        int index = Math.clamp(entityData.get(FORM), 0, MortarboundFormRules.Form.values().length - 1);
        return new MortarboundFormRules.State(MortarboundFormRules.Form.values()[index], entityData.get(FORM_TICKS));
    }
    public float emergence(float partialTick) {
        var state = formState();
        float value = MortarboundFormRules.emergence(state);
        if (state.form() == MortarboundFormRules.Form.EMERGING) value += partialTick / MortarboundFormRules.TRANSITION_TICKS;
        if (state.form() == MortarboundFormRules.Form.RETREATING) value -= partialTick / MortarboundFormRules.TRANSITION_TICKS;
        return Math.clamp(value, 0F, 1F);
    }
    private void setForm(MortarboundFormRules.State state) {
        entityData.set(FORM, state.form().ordinal());
        entityData.set(FORM_TICKS, state.ticks());
    }

    private static boolean clear(Level level, BlockPos cell) {
        for (int y = 0; y < 3; y++) {
            BlockPos pos = cell.above(y);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return false;
        }
        return true;
    }

    private static boolean clearSpan(Level level, BlockPos cell, Direction face) {
        if (!clear(level, cell)) return false;
        Direction side = face.getClockWise();
        return clear(level, cell.relative(side)) && clear(level, cell.relative(side.getOpposite()));
    }

    private static boolean supported(Level level, BlockPos cell, Direction face) {
        BlockPos wall = cell.relative(face.getOpposite());
        for (int y = 0; y < 3; y++) {
            BlockPos pos = wall.above(y);
            if (!level.getBlockState(pos).isFaceSturdy(level, pos, face)) return false;
        }
        return true;
    }

    private static Direction findFace(Level level, BlockPos cell) {
        for (Direction face : Direction.Plane.HORIZONTAL)
            if (supported(level, cell, face) && clearSpan(level, cell, face)) return face;
        return null;
    }

    private boolean attach() {
        BlockPos origin = blockPosition();
        for (int radius = 0; radius <= 3; radius++) {
            for (BlockPos sample : BlockPos.betweenClosed(origin.offset(-radius, -1, -radius), origin.offset(radius, 2, radius))) {
                Direction face = findFace(level(), sample);
                if (face == null) continue;
                wallCell = sample.immutable();
                entityData.set(FACE, face.get3DDataValue());
                setPos(wallCell.getX() + .5, wallCell.getY() + .1, wallCell.getZ() + .5);
                setNoGravity(true);
                return true;
            }
        }
        return false;
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            var form = formState().form();
            if ((form == MortarboundFormRules.Form.EMERGING || form == MortarboundFormRules.Form.RETREATING) && tickCount % 4 == 0) {
                level().addParticle(ParticleTypes.SQUID_INK, getX(), getY() + .7, getZ(), 0, -.015, 0);
                level().addParticle(ParticleTypes.SCULK_SOUL, getX(), getY() + 1, getZ(), 0, .01, 0);
            }
            if (emergence(0) > 0 && (tickCount % 18 == 0 || attackTicks() > 0 && tickCount % 3 == 0))
                level().addParticle(ParticleTypes.SCULK_SOUL, getX(), getY() + 1.3, getZ(), 0, .015, 0);
            if (tickCount % 31 == 0)
                level().addParticle(ParticleTypes.SQUID_INK, getX(), getY() + .8, getZ(), 0, -.02, 0);
            return;
        }
        if (cooldown > 0) cooldown--;
        if (forcedEmergenceTicks > 0) forcedEmergenceTicks--;
        int windup = attackTicks();
        if (windup > 0) {
            entityData.set(ATTACK, windup - 1);
            if (windup == 1) strike();
        }
        if (wallCell == null && !attach()) {
            discard();
            return;
        }
        if (!supported(level(), wallCell, wallFace()) || !clearSpan(level(), wallCell, wallFace())) {
            wallCell = null;
            travel = null;
            waypoint = null;
            if (!attach()) { discard(); return; }
            setForm(new MortarboundFormRules.State(MortarboundFormRules.Form.EMBEDDED, 0));
            return;
        }
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
        if (heard != null && level().getGameTime() - heardAt > HEARD_MEMORY_TICKS) heard = null;
        Player seen = visiblePlayer();
        if (seen != null) {
            heard = seen.position();
            heardAt = level().getGameTime();
        }
        boolean close = forcedEmergenceTicks > 0 || seen != null && distanceToSqr(seen) <= 9
                || heard != null && position().distanceToSqr(heard) <= 2.25;
        var previous = formState();
        var state = MortarboundFormRules.tick(previous, close && travel == null, !close && attackTicks() == 0);
        if (previous.form() != state.form() && (state.form() == MortarboundFormRules.Form.EMERGING
                || state.form() == MortarboundFormRules.Form.RETREATING))
            playSound(SoundEvents.SCULK_BLOCK_SPREAD, .55F, state.form() == MortarboundFormRules.Form.EMERGING ? .65F : .45F);
        setForm(state);
        if (state.form() == MortarboundFormRules.Form.EMBEDDED) {
            if (travel == null && tickCount % 8 == 0) {
                if (heard != null) advance();
                else if (tickCount % 24 == 0) patrol();
            }
            creep();
            return;
        }
        if (!MortarboundFormRules.canHunt(state)) {
            if (travel != null) setPos(center(wallCell));
            travel = null;
            waypoint = null;
            return;
        }
        Player victim = nearestPlayer();
        if (victim != null && cooldown == 0 && attackTicks() == 0) {
            entityData.set(ATTACK, 16);
            playSound(SoundInit.ENTITY_MORTARBOUND_ATTACK.get(), .8F, 1F);
        }
    }

    private void patrol() {
        Direction side = wallFace().getClockWise();
        if (patrolDirection < 0) side = side.getOpposite();
        BlockPos next = wallCell.relative(side);
        if (!supported(level(), next, wallFace()) || !clearSpan(level(), next, wallFace())) {
            patrolDirection = -patrolDirection;
            next = wallCell.relative(side.getOpposite());
        }
        if (supported(level(), next, wallFace()) && clearSpan(level(), next, wallFace())) {
            travel = new MortarboundWallRules.Step(next, wallFace());
            waypoint = center(next);
        }
    }

    private Player visiblePlayer() {
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(6)))
            if (!player.isSpectator() && !player.isCreative() && distanceToSqr(player) <= 36 && hasLineOfSight(player)) return player;
        return null;
    }

    private Player nearestPlayer() {
        if (heard == null) return null;
        Player closest = null;
        double best = 2.5 * 2.5;
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(2.5))) {
            if (player.isSpectator() || player.isCreative()) continue;
            double distance = distanceToSqr(player);
            if (distance < best) { closest = player; best = distance; }
        }
        return closest;
    }

    private void strike() {
        Player player = nearestPlayer();
        if (player != null) {
            player.hurt(damageSources().mobAttack(this), 4);
            Vec3 tug = position().subtract(player.position()).normalize().scale(.35);
            player.push(tug.x, .08, tug.z);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0));
        }
        gameEvent(GameEvent.ENTITY_DAMAGE);
        level().playSound(null, blockPosition(), SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.HOSTILE, .4F, .6F);
        cooldown = 40;
    }

    private void advance() {
        if (heard == null) return;
        var step = MortarboundWallRules.next(wallCell, wallFace(), heard,
                (cell, face) -> supported(level(), cell, face) && clearSpan(level(), cell, face), cell -> clear(level(), cell));
        if (step != null) {
            travel = step;
            // Follow the two exposed air cells around a convex corner rather than cutting through stone.
            boolean corner = step.face() != wallFace() && step.cell().getX() != wallCell.getX()
                    && step.cell().getZ() != wallCell.getZ();
            waypoint = corner ? center(step.cell().relative(wallFace())) : center(step.cell());
        }
    }

    private static Vec3 center(BlockPos cell) { return new Vec3(cell.getX() + .5, cell.getY() + .1, cell.getZ() + .5); }

    private void creep() {
        if (travel == null) return;
        if (!clearSpan(level(), travel.cell(), travel.face()) || !supported(level(), travel.cell(), travel.face())) {
            travel = null;
            waypoint = null;
            return;
        }
        Vec3 offset = waypoint.subtract(position());
        if (offset.lengthSqr() > .015) {
            Vec3 move = offset.normalize().scale(Math.min(.13, offset.length()));
            setPos(position().add(move));
            return;
        }
        Vec3 finish = center(travel.cell());
        if (waypoint.distanceToSqr(finish) > .01) {
            waypoint = finish;
            return;
        }
        wallCell = travel.cell();
        entityData.set(FACE, travel.face().get3DDataValue());
        travel = null;
        waypoint = null;
        setPos(finish);
        if (random.nextInt(3) == 0) playSound(SoundInit.ENTITY_MORTARBOUND_CRAWL.get(), .18F, .75F + random.nextFloat() * .2F);
    }

    public void hear(Vec3 source) {
        if (position().distanceToSqr(source) > 256 || woolBetween(source)) return;
        heard = source;
        heardAt = level().getGameTime();
    }

    private boolean woolBetween(Vec3 source) {
        Vec3 delta = source.subtract(position());
        int steps = Math.max(1, (int) Math.ceil(delta.length() * 4));
        for (int i = 1; i < steps; i++) {
            BlockPos pos = BlockPos.containing(position().add(delta.scale((double)i / steps)));
            if (level().getBlockState(pos).is(BlockTags.WOOL) || level().getBlockState(pos).is(BlockTags.WOOL_CARPETS)) return true;
        }
        return false;
    }

    @SubscribeEvent public static void onVibration(VanillaGameEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !event.getVanillaEvent().is(GameEventTags.VIBRATIONS)) return;
        Entity cause = event.getCause();
        if (cause instanceof MortarboundEntity || cause != null && (cause.isSpectator() || cause.dampensVibrations()
                || cause.isSteppingCarefully() && event.getVanillaEvent().is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING))) return;
        Vec3 pos = event.getEventPosition();
        for (MortarboundEntity mob : level.getEntitiesOfClass(MortarboundEntity.class, new AABB(pos, pos).inflate(16))) mob.hear(pos);
    }

    public static boolean canSpawn(EntityType<MortarboundEntity> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || pos.getY() >= 0 || !Monster.checkMonsterSpawnRules(type, level, reason, pos, random)) return false;
        if (level.getLevel() instanceof ServerLevel server) {
            for (var site : VigilSites.loaded(server)) if (site.layout().contains(site.local(pos))) return false;
        }
        for (BlockPos sample : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 2, 2)))
            if (findFace(level.getLevel(), sample) != null) return true;
        return false;
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (wallCell != null) tag.putLong("WallCell", wallCell.asLong());
        tag.putInt("WallFace", wallFace().get3DDataValue());
        tag.putInt("Cooldown", cooldown);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("WallCell")) wallCell = BlockPos.of(tag.getLong("WallCell"));
        entityData.set(FACE, tag.getInt("WallFace"));
        cooldown = tag.getInt("Cooldown");
        setForm(new MortarboundFormRules.State(MortarboundFormRules.Form.EMBEDDED, 0));
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        boolean damaged = super.hurt(source, amount);
        if (damaged && !level().isClientSide && source.getEntity() instanceof Player player) {
            heard = player.position();
            heardAt = level().getGameTime();
            forcedEmergenceTicks = MortarboundFormRules.TRANSITION_TICKS + 20;
        }
        return damaged;
    }

    @Override protected SoundEvent getAmbientSound() {
        return formState().form() == MortarboundFormRules.Form.EMBEDDED ? null : SoundInit.ENTITY_MORTARBOUND_AMBIENT.get();
    }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundInit.ENTITY_MORTARBOUND_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return SoundInit.ENTITY_MORTARBOUND_DEATH.get(); }
}
