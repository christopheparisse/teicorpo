/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.conllio.SentenceReader
import com.kotlinnlp.conllio.Token
import com.kotlinnlp.linguisticdescription.POSTag
import com.kotlinnlp.linguisticdescription.syntax.SyntacticDependency
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

class SentenceReaderSpec: Spek({

  describe("SentenceReader") {

    context("readSentence()") {

      val buffer = ArrayList<Pair<Int, String>>()
      buffer.add(Pair(0, "1\tthe\tthe\tdet\t_\t_\t2\tdet\t_\t_"))
      buffer.add(Pair(1, "2\tdogs\tdog\tnoun\t_\t_\t0\troot\t_\t_"))

      val sentence = SentenceReader(buffer).readSentence()

      it("should create the expected tokens at index 0") {

        assertEquals(
          sentence.tokens[0],
          Token(
            id = 1,
            form = "the",
            lemma = "the",
            posList = listOf(POSTag("det")),
            pos2List = listOf(POSTag("_")),
            feats = mapOf(),
            head = 2,
            syntacticDependencies = listOf(
              SyntacticDependency.String(type = "det", direction = SyntacticDependency.Direction.LEFT)
            ),
            lineNumber = 0))
      }

      it("should create the expected tokens at index 1") {

        assertEquals(
          sentence.tokens[1],
          Token(
            id = 2,
            form = "dogs",
            lemma = "dog",
            posList = listOf(POSTag("noun")),
            pos2List = listOf(POSTag("_")),
            feats = mapOf(),
            head = 0,
            syntacticDependencies = listOf(
              SyntacticDependency.String(type = "root", direction = SyntacticDependency.Direction.ROOT)
            ),
            lineNumber = 1))
      }
    }
  }
})
