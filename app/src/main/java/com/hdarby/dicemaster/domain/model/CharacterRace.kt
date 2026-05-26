package com.hdarby.dicemaster.domain.model

enum class CharacterRace(val displayName: String) {
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
    DROW("Drow"),
    HIGH_ELF("High Elf"),
    WOOD_ELF("Wood Elf"),
    HILL_DWARF("Hill Dwarf"),
    MOUNTAIN_DWARF("Mountain Dwarf"),
    LIGHTFOOT_HALFLING("Lightfoot Halfling"),
    STOUT_HALFLING("Stout Halfling"),
    FOREST_GNOME("Forest Gnome"),
    ROCK_GNOME("Rock Gnome"),

    // ── Elemental Evil Player's Companion ────────────────────────────────────
    AARAKOCRA("Aarakocra"),
    AIR_GENASI("Air Genasi"),
    DEEP_GNOME("Deep Gnome"),
    EARTH_GENASI("Earth Genasi"),
    FIRE_GENASI("Fire Genasi"),
    WATER_GENASI("Water Genasi"),

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
    DUERGAR("Duergar"),
    ELADRIN("Eladrin"),
    GITHYANKI("Githyanki"),
    GITHZERAI("Githzerai"),
    SEA_ELF("Sea Elf"),
    SHADAR_KAI("Shadar-kai"),

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
    ASTRAL_ELF("Astral Elf"),
    AUTOGNOME("Autognome"),
    GIFF("Giff"),
    HADOZEE("Hadozee"),
    PLASMOID("Plasmoid"),
    THRI_KREEN("Thri-kreen"),

    // ── Dragonlance: Shadow of the Dragon Queen ───────────────────────────────
    KENDER("Kender");
}

