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

import com.intellij.psi.PsiElement
import com.jetbrains.cidr.lang.psi.*
import com.jetbrains.cidr.lang.types.OCArrayType
import com.jetbrains.cidr.lang.types.OCType
import com.jetbrains.cidr.lang.util.OCElementFactory


object CocosPsiHelper {

	/**
	 * Checks a `OCType` for any known cocos type.
	 * @param type Any type.
	 * @return A known cocos type, or `null` if the type is not known.
	 * @see CocosType
	 */
	fun getTypeOf(type: OCType): CocosType? {
		return when(type.name) {
			"Color3B", "Color4B"   -> CocosType.Color
			"Vec2", "Vec3", "Vec4" -> CocosType.Vector
			"Size"                 -> CocosType.Size
			else                   -> null
		}
	}


	/**
	 * Checks a given Type, if it matches a char array.
	 * @param type    A type object.
	 * @return `true` if the type matches a char array, `false` otherwise.
	 */
	fun isCharArray(type: OCType): Boolean {
		if (type is OCArrayType) {
			when(type.arrayElementType.name) {
				"char", "const char" -> return true
			}
		}

		return false
	}


	/**
	 * Finds the next node visible in the editor.
	 * If a given node is inside a macro expansion this function returns the corresponding
	 * macro call node.
	 * @param psiElement    The node to be resolved.
	 * @return Either the original {@param psiElement} or a corresponding macro call node.
	 */
	fun resolveMacroCall(psiElement: PsiElement): PsiElement {
		val sibling = psiElement.prevSibling
		if (sibling is OCMacroCall) {
			val macroReference = sibling.macroReferenceElement
			val first = sibling.firstExpansionLeaf
			val last = sibling.lastExpansionLeaf

			if (first === last) {
				return sibling
			}
		}

		return psiElement
	}


	/**
	 * Parses a list of `OCLiteralExpression` elements from an `OCDeclarator`.
	 * This will return a list of `OCLiteralExpression`, only if all of it's arguments
	 * can be read as literal expressions.
	 * @param declarator A declarator list.
	 * @return A list of `OCLiteralExpression`, or `null`
	 */
	fun parseLiteralArgumentList(declarator: OCDeclarator?): Array<OCLiteralExpression>? {
		if (declarator != null) {
			val argumentList = declarator.argumentList
			if (argumentList != null) {
				return parseLiteralArgumentList(argumentList)
			}

			val initializer = declarator.initializerList
			if (initializer != null) {
				return parseLiteralArgumentList(initializer)
			}
		}

		return null
	}


	/**
	 * Parses a list of `OCLiteralExpression` elements from an `OCArgumentList`.
	 * This will return a list of `OCLiteralExpression`, only if all of it's arguments
	 * can be read as literal expressions.
	 * @param arguments An argument list to be parsed.
	 * @return A list of `OCLiteralExpression`, or `null`
	 */
	fun parseLiteralArgumentList(arguments: OCArgumentList?): Array<OCLiteralExpression>? {
		if (arguments != null) {
			return parseLiteralArgumentList(arguments.arguments)
		}

		return null
	}


	/**
	 * Parses a list of `OCLiteralExpression` elements from an `OCCompoundInitializer`.
	 * This will return a list of `OCLiteralExpression`, only if all of it's arguments
	 * can be read as literal expressions.
	 * @param arguments An argument list to be parsed.
	 * @return A list of `OCLiteralExpression`, or `null`
	 */
	fun parseLiteralArgumentList(arguments: OCCompoundInitializer?): Array<OCLiteralExpression>? {
		if (arguments != null) {
			return parseLiteralArgumentList(arguments.initializerExpressions)
		}

		return null
	}


	/**
	 * Parses a list of `OCLiteralExpression` elements from a list of arguments.
	 * This will return a list of `OCLiteralExpression`, only if all of it's arguments
	 * can be read as literal expressions.
	 * @param arguments An argument list to be parsed.
	 * @return A list of `OCLiteralExpression`, or `null`
	 */
	fun parseLiteralArgumentList(arguments: List<OCExpression>): Array<OCLiteralExpression>? {
		val color_arguments = ArrayList<OCLiteralExpression>()

		for(expr in arguments) {
			if (expr is OCLiteralExpression) {
				val exprType = expr.resolvedType
				val typeName = exprType.name

				if ("int" == typeName || "float" == typeName) {
					color_arguments.add(expr)
				}
				else {
					return null
				}
			}
		}

		return color_arguments.toTypedArray()
	}


	/**
	 * Extracts an integer value from a [OCLiteralExpression].
	 * If the value cannot be parsed, `defaultValue` will be returned.
	 * @param expression   A literal expression to be parsed.
	 * @param defaultValue A default value to be returned, if `expression` cannot be parsed.
	 * @return The value of `expression` or `defaultValue`.
	 */
	fun expr2number(expression: OCLiteralExpression?, defaultValue: Int): Int {
		if (expression != null) {
			val text = expression.rawLiteralText
			return Integer.parseInt(text)
		}

		return defaultValue
	}


	/**
	 * Replaces an existing expression with a new value.
	 * @param old_expression The expression to be replaced.
	 * @param value          The value to be inserted instead of the original expression.
	 */
	fun setInt(old_expression: OCLiteralExpression?, value: Int) {
		if (old_expression != null) {
			val stringValue = Integer.toString(value)
			val new_expression = OCElementFactory.expressionFromText(stringValue, old_expression)

			if (new_expression != null) {
				old_expression.replace(new_expression)
			}
		}
	}


	/**
	 * Returns a formatted string representing the value created by the expressions list.
	 * @param type        The data type to be created with the given expressions.
	 * @param expressions A list of expressions read from a constructor argument list.
	 * @return A formatted string, or `null`, if the string couldn't be formatted.
	 */
	fun formatArguments(type: CocosType, expressions: Array<OCLiteralExpression>): String? {
		try {
			when(type) {
				CocosType.Color -> {
					val sb = StringBuilder()
					if (expressions.size == 3 || expressions.size == 4) {
						var first = true
						for(expr in expressions) {
							sb.append(if (first) '#' else ' ')
							val v = Integer.parseInt(expr.rawLiteralText)
							val s = Integer.toHexString(v)

							if (v < 16) {
								sb.append('0')
							}

							sb.append(s)
							first = false
						}
					}

					return sb.toString()
				}

				CocosType.Vector -> {
					val sb = StringBuilder()
					sb.append('[')

					var first = true
					for(expr in expressions) {
						if (!first) {
							sb.append(';').append(' ')
						}

						val v = java.lang.Float.parseFloat(expr.rawLiteralText)
						val s = java.lang.Float.toString(v)
						sb.append(s)

						first = false
					}

					sb.append(']')

					return sb.toString()
				}

				CocosType.Size -> {
					val sb = StringBuilder()
					if (expressions.size == 2) {
						val a = expressions[0]
						val b = expressions[1]

						val f1 = java.lang.Float.parseFloat(a.rawLiteralText)
						val f2 = java.lang.Float.parseFloat(b.rawLiteralText)

						sb.append(f1)
						sb.append(" \u00D7 ")
						sb.append(f2)
					}

					return sb.toString()
				}
			}
		}
		catch(t: Throwable) {
			t.printStackTrace()
		}

		return null
	}
}

