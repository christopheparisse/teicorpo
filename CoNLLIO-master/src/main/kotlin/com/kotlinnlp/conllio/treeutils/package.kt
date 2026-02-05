/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.conllio.treeutils

/**
 * This package contains convenient methods to navigate a tree.
 *
 * A tree is represented by a simple Array of Integer, where the indexes are the node-id and the values the heads.
 * The root nodes have heads = null.
 */

/**
 * @param id the id of a node of the tree
 *
 * @returns all words going from the id-node up the path to the root
 */
fun Array<Int?>.getAncestors(id: Int): List<Int> {

  val ancestors = mutableListOf<Int>()
  var head: Int? = id
  var i = 0

  while (this[head!!] != head && i++ < this.size) {

    head = this[head]

    if (head == null)
      break
    else
      ancestors.add(head)
  }

  return ancestors
}

/**
  * @return True if the tree contains cycles.
  *
  * Implementation note: in an acyclic tree, the path from each word following the head relation
  * upwards always ends at the root node.
  */
fun Array<Int?>.containsCycle(): Boolean {

  this.indices.forEach { id ->

    val seen = mutableListOf<Int?>(id)

    this.getAncestors(id).forEach { ancestor ->
      if (ancestor in seen) {
        return true
      } else {
        seen.add(ancestor)
      }
    }
  }

  return false
}

/***
 * @return True if all the nodes on the tree are not-null and in the range [0, last-id]
 */
fun Array<Int?>.checkHeadsBoundaries(): Boolean = this.indices.none {
  this[it] != null && (this[it]!! < 0 || this[it]!! > this.lastIndex)
}

/**
 * @return True if the array represents a directed acyclic graph.
 */
fun Array<Int?>.isTree(): Boolean {

  if (!this.checkHeadsBoundaries()) return false

  val h = arrayOfNulls<Int>(this.size)

  this.indices.forEach { i ->

    var k = i

    loop@ while (k > 0) when {

      h[k] == i -> return false // not a tree

      h[k] in 0..(i - 1) -> break@loop

      this[k] == null -> break@loop

      else -> {
        h[k] = i
        k = this[k]!!
      }
    }
  }

  return !this.containsCycle()
}

/**
 * @param id the id of a node of the tree.
 * @return True if the id-node is involved in a non-projective arc.
 *
 * Implementation note: an arc h -> d, h < d is non-projective if there is a token k, h < k < d
 * such that h is not an ancestor of k. Same for h -> d, h > d.
 */
fun Array<Int?>.isNonProjectiveArc(id: Int): Boolean {

  val head = this[id]

  if (head == id || head == null) {

    return false

  } else {

    val range = if (head < id)
      IntRange(head + 1, id - 1)
    else
      IntRange(id + 1, head - 1)

    return range.any { k -> this.getAncestors(k).none { it == head } }
  }
}

/**
 * @return True is the tree contains at least one non-projective arc.
 */
fun Array<Int?>.isNonProjectiveTree(): Boolean = this.indices.any { this.isNonProjectiveArc(it) }

/**
 * @return the number of roots.
 */
fun Array<Int?>.countRoots(): Int = this.count { it == null }

/**
 * @return True if the tree is single root.
 */
fun Array<Int?>.isSingleRoot(): Boolean = this.countRoots() == 1

