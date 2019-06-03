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

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.cidr.lang.psi.OCLiteralExpression
import de.wieselbau.clion.cocos.CocosPsiHelper
import de.wieselbau.clion.cocos.strings.LanguageIdHelper.quote
import de.wieselbau.clion.cocos.strings.LanguageIdHelper.shorten
import de.wieselbau.clion.cocos.util.FoldingBuilderBase
import java.util.ArrayList


class StringsFoldingBuilderCpp : FoldingBuilderBase() {
	override fun collectFoldRegions(astNode: ASTNode, document: Document): List<FoldingDescriptor>? {
		val project = astNode.psi.project
		LanguageIdHelper.updateCache(project)
		val descriptors = ArrayList<FoldingDescriptor>()

		for(literalExpression in PsiTreeUtil.findChildrenOfType(astNode.psi, OCLiteralExpression::class.java)) {
			if (CocosPsiHelper.isCharArray(literalExpression.resolvedType)) {
				val elementToBeReplaced = CocosPsiHelper.resolveMacroCall(literalExpression)
				val range = elementToBeReplaced.textRange
				if (range.length == 0) {
					continue
				}

				val textId = literalExpression.unescapedLiteralText
				var textReplacement = LanguageIdHelper.findTextId(textId)

				if (textReplacement != null) {
					textReplacement = shorten(textReplacement)
					textReplacement = quote(textReplacement)
					val placeholderText = textReplacement

					val descriptor = object : FoldingDescriptor(
							elementToBeReplaced.node,
							range,
							FoldingGroup.newGroup(textId)
					) {
						override fun getPlaceholderText(): String? {
							return placeholderText
						}
					}

					descriptors.add(descriptor)
				}
			}
		}

		return descriptors
	}
}
