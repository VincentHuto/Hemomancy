package com.vincenthuto.hemomancy.common.rite.sigil;

import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IchorianSigilLoaderTest {

	@Test
	void parsesGroundConnectionsAndCompleteAwakenedForm() {
		IchorianSigilDefinition definition = parse("""
				{
				  "kind":"response", "tier":2, "color":"0x80CBC4",
				  "name":"Shunt", "purpose":"Redirects blood.", "stability":8,
				  "nodes":[[0,-1],[-1,0],[0,0],[1,0],[0,1]],
				  "connections":[[2,0],[2,1],[2,3],[2,4]],
				  "awakened_form":{
				    "forward":[0,0,-1],
				    "animation":{"style":"arterial_fork","pulse":1.2,"flex":0.9,"lag":0.2},
				    "nodes":[
				      {"source":0,"position":[0,0,-0.7],"role":"eye","radius":0.13},
				      {"source":1,"position":[-0.5,0,0.2],"role":"valve","radius":0.12},
				      {"source":2,"position":[0,0,0],"role":"organ","radius":0.18},
				      {"source":3,"position":[0.5,0,0.2],"role":"valve","radius":0.12},
				      {"source":4,"position":[0,0,0.7],"role":"limb_tip","radius":0.1}
				    ],
				    "vessels":[{"from":2,"to":1,"thickness":0.07}],
				    "membranes":[[0,1,2]]
				  }
				}""");

		assertEquals(new IchorianSigilDefinition.Connection(2, 0), definition.connections().getFirst());
		var anatomy = definition.awakenedForm().orElseThrow();
		assertEquals(IchorianSigilAnatomy.Style.ARTERIAL_FORK, anatomy.animation().style());
		assertEquals(IchorianSigilAnatomy.Role.EYE, anatomy.landmarks().getFirst().role());
		assertEquals(new IchorianSigilAnatomy.Vessel(2, 1, 0.07F), anatomy.vessels().getFirst());
		assertEquals(new IchorianSigilAnatomy.Membrane(0, 1, 2), anatomy.membranes().getFirst());
	}

	@Test
	void legacyJsonRetainsEmptyOptionalSections() {
		IchorianSigilDefinition definition = parse("""
				{
				  "kind":"support", "tier":1, "color":"0xE6A23C",
				  "name":"Legacy", "purpose":"Compatibility",
				  "nodes":[[-1,0],[0,-1],[1,0],[0,1]]
				}""");

		assertTrue(definition.connections().isEmpty());
		assertTrue(definition.awakenedForm().isEmpty());
	}

	@Test
	void malformedAwakenedFormFallsBackWithoutDroppingDefinition() {
		IchorianSigilDefinition definition = parse("""
				{
				  "kind":"support", "tier":1, "color":"0xE6A23C",
				  "name":"Broken", "purpose":"Fallback",
				  "nodes":[[-1,0],[1,0]],
				  "awakened_form":{
				    "forward":[0,0,-1],
				    "animation":{"style":"pendulous_ampulla","pulse":1,"flex":1,"lag":1},
				    "nodes":[
				      {"source":0,"position":[0,0,-1],"role":"eye","radius":0.1},
				      {"source":0,"position":[0,0,1],"role":"organ","radius":0.1}
				    ],
				    "vessels":[], "membranes":[]
				  }
				}""");

		assertEquals("Broken", definition.name());
		assertTrue(definition.awakenedForm().isEmpty());
	}

	private static IchorianSigilDefinition parse(String json) {
		return IchorianSigilLoader.parseDefinition(
				ResourceLocation.parse("hemomancy:test"),
				JsonParser.parseString(json).getAsJsonObject());
	}
}
