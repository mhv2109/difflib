package com.mhv2109.difflib

enum class Tag {
	REPLACE, INSERT, DELETE, EQUAL
}

data class Opcode(
	val tag: Tag,
	val alo: Int,
	val ahi: Int,
	val blo: Int,
	val bhi: Int
)

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