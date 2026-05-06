package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.AnnettaDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.projectile.HemolyticVialEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.util.SpecimenJarData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AnnettaKnowlesEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(AnnettaKnowlesEntity.class, EntityDataSerializers.INT);

    private static final int SILVER_AURA_INTERVAL = 60;
    private static final int VIAL_INTERVAL = 90;
    private static final int KERATIN_INTERVAL = 70;
    private static final double SILVER_AURA_RADIUS = 6.0D;
    private static final float SILVER_AURA_DAMAGE = 3.0F;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState phase2AnimationState = new AnimationState();

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hemomancy.annetta_knowles"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_10);

    public AnnettaKnowlesEntity(EntityType<? extends AnnettaKnowlesEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 100;
        this.bossEvent.setVisible(false);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 350.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ARMOR, 8.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, EncounterState.COWERING.id);
    }

    public EncounterState getEncounterState() {
        return EncounterState.byId(this.entityData.get(DATA_STATE));
    }

    private void setEncounterState(EncounterState state) {
        this.entityData.set(DATA_STATE, state.id);
        this.bossEvent.setVisible(state == EncounterState.PHASE_ONE);
    }

    public void setCowering() {
        this.setEncounterState(EncounterState.COWERING);
    }

    public boolean isCuredSupport() {
        return this.getEncounterState() == EncounterState.CURED_SUPPORT;
    }

    public void markResolvedAfterCure() {
        this.setEncounterState(EncounterState.RESOLVED);
        this.setTarget(null);
        this.getNavigation().stop();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.getEncounterState() == EncounterState.PHASE_ONE) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        EncounterState state = this.getEncounterState();
        if (state == EncounterState.COWERING) {
            ItemStack held = player.getMainHandItem();
            if (isToothPecksJar(held) && isHarbinger(player)) {
                startHarbingerRoute(serverPlayer, held);
                return InteractionResult.CONSUME;
            }
            if (held.is(ItemInit.draught_of_still_mercy.get()) && canCure(player)) {
                startCureRoute(serverPlayer, held);
                return InteractionResult.CONSUME;
            }
            openDialogue(serverPlayer, chooseCoweringDialogue(player));
            return InteractionResult.CONSUME;
        }

        if (state == EncounterState.CURED_SUPPORT) {
            openDialogue(serverPlayer, AnnettaDialogueTrees.curedSupport(this.getId()));
            return InteractionResult.CONSUME;
        }
        if (state == EncounterState.RESOLVED) {
            openDialogue(serverPlayer, AnnettaDialogueTrees.resolved(this.getId()));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private DialogueTree chooseCoweringDialogue(Player player) {
        if (canCure(player)) {
            return AnnettaDialogueTrees.coweringUnstainedHint(this.getId());
        }
        if (isHarbinger(player)) {
            return AnnettaDialogueTrees.coweringHarbingerHint(this.getId());
        }
        return AnnettaDialogueTrees.coweringNeutral(this.getId());
    }

    private void openDialogue(ServerPlayer player, DialogueTree tree) {
        PacketHandler.sendToPlayer(player, new OpenDialoguePacket(tree));
    }

    private void startHarbingerRoute(ServerPlayer player, ItemStack jarStack) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        CompoundTag specimen = SpecimenJarData.getSpecimen(jarStack);
        SpecimenJarData.clearSpecimen(jarStack);
        SpecimenJarData.releaseSpecimen(server, this.blockPosition().relative(player.getDirection().getOpposite()), specimen);

        this.setEncounterState(EncounterState.PHASE_ONE);
        this.setHealth(this.getMaxHealth());
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.absolution_dagger.get()));
        this.setTarget(player);
        addNearbyPlayersToBossBar(server);

        server.playSound(null, this.blockPosition(), SoundEvents.GENERIC_HURT, SoundSource.HOSTILE, 1.2F, 1.3F);
        server.sendParticles(ParticleTypes.DAMAGE_INDICATOR, this.getX(), this.getY() + 1.0D, this.getZ(),
                12, 0.35D, 0.5D, 0.35D, 0.05D);
        broadcastNearby(server, Component.translatable("hemomancy.annetta.event.bitten")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }

    private void startCureRoute(ServerPlayer player, ItemStack brewStack) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            brewStack.shrink(1);
        }
        this.setEncounterState(EncounterState.CURED_SUPPORT);
        this.setHealth(this.getMaxHealth());
        this.setTarget(null);
        this.getNavigation().stop();

        LatentAnnettaInfectionEntity infection = EntityInit.latent_annetta_infection.get().create(server);
        if (infection != null) {
            infection.moveTo(this.getX() + 2.0D, this.getY(), this.getZ() + 2.0D, this.getYRot(), 0.0F);
            infection.setTarget(player);
            infection.setPersistenceRequired();
            server.addFreshEntity(infection);
        }

        server.playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 1.2F, 1.4F);
        server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.0D, this.getZ(),
                40, 0.5D, 0.8D, 0.5D, 0.05D);
        broadcastNearby(server, Component.translatable("hemomancy.annetta.event.cured")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            idleAnimationState.animateWhen(this.isAlive(), this.tickCount);
            phase2AnimationState.animateWhen(false, this.tickCount);
            return;
        }

        EncounterState state = this.getEncounterState();
        if (state != EncounterState.PHASE_ONE) {
            this.setTarget(null);
            this.getNavigation().stop();
            this.bossEvent.setVisible(false);
            if (state == EncounterState.CURED_SUPPORT && this.tickCount % 60 == 0) {
                tickCuredSupport((ServerLevel) this.level());
            }
            return;
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        tickPhaseOne((ServerLevel) this.level());
    }

    private void tickPhaseOne(ServerLevel server) {
        if (this.tickCount % SILVER_AURA_INTERVAL == 0) {
            server.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.0D, this.getZ(),
                    30, SILVER_AURA_RADIUS * 0.5D, 0.8D, SILVER_AURA_RADIUS * 0.5D, 0.02D);
            server.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.2F, 1.6F);

            for (Player player : server.getEntitiesOfClass(Player.class, new AABB(this.blockPosition()).inflate(SILVER_AURA_RADIUS))) {
                HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
                    if (volume.isActive()) {
                        player.hurt(this.damageSources().magic(), SILVER_AURA_DAMAGE);
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
                    }
                });
            }
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.tickCount % VIAL_INTERVAL == 20) {
            throwHemolyticVial(server, target);
        }

        if (this.getHealth() <= this.getMaxHealth() * 0.5F && this.tickCount % KERATIN_INTERVAL == 35) {
            slashWithHairAndNails(server);
        }
    }

    private void throwHemolyticVial(ServerLevel server, LivingEntity target) {
        HemolyticVialEntity vial = new HemolyticVialEntity(server, this);
        vial.setPos(this.getX(), this.getEyeY() - 0.1D, this.getZ());
        Vec3 delta = target.getEyePosition().subtract(vial.position());
        vial.shoot(delta.x, delta.y, delta.z, 0.9F, 6.0F);
        server.addFreshEntity(vial);
        server.playSound(null, this.blockPosition(), SoundEvents.SPLASH_POTION_THROW, SoundSource.HOSTILE, 1.0F, 0.8F);
    }

    private void slashWithHairAndNails(ServerLevel server) {
        AABB area = new AABB(this.blockPosition()).inflate(5.0D);
        server.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0D, this.getZ(),
                40, 2.0D, 0.7D, 2.0D, 0.05D);
        server.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.0F, 0.55F);
        for (Player player : server.getEntitiesOfClass(Player.class, area)) {
            player.hurt(this.damageSources().mobAttack(this), 5.0F);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, true));
        }
    }

    private void tickCuredSupport(ServerLevel server) {
        AABB supportArea = new AABB(this.blockPosition()).inflate(18.0D);
        for (LatentAnnettaInfectionEntity infection : server.getEntitiesOfClass(LatentAnnettaInfectionEntity.class, supportArea)) {
            infection.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, true));
            server.sendParticles(ParticleTypes.END_ROD, infection.getX(), infection.getY() + 1.0D, infection.getZ(),
                    10, 0.35D, 0.5D, 0.35D, 0.02D);
        }
        for (ServerPlayer player : server.getEntitiesOfClass(ServerPlayer.class, supportArea)) {
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(3.0F);
            }
            player.removeEffect(EffectInit.blood_loss);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, true));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        EncounterState state = this.getEncounterState();
        if (state == EncounterState.COWERING || state == EncounterState.CURED_SUPPORT || state == EncounterState.RESOLVED) {
            if (source.getEntity() instanceof Player player && player.isCreative()) {
                return super.hurt(source, amount);
            }
            return false;
        }
        if (state == EncounterState.PHASE_ONE && !this.level().isClientSide && amount >= this.getHealth()) {
            triggerMutation(source);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.getEncounterState() == EncounterState.PHASE_ONE && super.canAttack(target);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && this.getEncounterState() == EncounterState.PHASE_ONE && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
        }
        return result;
    }

    private void triggerMutation(DamageSource source) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }

        StainedPriestessEntity priestess = EntityInit.stained_priestess.get().create(server);
        if (priestess != null) {
            priestess.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            if (source.getEntity() instanceof LivingEntity living) {
                priestess.setTarget(living);
            }
            priestess.setPersistenceRequired();
            server.addFreshEntity(priestess);
        }

        server.sendParticles(ParticleTypes.FALLING_LAVA, this.getX(), this.getY() + 1.0D, this.getZ(),
                80, 0.6D, 1.0D, 0.6D, 0.08D);
        server.playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.2F, 0.7F);
        broadcastNearby(server, Component.translatable("hemomancy.annetta.event.mutation")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));

        this.bossEvent.removeAllPlayers();
        this.discard();
    }

    private void addNearbyPlayersToBossBar(ServerLevel server) {
        for (ServerPlayer player : server.getPlayers(p -> p.distanceToSqr(this) <= 96.0D * 96.0D)) {
            this.bossEvent.addPlayer(player);
        }
    }

    private void broadcastNearby(ServerLevel server, Component message) {
        for (ServerPlayer player : server.getPlayers(p -> p.distanceToSqr(this) <= 48.0D * 48.0D)) {
            player.displayClientMessage(message, false);
        }
    }

    private boolean isHarbinger(Player player) {
        boolean activeBlood = HemoCapabilityAccess.getBloodVolume(player)
                .map(IBloodVolume::isActive)
                .orElse(false);
        return activeBlood && HemoCapabilityAccess.getPlayerDegreeNumber(player) > 0;
    }

    private boolean canCure(Player player) {
        return HemoCapabilityAccess.getUnstainedProgress(player)
                .map(progress -> progress.hasClarityUnlocked())
                .orElse(false);
    }

    private boolean isToothPecksJar(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(com.vincenthuto.hemomancy.common.init.BlockInit.specimen_jar.get().asItem())) {
            return false;
        }
        CompoundTag specimen = SpecimenJarData.getSpecimen(stack);
        return EntityType.byString(specimen.getString("id"))
                .map(type -> type == EntityInit.tooth_pecks.get())
                .orElse(false);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("EncounterState", this.getEncounterState().name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setEncounterState(EncounterState.byName(tag.getString("EncounterState")));
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    public enum EncounterState {
        COWERING(0),
        PHASE_ONE(1),
        CURED_SUPPORT(2),
        RESOLVED(3);

        private final int id;

        EncounterState(int id) {
            this.id = id;
        }

        private static EncounterState byId(int id) {
            for (EncounterState state : values()) {
                if (state.id == id) {
                    return state;
                }
            }
            return COWERING;
        }

        private static EncounterState byName(String name) {
            for (EncounterState state : values()) {
                if (state.name().equals(name)) {
                    return state;
                }
            }
            return COWERING;
        }
    }
}
