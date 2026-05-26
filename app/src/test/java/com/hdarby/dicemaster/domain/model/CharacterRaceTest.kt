package com.hdarby.dicemaster.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterRaceTest {

    @Test
    fun `enum has exactly 67 entries`() {
        assertEquals(67, CharacterRace.entries.size)
    }

    @Test
    fun `every entry has a non-blank displayName`() {
        CharacterRace.entries.forEach { race ->
            assertTrue(
                "Expected non-blank displayName for $race",
                race.displayName.isNotBlank()
            )
        }
    }

    @Test
    fun `every displayName is unique`() {
        val displayNames = CharacterRace.entries.map { it.displayName }
        assertEquals(
            "Expected all displayNames to be unique",
            displayNames.size,
            displayNames.toSet().size
        )
    }

    @Test
    fun `PHB core races are present with correct display names`() {
        assertEquals("Dragonborn", CharacterRace.DRAGONBORN.displayName)
        assertEquals("Dwarf", CharacterRace.DWARF.displayName)
        assertEquals("Elf", CharacterRace.ELF.displayName)
        assertEquals("Gnome", CharacterRace.GNOME.displayName)
        assertEquals("Half-Elf", CharacterRace.HALF_ELF.displayName)
        assertEquals("Half-Orc", CharacterRace.HALF_ORC.displayName)
        assertEquals("Halfling", CharacterRace.HALFLING.displayName)
        assertEquals("Human", CharacterRace.HUMAN.displayName)
        assertEquals("Tiefling", CharacterRace.TIEFLING.displayName)
    }

    @Test
    fun `PHB subraces are present with correct display names`() {
        assertEquals("Drow", CharacterRace.DROW.displayName)
        assertEquals("High Elf", CharacterRace.HIGH_ELF.displayName)
        assertEquals("Wood Elf", CharacterRace.WOOD_ELF.displayName)
        assertEquals("Hill Dwarf", CharacterRace.HILL_DWARF.displayName)
        assertEquals("Mountain Dwarf", CharacterRace.MOUNTAIN_DWARF.displayName)
        assertEquals("Lightfoot Halfling", CharacterRace.LIGHTFOOT_HALFLING.displayName)
        assertEquals("Stout Halfling", CharacterRace.STOUT_HALFLING.displayName)
        assertEquals("Forest Gnome", CharacterRace.FOREST_GNOME.displayName)
        assertEquals("Rock Gnome", CharacterRace.ROCK_GNOME.displayName)
    }

    @Test
    fun `supplemental races are present with correct display names`() {
        assertEquals("Goliath", CharacterRace.GOLIATH.displayName)
        assertEquals("Aasimar", CharacterRace.AASIMAR.displayName)
        assertEquals("Warforged", CharacterRace.WARFORGED.displayName)
        assertEquals("Githyanki", CharacterRace.GITHYANKI.displayName)
        assertEquals("Githzerai", CharacterRace.GITHZERAI.displayName)
        assertEquals("Yuan-ti Pureblood", CharacterRace.YUAN_TI_PUREBLOOD.displayName)
        assertEquals("Shadar-kai", CharacterRace.SHADAR_KAI.displayName)
        assertEquals("Plasmoid", CharacterRace.PLASMOID.displayName)
        assertEquals("Astral Elf", CharacterRace.ASTRAL_ELF.displayName)
        assertEquals("Kender", CharacterRace.KENDER.displayName)
    }

    @Test
    fun `entries can be found by displayName`() {
        val found = CharacterRace.entries.find { it.displayName == "Goliath" }
        assertNotNull(found)
        assertEquals(CharacterRace.GOLIATH, found)
    }

    @Test
    fun `HUMAN is the default new-character race`() {
        // The dialog defaults to CharacterRace.HUMAN.displayName for new characters
        assertEquals("Human", CharacterRace.HUMAN.displayName)
    }
}


