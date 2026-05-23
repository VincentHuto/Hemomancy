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
		this.profile = tag.contains(TAG_PROFILE, Tag.TAG_STRING)
				? Profile.byId(tag.getString(TAG_PROFILE))
				: Profile.BLOOD_TEMPLE;
		this.variantSeed = tag.contains(TAG_VARIANT_SEED, Tag.TAG_LONG)
				? tag.getLong(TAG_VARIANT_SEED)
				: worldPosition.asLong();
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
