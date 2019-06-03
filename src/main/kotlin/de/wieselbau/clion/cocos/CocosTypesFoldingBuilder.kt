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

package de.wieselbau.clion.cocos

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.cidr.lang.psi.*
import de.wieselbau.clion.cocos.util.FoldingBuilderBase

import java.util.ArrayList


/**
 * Folds declarations of some simple types of cocos2dx like Color- or Vector-types
 */
class CocosTypesFoldingBuilder : FoldingBuilderBase() {
	override fun collectFoldRegions(astNode: ASTNode, document: Document): List<FoldingDescriptor>? {
		val descriptors = ArrayList<FoldingDescriptor>()

		for(callExpr in PsiTreeUtil.findChildrenOfType(astNode.psi, OCCallExpression::class.java)) {
			val type = CocosPsiHelper.getTypeOf(callExpr.resolvedType) ?: continue

			val arguments = CocosPsiHelper.parseLiteralArgumentList(callExpr.argumentList) ?: continue

			val placeholder = CocosPsiHelper.formatArguments(type, arguments) ?: continue

			val range = callExpr.textRange
			if (range.length <= 0) {
				continue
			}

			val descriptor = object : FoldingDescriptor(
					callExpr.node,
					range,
					FoldingGroup.newGroup(callExpr.text)
			) {
				override fun getPlaceholderText(): String? {
					return placeholder
				}
			}

			descriptors.add(descriptor)
		}

		for(declExpr in PsiTreeUtil.findChildrenOfType(astNode.psi, OCDeclarator::class.java)) {
			var arguments: Array<OCLiteralExpression>? = null
			var range: TextRange? = null
			var node: ASTNode? = null

			val type = CocosPsiHelper.getTypeOf(declExpr.type) ?: continue

			run {
				val argumentList = declExpr.argumentList
				if (argumentList != null) {
					arguments = CocosPsiHelper.parseLiteralArgumentList(argumentList)
					node = argumentList.node
					range = TextRange(
							argumentList.textRange.startOffset + 1,
							argumentList.textRange.endOffset - 1
					)
				}

				val initializerList = declExpr.initializerList
				if (initializerList != null) {
					arguments = CocosPsiHelper.parseLiteralArgumentList(initializerList)
					node = initializerList.node
					range = TextRange(
							initializerList.textRange.startOffset + 1,
							initializerList.textRange.endOffset - 1
					)
				}
			}

			if (arguments == null) {
				continue
			}

			val placeholder = CocosPsiHelper.formatArguments(type, arguments!!) ?: continue

			if (range!!.length <= 0) {
				continue
			}

			val descriptor = object : FoldingDescriptor(
					node!!,
					range!!,
					FoldingGroup.newGroup(node!!.text)
			) {
				override fun getPlaceholderText(): String? {
					return placeholder
				}
			}

			descriptors.add(descriptor)
		}

		return descriptors
	}
}
