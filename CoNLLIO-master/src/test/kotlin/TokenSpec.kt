/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.conllio.Token
import com.kotlinnlp.linguisticdescription.POSTag
import com.kotlinnlp.linguisticdescription.syntax.SyntacticDependency
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * TokenSpec Tests Class.
 */
object TokenSpec : Spek({

  describe("Token") {

    context("Constructor") {

      it("should raise an exception in case the id is 0") {
        assertFailsWith<Token.InvalidTokenId> {
          Token(
            id = 0,
            form = Token.EMPTY_FILLER,
            lemma = Token.EMPTY_FILLER,
            posList = listOf(POSTag(Token.EMPTY_FILLER)),
            pos2List = listOf(POSTag(Token.EMPTY_FILLER)),
            feats = mapOf(),
            head = 0,
            syntacticDependencies = listOf(SyntacticDependency(Token.EMPTY_FILLER)),
            lineNumber = 0)
        }
      }

      it("should raise an exception in case the head < 0") {
        assertFailsWith<Token.InvalidTokenHead> {
          Token(
            id = 1,
            form = Token.EMPTY_FILLER,
            lemma = Token.EMPTY_FILLER,
            posList = listOf(POSTag(Token.EMPTY_FILLER)),
            pos2List = listOf(POSTag(Token.EMPTY_FILLER)),
            feats = mapOf(),
            head = -1,
            syntacticDependencies = listOf(SyntacticDependency(Token.EMPTY_FILLER)),
            lineNumber = 0)
        }
      }

      it("should raise an exception in case the head == id") {
        assertFailsWith<Token.InvalidTokenHead> {
          Token(
            id = 1,
            form = Token.EMPTY_FILLER,
            lemma = Token.EMPTY_FILLER,
            posList = listOf(POSTag(Token.EMPTY_FILLER)),
            pos2List = listOf(POSTag(Token.EMPTY_FILLER)),
            feats = mapOf(),
            head = 1,
            syntacticDependencies = listOf(SyntacticDependency(Token.EMPTY_FILLER)),
            lineNumber = 0)
        }
      }

      it("should raise an exception in case the form is empty") {
        assertFailsWith<Token.InvalidTokenForm> {
          Token(
            id = 1,
            form = "",
            lemma = Token.EMPTY_FILLER,
            posList = listOf(POSTag(Token.EMPTY_FILLER)),
            pos2List = listOf(POSTag(Token.EMPTY_FILLER)),
            feats = mapOf(),
            head = 0,
            syntacticDependencies = listOf(SyntacticDependency(Token.EMPTY_FILLER)),
            lineNumber = 0)
        }
      }

      it("should raise an exception in case the pos is empty") {
        assertFailsWith<Token.InvalidTokenPOS> {
          Token(
            id = 1,
            form = Token.EMPTY_FILLER,
            lemma = Token.EMPTY_FILLER,
            posList = listOf(POSTag("")),
            pos2List = listOf(POSTag(Token.EMPTY_FILLER)),
            feats = mapOf(),
            head = 0,
            syntacticDependencies = listOf(SyntacticDependency(Token.EMPTY_FILLER)),
            lineNumber = 0)
        }
      }
    }

    context("toCoNLLString()") {

      it("should return the expected CoNLL line") {
        assertEquals(
          "1\tdogs\tdog\tnoun\t_\t_\t0\troot\t_\t_",
          Token(
            id = 1,
            form = "dogs",
            lemma = "dog",
            posList = listOf(POSTag("noun")),
            pos2List = listOf(POSTag(Token.EMPTY_FILLER)),
            feats = mapOf(),
            head = 0,
            syntacticDependencies = listOf(SyntacticDependency("root")),
            lineNumber = 0).toCoNLLString())
      }
    }
  }
})
