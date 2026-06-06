# Developer Reference

This page provides links and guidance for developers, contributors, and those who want to understand Hemomancy's technical implementation.

---

## Documentation

### Primary References

**[HEMOMANCY_REFERENCE.md](https://github.com/VincentHuto/Hemomancy/blob/main/docs/HEMOMANCY_REFERENCE.md)**
- **Complete technical reference** (~390KB)
- Every system, item, block, entity, manipulation documented
- Implementation status for all features
- Code structure and architecture
- Registry contents
- Packet definitions
- Capability system details
- **This is the source of truth for implementation**

**[LORE_REFERENCE.md](https://github.com/VincentHuto/Hemomancy/blob/main/docs/LORE_REFERENCE.md)**
- **Complete lore documentation** (~74KB)
- World history, cosmology, factions
- Character backgrounds and motivations
- Narrative themes and tone guidelines
- **This is the source of truth for worldbuilding**

**[MNA_COMPATIBILITY_BRAINSTORM.md](https://github.com/VincentHuto/Hemomancy/blob/main/docs/MNA_COMPATIBILITY_BRAINSTORM.md)**
- Mana and Artifice integration design
- Each feature includes "MnA Justification"
- Planned and implemented status
- Design philosophy for cross-mod features

### Additional Documentation

**[DIALOGUE_ITEM_QUERY_GUIDE.md](https://github.com/VincentHuto/Hemomancy/blob/main/docs/DIALOGUE_ITEM_QUERY_GUIDE.md)**
- Guide for Item Inquiry dialogue system
- How NPCs respond to shown items
- Dialogue tree structure
- JSON format and examples

**Other Docs:**
- `docs/alpharoadmap.md` — Roadmap and planning
- `docs/fungalscar.md` — Scar system design notes
- `docs/hybrid_progression_matrix.md` — Progression balancing

---

## Technical Overview

### Mod Information
- **Mod ID:** `hemomancy`
- **Package:** `com.vincenthuto.hemomancy`
- **Version:** `6.0.1-neoforge.1.21.1.0`
- **Minecraft:** `1.21.1`
- **NeoForge:** `21.1.219` (21.1.x range)
- **Java:** `21`

### Architecture

**Entrypoint:** `src/main/java/com/vincenthuto/hemomancy/Hemomancy.java`
- Registers all DeferredRegisters
- Sets up configs
- Handles optional mod integration (MnA, Curios)
- Wires capabilities and packets
- Populates creative tabs

**Main Packages:**
```
com.vincenthuto.hemomancy/
├── client/              # Client-only rendering, screens, particles
├── common/
│   ├── init/            # Registry classes (*Init.java)
│   ├── block/           # Block classes
│   ├── tile/            # BlockEntity classes
│   ├── item/            # Item classes
│   ├── entity/          # Entity classes (mobs, projectiles)
│   ├── capability/      # Player capability interfaces/implementations
│   ├── manipulation/    # Blood manipulation implementations
│   ├── rite/            # Cardinal rite logic
│   ├── recipe/          # Custom recipe types
│   ├── event/           # Event handlers
│   ├── network/         # Packet system
│   ├── menu/            # Container menus
│   └── worldgen/        # Features, biomes, structures
├── compat/
│   ├── mna/             # Mana and Artifice integration
│   ├── curios/          # Curios integration
│   └── jei/             # JEI recipe categories
├── config/              # Config classes
└── mixin/               # Mixins
```

**Resources:**
```
src/main/resources/
├── assets/hemomancy/
│   ├── textures/        # All textures
│   ├── models/          # Item/block models
│   ├── sounds/          # Sound events
│   └── lang/            # Translations
├── data/hemomancy/
│   ├── recipe/          # Recipes (singular in this branch)
│   ├── loot_table/      # Loot tables (singular)
│   ├── tags/            # Tags
│   ├── structure/       # NBT structures
│   └── dialogue_inquiry/ # Item inquiry dialogues
└── META-INF/
    ├── accesstransformer.cfg
    └── neoforge.mods.toml
```

---

## Key Systems

### Capability System (NeoForge 1.21 Attachments)

Hemomancy uses NeoForge's attachment system for player state:

**Definitions:** `HemoAttachmentTypes` + `HemoCapabilityKeys`
**Registration:** `HemoCapabilityRegistrar`
**Access:** `HemoCapabilityAccess` utility methods

**Core Capabilities:**
- `IBloodVolume` — Blood amount and regeneration
- `IBloodTendency` — Eight tendency alignment levels
- `IVascularSystem` — Vein sections and health
- `IKnownManipulations` — Learned manipulations
- `IInitiatoryDegree` — Harbinger progression
- `IUnstainedProgress` — Unstained purification state
- `IVisceralOrgans` — Organ extraction/effects
- `IBloodlineData` — Bloodline membership
- And more...

**Old Pattern (Don't Use):**
- Don't use old `ICapabilityProvider` patterns
- Don't use legacy `AttachCapabilitiesEvent`
- Use attachments via `getData()`/`setData()`

### Networking (Payload-Based)

NeoForge 1.21 uses `CustomPacketPayload` system:

**Location:** `common/network/PacketHandler.java`

**Pattern:**
```java
public record MyPacket(int data) implements CustomPacketPayload {
    public static final Type<MyPacket> TYPE = new Type<>(
        Hemomancy.rloc("my_packet")
    );

    public static final StreamCodec<ByteBuf, MyPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, MyPacket::data,
            MyPacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
```

**Registration:**
- `playToClient(...)` for client-bound
- `playToServer(...)` for server-bound
- ~60 packets across 8 channels currently

**Don't Use:**
- Old `SimpleChannel` patterns
- Legacy message handling

### Blood Manipulation System

**Registry:** `ManipulationInit` (DeferredRegister)
**Implementation:** `common/manipulation/<tendency>/` packages
**Base Class:** `BloodManipulation`

**Each Manipulation Defines:**
- Cost (blood amount)
- Align level (tendency requirement)
- XP cost
- Type (QUICK/CHARGED/PASSIVE/CONTINUOUS)
- Rank (HUMILIS/MEDIOCRITAS/SUMMA/PERFECTUS)
- Tendency
- Vein section
- Cooldown
- Action lambda (what it does)
- Optional: Drudge action

**Tendencies (Internal):**
- `ANIMUS`, `FLAMMEUS`, `DUCTILIS`, `LUX`
- `MORTEM`, `CONGEATIO`, `FERRIC`, `TENEBRIS`

**Note:** Enzyme items use different vocabulary (Vivacious, Fervent, etc.) — don't "normalize" them.

### Recipe Types

Custom recipe types in `common/recipe/`:
- `ScarRecipeType` — Chisel Station scar carving
- `DistillationRecipeType` — Distillery recipes
- `RecallerRecipeType` — Visceral Recaller binding
- `IncubatorRecipeType` — Morphling Incubator mutations
- `BloodStructureRecipeType` — Multi-block structures
- `CardinalRiteRecipeType` — Altar rituals

All use NeoForge 1.21 recipe system with JSON datapacks.

### Worldgen (TerraBlender)

**Biomes:**
- Overworld: Sporecrown Thicket, Hyphal Spires, Drifting Mycelium
- Nether: Fungal Gardens, Fungal Isles

**Structures:**
- Harbinger Outposts (overworld)
- Mausoleums (underground)
- Trial Chambers (WIP placement)

**Features:**
- Erythrocoral reefs (ocean depth)
- Bloodwort plants
- Infected caps and fungal blocks
- Custom trees and vegetation

**Registration:** `BiomeInit`, `PlacedFeatureInit`, `ConfiguredFeatureInit`

---

## Building and Testing

### Build Commands

From project root (Windows PowerShell):
```powershell
./gradlew.bat build           # Build mod JAR
./gradlew.bat runClient       # Test in client
./gradlew.bat runServer       # Test dedicated server
./gradlew.bat runData         # Run data generators
```

Linux/Mac:
```bash
./gradlew build
./gradlew runClient
./gradlew runServer
./gradlew runData
```

### Build Output
- **Main JAR:** `build/libs/hemomancy-6.0.1-neoforge.1.21.1.0.jar`
- **Generated Resources:** `src/generated/resources/`

### Data Generation

**Location:** `src/main/java/com/vincenthuto/hemomancy/datagen/DataGeneration.java`

**Currently Enabled:**
- Blockstate provider
- Item model provider
- Language provider

**Currently Disabled (Intentional):**
- Server recipe provider (recipes hand-authored)
- Server tag provider (tags hand-authored)
- Entity loot provider (loot hand-authored)

**Why Some Are Disabled:**
The generated outputs have been hand-tuned and committed to `src/main/resources/`. Re-enabling providers would overwrite customization. If re-enabling, port JSON values into provider code first.

### Testing

**Test Location:** `src/test/java/com/vincenthuto/hemomancy/`

**Coverage:**
- Focused tests for key systems
- Validate before major changes
- Run with: `./gradlew test`

---

## Contributing

### Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork:** `git clone https://github.com/YourUsername/Hemomancy.git`
3. **Set up HutosLib:**
   - If you have HutosLib as sibling directory, it includes automatically
   - Otherwise, resolves from Maven
4. **Import in IDE** (IntelliJ IDEA or Eclipse with NeoGradle plugin)
5. **Run `./gradlew build`** to verify setup

### Code Style

- **Follow existing patterns** in the codebase
- **NeoForge 1.21 APIs only** — no legacy Forge imports
- **Registry objects** use `DeferredHolder` and `snake_case` IDs
- **Java classes** use `PascalCase`
- **Packages** organized by system (manipulation, block, entity, etc.)

### Lore and Tone

When adding content:
- **Read LORE_REFERENCE.md** first
- **Preserve moral ambiguity** — no simple good/evil
- **Harbinger content** uses Latinate/ecclesiastical language
- **Unstained content** uses sacramental/Anglo-Saxon language
- **Internal tendency names** stay as-is (don't change ANIMUS to VIVACIOUS)

### Pull Request Process

1. **Create a feature branch:** `git checkout -b feature/your-feature-name`
2. **Make changes** following style guidelines
3. **Test thoroughly** (build, runClient, verify changes)
4. **Update documentation** if adding/changing systems
5. **Commit with clear messages:** `git commit -m "Add: new manipulation for Animus tendency"`
6. **Push to your fork:** `git push origin feature/your-feature-name`
7. **Open Pull Request** on main repository
8. **Respond to review feedback**

### What to Contribute

**Welcome:**
- Bug fixes
- New manipulations (with design doc)
- Texture improvements
- Lore expansion (consistent with existing)
- Performance optimizations
- Documentation improvements
- Translation (other languages)

**Discuss First:**
- Major systems (open issue/discussion)
- Balance changes (justify reasoning)
- API changes (affects other mods)
- Lore contradictions (align with reference)

**Not Accepted:**
- Changes that simplify moral complexity
- Features that make paths not mutually exclusive
- Content that breaks tone (e.g., "evil Harbingers" framing)
- Performance-degrading changes without justification

---

## API and Integration

### For Other Mod Developers

**Current State:**
- No formal API yet (planned)
- Integration via capabilities/attachments
- Access player blood state through `HemoCapabilityAccess`

**Example Integration:**
```java
// Check if player has active blood magic
IBloodVolume volume = player.getData(HemoAttachmentTypes.BLOOD_VOLUME);
if (volume != null && volume.isActive()) {
    float currentBlood = volume.getBloodLevel();
    // Your integration logic
}
```

**Future API:**
A formal API is planned for stable cross-mod integration. Until then, use capabilities carefully.

**Documentation:**
See HEMOMANCY_REFERENCE.md sections on:
- Capability system (§2)
- Networking (§4)
- Blood Manipulations (§8)
- Events (§various)

### Contact for Integration

- **GitHub Issues:** [Report bugs or request features](https://github.com/VincentHuto/Hemomancy/issues)
- **GitHub Discussions:** [Ask questions or propose ideas](https://github.com/VincentHuto/Hemomancy/discussions)

---

## Licensing

**License:** All Rights Reserved © VincentHuto

**What this means:**
- Hemomancy is proprietary
- Permission required for redistribution
- Modpacks: Generally allowed (confirm with author)
- Derivative works: Requires permission
- Read the full license in the repository

**Dependencies:**
- HutosLib: Check its license
- GeckoLib: MIT License
- TerraBlender: LGPLv2.1
- Minecraft/NeoForge: Mojang EULA / NeoForge license

---

## Additional Resources

### Community
- **GitHub Repository:** [VincentHuto/Hemomancy](https://github.com/VincentHuto/Hemomancy)
- **Issues Tracker:** [Report Bugs](https://github.com/VincentHuto/Hemomancy/issues)
- **Discussions:** [Ask Questions](https://github.com/VincentHuto/Hemomancy/discussions)

### Tools

**Skill Tree Editor**
Location: `tools/skill_tree_editor/`
- Edit manipulation tree via `manipulations.html`
- Reads/writes `ManipulationTreeInit.java`
- Visual tree editor for skill connections

### Useful Links
- [NeoForge Documentation](https://docs.neoforged.net/)
- [Minecraft Wiki (Modding)](https://minecraft.fandom.com/wiki/Tutorials/Creating_mods)
- [Parchment Mappings](https://parchmentmc.org/)
- [GeckoLib Docs](https://github.com/bernie-g/geckolib/wiki)

---

## Troubleshooting Development Issues

### Build Failures

**Issue:** "HutosLib not found"
- Ensure HutosLib is in sibling directory `../HutosLib` OR
- Check Maven resolution in logs

**Issue:** "Invalid NeoForge version"
- Verify `gradle.properties` has correct versions
- Run `./gradlew --refresh-dependencies`

**Issue:** "Access transformer errors"
- Check `META-INF/accesstransformer.cfg`
- Verify targets exist in Minecraft/NeoForge

### Runtime Issues

**Issue:** "Mixin application failed"
- Check `hemomancy.mixins.json` and `hemomancy.mna.mixins.json`
- Verify mixin targets exist
- Check logs for specific mixin errors

**Issue:** "Capability not found"
- Ensure `HemoCapabilityRegistrar` fired
- Check capability is registered to correct event
- Verify attachment types are initialized

**Issue:** "Packet not registered"
- Check `PacketHandler.registerPackets()`
- Verify payload TYPE and STREAM_CODEC
- Ensure registration happens in mod constructor

### Data Generation Issues

**Issue:** "Provider overwriting hand-authored JSON"
- Check `DataGeneration.java` for enabled providers
- Disable providers for hand-authored content
- Port JSON to provider if you want generation

---

## Version History

**6.0.1-neoforge.1.21.1.0** (Current)
- NeoForge 1.21.1 port
- Attachment-based capabilities
- Payload-based networking
- Flexible Founding Sanctums
- Somatic Loom memory-weaving rewrite
- Harbinger outpost NPC recruitment
- Annetta Knowles encounter partial implementation
- Vesper/Mycophant entity wiring
- Blood Moon sync
- Various system polish

**5.x** (Legacy Forge 1.20.1)
- Final Forge builds
- Core systems established
- See older documentation for details

---

## Next Steps

- **[[Home]]** — Return to wiki home
- **[[Getting Started]]** — Install and play the mod
- **[[Mod Compatibility]]** — Integration with other mods
- **[HEMOMANCY_REFERENCE.md](https://github.com/VincentHuto/Hemomancy/blob/main/docs/HEMOMANCY_REFERENCE.md)** — Full technical documentation

---

*"The code, like the blood, remembers. Study it well, and you'll find the patterns that connect biology to mechanics, lore to implementation, cosmic horror to Minecraft blocks."*

— Developer philosophy
