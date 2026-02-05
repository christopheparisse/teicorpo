/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.conllio.CoNLLReader
import com.kotlinnlp.conllio.Sentence
import java.io.File

/**
 * Read all .conllu files in all sub-directories of the one given as argument.
 * For each tree-bank calculate some statistics (e.g. the number of syntactical annotated sentences and how many
 * of them are non-projective) and display the first sentence.
 */
fun main(args : Array<String>) {

  val file = File(args[0])

  file.walk().filter { it.extension == "conllu"}.forEach {

    println("Reading $it ...")

    var firstSentence: Sentence? = null
    var sentenceCount = 0
    var nonProjectiveCount = 0
    var annotatedSentencesCount = 0

    CoNLLReader.forEachSentence(file = it) {

      sentenceCount++

      if (firstSentence == null) firstSentence = it

      if (it.hasAnnotatedHeads()) {

        it.assertValidCoNLLTree() // raise exception in case of invalid tree

        annotatedSentencesCount++
        if (it.isNonProjective()) nonProjectiveCount++
      }
    }

    println("sentences: $sentenceCount (with-heads: $annotatedSentencesCount non-projective: $nonProjectiveCount)\n")

    println(firstSentence!!.toCoNLLString(writeComments = true) + '\n')
  }
}
