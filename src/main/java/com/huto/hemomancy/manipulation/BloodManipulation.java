package com.huto.hemomancy.manipulation;

import java.util.Random;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.capabilities.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.capabilities.tendency.IBloodTendency;
import com.huto.hemomancy.capabilities.vascularsystem.EnumVeinSections;
import com.huto.hemomancy.entity.projectile.EntityBloodShot;
import com.huto.hemomancy.font.ModTextFormatting;
import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.data.GlowParticleData;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.server.ServerWorld;

public class BloodManipulation {
	String name;
	double cost;
	float alignLevel;
	EnumBloodTendency tend;
	EnumManipulationRank rank;
	EnumVeinSections section;

	public BloodManipulation(String name, double cost, float alignLevel, EnumManipulationRank rank,
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
			shot.shoot((double) vector3f.getX(), (double) vector3f.getY(), (double) vector3f.getZ(), 6.15F, 1.0f);
			world.addEntity(shot);
			
			/*
			 * EntityBloodShot shot1 = new EntityBloodShot(world, player);
			 * shot1.shoot((double) vector3f.getX(), (double) vector3f.getY()+0.025,
			 * (double) vector3f.getZ(), 6.15F, 1.0f); world.addEntity(shot1);
			 * 
			 * 
			 * EntityBloodShot shot2 = new EntityBloodShot(world, player);
			 * shot2.shoot((double) vector3f.getX(), (double) vector3f.getY()-0.025,
			 * (double) vector3f.getZ(), 6.15F, 1.0f); world.addEntity(shot2);
			 * 
			 * EntityBloodShot shot3 = new EntityBloodShot(world, player);
			 * shot3.shoot((double) vector3f.getX(), (double) vector3f.getY()+0.05, (double)
			 * vector3f.getZ(), 6.15F, 1.0f); world.addEntity(shot3);
			 * 
			 * 
			 * EntityBloodShot shot4 = new EntityBloodShot(world, player);
			 * shot4.shoot((double) vector3f.getX(), (double) vector3f.getY()-0.05, (double)
			 * vector3f.getZ(), 6.15F, 1.0f); world.addEntity(shot4);
			 */			
			
			
		}
		if (nameIn.equals(ManipulationInit.blood_rush.getName())) {
			player.addPotionEffect(new EffectInstance(PotionInit.blood_rush.get(), 250, 1));
		}
		if (nameIn.equals(ManipulationInit.aneurysm.getName())) {
			ServerWorld sWorld = (ServerWorld) world;
			BlockPos pos = player.getPosition();
			Random random = player.world.rand;
			for (int i = 0; i < 30; i++) {
				sWorld.spawnParticle(GlowParticleData.createData(new ParticleColor(255 * random.nextFloat(), 0, 0)),
						pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
						pos.getZ() + random.nextDouble(), 3, 0f, 0.2f, 0f, sWorld.rand.nextInt(3) * 0.015f);
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
		nbt.putFloat("level", alignLevel);
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

	public float getAlignLevel() {
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
