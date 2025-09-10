/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.conllio.treeutils.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TreeUtils Tests Class.
 */
object TreeUtilsSpec : Spek({

  describe("TreeUtils") {

    context("isTree") {

      val heads: Array<Int?> = arrayOf(2, 2, null, 2, 6, 6, 2, 8, 2, 8, 11, 8)

      it("should return the pre-computed value") {
        assertTrue(heads.isTree())
      }
    }

    context("isSingleRoot") {

      val headsSingleRoot: Array<Int?> = arrayOf(2, 2, null, 2, 6, 6, 2, 8, 2, 8, 11, 8)
      val headsMultipleRoot: Array<Int?> = arrayOf(2, 2, null, 2, 6, 6, 2, 8, null, 8, 11, 8)

      it("should return the pre-computed value with headsSingleRoot") {
        assertTrue(headsSingleRoot.isSingleRoot())
      }

      it("should return the pre-computed value with headsMultipleRoot") {
        assertFalse(headsMultipleRoot.isSingleRoot())
      }

      it("should return the expect number of roots with headsSingleRoot") {
        assertEquals(1, headsSingleRoot.countRoots())
      }

      it("should return the expect number of roots with headsMultipleRoot") {
        assertEquals(2, headsMultipleRoot.countRoots())
      }

    }

    context("countRoots") {

      val headsSingleRoot: Array<Int?> = arrayOf(2, 2, null, 2, 6, 6, 2, 8, 2, 8, 11, 8)
      val headsMultipleRoot: Array<Int?> = arrayOf(2, 2, null, 2, 6, 6, 2, 8, null, 8, 11, 8)

      it("should return the expect number of roots with headsSingleRoot") {
        assertEquals(1, headsSingleRoot.countRoots())
      }

      it("should return the expect number of roots with headsMultipleRoot") {
        assertEquals(2, headsMultipleRoot.countRoots())
      }

    }

    context("getAncestors") {

      val heads: Array<Int?> = arrayOf(2, 2, null, 2, 6, 6, 2, 8, 2, 8, 11, 8)

      it("should return the pre-computed values") {
        assertEquals(listOf(11, 8, 2), heads.getAncestors(10))
      }
    }

    context("containsCycle") {

      val heads: Array<Int?> = arrayOf(2, 2, null, 2, 6, 6, 2, 8, 10, 8, 11, 8)

      it("should return the pre-computed values") {
        assertTrue(heads.containsCycle())
      }
    }


    context("isNonProjectiveArc") {

      // You cannot put flavor into   a   bean that is not already there
      //  0    1     2    3      4    5    6    7    8  9   10     11
      //  2    2    -1    2      6    6    2    8    2  8   11     8

      val heads: Array<Int?> = arrayOf(2, 2, null, 2, 6, 6, 2, 8, 3, 8, 11, 8)

      it("should return false at index 0") {
        assertFalse(heads.isNonProjectiveArc(0))
      }

      it("should return false at index 1") {
        assertFalse(heads.isNonProjectiveArc(1))
      }

      it("should return false at index 2") {
        assertFalse(heads.isNonProjectiveArc(2))
      }

      it("should return false at index 3") {
        assertFalse(heads.isNonProjectiveArc(3))
      }

      it("should return false at index 4") {
        assertFalse(heads.isNonProjectiveArc(4))
      }

      it("should return false at index 5") {
        assertFalse(heads.isNonProjectiveArc(5))
      }

      it("should return false at index 6") {
        assertFalse(heads.isNonProjectiveArc(6))
      }

      it("should return false at index 7") {
        assertFalse(heads.isNonProjectiveArc(7))
      }

      it("should return false at index 8") {
        assertTrue(heads.isNonProjectiveArc(8))
      }

      it("should return false at index 9") {
        assertFalse(heads.isNonProjectiveArc(9))
      }

      it("should return false at index 10") {
        assertFalse(heads.isNonProjectiveArc(10))
      }

      it("should return false at index 11") {
        assertFalse(heads.isNonProjectiveArc(11))
      }
    }

    context("isNonProjectiveTree") {

      // You cannot put flavor into   a   bean that is not already there
      //  0    1     2    3      4    5    6    7    8  9   10     11
      //  2    2    -1    2      6    6    2    8    2  8   11     8

      val headsNonProjective: Array<Int?> = arrayOf(2, 2, null, 2, 6, 6, 2, 8, 3, 8, 11, 8)
      val headsProjective: Array<Int?> = arrayOf(2, 2, null, 2, 6, 6, 2, 8, 2, 8, 11, 8)

      it("should return the pre-computed values on headsNonProjective") {
        assertTrue(headsNonProjective.isNonProjectiveTree())
      }

      it("should return the pre-computed values on headsProjective") {
        assertFalse(headsProjective.isNonProjectiveTree())
      }
    }
  }
})
