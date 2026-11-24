# Hemomancy NPC dialogue concept

Open `index.html` in a browser with the rest of this folder intact. Use the Material picker to review the five sprite sets on the same scene. The Alchemist remains the sample speaker in each skin. Reload to reset the demonstration.

The current renderer puts the portrait and greeting above a scrollable content area. The hub starts with four category cards and adds a separate Important tab when applicable. Long greetings can spill into a separate reading view. Inquiries and trade have their own layouts. The current sprites use flat, themed nine-slices, colored category cards and shared icons.

The draft keeps the left-side placement and visible world. It replaces the hub grid with a stable portrait and destination rail. A parchment reading surface holds the greeting, assignment, inquiry answer or trade, with enough width for prose. Back and Leave stay below that surface. The compact layout folds the rail across the top. The Important shortcut sits apart from regular navigation, and unread lore stays with Conversation.

The texture kit contains 35 PNGs across Blood, Unstained, Fungal, Still Waters and Crimson Troupe. Each uses 64×64 repeatable cover and paper tiles, a 32×32 frame with 9-pixel borders, and 16×16 row, focus, disabled and slot sprites with 5-pixel borders. The prototype reuses the existing Alchemist portrait, crest, item art and glyphs. The world backdrop is a cropped local Minecraft capture.

Try First Draws and its supply receipt, the Living Blood lesson and its three follow-up questions, either inquiry item, and the Fresh draw trade. The demo uses current localized lines with illustrative inventory, stock and attention states. The greeting shortcut, locked quest preview and its placement in Important are concept choices. All simulated interactions are local to the preview.

For a future client pass, map the rail to the existing navigation states and keep the server’s current eligibility, item checks, trade refusals and dispatch. Use the native font, inventory slots and item tooltips. Review at multiple Minecraft GUI scales with long localized lines, the no-offers state and non-entity speakers.
