package com.hdarby.dicemaster.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterRaceTest {

    @Test
    fun `enum has exactly 69 entries`() {
        assertEquals(69, CharacterRace.entries.size)
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

    // ── Hierarchy / parent-child tests ───────────────────────────────────────

    @Test
    fun `standalone races have no parent`() {
        listOf(
            CharacterRace.HUMAN, CharacterRace.DRAGONBORN, CharacterRace.HALF_ELF,
            CharacterRace.HALF_ORC, CharacterRace.TIEFLING, CharacterRace.GOLIATH,
            CharacterRace.WARFORGED, CharacterRace.GENASI, CharacterRace.GITH
        ).forEach { race ->
            assertNull("Expected ${race.displayName} to have no parent", race.parent)
        }
    }

    @Test
    fun `elf subraces have ELF as parent`() {
        listOf(
            CharacterRace.HIGH_ELF, CharacterRace.WOOD_ELF, CharacterRace.DROW,
            CharacterRace.SEA_ELF, CharacterRace.ELADRIN, CharacterRace.SHADAR_KAI,
            CharacterRace.ASTRAL_ELF
        ).forEach { subrace ->
            assertEquals(
                "Expected ${subrace.displayName} parent to be Elf",
                CharacterRace.ELF, subrace.parent
            )
        }
    }

    @Test
    fun `dwarf subraces have DWARF as parent`() {
        listOf(CharacterRace.HILL_DWARF, CharacterRace.MOUNTAIN_DWARF, CharacterRace.DUERGAR)
            .forEach { subrace ->
                assertEquals(CharacterRace.DWARF, subrace.parent)
            }
    }

    @Test
    fun `gnome subraces have GNOME as parent`() {
        listOf(CharacterRace.FOREST_GNOME, CharacterRace.ROCK_GNOME, CharacterRace.DEEP_GNOME)
            .forEach { subrace ->
                assertEquals(CharacterRace.GNOME, subrace.parent)
            }
    }

    @Test
    fun `halfling subraces have HALFLING as parent`() {
        listOf(CharacterRace.LIGHTFOOT_HALFLING, CharacterRace.STOUT_HALFLING)
            .forEach { subrace ->
                assertEquals(CharacterRace.HALFLING, subrace.parent)
            }
    }

    @Test
    fun `genasi subraces have GENASI as parent`() {
        listOf(
            CharacterRace.AIR_GENASI, CharacterRace.EARTH_GENASI,
            CharacterRace.FIRE_GENASI, CharacterRace.WATER_GENASI
        ).forEach { subrace ->
            assertEquals(CharacterRace.GENASI, subrace.parent)
        }
    }

    @Test
    fun `gith subraces have GITH as parent`() {
        listOf(CharacterRace.GITHYANKI, CharacterRace.GITHZERAI).forEach { subrace ->
            assertEquals(CharacterRace.GITH, subrace.parent)
        }
    }

    @Test
    fun `topLevelRaces contains no subraces`() {
        CharacterRace.topLevelRaces.forEach { race ->
            assertNull(
                "Expected ${race.displayName} in topLevelRaces to have no parent",
                race.parent
            )
        }
    }

    @Test
    fun `subracesOf ELF returns all 7 elf varieties`() {
        val elfSubraces = CharacterRace.subracesOf(CharacterRace.ELF)
        assertEquals(7, elfSubraces.size)
        assertTrue(elfSubraces.contains(CharacterRace.HIGH_ELF))
        assertTrue(elfSubraces.contains(CharacterRace.WOOD_ELF))
        assertTrue(elfSubraces.contains(CharacterRace.DROW))
        assertTrue(elfSubraces.contains(CharacterRace.SEA_ELF))
        assertTrue(elfSubraces.contains(CharacterRace.ELADRIN))
        assertTrue(elfSubraces.contains(CharacterRace.SHADAR_KAI))
        assertTrue(elfSubraces.contains(CharacterRace.ASTRAL_ELF))
    }

    @Test
    fun `subracesOf HUMAN returns empty list`() {
        assertTrue(CharacterRace.subracesOf(CharacterRace.HUMAN).isEmpty())
    }

    @Test
    fun `findByDisplayName returns correct entry`() {
        assertEquals(CharacterRace.GOLIATH, CharacterRace.findByDisplayName("Goliath"))
        assertEquals(CharacterRace.HIGH_ELF, CharacterRace.findByDisplayName("High Elf"))
        assertNull(CharacterRace.findByDisplayName("Unknown Race"))
    }

    @Test
    fun `topLevelRaceFor resolves subrace to its parent`() {
        assertEquals(CharacterRace.ELF, CharacterRace.topLevelRaceFor("High Elf"))
        assertEquals(CharacterRace.DWARF, CharacterRace.topLevelRaceFor("Hill Dwarf"))
        assertEquals(CharacterRace.GENASI, CharacterRace.topLevelRaceFor("Fire Genasi"))
        assertEquals(CharacterRace.GITH, CharacterRace.topLevelRaceFor("Githyanki"))
    }

    @Test
    fun `topLevelRaceFor returns self for standalone race`() {
        assertEquals(CharacterRace.HUMAN, CharacterRace.topLevelRaceFor("Human"))
        assertEquals(CharacterRace.GOLIATH, CharacterRace.topLevelRaceFor("Goliath"))
    }

    @Test
    fun `topLevelRaceFor returns null for unknown or null input`() {
        assertNull(CharacterRace.topLevelRaceFor(null))
        assertNull(CharacterRace.topLevelRaceFor("Hobbit"))
    }

    @Test
    fun `HUMAN is the default new-character race`() {
        assertEquals("Human", CharacterRace.HUMAN.displayName)
    }
}
