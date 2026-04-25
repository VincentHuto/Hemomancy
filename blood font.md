Implement the plan:

Blood Alphabet — Player Experience Enhancement Plan

The font infrastructure is complete: hemomancy:blood is a fully mapped ASCII→rune bitmap font, and Component.literal("text").withStyle(Style.EMPTY.withFont(Hemomancy.rloc("blood"))) renders it anywhere a Component renders in the GUI. EngramBlocks use the identical visual alphabet. Here's how to build meaningful discovery and intrigue around it:


1. Tiered Cipher State on BloodStructureHintItem

Add an NBT boolean Deciphered to BloodStructureHintItem. When false, BloodStructureHintScreen renders all text labels (structure name, "Hold in Hand:", "Activate Block:", instructions, "Materials:") in the blood font with a deep crimson color — the player sees recognizable runic shapes but can't read them. When true, it renders in normal text as it does now.

Unlock vector options (pick one or layer them):


    Degree gate: Automatically deciphered at Illuminatus (degree 3) or above — by this point the player is an initiated Harbinger who has learned the Order's written tongue.

    Spending blood: A right-click that costs, say, 3000 blood "meditates" on the parchment and deciphers it, burning the meaning into the player's mind. Single-use; flip the NBT.

    Cardinal Rite: A small utility rite ("Rite of the Sanguine Script") performed at the Sanctum Pedestal — costs one hint item + blood; the rite deciphers it and returns it decoded.


The partial decipher variant is more interesting: split the screen's info into tiers and reveal them one at a time. First right-click unlocks the structure name. Second unlocks blood cost. Third unlocks materials. Full degree unlocks instructions. This maps discovery arc to effort arc.


2. Blood Font in BloodStructureHintScreen — Flavor Text Layer

Even after deciphering, render a small strip of blood-font text as pure atmosphere — e.g., the structure's name rendered in runes as a "seal" or subtitle above the English name, always in the blood font at crimson. This keeps the visual identity of the alphabet visible even to players who've earned the translation.


3. Engram Blocks as In-World Cipher Puzzles

The EngramBlock already has CHARACTERINDEX (0–25 = a–z). This is the same alphabet. Worldgen structures (the Founding Sanctum, Harbinger safe-houses, the Sanguine Monolith area) could place sequences of engram blocks that spell real words or short phrases — a degree number in Latin, a location hint, a name. Players who've learned to read the font (via the deciphered hint item, or the Liber Sanguinum's font preview page) can decode them.

Concretely: create a utility method EngramBlock.spellWord(String word, BlockPos origin, Direction facing, Level level) that places a row of engram blocks at the given position with CHARACTERINDEX set to each letter's 0-based offset. Worldgen structure nbt files or JigsawPieces can call this during generation. Or hand-author NBT structure files with specific character indices.

The font preview page in the guide (HemoBloodStructureGuidePage) already shows the alphabet side-by-side in normal and blood font — this page becomes the player's "rosetta stone" reward, accessible once they reach Illuminatus.


4. Blood Font Tooltips as Lore Fragments

For items that should feel ancient or pre-translation, add a blood-font tooltip line rendered by overriding appendHoverText. A rune string that, when the player has learned to read it (degree check), resolves to a Latin phrase or a cryptic structural clue. Before that check it's just decorative glyphs that signal "there is meaning here if you know how to look."

Items that could carry this: the Blood Monolith focus item, Hematic Memory items, the hint item itself, the Engram Stamp.


5. HemoBloodStructureGuidePage as the Rosetta Stone

Uncomment and refine the current TODO block. Make the font preview page the reward for reaching a specific degree — it's the page in the Liber Sanguinum that officially teaches the player what each rune means. Add a caption line: "The Order commits its instructions to a written tongue unknown to the uninitiated." The visual layout (blood font line, then normal text line beneath) already demonstrates the mapping perfectly.


Implementation Order (suggested)


    Add Deciphered NBT + degree-check logic to BloodStructureHintItem.use() and BloodStructureHintScreen — this is the most impactful single change and the direct follow-through on the TODO.

    Refactor BloodStructureHintScreen.drawInfoPanel() to select blood-font vs. normal rendering based on deciphered state.

    Add the "always-visible" blood-font name strip as atmosphere.

    Wire the Liber Sanguinum rosetta-stone page to degree-gating.

    Optionally add engram-block word-spelling to one worldgen structure as an environmental puzzle.

