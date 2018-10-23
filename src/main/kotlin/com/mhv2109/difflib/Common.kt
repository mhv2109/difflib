package com.mhv2109.difflib

/**
 * Describes the basic actions for turning one sequence into another.
 */
enum class Tag {
	REPLACE, INSERT, DELETE, EQUAL
}

/**
 * Tag and subList indices for turning sequence a into sequence b.
 * @param Tag basic action
 * @param alo lower bound of a on which to perform action
 * @param ahi upper bound of a on which to perform action
 * @param blo lower bound of b of which to use for action
 * @param bhi upper bound of b of which to use for action
 */
data class Opcode(
	val tag: Tag,
	val alo: Int,
	val ahi: Int,
	val blo: Int,
	val bhi: Int
)

/**
 * Describes subLists of sequence a and sequence b that match.
 * @param a index of a
 * @param b index of b
 * @param size number of matching elements
 */
data class Match(
	val a: Int,
	val b: Int,
	val size: Int
)

internal fun calculateRatio(matches: Int, length: Int): Double {
	if(length > 0)
		return 2.0 * matches / length
	return 1.0
}

internal fun countLeading(line: String, ch: Char): Int {
	var i = 0
	val n = line.length
	while(i < n && line[i] == ch)
		i += 1
	return i
}