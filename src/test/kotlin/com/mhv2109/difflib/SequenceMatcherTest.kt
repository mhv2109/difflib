package com.mhv2109.difflib

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions

/**
 * SequenceMatcher doctests.
 */
class SequenceMatcherTest {

	@Test
	fun setSeq1() {
		val s = SequenceMatcher("abcd", "bcde")
		Assertions.assertEquals(0.75, s.ratio())
		s.setSeq1("bcde")
		Assertions.assertEquals(1.0, s.ratio())
	}

	@Test
	fun setSeq2() {
		val s = SequenceMatcher("abcd", "bcde")
		Assertions.assertEquals(0.75, s.ratio())
		s.setSeq2("abcd")
		Assertions.assertEquals(1.0, s.ratio())
	}

	@Test
	fun findLongestMatch() {
		val s = SequenceMatcher(" abcd", "abcd abcd")
		Assertions.assertEquals(Match(0, 4, 5), s.findLongestMatch(0, 5, 0, 9))
	}

	@Test
	fun findLongestMatch2() {
		val a = "123456789101112131415161718192021222324252627282930313233343536373839"
		val b = "12345678i91011121314151617181920x21222829303132333435y36373839"
		val s = SequenceMatcher(a, b)
		Assertions.assertEquals(
			Match(8, 9, 23),
			s.findLongestMatch(0, a.length, 0, b.length)
		)
	}

	@Test
	fun getMatchingBlocks() {
		val s = SequenceMatcher("abxcd", "abcd")
		Assertions.assertEquals(listOf(Match(a=0, b=0, size=2), Match(a=3, b=2, size=2), Match(5, 4, 0)),
			s.getMatchingBlocks())
	}

	@Test
	fun getMatchingBlocks2() {
		val a = "123456789101112131415161718192021222324252627282930313233343536373839"
		val b = "12345678i91011121314151617181920x21222829303132333435y36373839"
		val s = SequenceMatcher(a, b)
		Assertions.assertEquals(
			listOf(
				Match(0, 0, 8),
				Match(8, 9, 23),
				Match(31, 33, 4),
				Match(45, 37, 16),
				Match(61, 54, 8),
				Match(69, 62, 0)
			),
			s.getMatchingBlocks()
		)
	}

	@Test
	fun getOpcodes() {
		val s = SequenceMatcher("qabxcd", "abycdf")
		Assertions.assertEquals(
			listOf(
				Opcode(Tag.DELETE, 0, 1, 0, 0),
				Opcode(Tag.EQUAL, 1, 3, 0, 2),
				Opcode(Tag.REPLACE, 3, 4, 2, 3),
				Opcode(Tag.EQUAL, 4, 6, 3, 5),
				Opcode(Tag.INSERT, 6, 6, 5, 6)
			),
			s.getOpcodes())
	}

	@Test
	fun getOpcodes2() {
		val a = "123456789101112131415161718192021222324252627282930313233343536373839"
		val b = "12345678i91011121314151617181920x21222829303132333435y36373839"
		val s = SequenceMatcher(a, b)
		Assertions.assertEquals(
			listOf(
				Opcode(Tag.EQUAL, 0, 8, 0, 8),
				Opcode(Tag.INSERT, 8, 8, 8, 9),
				Opcode(Tag.EQUAL, 8, 31, 9, 32),
				Opcode(Tag.INSERT, 31, 31, 32, 33),
				Opcode(Tag.EQUAL, 31, 35, 33, 37),
				Opcode(Tag.DELETE, 35, 45, 37, 37),
				Opcode(Tag.EQUAL, 45, 61, 37, 53),
				Opcode(Tag.INSERT, 61, 61, 53, 54),
				Opcode(Tag.EQUAL, 61, 69, 54, 62)
			),
			s.getOpcodes()
		)
	}

	@Test
	fun getGroupedOpcodes() {
		val a = "123456789101112131415161718192021222324252627282930313233343536373839"
		val b = "12345678i91011121314151617181920x21222829303132333435y36373839"
		val s = SequenceMatcher(a, b)
		Assertions.assertEquals(
			listOf(
				listOf(
					Opcode(Tag.EQUAL, 5,8,5,8),
					Opcode(Tag.INSERT,  8, 8, 8, 9),
					Opcode(Tag.EQUAL,  8, 11, 9, 12)
				),
				listOf(
					Opcode(Tag.EQUAL, 28, 31, 29, 32),
					Opcode(Tag.INSERT, 31, 31, 32, 33),
					Opcode(Tag.EQUAL, 31, 35, 33, 37),
					Opcode(Tag.DELETE, 35, 45, 37, 37),
					Opcode(Tag.EQUAL, 45, 48, 37, 40)
				),
				listOf(
					Opcode(Tag.EQUAL, 58, 61, 50, 53),
					Opcode(Tag.INSERT, 61, 61, 53, 54),
					Opcode(Tag.EQUAL, 61, 64, 54, 57)
				)
			),
			s.getGroupedOpcodes()
		)
	}

}