package com.vincenthuto.hemomancy.compat.mna.tile;

import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.items.ChargeableItem;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.runeforging.PedestalBlock;
import com.mna.blocks.tileentities.PedestalTile;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.sorcery.targeting.SpellSigil;
import com.mna.network.ServerMessageDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class BrokenManaTrapazahedronBlockEntity extends BlockEntity implements GeoBlockEntity {
    private ArrayList<Long> knownPedestalLocations;
    private static final int SCAN_RADIUS = 8;
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    public BrokenManaTrapazahedronBlockEntity(BlockPos pos, BlockState state) {
        super(MnAPluginBlockEntityInit.broken_mana_trapazahedron.get(), pos, state);
        this.knownPedestalLocations = new ArrayList<>();
    }

    public static void ServerTick(Level level, BlockPos pos, BlockState state, BrokenManaTrapazahedronBlockEntity tile) {

        if (!level.isClientSide()) {
            if (level.getGameTime() % 80L == 0L) {
                tile.addEffectsToPlayers();
                tile.rechargeConstructs();
                tile.scanForPedestals();
            }

            if (level.getGameTime() % 5L == 0L) {
                tile.chargeItems();
            }

            if (level.getGameTime() % 100L == 0L) {
                tile.chargeSigils();
            }
        }
    }

    private void addEffectsToPlayers() {
        double radius = 16.0;
        AABB axisalignedbb = new AABB(this.worldPosition).inflate(radius);
        List<Player> list = this.getLevel().getEntitiesOfClass(Player.class, axisalignedbb);
        Vec3 myPos = new Vec3(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5);

        for (Player playerentity : list) {
            double dist = myPos.distanceTo(playerentity.position());
            int magnitudeByDist = 4 - (int)Math.floor(dist / 4.0);
            playerentity.addEffect(new MobEffectInstance(EffectInit.MANA_REGEN.get(), 100, magnitudeByDist, true, false, true));
        }
    }

    private void rechargeConstructs() {
        double radius = 16.0;
        AABB axisalignedbb = new AABB(this.worldPosition).inflate(radius);
        List<Construct> list = this.getLevel().getEntitiesOfClass(Construct.class, axisalignedbb);
        Vec3 myPos = new Vec3(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5);

        for (Construct construct : list) {
            if (construct.getManaPct() != 1.0F && construct.getConstructData().isCapabilityEnabled(ConstructCapability.STORE_MANA)) {
                double dist = myPos.distanceTo(construct.position());
                int magnitudeByDist = 4 - (int)Math.floor(dist / 4.0);
                float manaRestored = magnitudeByDist * 50;
                construct.adjustMana(manaRestored);
                ServerMessageDispatcher.sendParticleSpawn(
                    this.worldPosition.getX() + 0.5,
                    this.worldPosition.getY() + 1,
                    this.worldPosition.getZ() + 0.5,
                    construct.getX(),
                    construct.getY() + 0.8,
                    construct.getZ(),
                    0,
                    32.0F,
                    this.level.dimension(),
                    ParticleInit.LIGHTNING_BOLT.get()
                );
            }
        }
    }

    private void chargeItems() {
        if (this.knownPedestalLocations.size() != 0) {
            int i = (int)Math.floor(Math.random() * this.knownPedestalLocations.size());
            BlockPos curPos = BlockPos.of(this.knownPedestalLocations.get(i));
            if (this.getLevel().isLoaded(curPos)) {
                BlockState state = this.getLevel().getBlockState(curPos);
                if (state.getBlock() instanceof PedestalBlock) {
                    if (this.level.getBlockEntity(curPos) instanceof PedestalTile pedestal) {
                        ItemStack stack = pedestal.getItem(0);
                        if (stack.getItem() instanceof ChargeableItem) {
                            ChargeableItem item = (ChargeableItem)stack.getItem();
                            if (item.getMana(stack) < item.getMaxMana()) {
                                item.refundMana(pedestal.getItem(0), 50.0F, null);
                                ServerMessageDispatcher.sendParticleSpawn(
                                    this.worldPosition.getX() + 0.5,
                                    this.worldPosition.getY() + 1,
                                    this.worldPosition.getZ() + 0.5,
                                    curPos.getX() + 0.5,
                                    curPos.getY() + 0.9,
                                    curPos.getZ() + 0.5,
                                    0,
                                    32.0F,
                                    this.level.dimension(),
                                    ParticleInit.LIGHTNING_BOLT.get()
                                );
                            }
                        }
                    }
                } else {
                    this.knownPedestalLocations.remove(i--);
                }
            }
        }
    }

    private void chargeSigils() {
        double radius = 16.0;
        AABB axisalignedbb = new AABB(this.worldPosition).inflate(radius);
        this.getLevel()
            .getEntities((EntityTypeTest<Entity, SpellSigil>)EntityInit.SPELL_RUNE.get(), axisalignedbb, e -> e instanceof SpellSigil && e.isPermanent())
            .forEach(sigil -> sigil.addCharge());
    }

    private void scanForPedestals() {
        for (int i = -8; i <= 8; i++) {
            for (int j = -8; j <= 8; j++) {
                for (int k = -8; k <= 8; k++) {
                    BlockPos curPos = this.worldPosition.offset(i, j, k);
                    if (this.getLevel().isLoaded(curPos)) {
                        BlockState state = this.getLevel().getBlockState(curPos);
                        if (state.getBlock() instanceof PedestalBlock) {
                            long posLong = curPos.asLong();
                            if (!this.knownPedestalLocations.contains(posLong)) {
                                this.knownPedestalLocations.add(posLong);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void registerControllers(ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.broken_mana_trapazahedron_armature.idle"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }
}
