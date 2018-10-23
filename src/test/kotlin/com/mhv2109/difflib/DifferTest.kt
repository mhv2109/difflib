package com.mhv2109.difflib

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Differ doctests.
 */
class DifferTest {

	@Test
	fun compare() {
		val d = Differ()
		val a = listOf("one\n", "two\n", "three\n")
		val b = listOf("ore\n", "tree\n", "emu\n")
		val result = listOf(
			"- one\n", "?  ^\n", "+ ore\n", "?  ^\n", "- two\n",
			"- three\n", "?  -\n", "+ tree\n", "+ emu\n")
		Assertions.assertEquals(result, d.compare(a, b))
	}

}