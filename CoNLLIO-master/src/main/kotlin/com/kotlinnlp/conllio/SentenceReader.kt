/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.conllio

import com.kotlinnlp.linguisticdescription.POSTag
import com.kotlinnlp.linguisticdescription.syntax.SyntacticDependency

/**
 * The SentenceReader.
 *
 * @param lines the CoNLL-style lines to parse
 */
class SentenceReader(private val lines: ArrayList<Pair<Int, String>>) {

  private companion object {

    /**
     * Token/word are indexed with integers like 1, 2, 3 ...
     */
    val lineStartWithTokenId = Regex("^[0-9]+[\t]+")

    /**
     * Multi-word tokens are indexed with integer ranges like 1-2 or 3-5.
     */
    val lineStartWithTokensRange = Regex("^[0-9]+-[0-9]+[\t]+")

    /**
     * Empty nodes are indexed like i.1, i.2, etc.
     * The the numbers after the decimal point must form a sequence starting at 1.
     */
    val lineStartWithEmptyNodeId = Regex("^[0-9]+.[0-9]+[\t]+")

    /**
     * Form of the features names
     */
    val featuresNamesForm = Regex("[A-Z0-9][A-Z0-9a-z]*([[a-z0-9]+])?")

    /**
     * Form of the features values
     */
    val featureValuesForm = Regex("[A-Z0-9][a-zA-Z0-9]*")

    /**
     * @return true if the string is a multi-word tokens line
     */
    fun String.isMultiWordTokensLine(): Boolean = lineStartWithTokensRange.containsMatchIn(this)

    /**
     * @return true if the string is an empty node line
     */
    fun String.isEmptyNodeLine(): Boolean = lineStartWithEmptyNodeId.containsMatchIn(this)

    /**
     * @return true if the string is a token line
     */
    fun String.isTokenLine(): Boolean = lineStartWithTokenId.containsMatchIn(this)

    /**
     * @return true if the string is a comment line
     */
    fun String.isCommentLine(): Boolean = this.isNotEmpty() && this[0] == '#'
  }

  /**
   * Exception in case of invalid CoNLL line.
   *
   * @param lineIndex the index of the invalid line
   * @param line the string line
   */
  class InvalidLine(lineIndex: Int, line: String) : Throwable("Invalid line [$lineIndex]: $line")

  /**
   * Current index of the line which is being read.
   */
  private var lineIndex: Int = 0

  /**
   * Current line which is being read.
   */
  private val curLine: Pair<Int, String> get() = this.lines[this.lineIndex]

  /**
   * A Map that might contain "sent_id" and "text" and other sentence information collected from the comment lines.
   */
  private val sentenceInfo = mutableMapOf<String, String>()

  /**
   * List of [Token]s populated as they are read.
   */
  private val tokens = ArrayList<Token>()

  /**
   * Read the [lines] in the CoNLL format to build a [Sentence].
   *
   * During reading it increments the [lineIndex] variable.
   *
   * This function supports the following lines format:
   * - comment lines
   * - token lines
   * - multi-word tokens lines
   * - empty node lines
   *
   * @return a new Sentence
   */
  fun readSentence(): Sentence {

    this.reset()

    while (this.lineIndex < this.lines.size) {

      val (_, body) = this.curLine

      when {
        body.isCommentLine() && this.tokens.isEmpty() -> this.readComment()
        body.isMultiWordTokensLine() -> this.readMultiWordTokens()
        body.isTokenLine() -> this.readSingleToken()
        body.isEmptyNodeLine() -> this.readEmptyNode()
        else -> throw InvalidLine(lineIndex = this.lineIndex, line = body)
      }

      this.lineIndex++
    }

    return Sentence(
      sentenceId = this.sentenceInfo.getOrDefault("sent_id", ""),
      text = this.sentenceInfo.getOrDefault("text", ""),
      tokens = this.tokens)
  }

  /**
   *
   * In the CoNLL-U format, a word/token line contains the annotations in 10 fields separated by single tab characters:
   *
   *   ID: Word index, integer starting at 1 for each new sentence.
   *   FORM: Word form or punctuation symbol.
   *   LEMMA: Lemma or stem of word form.
   *   UPOSTAG: Universal part-of-speech tag.
   *   XPOSTAG: Language-specific part-of-speech tag; underscore if not available.
   *   FEATS: List of morphological features from the universal feature inventory
   *          or from a defined language-specific extension; underscore if not available.
   *   HEAD: Head of the current word, which is either a value of ID or zero (0).
   *   DEPREL: Universal dependency relation to the HEAD (root iff HEAD = 0)
   *           or a defined language-specific subtype of one.
   *   DEPS: Enhanced dependency graph in the form of a list of head-deprel pairs.
   *   MISC: Any other annotation.
   *
   * As in principle it reads generic CoNLL-style, the labels UPOSTAG and XPOSTAG have been renamed in "pos" and "pos2".
   *
   * The fields must additionally meet the following constraints:
   * - Fields must not be empty.
   * - Fields other than FORM and LEMMA must not contain space characters.
   * - Underscore (_) is used to denote unspecified values in all fields except ID.
   *
   * Note: in this implementation we ignore the fields DEPS and MISC.
   *
   * @param i a line number
   * @param body a line content
   * @param multiWord the [MultiWord] in case of multi-word tokens
   *
   * @return a new Token
   */
  private fun buildToken(i: Int, body: String, multiWord: MultiWord? = null): Token {

    val fields: List<String> = body.split('\t')

    require(fields.size == 10) { "Invalid content at line $i:\n$body" }

    require(fields.slice(3 until 10).none { it.contains(" ") }) {
      "Fields other than FORM and LEMMA must not contain space characters.\nLine $i: $body\n"
    }

    val id: Int = fields[0].toInt()
    val head: Int? = if (fields[6] == Token.EMPTY_FILLER) null else fields[6].toInt()
    val direction: SyntacticDependency.Direction = this.buildDirection(id = id, head = head)

    return Token(
      id = id,
      form = fields[1].trim(),
      lemma = fields[2].trim(),
      posList = fields[3].split(Token.COMPONENTS_SEP).map { POSTag(it) },
      pos2List = fields[4].split(Token.COMPONENTS_SEP).map { POSTag(it) },
      feats = this.extractFeatures(fields[5]),
      head = head,
      syntacticDependencies = fields[7].split(Token.COMPONENTS_SEP).map {
        SyntacticDependency(annotation = it, direction = direction)
      },
      multiWord = multiWord,
      lineNumber = i
    )
  }

  /**
   * Build the syntactic dependency direction of a token.
   * Note: the ID follows the incremental order of the CoNLL sentence tokens.
   *
   * @param id the id of the token
   * @param head the head of the token
   *
   * @return the syntactic dependency direction of the token
   */
  private fun buildDirection(id: Int, head: Int?): SyntacticDependency.Direction = when {
    head == null -> SyntacticDependency.Direction.NULL
    head == 0 -> SyntacticDependency.Direction.ROOT
    head > id -> SyntacticDependency.Direction.LEFT
    else -> SyntacticDependency.Direction.RIGHT
  }

  /**
   * Reset the [SentenceReader]
   */
  private fun reset() {
    this.lineIndex = 0
    this.tokens.clear()
    this.sentenceInfo.clear()
  }

  /**
   * Read a comment line and add its content to the sentenceInfo.
   *
   * TODO: support not key-value comments
   */
  private fun readComment() {

    val (_, body) = this.curLine

    if (body.contains("=")) {
      val (key, value) = body.removePrefix("#").extractKeyValue('=')
      this.sentenceInfo[key] = value
    }
  }

  /**
   * Extract a range from a multi-word tokens index.
   * Multi-word tokens are indexed with integer ranges like 1-2 or 3-5.
   *
   * @return the extracted range
   */
  private fun extractMultiWordRange(str: String): IntRange {

    val (firstTokenId, lastTokenId) = str.extractKeyValue('-').toIntPair()

    require(firstTokenId < lastTokenId) { "Required first token Id < last token id" }
    require(firstTokenId == this.tokens.size + 1) { "Required consecutive ids."}

    return firstTokenId .. lastTokenId
  }

  /**
   * The multi-word token line contains an Id-range and a FORM value – the string that occurs in the sentence –
   * but have an underscore in all the remaining fields except MISC.
   *
   * @return <Id-range, FORM> Pair
   */
  private fun readMultiWordInfo(): MultiWord {
    val (_, body) = this.curLine
    val splitBody = body.split('\t')
    return MultiWord(form = splitBody[1], range = this.extractMultiWordRange(splitBody[0]))
  }

  /**
   * Read a sequence of [Token]s on the basis of a multi-word range.
   * It expects that the [curLine] is a multi-word tokens line; from this line
   * it reads the ids range and the multi-word tokens form, here "multiWordForm".
   *
   * Set at each token of the range the "multiWordRange" property.
   * Set at the first token of the range the "multiWordForm" property.
   */
  private fun readMultiWordTokens() {

    val multiWord: MultiWord = this.readMultiWordInfo()

    multiWord.range.forEach {

      this.lineIndex++

      require(this.curLine.second.isTokenLine()) { "Expected Token at line $lineIndex" }

      this.addToken(this.buildToken(
        i = this.curLine.first,
        body = this.curLine.second,
        multiWord = multiWord))
    }
  }

  /**
   * Read a single [Token]
   *
   * @return the last inserted Token
   */
  private fun readSingleToken(): Token {
    val token: Token = this.buildToken(i = this.curLine.first, body = this.curLine.second)
    this.addToken(token)
    return token
  }

  /**
   * Add the input [token] to the token list
   *
   * Constraint: the token id starts at 1 for each new sentence
   *
   * @param token the token to add to the token list [tokens]
   */
  private fun addToken(token: Token) {

    if (this.tokens.isEmpty()) {

      if (token.id != 1)
        throw Token.InvalidTokenId("[Line ${token.lineNumber}] The first token of the sentence must have ID 1")

    } else {

      if (this.tokens.last().id != token.id - 1)
        throw Token.InvalidTokenId("[Line ${token.lineNumber}] Not incremental IDs")
    }

    this.tokens.add(token)
  }

  /**
   * Read an Empty Node (ellipsis, traces, ..)
   */
  private fun readEmptyNode() {
    // TODO: not yet implemented
  }

  /**
   * @param feats a list of morphological features, with vertical bar (|) as list separator and with underscore
   * to represent the empty list. All features should be represented as attribute-value pairs, with an equals sign (=)
   * separating the attribute from the value.
   *
   * @return a <feature-name, feature-value> Map
   */
  private fun extractFeatures(feats: String): Map<String, String> {

    val result = mutableMapOf<String, String>()

    if (feats != Token.EMPTY_FILLER) {
      feats
        .split("|")
        .map { it.extractKeyValue('=') }
        .forEach { (featureName, featureValue) ->
          require(featuresNamesForm.containsMatchIn(featureName)) { "$featureName is an invalid feature name" }
          require(featureValuesForm.containsMatchIn(featureValue)) { "$featureValue is an invalid feature value" }
          result[featureName] = featureValue
        }
    }

    return result
  }
}
