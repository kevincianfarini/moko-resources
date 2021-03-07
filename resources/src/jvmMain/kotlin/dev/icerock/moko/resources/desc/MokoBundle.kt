/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import java.text.ChoiceFormat
import java.text.Format
import java.text.MessageFormat
import java.util.ResourceBundle
import java.util.Locale

object MokoBundle {
    private const val BUNDLE_NAME = "localization.MokoBundle"
    private const val PLURALS_BUNDLE_NAME = "localization.MokoPluralsBundle"

    fun getString(key: String, args: List<Any>? = null) =
        with(ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString(key)) {
            if (args == null) {
                this
            } else {
                MessageFormat.format(this, args)
            }
        }

    /**
     * This method works under the assumption, that the {0} argument is always the plural argument
     * all others are simple arguments
     * For ex: Lorem {1} ipsum {0} -> Is the correct format, if the second argument should be
     * pluralized
     */
    fun getPluralString(
        key: String,
        number: Int,
        numberFormat: List<Pair<Double, String>>,
        args: List<Any> = emptyList()
    ): String = with(
        ResourceBundle.getBundle(PLURALS_BUNDLE_NAME, Locale.getDefault())
    ) {
        val messageFormat = MessageFormat("").apply { locale = Locale.getDefault() }

        val (limits, strings) = numberFormat.map { it.first } to numberFormat.map { getString(it.second) }
        val choiceFormat = ChoiceFormat(limits.toDoubleArray(), strings.toTypedArray())

        messageFormat.apply {
            applyPattern(getString(key))
            formats = arrayOf<Format?>(choiceFormat) + args.map { null }.toTypedArray<Format?>()
        }
        val messageArguments = args.toMutableList().apply { add(0, number) }.toTypedArray()

        return messageFormat.format(messageArguments)
    }
}