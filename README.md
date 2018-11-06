# Difflib

This is a Kotlin port of the `difflib` Python package found 
[here](https://github.com/python/cpython/blob/3.7/Lib/difflib.py).  Requires JRE >= 8.

# Features

## SequenceMatcher
A flexible class for comparing pairs of sequences.  The basic algorithm predates, and is a little fancier than, an 
algorithm published in the late 1980's by Ratcliff and Obershelp under the hyperbolic name "gestalt pattern matching".

```
val s = SequenceMatcher("abcd".toList(), "bcde".toList())
s.ratio() // 0.75
```

## Differ
Differ is a class for comparing sequences of lines of text, and producing human-readable differences or deltas.

```
val d = Differ()
val a = listOf("one\n", "two\n", "three\n")
val b = listOf("ore\n", "tree\n", "emu\n")
d.compare(a, b)
```

## HtmlDiff
Note: This class depends on experimental Kotlin features!

For producing HTML side by side comparison with change highlights. The current implementation differs from the original
Python `difflib.HtmlDiff` in that it uses `SequenceMatcher` to produce side-by-side comparisons instead of `Differ`.

```
val from = listOf("one", "two", "three")
val to = listOf("once", "too", "tree")
htmlDiff.makeFile(from, to)
```
