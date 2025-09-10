/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.conllio

import java.io.File

/**
 * The CoNLLReader is designed to read CoNLL-style data format, turning them into a list of [Sentence]s.
 *
 * It is possible to read the CoNLL-X data format used in the CoNLL 2006 Shared Task and the CoNLL-U data
 * format used in the CoNLL 2017 Shared Task.
 *
 * In a few words, it expects data encoded in plain text files (UTF-8) with three types of lines:
 * - Word lines containing the annotation of a word/token.
 * - Blank lines marking sentence boundaries.
 * - Comment lines starting with hash (#).
 */
object CoNLLReader {

  /**
   * Iterate over the [Sentence]s of a given file in CoNLL format.
   *
   * @param file the CoNLL file to read
   */
  fun forEachSentence(file: File, callback: (Sentence) -> Unit) {
    this.forEachSentence(file.readText(charset = Charsets.UTF_8)) { callback(it) }
  }

  /**
   * Iterate over the [Sentence]s contained in a string in CoNLL format.
   *
   * @param text a string containing sentences in CoNLL-style format
   */
  fun forEachSentence(text: String, callback: (Sentence) -> Unit) {

    val lines = text.split('\n')
    val buffer = ArrayList<Pair<Int, String>>()

    lines.withIndex().forEach { (i, line) ->

      if (line.isSentenceBoundary() && buffer.isNotEmpty()) {

        val sentence = try {
          SentenceReader(buffer).readSentence()
        } catch (e: SentenceReader.InvalidLine) {
          throw SentenceReader.InvalidLine(lineIndex = i, line = line)
        }

        buffer.clear()

        callback(sentence)

      } else {
        buffer.add(Pair(i, line))
      }
    }
  }

  /**
   * Blank lines marking sentence boundaries.
   *
   * @return True if the string (after trim) is empty.
   */
  private fun String.isSentenceBoundary(): Boolean = this.trim().isEmpty()
}
