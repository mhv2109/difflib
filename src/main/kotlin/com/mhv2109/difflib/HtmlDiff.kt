package com.mhv2109.difflib

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlin.math.max

private data class DataRow(
    val from: String,
    val to: String,
    val opcode: List<Opcode>
)

private fun pageTemplate(dataRows: Collection<DataRow>) = createHTML().html {
    head {
        meta {
            httpEquiv = "Content-Type"
            content = "text/html"
            charset = "utf8"
        }
        style {
            type = "text/css"
            +".diff_equal {background-color:#c0c0c0}\n"
            +".diff_insert {background-color:#aaffaa}\n"
            +".diff_replace {background-color:#ffff77}\n"
            +".diff_delete {background-color:#ffaaaa}\n"
            +"table.diff {font-family:Courier; border:medium;}"
        }
    }
    body {
        div {
            unsafe {
                +tableTemplate(dataRows)
            }
        }
        div {
            table {
                classes = setOf("diff")
                summary = "Legend"
                tr {
                    th {
                        colSpan = "2"
                        +"Legend"
                    }
                }
                tr {
                    td {
                        table {
                            attributes["border"] = ""
                            summary = "Colors"
                            tr {
                                th {
                                    +"Colors"
                                }
                            }
                            for(tag in Tag.values())
                                tr {
                                    td {
                                        classes = setOf(getHtmlClass(tag))
                                        +tag.toString()
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Given Tag, return an HTML class.
 *
 * @param tag Tag from Opcode
 */
fun getHtmlClass(tag: Tag): String = when(tag) {
    Tag.EQUAL -> "diff_equal"
    Tag.INSERT -> "diff_insert"
    Tag.DELETE -> "diff_delete"
    Tag.REPLACE -> "diff_replace"
}

private fun tableTemplate(dataRows: Collection<DataRow>) = createHTML().table {
    classes = setOf("diff")
    id = "difflib_chg"

    attributes["cellspacing"] = "0"
    attributes["cellpadding"] = "0"
    attributes["rules"] = "groups"

    colGroup {  }
    colGroup {  }
    colGroup {  }

    tbody {
        var rownum = 1
        for(row in dataRows)
            tr {
                td {
                    span {
                        classes = setOf("diff_index")
                        +rownum.toString()
                    }
                }
                td {
                    for (op in row.opcode) {
                        val klass = getHtmlClass(op.tag)
                        span {
                            classes = setOf(klass)
                            +row.from.substring(op.alo, op.ahi)
                        }
                    }
                }
                td {
                    for (op in row.opcode) {
                        val klass = getHtmlClass(op.tag)
                        span {
                            classes = setOf(klass)
                            +row.to.substring(op.blo, op.bhi)
                        }
                    }
                }
                rownum++
            }
    }
}

/**
 * For producing HTML side by side comparison with change highlights. This class can be used to create an HTML table
 * (or a complete HTML file containing the table) showing a side by side, line by line comparison of text with
 * inter-line and intra-line change highlights.
 */
class HtmlDiff(
    private val isJunk: (Char) -> Boolean = { false }
) {

    private val cruncher = SequenceMatcher(isJunk=isJunk)

    private fun makeRows(from: Collection<String>, to: Collection<String>): List<DataRow> {
        val nrows = max(from.size, to.size)

        val alist = from.toList()
        val blist = to.toList()

        val dataRows = mutableListOf<DataRow>()
        for(i in 0 until nrows) {
            val thisa = alist.getOrNull(i) ?: ""
            val thisb = blist.getOrNull(i) ?: ""

            cruncher.setSeqs(thisa.toList(), thisb.toList())
            val opcodes = cruncher.getOpcodes()
            dataRows.add(DataRow(thisa, thisb, opcodes))
        }

        return dataRows.toList()
    }

    /**
     * Returns HTML table of side by side comparison with change highlights.
     *
     * @param from "Original" Strings
     * @param to Strings to compare to 'from'
     */
    fun makeTable(from: Collection<String>, to: Collection<String>): String {
        return tableTemplate(makeRows(from, to))
    }

    /**
     * Returns HTML file of side by side comparison with change highlights.
     *
     * @param from "Original" Strings
     * @param to Strings to compare to 'from'
     */
    fun makeFile(from: Collection<String>, to: Collection<String>): String {
        return pageTemplate(makeRows(to, from))
    }

}