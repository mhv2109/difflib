package com.mhv2109.difflib

private data class QueueElem(
	val alo: Int,
	val ahi: Int,
	val blo: Int,
	val bhi: Int
)

/**
 * Kotlin implementation of Python difflib.SequenceMatcher.
 *
 * From difflib docs:
 *
 * The basic algorithm predates, and is a little fancier than, an algorithm published in the late 1980's by Ratcliff and
 * Obershelp under the hyperbolic name "gestalt pattern matching".  The basic idea is to find the longest contiguous
 * matching subsequence that contains no "junk" elements (R-O doesn't address junk).  The same idea is then applied
 * recursively to the pieces of the sequences to the left and to the right of the matching subsequence.  This does not
 * yield minimal edit sequences, but does tend to yield matches that "look right" to people.
 *
 * Reference implementation here: https://github.com/python/cpython/blob/3.7/Lib/difflib.py
 *
 * @param a the first of two sequences to be compared, by default, an empty List
 * @param b the second of two sequences to be compared, by default, an empty List
 * @param isJunk a one-argument function that takes a sequence element and returns true iff the element is junk
 * @param autoJunk should be set to False to disable the "automatic junk heuristic" that treats popular elements as junk
 */
class SequenceMatcher<T>(
	private var a: List<T> = emptyList(),
	private var b: List<T> = emptyList(),
	private val isJunk: (T) -> Boolean = { false },
	private val autoJunk: Boolean = true
) {

	private val opcodes = mutableListOf<Opcode>()
	private var matchingBlocks = mutableListOf<Match>()

	private val b2j = mutableMapOf<T, MutableList<Int>>()
	private val bjunk = mutableSetOf<T>()
	private val bpopular = mutableSetOf<T>()

	init {
		setSeqs(a, b)
	}

	fun setSeqs(a: List<T>, b: List<T>) {
		setSeq1(a)
		setSeq2(b)
	}

	fun setSeq1(a: List<T>) {
		this.a = a
		opcodes.clear()
		matchingBlocks.clear()
	}

	fun setSeq2(b: List<T>) {
		this.b = b
		opcodes.clear()
		matchingBlocks.clear()
		chainB()
	}

	private fun chainB() {
		b2j.clear()

		b.forEachIndexed { index, c ->
			if(b2j.containsKey(c))
				b2j[c]!!.add(index)
			else
				b2j[c] = mutableListOf(index)
		}

		// purge junk elements
		bjunk.clear()
		b2j.keys.forEach {
			if(isJunk(it))
				bjunk.add(it)
		}
		bjunk.forEach {
			b2j.remove(it)
		}

		// purge popular elements that are not junk
		bpopular.clear()
		val n = b.size
		if(autoJunk && n >= 200) {
			val ntest = n / 100 + 1
			b2j.forEach { t, u ->
				if(u.size > ntest)
					bpopular.add(t)
			}
			bpopular.forEach {
				b2j.remove(it)
			}
		}
	}

	fun findLongestMatch(alo: Int, ahi: Int, blo: Int, bhi: Int): Match {
		var besti = alo
		var bestj = ahi
		var bestSize = 0

		// find longest junk-free match
		var j2len = mutableMapOf<Int, Int>()
		for(i in alo until ahi) {
			// look at all instances of a[i] in b.  Note that because b2j has no junk keys, the loop is skipped if
			// a[i] is junk
			val newj2len = mutableMapOf<Int, Int>()
			for(j in b2j[a[i]] ?: emptyList<Int>()) {
				if(j < blo)
					continue
				if(j >= bhi)
					break
				val k = (j2len[j-1] ?: 0) + 1
				newj2len[j] = k
				if(k > bestSize) {
					besti = i - k + 1
					bestj = j - k + 1
					bestSize = k
				}
			}
			j2len = newj2len
		}

		// Extend the best by non-junk elements on each end
		while(besti > alo &&
			bestj > blo &&
			!bjunk.contains(b[bestj - 1]) &&
			(a[besti - 1] == b[bestj - 1])) {

			besti -= 1
			bestj -= 1
			bestSize += 1

		}

		while((besti + bestSize < ahi) &&
			(bestj + bestSize < bhi) &&
			!bjunk.contains(b[bestj + bestSize]) &&
			(a[besti + bestSize] == b[bestj + bestSize])) {

			bestSize += 1

		}

		// add all matching junk on each side of Match
		while((besti > alo) &&
			(bestj > blo) &&
			bjunk.contains(b[bestj - 1]) &&
			(a[besti - 1] == b[bestj - 1])) {

			besti -= 1
			bestj -= 1
			bestSize += 1

		}

		while((besti + bestSize < ahi) &&
			(bestj + bestSize < bhi) &&
			bjunk.contains(b[bestj + bestSize]) &&
			(a[besti + bestSize] == b[bestj + bestSize])) {

			bestSize += 1

		}

		return Match(besti, bestj, bestSize)
	}

	fun getMatchingBlocks(): List<Match> {
		if(matchingBlocks.isNotEmpty())
			return matchingBlocks.toList()

		val la = a.size
		val lb = b.size

		val queue = mutableListOf(QueueElem(0, la, 0, lb))
		matchingBlocks.clear()
		while(!queue.isEmpty()) {
			val elem = queue.removeAt(queue.size - 1)
			val match = findLongestMatch(elem.alo, elem.ahi, elem.blo, elem.bhi)
			if(match.size > 0) { // if size == 0, there was no matching block.
				matchingBlocks.add(match)
				if(elem.alo < match.a && elem.blo < match.b)
					queue.add(QueueElem(elem.alo, match.a, elem.blo, match.b))
				if(match.a + match.size < elem.ahi && match.b + match.size < elem.bhi)
					queue.add(QueueElem(match.a + match.size, elem.ahi, match.b + match.size, elem.bhi))
			}
		}
		matchingBlocks = matchingBlocks.sortedWith(compareBy(Match::a, Match::b, Match::size)).toMutableList()

		// collapse adjacent equal blocks
		var i1 = 0
		var j1 = 0
		var k1 = 0
		val nonAdjacent = mutableListOf<Match>()
		matchingBlocks.forEach {
			if(i1 + k1 == it.a && j1 + k1 == it.b)
				k1 += it.size
			else {
				if (k1 > 0)
					nonAdjacent.add(Match(i1, j1, k1))
				i1 = it.a
				j1 = it.b
				k1 = it.size
			}
		}
		if(k1 > 0)
			nonAdjacent.add(Match(i1, j1, k1))

		nonAdjacent.add(Match(la, lb, 0))
		matchingBlocks = nonAdjacent
		return matchingBlocks.toList()
	}

	fun ratio(): Double {
		val matches = getMatchingBlocks().map { it.size }.toIntArray().sum()
		return calculateRatio(matches, a.size + b.size)
	}

	fun getOpcodes(): List<Opcode> {
		if(opcodes.isNotEmpty())
			return opcodes

		var i = 0
		var j = 0
		getMatchingBlocks().forEach {
			val tag: Tag? = when {
				i < it.a && j < it.b -> Tag.REPLACE
				i < it.a -> Tag.DELETE
				j < it.b -> Tag.INSERT
				else -> null
			}
			if(tag != null)
				opcodes.add(Opcode(tag, i, it.a, j, it.b))
			i = it.a + it.size
			j = it.b + it.size
			if(it.size > 0)
				opcodes.add(Opcode(Tag.EQUAL, it.a, i, it.b, j))
		}

		return opcodes.toList()
	}

	fun getGroupedOpcodes(n: Int = 3): List<List<Opcode>> {

		var codes = getOpcodes().toMutableList()

		if(codes.isEmpty())
			codes = mutableListOf(Opcode(Tag.EQUAL, 0,1,0,1))
		// Fixup leading and trailing groups if they show no changes
		if(codes.first().tag == Tag.EQUAL) {
			val code = codes.first()
			codes[0] = code.copy(alo = Math.max(code.alo, code.ahi-n), blo = Math.max(code.blo, code.bhi-n))
		}
		if(codes.last().tag == Tag.EQUAL) {
			val code = codes.last()
			codes[codes.size-1] = code.copy(ahi = Math.min(code.ahi, code.alo+n), bhi = Math.min(code.bhi, code.blo+n))
		}

		val nn = n + n
		val group = mutableListOf<Opcode>()
		val result = mutableListOf<List<Opcode>>()
		codes.forEach {
			if(it.tag == Tag.EQUAL && it.ahi-it.alo > nn) {
				group.add(it.copy(ahi = Math.min(it.ahi, it.alo+n), bhi = Math.min(it.bhi, it.blo+n)))
				result.add(group.toList())
				group.clear()
				group.add(it.copy(alo = Math.max(it.alo, it.ahi-n), blo = Math.max(it.blo, it.bhi-n)))
			} else {
				group.add(it.copy())
			}
		}
		if(group.isNotEmpty() && !(group.size == 1 && group.first().tag == Tag.EQUAL))
			result.add(group.toList())

		return result.toList()
	}

}

/**
 * Use SequenceMatcher to return list of the best "good enough" matches.
 *
 * @param sequence sequence for which close matches are desired (typically a string cast to a List<Char>)
 * @param possibilities possibilities is a list of sequences against which to match
 * @param n the maximum number of close matches to return
 * @param cutoff (default 0.6) is a Double in [0, 1]
 */
fun <T> getCloseMatches(sequence: List<T>, possibilities: Collection<List<T>>,
						n: Int = 3, cutoff: Double = 0.6): List<List<T>> {

	if(n <= 0)
		throw IllegalArgumentException("Expected: n > 0. Actual: n = $n")
	if(cutoff > 1.0 || cutoff < 0.0)
		throw IllegalArgumentException("Expected: 0.0 < cutoff < 1.0. Actual: cutoff = $cutoff")

	var allResults = mutableListOf<Pair<Double, List<T>>>()
	val s = SequenceMatcher<T>()
	s.setSeq2(sequence)
	possibilities.forEach {
		s.setSeq1(it)
		val ratio = s.ratio()
		if(ratio >= cutoff)
			allResults.add(Pair(ratio, it))
	}
	allResults = allResults.sortedWith(compareBy { it.first } ).toMutableList()

	val result = mutableListOf<List<T>>()
	for(i in 0 until n) {
		if(i < allResults.size)
			result.add(allResults[i].second)
		else
			break
	}
	return result.toList()
}