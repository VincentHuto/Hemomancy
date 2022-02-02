package com.vincenthuto.hemomancy.entity.brain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.npc.VillagerProfession;

public class EntityBrainData {
	public static final Codec<EntityBrainData> CODEC = RecordCodecBuilder.create((p_35570_) -> {
		return p_35570_.group(Registry.VILLAGER_PROFESSION.byNameCodec().fieldOf("profession").orElseGet(() -> {
			return VillagerProfession.NONE;
		}).forGetter((p_150022_) -> {
			return p_150022_.profession;
		})).apply(p_35570_, EntityBrainData::new);
	});
	private final VillagerProfession profession;

	public EntityBrainData(VillagerProfession p_35558_) {

		this.profession = p_35558_;
	}

	public VillagerProfession getProfession() {
		return this.profession;
	}

	public EntityBrainData setProfession(VillagerProfession pProfession) {
		return new EntityBrainData(pProfession);
	}

}
