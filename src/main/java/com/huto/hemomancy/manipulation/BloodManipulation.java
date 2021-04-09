package com.huto.hemomancy.manipulation;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.huto.hemomancy.capa.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.tendency.IBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.entity.projectile.EntityBloodShot;
import com.huto.hemomancy.font.ModTextFormatting;
import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.particle.factory.GlowParticleFactory;
import com.huto.hemomancy.particle.util.ParticleColor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BloodManipulation {
	String name;
	double cost;
	double alignLevel;
	EnumBloodTendency tend;
	EnumManipulationRank rank;
	EnumVeinSections section;

	public BloodManipulation(String name, double cost, double alignLevel, EnumManipulationRank rank,
			EnumBloodTendency tendency, EnumVeinSections section) {
		this.name = name;
		this.cost = cost;
		this.alignLevel = alignLevel;
		this.tend = tendency;
		this.rank = rank;
		this.section = section;
	}

	public void performAction(PlayerEntity player, ServerWorld world, ItemStack heldItemMainhand, BlockPos position) {
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
				.orElseThrow(NullPointerException::new);
		if (!player.world.isRemote) {
			if (volume.getBloodVolume() > cost && tendency.getAlignmentByTendency(tend) >= alignLevel) {
				volume.subtractBloodVolume((float) cost);
				getActionByName(name, player, world, heldItemMainhand, position);
			}
		}
	}

	public void getActionByName(String nameIn, PlayerEntity player, ServerWorld world, ItemStack heldItemMainhand,
			BlockPos position) {

		if (nameIn.equals(ManipulationInit.blood_shot.getName())) {
			Vector3d vector3d1 = player.getUpVector(1.0F);
			Quaternion quaternion = new Quaternion(new Vector3f(vector3d1), 0.0f, true);
			Vector3d vector3d = player.getLook(1.0F);
			Vector3f vector3f = new Vector3f(vector3d);
			vector3f.transform(quaternion);
			EntityBloodShot shot = new EntityBloodShot(world, player);
			shot.shoot((double) vector3f.getX(), (double) vector3f.getY(), (double) vector3f.getZ(), 4.5F, 1.0f);
			world.addEntity(shot);
		}
		if (nameIn.equals(ManipulationInit.blood_rush.getName())) {
			player.addPotionEffect(new EffectInstance(PotionInit.blood_rush.get(), 250, 1));
		}
		if (nameIn.equals(ManipulationInit.aneurysm.getName())) {
			ServerWorld sWorld = (ServerWorld) world;
			BlockPos pos = player.getPosition();
			Random random = player.world.rand;
			for (int i = 0; i < 30; i++) {
				sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(255 * random.nextFloat(), 0, 0)),
						pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
						pos.getZ() + random.nextDouble(), 3, 0f, 0.2f, 0f, sWorld.rand.nextInt(3) * 0.015f);
			}
		}
		if (nameIn.equals(ManipulationInit.ferric_transmutation.getName())) {
			ServerWorld sWorld = (ServerWorld) world;
			BlockPos pos = player.getPosition();
			Random random = player.world.rand;
			if (heldItemMainhand.getItem() == Items.GOLD_INGOT) {
				heldItemMainhand.shrink(1);
				ItemEntity iron = new ItemEntity(sWorld, pos.getX(), pos.getY(), pos.getZ(),
						new ItemStack(Items.IRON_INGOT));
				sWorld.addEntity(iron);
			}
			for (int i = 0; i < 30; i++) {
				sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(255 * random.nextFloat(), 0, 0)),
						pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
						pos.getZ() + random.nextDouble(), 3, 0f, 0.2f, 0f, sWorld.rand.nextInt(3) * 0.015f);
			}
		}
		if (nameIn.equals(ManipulationInit.activation_potential.getName())) {
			List<MobEntity> targets = player.world
					.getEntitiesWithinAABB(MobEntity.class, player.getBoundingBox().grow(5.0)).stream()
					.filter(e -> e.canEntityBeSeen((Entity) player)).collect(Collectors.toList());
			if (targets.size() > 0) {
				for (int i = 0; i < targets.size(); ++i) {
					MobEntity target = targets.get(i);
					Vector3d translation = new Vector3d(0.0, 1, 0);
					Vector3d speedVec = new Vector3d(target.getPosition().getX(),
							(float) target.getPosition().getY() + target.getHeight() / 2.0f,
							target.getPosition().getZ());
					PacketHandler.sendLightningSpawn(player.getPositionVec().add(translation), speedVec, 64.0f,
							(RegistryKey<World>) player.world.getDimensionKey(), ParticleColor.RED, 2, 10, 9, 0.2f);
					target.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) player), 5.0f);
				}
			}
		}
	}

	/*
	 * Reads a NBT tag and converts it to a manipulation
	 */
	public static BloodManipulation deserialize(CompoundNBT nbt) {
		if (nbt != null && !nbt.isEmpty()) {
			if (nbt.contains("name") && nbt.contains("cost") && nbt.contains("level") && nbt.contains("tendency")
					&& nbt.contains("rank") && nbt.contains("section")) {

				BloodManipulation manip = new BloodManipulation(nbt.getString("name"), nbt.getDouble("cost"),
						nbt.getFloat("level"), EnumManipulationRank.valueOf(nbt.getString("rank")),
						EnumBloodTendency.valueOf(nbt.getString("tendency")),
						EnumVeinSections.valueOf(nbt.getString("section")));

				return manip;
			}
		}
		return null;
	}

	/*
	 * Writes a NBT tag from this manipulation
	 */
	public CompoundNBT serialize() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("name", name);
		nbt.putDouble("cost", cost);
		nbt.putDouble("level", alignLevel);
		nbt.putString("rank", rank.name());
		nbt.putString("tendency", tend.name());
		nbt.putString("section", section.name());
		return nbt;
	}

	public String getName() {
		return name;
	}

	public String getProperName() {
		return ModTextFormatting.convertInitToLang(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getAlignLevel() {
		return alignLevel;
	}

	public void setAlignLevel(float alignLevel) {
		this.alignLevel = alignLevel;
	}

	public EnumBloodTendency getTend() {
		return tend;
	}

	public void setTend(EnumBloodTendency tend) {
		this.tend = tend;
	}

	public EnumManipulationRank getRank() {
		return rank;
	}

	public void setRank(EnumManipulationRank rank) {
		this.rank = rank;
	}

	public EnumVeinSections getSection() {
		return section;
	}

	public void setSection(EnumVeinSections section) {
		this.section = section;
	}

}
