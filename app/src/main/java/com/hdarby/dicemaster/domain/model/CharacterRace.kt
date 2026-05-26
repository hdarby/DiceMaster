package com.hdarby.dicemaster.domain.model

enum class CharacterRace(val displayName: String, val parent: CharacterRace? = null) {
    // ── Core Races (Player's Handbook) ──────────────────────────────────────
    DRAGONBORN("Dragonborn"),
    DWARF("Dwarf"),
    ELF("Elf"),
    GNOME("Gnome"),
    HALF_ELF("Half-Elf"),
    HALF_ORC("Half-Orc"),
    HALFLING("Halfling"),
    HUMAN("Human"),
    TIEFLING("Tiefling"),

    // ── PHB Subraces ─────────────────────────────────────────────────────────
    DROW("Drow", ELF),
    HIGH_ELF("High Elf", ELF),
    WOOD_ELF("Wood Elf", ELF),
    HILL_DWARF("Hill Dwarf", DWARF),
    MOUNTAIN_DWARF("Mountain Dwarf", DWARF),
    LIGHTFOOT_HALFLING("Lightfoot Halfling", HALFLING),
    STOUT_HALFLING("Stout Halfling", HALFLING),
    FOREST_GNOME("Forest Gnome", GNOME),
    ROCK_GNOME("Rock Gnome", GNOME),

    // ── Elemental Evil Player's Companion ────────────────────────────────────
    AARAKOCRA("Aarakocra"),
    GENASI("Genasi"),
    AIR_GENASI("Air Genasi", GENASI),
    DEEP_GNOME("Deep Gnome", GNOME),
    EARTH_GENASI("Earth Genasi", GENASI),
    FIRE_GENASI("Fire Genasi", GENASI),
    WATER_GENASI("Water Genasi", GENASI),

    // ── Volo's Guide to Monsters ─────────────────────────────────────────────
    AASIMAR("Aasimar"),
    BUGBEAR("Bugbear"),
    FIRBOLG("Firbolg"),
    GOBLIN("Goblin"),
    GOLIATH("Goliath"),
    HOBGOBLIN("Hobgoblin"),
    KENKU("Kenku"),
    KOBOLD("Kobold"),
    LIZARDFOLK("Lizardfolk"),
    ORC("Orc"),
    TABAXI("Tabaxi"),
    TRITON("Triton"),
    YUAN_TI_PUREBLOOD("Yuan-ti Pureblood"),

    // ── Mordenkainen's Tome of Foes ──────────────────────────────────────────
    DUERGAR("Duergar", DWARF),
    ELADRIN("Eladrin", ELF),
    GITH("Gith"),
    GITHYANKI("Githyanki", GITH),
    GITHZERAI("Githzerai", GITH),
    SEA_ELF("Sea Elf", ELF),
    SHADAR_KAI("Shadar-kai", ELF),

    // ── Eberron: Rising from the Last War ────────────────────────────────────
    CHANGELING("Changeling"),
    KALASHTAR("Kalashtar"),
    SHIFTER("Shifter"),
    WARFORGED("Warforged"),

    // ── Guildmaster's Guide to Ravnica ───────────────────────────────────────
    CENTAUR("Centaur"),
    LOXODON("Loxodon"),
    MINOTAUR("Minotaur"),
    SIMIC_HYBRID("Simic Hybrid"),
    VEDALKEN("Vedalken"),

    // ── Mythic Odysseys of Theros ────────────────────────────────────────────
    LEONIN("Leonin"),
    SATYR("Satyr"),

    // ── Van Richten's Guide to Ravenloft ─────────────────────────────────────
    DHAMPIR("Dhampir"),
    HEXBLOOD("Hexblood"),
    REBORN("Reborn"),

    // ── The Wild Beyond the Witchlight ───────────────────────────────────────
    FAIRY("Fairy"),
    HARENGON("Harengon"),

    // ── Strixhaven: A Curriculum of Chaos ────────────────────────────────────
    OWLIN("Owlin"),

    // ── Spelljammer: Adventures in Space ─────────────────────────────────────
    ASTRAL_ELF("Astral Elf", ELF),
    AUTOGNOME("Autognome"),
    GIFF("Giff"),
    HADOZEE("Hadozee"),
    PLASMOID("Plasmoid"),
    THRI_KREEN("Thri-kreen"),

    // ── Dragonlance: Shadow of the Dragon Queen ───────────────────────────────
    KENDER("Kender");

    companion object {
        /** All races that appear at the top level of the race picker (no parent). */
        val topLevelRaces: List<CharacterRace> by lazy { entries.filter { it.parent == null } }

        /** All subraces that belong to the given parent race. */
        fun subracesOf(parent: CharacterRace): List<CharacterRace> =
            entries.filter { it.parent == parent }

        /** Find a race by its display name, or null if not found. */
        fun findByDisplayName(displayName: String): CharacterRace? =
            entries.find { it.displayName == displayName }

        /**
         * Given a stored race display-name string, return the top-level group race.
         * E.g. "High Elf" → ELF, "Human" → HUMAN, unknown → null.
         */
        fun topLevelRaceFor(raceDisplayName: String?): CharacterRace? {
            val race = findByDisplayName(raceDisplayName ?: return null) ?: return null
            return race.parent ?: race
        }
    }
}
