/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.conllio

import java.io.File

/**
 * The CoNLLWriter.
 */
object CoNLLWriter {

  /**
   * Write a list of [Sentence]s in the CoNLL-style format to the file [outputFilePath].
   *
   * @param outputFilePath the output file path.
   *
   * @param sentences sequence of [Sentence]s to write.
   */
  fun toFile(sentences: List<Sentence>, writeComments: Boolean, outputFilePath: String) {

    File(outputFilePath).printWriter().use { out ->
      sentences.forEach {
        out.write("%s\n\n".format(it.toCoNLLString(writeComments = writeComments)))
      }
    }
  }
}
