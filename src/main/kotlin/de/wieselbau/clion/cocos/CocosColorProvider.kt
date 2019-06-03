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

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.jetbrains.cidr.lang.psi.OCCallExpression
import com.jetbrains.cidr.lang.psi.OCDeclarator
import com.jetbrains.cidr.lang.psi.OCLiteralExpression

import java.awt.*


class CocosColorProvider : ElementColorProvider {
	override fun getColorFrom(psiElement: PsiElement): Color? {
		val args = getColorArguments(psiElement)

		if (args != null) {
			return createColor(args)
		}

		return null
	}

	override fun setColorTo(psiElement: PsiElement, color: Color) {
		val args = getColorArguments(psiElement)

		if (args != null) {
			CocosPsiHelper.setInt(args[0], color.red)
			CocosPsiHelper.setInt(args[1], color.green)
			CocosPsiHelper.setInt(args[2], color.blue)

			if (args.size >= 4) {
				CocosPsiHelper.setInt(args[3], color.alpha)
			}
		}
	}


	/**
	 * Extracts attributes of a color expression from a given PSI element.
	 * This will only return a result, if the call contains a pure list of literal values
	 * and is not built via variable references or function calls.
	 * @param psiElement A psi element which may contain a color value.
	 * @return A list of literal expressions building a color value or `null`,
	 * if no color can be built.
	 * @see .createColor
	 */
	private fun getColorArguments(psiElement: PsiElement): Array<OCLiteralExpression>? {
		if (psiElement is OCCallExpression) {

			if (CocosPsiHelper.getTypeOf(psiElement.resolvedType) == CocosType.Color) {
				return CocosPsiHelper.parseLiteralArgumentList(psiElement.arguments)
			}
		}

		if (psiElement is OCDeclarator) {

			if (CocosPsiHelper.getTypeOf(psiElement.type) == CocosType.Color) {
				return CocosPsiHelper.parseLiteralArgumentList(psiElement)
			}
		}

		return null
	}


	/**
	 * Creates a color object from a list of numeric expressions
	 * @param args A list of numeric expressions
	 * @see .getColorArguments
	 */
	private fun createColor(args: Array<OCLiteralExpression>): Color? {
		if (args.size == 3 || args.size == 4) {
			val rExpr = args[0]
			val gExpr = args[1]
			val bExpr = args[2]
			val aExpr = if (args.size == 4) args[3] else null

			return Color(
					CocosPsiHelper.expr2number(rExpr, 0),
					CocosPsiHelper.expr2number(gExpr, 0),
					CocosPsiHelper.expr2number(bExpr, 0),
					CocosPsiHelper.expr2number(aExpr, 255)
			)
		}

		return null
	}
}

