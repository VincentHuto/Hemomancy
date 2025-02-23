package com.vincenthuto.hemomancy.compat.mna.spell;

import java.util.ArrayList;
import java.util.List;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ComponentDodo extends SpellEffect {

	public static final ArrayList<SpellReagent> required_reagents = new ArrayList<SpellReagent>();

	static {
		required_reagents.add(new SpellReagent(null, new ItemStack(Items.FEATHER), false, false, true));
	}

	public ComponentDodo(ResourceLocation guiIcon) {
		super(guiIcon, new AttributeValuePair[] { new AttributeValuePair(Attribute.DURATION, 30, 10, 300, 10, 0.5f) });
	}

	@Override
	public int requiredXPForRote() {
		return 150;
	}

	@Override
	public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target,
			IModifiedSpellPart<SpellEffect> attributes, SpellContext context) {

		Mob spawn = (Mob) EntityType.CHICKEN.create(context.getLevel()); // double check that all the EntityTypes
																			// inherit from Mob;
		// these all do.
		if (spawn == null) {
			return ComponentApplicationResult.FAIL;
		}

		// move the spawn to the target's position
		spawn.setPos(target.getPosition().add(0, 1, 0));

		// make the spawn a summon
		// this will override the mob's AI to make it act like a summon
		ManaAndArtificeMod.getSummonHelper().makeSummon(spawn, source.getCaster(),
				(int) attributes.getValue(Attribute.DURATION) * 20); // convert the duration into seconds by multiplying
																		// by 20 (ticks/second)

		// spawn it in the world!
		context.getLevel().addFreshEntity(spawn);

		// Set it so all further components target the summon
		target.overrideSpellTarget(spawn);

		return ComponentApplicationResult.SUCCESS;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.WATER;
	}

	@Override
	public List<SpellReagent> getRequiredReagents(Player caster, InteractionHand hand) {
		return required_reagents;
	}

	@Override
	public float initialComplexity() {
		return 10;
	}

}
