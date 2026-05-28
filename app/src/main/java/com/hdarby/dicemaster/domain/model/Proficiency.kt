package com.hdarby.dicemaster.domain.model

enum class ProficiencyCategory(val displayName: String) {
    SKILLS("Skills"),
    SAVING_THROWS("Saving Throws"),
    ARMOR("Armor"),
    WEAPONS("Weapons"),
    TOOLS("Tools"),
    LANGUAGES("Languages")
}

enum class Proficiency(val displayName: String, val category: ProficiencyCategory) {
    // Skills
    ACROBATICS("Acrobatics", ProficiencyCategory.SKILLS),
    ANIMAL_HANDLING("Animal Handling", ProficiencyCategory.SKILLS),
    ARCANA("Arcana", ProficiencyCategory.SKILLS),
    ATHLETICS("Athletics", ProficiencyCategory.SKILLS),
    DECEPTION("Deception", ProficiencyCategory.SKILLS),
    HISTORY("History", ProficiencyCategory.SKILLS),
    INSIGHT("Insight", ProficiencyCategory.SKILLS),
    INTIMIDATION("Intimidation", ProficiencyCategory.SKILLS),
    INVESTIGATION("Investigation", ProficiencyCategory.SKILLS),
    MEDICINE("Medicine", ProficiencyCategory.SKILLS),
    NATURE("Nature", ProficiencyCategory.SKILLS),
    PERCEPTION("Perception", ProficiencyCategory.SKILLS),
    PERFORMANCE("Performance", ProficiencyCategory.SKILLS),
    PERSUASION("Persuasion", ProficiencyCategory.SKILLS),
    RELIGION("Religion", ProficiencyCategory.SKILLS),
    SLEIGHT_OF_HAND("Sleight of Hand", ProficiencyCategory.SKILLS),
    STEALTH("Stealth", ProficiencyCategory.SKILLS),
    SURVIVAL("Survival", ProficiencyCategory.SKILLS),

    // Saving Throws
    SAVING_THROW_STRENGTH("Strength", ProficiencyCategory.SAVING_THROWS),
    SAVING_THROW_DEXTERITY("Dexterity", ProficiencyCategory.SAVING_THROWS),
    SAVING_THROW_CONSTITUTION("Constitution", ProficiencyCategory.SAVING_THROWS),
    SAVING_THROW_INTELLIGENCE("Intelligence", ProficiencyCategory.SAVING_THROWS),
    SAVING_THROW_WISDOM("Wisdom", ProficiencyCategory.SAVING_THROWS),
    SAVING_THROW_CHARISMA("Charisma", ProficiencyCategory.SAVING_THROWS),

    // Armor
    LIGHT_ARMOR("Light Armor", ProficiencyCategory.ARMOR),
    MEDIUM_ARMOR("Medium Armor", ProficiencyCategory.ARMOR),
    HEAVY_ARMOR("Heavy Armor", ProficiencyCategory.ARMOR),
    SHIELDS("Shields", ProficiencyCategory.ARMOR),

    // Weapons
    SIMPLE_WEAPONS("Simple Weapons", ProficiencyCategory.WEAPONS),
    MARTIAL_WEAPONS("Martial Weapons", ProficiencyCategory.WEAPONS),

    // Tools
    ALCHEMISTS_SUPPLIES("Alchemist's Supplies", ProficiencyCategory.TOOLS),
    BREWERS_SUPPLIES("Brewer's Supplies", ProficiencyCategory.TOOLS),
    CALLIGRAPHERS_SUPPLIES("Calligrapher's Supplies", ProficiencyCategory.TOOLS),
    CARPENTERS_TOOLS("Carpenter's Tools", ProficiencyCategory.TOOLS),
    CARTOGRAPHERS_TOOLS("Cartographer's Tools", ProficiencyCategory.TOOLS),
    COBBLERS_TOOLS("Cobbler's Tools", ProficiencyCategory.TOOLS),
    COOKS_UTENSILS("Cook's Utensils", ProficiencyCategory.TOOLS),
    DISGUISE_KIT("Disguise Kit", ProficiencyCategory.TOOLS),
    FORGERY_KIT("Forgery Kit", ProficiencyCategory.TOOLS),
    GAMING_SET("Gaming Set", ProficiencyCategory.TOOLS),
    GLASSBLOWERS_TOOLS("Glassblower's Tools", ProficiencyCategory.TOOLS),
    HERBALISM_KIT("Herbalism Kit", ProficiencyCategory.TOOLS),
    JEWELERS_TOOLS("Jeweler's Tools", ProficiencyCategory.TOOLS),
    LAND_VEHICLES("Land Vehicles", ProficiencyCategory.TOOLS),
    LEATHERWORKERS_TOOLS("Leatherworker's Tools", ProficiencyCategory.TOOLS),
    MASONS_TOOLS("Mason's Tools", ProficiencyCategory.TOOLS),
    MUSICAL_INSTRUMENT("Musical Instrument", ProficiencyCategory.TOOLS),
    NAVIGATORS_TOOLS("Navigator's Tools", ProficiencyCategory.TOOLS),
    PAINTERS_SUPPLIES("Painter's Supplies", ProficiencyCategory.TOOLS),
    POISONERS_KIT("Poisoner's Kit", ProficiencyCategory.TOOLS),
    POTTERS_TOOLS("Potter's Tools", ProficiencyCategory.TOOLS),
    SMITHS_TOOLS("Smith's Tools", ProficiencyCategory.TOOLS),
    THIEVES_TOOLS("Thieves' Tools", ProficiencyCategory.TOOLS),
    TINKERS_TOOLS("Tinker's Tools", ProficiencyCategory.TOOLS),
    WATER_VEHICLES("Water Vehicles", ProficiencyCategory.TOOLS),
    WEAVERS_TOOLS("Weaver's Tools", ProficiencyCategory.TOOLS),
    WOODCARVERS_TOOLS("Woodcarver's Tools", ProficiencyCategory.TOOLS),

    // Languages
    ABYSSAL("Abyssal", ProficiencyCategory.LANGUAGES),
    CELESTIAL("Celestial", ProficiencyCategory.LANGUAGES),
    COMMON("Common", ProficiencyCategory.LANGUAGES),
    DEEP_SPEECH("Deep Speech", ProficiencyCategory.LANGUAGES),
    DRACONIC("Draconic", ProficiencyCategory.LANGUAGES),
    DWARVISH("Dwarvish", ProficiencyCategory.LANGUAGES),
    ELVISH("Elvish", ProficiencyCategory.LANGUAGES),
    GIANT("Giant", ProficiencyCategory.LANGUAGES),
    GNOMISH("Gnomish", ProficiencyCategory.LANGUAGES),
    GOBLIN("Goblin", ProficiencyCategory.LANGUAGES),
    HALFLING("Halfling", ProficiencyCategory.LANGUAGES),
    INFERNAL("Infernal", ProficiencyCategory.LANGUAGES),
    ORC("Orc", ProficiencyCategory.LANGUAGES),
    PRIMORDIAL("Primordial", ProficiencyCategory.LANGUAGES),
    SYLVAN("Sylvan", ProficiencyCategory.LANGUAGES),
    UNDERCOMMON("Undercommon", ProficiencyCategory.LANGUAGES);

    companion object {
        fun byCategory(category: ProficiencyCategory): List<Proficiency> =
            entries.filter { it.category == category }
    }
}

private const val PROFICIENCY_DELIMITER = ","

fun Set<Proficiency>.toCommaSeparated(): String =
    joinToString(PROFICIENCY_DELIMITER) { it.name }

fun String.toProficiencySet(): Set<Proficiency> =
    if (isBlank()) emptySet()
    else split(PROFICIENCY_DELIMITER)
        .mapNotNull { runCatching { Proficiency.valueOf(it.trim()) }.getOrNull() }
        .toSet()
