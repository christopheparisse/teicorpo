/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.conllio

/**
 * @property form the original form that occurs in the sentence in case of multi-word tokens
 * @property range the id-range to which this token belongs in case of multi-word tokens.
 *                 Multi-word tokens are indexed with integer ranges like 1-2 or 3-5
 */
data class MultiWord(val form: String, val range: IntRange){

  /**
   * @return a string of the format first-last id (e.g. 1-2 or 3-5)
   */
  fun getCoNLLRange(): String = "${this.range.first}-${this.range.last}"
}