/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.conllio.utils

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Execute the command contained on this string.
 */
fun String.runAsCommand(workingDir: File = File(".")): String? {
  try {
    val parts = this.split("\\s".toRegex())

    val proc = ProcessBuilder(*parts.toTypedArray())
      .directory(workingDir)
      .redirectOutput(ProcessBuilder.Redirect.PIPE)
      .redirectError(ProcessBuilder.Redirect.PIPE)
      .start()

    proc.waitFor(60, TimeUnit.MINUTES)

    return proc.inputStream.bufferedReader().readText()

  } catch(e: IOException) {
    e.printStackTrace()
    return null
  }
}