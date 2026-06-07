# Hemomancy Wiki

This directory contains the GitHub Wiki pages for the Hemomancy mod.

## Wiki Structure

### Core Pages
- **Home.md** — Main landing page with overview and navigation
- **Public-Alpha-Readiness.md** - Alpha status, known limitations, and tester path
- **Getting-Started.md** — Installation and first steps guide
- **Harbinger-Path.md** — Complete guide to the blood magic progression path
- **Unstained-Path.md** — Complete guide to the purification path
- **Blood-Systems.md** — Deep dive into manipulations, tendencies, and mechanics
- **Lore-and-Story.md** — Narrative, factions, cosmology, and themes
- **Mod-Compatibility.md** — Integration with other mods and modpack tips
- **Developer-Reference.md** — Technical documentation and contribution guide

## Publishing to GitHub Wiki

To publish these pages to the GitHub wiki:

1. **Clone the wiki repository:**
   ```bash
   git clone https://github.com/VincentHuto/Hemomancy.wiki.git
   ```

2. **Copy the markdown files:**
   ```bash
   cp wiki/*.md Hemomancy.wiki/
   ```

3. **Commit and push:**
   ```bash
   cd Hemomancy.wiki
   git add .
   git commit -m "Add comprehensive wiki documentation"
   git push origin master
   ```

Alternatively, use GitHub's wiki web interface to create/edit pages by copying the content from these files.

## Maintaining the Wiki

### When to Update
- New features added to the mod
- Balance changes to blood costs, manipulations, or progression
- Lore expansions or clarifications
- Bug fixes that change behavior
- Version updates

### Style Guidelines
- Keep player-facing language clear and accessible
- Link between related pages using `[[Page-Name]]` syntax
- Preserve the moral ambiguity tone (neither path is objectively good/evil)
- Use the same vocabulary as in-game (don't "normalize" terminology)
- Include examples and tips for practical gameplay

### Authoritative Sources
When updating, always check:
- **docs/HEMOMANCY_REFERENCE.md** — Implementation details and status
- **docs/LORE_REFERENCE.md** — Canonical lore and worldbuilding
- Current code in `src/` — Source of truth when docs conflict

## Wiki Pages Overview

### Home (Home.md)
Entry point with:
- Mod overview and philosophy
- Quick navigation to all sections
- Key features highlight
- Installation requirements
- Credits and links

### Getting Started (Getting-Started.md)
For new players:
- Installation instructions
- The Mortal Display (activation point)
- Basic blood mechanics
- Path choice overview
- Essential early items
- UI and controls
- Troubleshooting

### Public Alpha Readiness (Public-Alpha-Readiness.md)
For testers and release prep:
- Implemented/Partial/Dormant/Planned status legend
- Current alpha-ready systems
- Known alpha limitations
- Compatibility status for JEI, MnA, and Curios
- Recommended tester path and report guidance

### Harbinger Path (Harbinger-Path.md)
Complete Harbinger guide:
- Seven degrees explained
- Cardinal Rites
- Bloodlines and recruitment
- Somatic Loom (Memory Weaving)
- Drudges, Morphlings, Puppeteering
- Equipment and armor
- The Qliphoth
- Fungal Whispers
- Two endings (Mycophant vs Vesper)

### Unstained Path (Unstained-Path.md)
Complete Unstained guide:
- Five purification stages
- Hemolytic Solution mechanics
- White Humor Purification rituals
- Copper equipment
- Guardian combat style
- Lethean water
- Our Lady of Still Waters
- Biological immortality
- Philosophical considerations

### Blood Systems (Blood-Systems.md)
Mechanics deep dive:
- Blood Volume management
- Eight tendencies explained
- Manipulation types and ranks
- Learning manipulations
- Tendency alignment
- Vascular system
- Skill tree
- Status effects
- Advanced techniques

### Lore and Story (Lore-and-Story.md)
Narrative and worldbuilding:
- Core premise and themes
- Timeline and setting
- The Fungal Entity (cosmic truth)
- The two factions
- Our Lady of Still Waters
- Key locations (Fungal Dimension, Qliphoth)
- Major characters and encounters
- Moral philosophy
- The two endings

### Mod Compatibility (Mod-Compatibility.md)
Integration guide:
- Required dependencies
- JEI recipe viewing
- Dormant Mana and Artifice integration target
- Dormant Curios integration target
- Modpack creation tips
- Config recommendations
- Planned future integrations

### Developer Reference (Developer-Reference.md)
Technical documentation:
- Links to full reference docs
- Technical overview
- Architecture and packages
- Key systems (capabilities, networking, manipulations)
- Building and testing
- Contributing guidelines
- API for other mod developers

## Future Expansion

Pages that could be added:
- **World-Content.md** — Biomes, structures, mobs in detail
- **Advanced-Mechanics.md** — Detailed guides for drudges, morphlings, puppeteering, blood routing
- **Factions.md** — Deep dive into Hematic Order and Unstained beliefs
- **Items-and-Blocks.md** — Complete item/block reference
- **Configuration.md** — Detailed config option documentation
- **FAQ.md** — Common questions and answers
- **Troubleshooting.md** — Extended problem-solving guide

## Links

- **GitHub Repository:** https://github.com/VincentHuto/Hemomancy
- **Issues:** https://github.com/VincentHuto/Hemomancy/issues
- **Discussions:** https://github.com/VincentHuto/Hemomancy/discussions
- **Full Reference Docs:** https://github.com/VincentHuto/Hemomancy/tree/main/docs

---

*"The wiki, like the Liber Sanguinum, is a living document. It grows, adapts, and remembers."*
