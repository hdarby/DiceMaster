package com.hdarby.dicemaster.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProficiencyTest {

    // ── toCommaSeparated ──────────────────────────────────────────────────────

    @Test
    fun `toCommaSeparated - empty set returns empty string`() {
        assertEquals("", emptySet<Proficiency>().toCommaSeparated())
    }

    @Test
    fun `toCommaSeparated - single proficiency returns its enum name`() {
        val result = setOf(Proficiency.ACROBATICS).toCommaSeparated()
        assertEquals("ACROBATICS", result)
    }

    @Test
    fun `toCommaSeparated - multiple proficiencies joins with comma delimiter`() {
        val input = linkedSetOf(Proficiency.STEALTH, Proficiency.PERCEPTION, Proficiency.ATHLETICS)
        val result = input.toCommaSeparated()
        val parts = result.split(",")
        assertEquals(3, parts.size)
        assertTrue(parts.contains("STEALTH"))
        assertTrue(parts.contains("PERCEPTION"))
        assertTrue(parts.contains("ATHLETICS"))
    }

    // ── toProficiencySet ──────────────────────────────────────────────────────

    @Test
    fun `toProficiencySet - empty string returns empty set`() {
        assertTrue("".toProficiencySet().isEmpty())
    }

    @Test
    fun `toProficiencySet - blank string returns empty set`() {
        assertTrue("   ".toProficiencySet().isEmpty())
    }

    @Test
    fun `toProficiencySet - single valid name returns single-element set`() {
        val result = "ATHLETICS".toProficiencySet()
        assertEquals(setOf(Proficiency.ATHLETICS), result)
    }

    @Test
    fun `toProficiencySet - multiple valid names returns correct set`() {
        val result = "ARCANA,HISTORY,RELIGION".toProficiencySet()
        assertEquals(setOf(Proficiency.ARCANA, Proficiency.HISTORY, Proficiency.RELIGION), result)
    }

    @Test
    fun `toProficiencySet - unknown value is silently ignored`() {
        val result = "ACROBATICS,NOT_A_REAL_PROFICIENCY,STEALTH".toProficiencySet()
        assertEquals(setOf(Proficiency.ACROBATICS, Proficiency.STEALTH), result)
    }

    @Test
    fun `toProficiencySet - values with surrounding whitespace are trimmed`() {
        val result = " ATHLETICS , STEALTH ".toProficiencySet()
        assertEquals(setOf(Proficiency.ATHLETICS, Proficiency.STEALTH), result)
    }

    @Test
    fun `toProficiencySet and toCommaSeparated are round-trip inverses`() {
        val original = setOf(
            Proficiency.ACROBATICS,
            Proficiency.LIGHT_ARMOR,
            Proficiency.COMMON,
            Proficiency.THIEVES_TOOLS,
            Proficiency.SAVING_THROW_DEXTERITY
        )
        val serialised = original.toCommaSeparated()
        val deserialised = serialised.toProficiencySet()
        assertEquals(original, deserialised)
    }

    @Test
    fun `toProficiencySet round-trips full proficiency set correctly`() {
        val all = Proficiency.entries.toSet()
        assertEquals(all, all.toCommaSeparated().toProficiencySet())
    }

    // ── byCategory ────────────────────────────────────────────────────────────

    @Test
    fun `byCategory returns only proficiencies belonging to that category`() {
        val skills = Proficiency.byCategory(ProficiencyCategory.SKILLS)
        assertTrue(skills.all { it.category == ProficiencyCategory.SKILLS })
    }

    @Test
    fun `byCategory for SKILLS returns 18 entries`() {
        assertEquals(18, Proficiency.byCategory(ProficiencyCategory.SKILLS).size)
    }

    @Test
    fun `byCategory for SAVING_THROWS returns 6 entries`() {
        assertEquals(6, Proficiency.byCategory(ProficiencyCategory.SAVING_THROWS).size)
    }

    @Test
    fun `byCategory for ARMOR returns 4 entries`() {
        assertEquals(4, Proficiency.byCategory(ProficiencyCategory.ARMOR).size)
    }

    @Test
    fun `byCategory for WEAPONS returns 2 entries`() {
        assertEquals(2, Proficiency.byCategory(ProficiencyCategory.WEAPONS).size)
    }

    @Test
    fun `byCategory partitions all proficiencies with no overlaps`() {
        val totalFromCategories = ProficiencyCategory.entries.sumOf { Proficiency.byCategory(it).size }
        assertEquals(Proficiency.entries.size, totalFromCategories)
    }

    @Test
    fun `every category has at least one proficiency`() {
        ProficiencyCategory.entries.forEach { category ->
            assertTrue(
                "Expected at least one proficiency for $category",
                Proficiency.byCategory(category).isNotEmpty()
            )
        }
    }

    // ── Proficiency enum ──────────────────────────────────────────────────────

    @Test
    fun `Proficiency has exactly 6 categories`() {
        assertEquals(6, ProficiencyCategory.entries.size)
    }

    @Test
    fun `every Proficiency has a non-blank displayName`() {
        Proficiency.entries.forEach { proficiency ->
            assertTrue(
                "Expected non-blank displayName for $proficiency",
                proficiency.displayName.isNotBlank()
            )
        }
    }

    @Test
    fun `every ProficiencyCategory has a non-blank displayName`() {
        ProficiencyCategory.entries.forEach { category ->
            assertTrue(
                "Expected non-blank displayName for $category",
                category.displayName.isNotBlank()
            )
        }
    }
}

