package com.mhv2109.difflib

/**
 * Differ is a class for comparing sequences of lines of text, and producing human-readable differences or deltas.
 * Differ uses SequenceMatcher both to compare sequences of lines, and to compare sequences of characters within similar
 * (near-matching) lines.
 *
 * @param lineJunk A function that should accept a single string argument, and return true iff the string is junk
 * @param charJunk A function that should accept a single Char and return __true__ if junk
 */
class Differ(
	private val lineJunk: (String) -> Boolean = { _ -> false },
	private val charJunk: (Char) -> Boolean = { _ -> false }
) {

	/**
	 * Compare two sequences of lines; generate the resulting delta. Each sequence must contain individual single-line
	 * strings ending with newlines.
	 */
	fun compare(a: List<String>, b: List<String>): List<String> {

		val result = mutableListOf<String>()
		val cruncher = SequenceMatcher(a, b, this.lineJunk)
		val opcodes = cruncher.getOpcodes()
		opcodes.forEach {
			val g: List<String> = when(it.tag) {
				Tag.REPLACE -> fancyReplace(a, it.alo, it.ahi, b, it.blo, it.bhi)
				Tag.DELETE -> dump('-', a, it.alo, it.ahi)
				Tag.INSERT -> dump('+', b, it.blo, it.bhi)
				Tag.EQUAL -> dump(' ', a, it.alo, it.ahi)
			}
			result.addAll(g)
		}

		return result.toList()
	}

	/**
	 * When replacing one block of lines with another, search the blocks for *similar* lines; the best-matching pair
	 * (if any) is used as a synch point, and intraline difference marking is done on the similar pair. Lots of work,
	 * but often worth it.
	 */
	private fun fancyReplace(a: List<String>, alo: Int, ahi: Int, b: List<String>, blo: Int, bhi: Int): List<String> {

		var bestRatio = 0.74
		val cutoff = 0.75

		val cruncher = SequenceMatcher(isJunk = this.charJunk)
		var eqi = -1
		var eqj = -1
		var besti = 0
		var bestj = 0

		// search for the pair that matches best without being identical
		for(j in blo until bhi) {
			val bj = b[j]
			cruncher.setSeq2(bj.toList())
			for(i in alo until ahi) {
				val ai = a[i]
				if(ai == bj) {
					if(eqi == -1) {
						eqi = i
						eqj = j
					}
					continue
				}
				cruncher.setSeq1(ai.toList())
				val ratio = cruncher.ratio()
				if(ratio > bestRatio) {
					bestRatio = ratio
					besti = i
					bestj = j
				}
			}
		}

		if(bestRatio < cutoff) { // no non-identical "pretty close" pair
			if(eqi == -1) {
				// no identical pair either -- treat it as a straight replace
				return plainReplace(a, alo, ahi, b, blo, bhi)
			}
			// identical pair
			besti = eqi
			bestj = eqj
			bestRatio = 1.0
		} else {
			// there's a close pair, so forget the identical pair (if any)
			eqi = -1
		}

		// pump out diffs from before the synch point
		val result = mutableListOf<String>()
		result.addAll(fancyHelper(a, alo, besti, b, blo, bestj))

		// do intraline marking on the synch pair
		val aelt = a[besti]
		val belt = b[bestj]
		if(eqi == -1) {
			// pump out a '-', '?', '+', '?' quad for the synched lines
			var atags = ""
			var btags = ""
			cruncher.setSeqs(aelt.toList(), belt.toList())
			val opcodes = cruncher.getOpcodes()
			opcodes.forEach {
				val la = it.ahi - it.alo
				val lb = it.bhi - it.blo
				when(it.tag) {
					Tag.REPLACE -> {
						atags += "^".repeat(la)
						btags += "^".repeat(lb)
					}
					Tag.DELETE -> atags += "-".repeat(la)
					Tag.INSERT -> btags += "+".repeat(lb)
					Tag.EQUAL -> {
						atags += " ".repeat(la)
						btags += " ".repeat(lb)
					}
				}
			}
			result.addAll(qformat(aelt, belt, atags, btags))
		} else {
			result.add("  $aelt")
		}

		result.addAll(fancyHelper(a, besti+1, ahi, b, bestj+1, bhi))
		return result.toList()
	}

	private fun qformat(aline: String, bline: String, atags: String, btags: String): List<String> {
		var common = Math.min(countLeading(aline, '\t'), countLeading(bline, '\t'))
		common = Math.min(common, countLeading(atags.substring(common), ' '))
		common = Math.min(common, countLeading(btags.substring(common), ' '))

		val _atags = atags.substring(common).trimEnd { it -> it == ' ' }
		val _btags = btags.substring(common).trimEnd { it -> it == ' ' }

		val result = mutableListOf<String>()

		result.add("- $aline")
		if(_atags.isNotEmpty())
			result.add("? ${"\t".repeat(common)}$_atags\n")

		result.add("+ $bline")
		if(_btags.isNotEmpty())
			result.add("? ${"\t".repeat(common)}$_btags\n")

		return result.toList()
	}

	private fun dump(tag: Char, x: List<String>, lo: Int, hi: Int): List<String> {
		val result = mutableListOf<String>()
		for(i in lo until hi) {
			result.add("$tag ${x[i]}")
		}
		return result.toList()
	}

	private fun plainReplace(a: List<String>, alo: Int, ahi: Int, b: List<String>, blo: Int, bhi: Int): List<String> {
		val result = mutableListOf<String>()
		val first: List<String>
		val second: List<String>

		if(bhi - blo < ahi - alo) {
			first = dump('+', b, blo, bhi)
			second = dump('-', a, alo, ahi)
		} else {
			first = dump('-', a, alo, ahi)
			second = dump('+', b, blo, bhi)
		}

		result.addAll(first)
		result.addAll(second)
		return result.toList()
	}

	private fun fancyHelper(a: List<String>, alo: Int, ahi: Int, b: List<String>, blo: Int, bhi: Int): List<String> {
		val g: List<String> = if(alo < ahi) {
			if(blo < bhi) {
				fancyReplace(a, alo, ahi, b, blo, bhi)
			} else {
				dump('-', a, alo, ahi)
			}
		} else if(blo < bhi) {
			dump('+', b, blo, bhi)
		} else {
			emptyList()
		}

		return g
	}
}