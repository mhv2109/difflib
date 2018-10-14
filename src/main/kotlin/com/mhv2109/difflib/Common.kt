package com.mhv2109.difflib

enum class Tag {
	REPLACE, INSERT, DELETE, EQUAL
}

data class Opcode(
	val tag: Tag,
	val i1: Int,
	val i2: Int,
	val j1: Int,
	val j2: Int
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