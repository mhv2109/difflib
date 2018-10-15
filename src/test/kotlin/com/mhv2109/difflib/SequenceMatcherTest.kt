package com.mhv2109.difflib

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions

/**
 * SequenceMatcher doctests.
 */
class SequenceMatcherTest {

	@Test
	fun setSeq1() {
		val s = SequenceMatcher("abcd".toList(), "bcde".toList())
		Assertions.assertEquals(0.75, s.ratio())
		s.setSeq1("bcde".toList())
		Assertions.assertEquals(1.0, s.ratio())
	}

	@Test
	fun setSeq2() {
		val s = SequenceMatcher("abcd".toList(), "bcde".toList())
		Assertions.assertEquals(0.75, s.ratio())
		s.setSeq2("abcd".toList())
		Assertions.assertEquals(1.0, s.ratio())
	}

	@Test
	fun findLongestMatch() {
		val s = SequenceMatcher(" abcd".toList(), "abcd abcd".toList())
		Assertions.assertEquals(Match(0, 4, 5), s.findLongestMatch(0, 5, 0, 9))
	}

	@Test
	fun findLongestMatch2() {
		val a = listOf(
			"1", "2", "3", "4", "5", "6", "7", "8", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
			"30", "31", "32", "33", "34", "35", "36", "37", "38", "39"
		)
		val b = listOf(
			"1", "2", "3", "4", "5", "6", "7", "8", "i", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20x", "21", "22", "28", "29",
			"30", "31", "32", "33", "34", "35y", "36", "37", "38", "39"
		)
		val s = SequenceMatcher(a.toList(), b.toList())
		Assertions.assertEquals(
			Match(8, 9, 11),
			s.findLongestMatch(0, a.size, 0, b.size)
		)
	}

	@Test
	fun getMatchingBlocks() {
		val s = SequenceMatcher("abxcd".toList(), "abcd".toList())
		Assertions.assertEquals(listOf(Match(a=0, b=0, size=2), Match(a=3, b=2, size=2), Match(5, 4, 0)),
			s.getMatchingBlocks())
	}

	@Test
	fun getMatchingBlocks2() {
		val a = listOf(
			"1", "2", "3", "4", "5", "6", "7", "8", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
			"30", "31", "32", "33", "34", "35", "36", "37", "38", "39"
		)
		val b = listOf(
			"1", "2", "3", "4", "5", "6", "7", "8", "i", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20x", "21", "22", "28", "29",
			"30", "31", "32", "33", "34", "35y", "36", "37", "38", "39"
		)
		val s = SequenceMatcher(a.toList(), b.toList())
		Assertions.assertEquals(
			listOf(
				Match(0, 0, 8),
				Match(8, 9, 11),
				Match(20, 21, 2),
				Match(27, 23, 7),
				Match(35, 31, 4),
				Match(39, 35, 0)
			),
			s.getMatchingBlocks()
		)
	}

	@Test
	fun getOpcodes() {
		val s = SequenceMatcher("qabxcd".toList(), "abycdf".toList())
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
		val a = listOf(
			"1", "2", "3", "4", "5", "6", "7", "8", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
			"30", "31", "32", "33", "34", "35", "36", "37", "38", "39"
		)
		val b = listOf(
			"1", "2", "3", "4", "5", "6", "7", "8", "i", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20x", "21", "22", "28", "29",
			"30", "31", "32", "33", "34", "35y", "36", "37", "38", "39"
		)
		val s = SequenceMatcher(a, b)
		Assertions.assertEquals(
			listOf(
				Opcode(Tag.EQUAL, 0, 8, 0, 8),
				Opcode(Tag.INSERT, 8, 8, 8, 9),
				Opcode(Tag.EQUAL, 8, 19, 9, 20),
				Opcode(Tag.REPLACE, 19, 20, 20, 21),
				Opcode(Tag.EQUAL, 20, 22, 21, 23),
				Opcode(Tag.DELETE, 22, 27, 23, 23),
				Opcode(Tag.EQUAL, 27, 34, 23, 30),
				Opcode(Tag.REPLACE, 34, 35, 30, 31),
				Opcode(Tag.EQUAL, 35, 39, 31, 35)
			),
			s.getOpcodes()
		)
	}

	@Test
	fun getGroupedOpcodes() {
		val a = listOf(
			"1", "2", "3", "4", "5", "6", "7", "8", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
			"30", "31", "32", "33", "34", "35", "36", "37", "38", "39"
		)
		val b = listOf(
			"1", "2", "3", "4", "5", "6", "7", "8", "i", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20x", "21", "22", "28", "29",
			"30", "31", "32", "33", "34", "35y", "36", "37", "38", "39"
		)
		val s = SequenceMatcher(a, b)
		Assertions.assertEquals(
			listOf(
				listOf(
					Opcode(Tag.EQUAL, 5,8,5,8),
					Opcode(Tag.INSERT,  8, 8, 8, 9),
					Opcode(Tag.EQUAL,  8, 11, 9, 12)
				),
				listOf(
					Opcode(Tag.EQUAL, 16, 19, 17, 20),
					Opcode(Tag.REPLACE, 19, 20, 20, 21),
					Opcode(Tag.EQUAL, 20, 22, 21, 23),
					Opcode(Tag.DELETE, 22, 27, 23, 23),
					Opcode(Tag.EQUAL, 27, 30, 23, 26)
				),
				listOf(
					Opcode(Tag.EQUAL, 31, 34, 27, 30),
					Opcode(Tag.REPLACE, 34, 35, 30, 31),
					Opcode(Tag.EQUAL, 35, 38, 31, 34)
				)
			),
			s.getGroupedOpcodes()
		)
	}

}