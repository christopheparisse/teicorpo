/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.conllio

/**
 * Extract a key-value pair from a String, using a [separator] to separate the key from the value.
 *
 * @param separator the separator used to separate the key from the value
 *
 * @return a <key, value> pair
 */
internal fun String.extractKeyValue(separator: Char): Pair<String, String> {
  require(this.contains(separator))
  return Pair(this.substringBefore(separator).trim(), this.substringAfter(separator).trim())
}

/**
 * @return a <Int, Int> Pair
 */
internal fun Pair<String, String>.toIntPair() = Pair(this.first.toInt(), this.second.toInt())
