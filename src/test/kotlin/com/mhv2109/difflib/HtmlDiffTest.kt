package com.mhv2109.difflib

import org.junit.jupiter.api.Test

/**
 * Print outputs and check visually
 *
 * TODO: improve!
 */
class HtmlDiffTest {

    private val htmlDiff = HtmlDiff()

    @Test
    fun makeFile1() {
        val from = listOf("one", "two", "three")
        val to = listOf("once", "too", "tree")
        println(htmlDiff.makeFile(from, to))
    }

    @Test
    fun makeFile2() {
        val from = listOf("o")
        val to = listOf("e")
        println(htmlDiff.makeFile(from, to))
    }

    @Test
    fun makeTable() {
        val from = listOf("one", "two", "three")
        val to = listOf("once", "too", "tree")
        println(htmlDiff.makeTable(from, to))
    }
}