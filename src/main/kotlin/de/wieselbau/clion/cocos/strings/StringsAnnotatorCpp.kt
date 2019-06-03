/*
 * Copyright (C) 2019
 * Christian Fischer
 *
 * cocos2dx Support
 *
 * This plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 */

package de.wieselbau.clion.cocos.strings

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.psi.PsiElement
import com.jetbrains.cidr.lang.psi.OCLiteralExpression
import de.wieselbau.clion.cocos.CocosPsiHelper


class StringsAnnotatorCpp : Annotator {
	override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
		if (psiElement is OCLiteralExpression) {
			if (CocosPsiHelper.isCharArray(psiElement.resolvedType)) {
				val range = psiElement.textRange
				if (range.length == 0) {
					return
				}

				val textId = psiElement.unescapedLiteralText
				val textReplacement = LanguageIdHelper.findTextId(textId)

				if (textReplacement != null) {
					val annotation = annotationHolder.createInfoAnnotation(
							psiElement,
							textReplacement
					)

					annotation.setTextAttributes(DefaultLanguageHighlighterColors.CONSTANT)
				}
			}
		}
	}
}
