/*
 * Copyright (C) 2017
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

package de.wieselbau.clion.cocos;

import com.intellij.psi.PsiElement;
import com.jetbrains.cidr.lang.psi.*;
import com.jetbrains.cidr.lang.types.OCArrayType;
import com.jetbrains.cidr.lang.types.OCType;
import com.jetbrains.cidr.lang.util.OCElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class CocosPsiHelper {
	private CocosPsiHelper() {
	}


	/**
	 * Checks a {@code OCType} for any known cocos type.
	 * @param type Any type.
	 * @return A known cocos type, or {@code null} if the type is not known.
	 * @see CocosType
	 */
	public static @Nullable CocosType getTypeOf(@NotNull OCType type) {
		String name = type.getName();

		// color types
		if (
				"Color3B".equals(name)
			||	"Color4B".equals(name)
		) {
			return CocosType.Color;
		}

		// vector types
		if (
				"Vec2".equals(name)
			||	"Vec3".equals(name)
			||	"Vec4".equals(name)
			||	"Point".equals(name)
		) {
			return CocosType.Vector;
		}

		// size types
		if (
				"Size".equals(name)
		) {
			return CocosType.Size;
		}

		return null;
	}


	/**
	 * Checks a given Type, if it matches a char array.
	 * @param type	A type object.
	 * @return {@code true} if the type matches a char array, {@code false} otherwise.
	 */
	public static boolean isCharArray(@NotNull OCType type) {
		if (type instanceof OCArrayType) {
			OCArrayType arrayType = (OCArrayType)type;
			OCType elementType = arrayType.getArrayElementType();

			if (
					"char".equals(elementType.getName())
				||	"const char".equals(elementType.getName())
			) {
				return true;
			}
		}

		return false;
	}


	/**
	 * Finds the next node visible in the editor.
	 * If a given node is inside a macro expansion this function returns the corresponding
	 * macro call node.
	 * @param psiElement	The node to be resolved.
	 * @return Either the original {@param psiElement} or a corresponding macro call node.
	 */
	public static @NotNull PsiElement resolveMacroCall(@NotNull PsiElement psiElement) {
		PsiElement sibling = psiElement.getPrevSibling();
		if (sibling instanceof OCMacroCall) {
			OCMacroCall macroCall = (OCMacroCall)sibling;
			OCReferenceElement macroReference = macroCall.getMacroReferenceElement();
			PsiElement first = macroCall.getFirstExpansionLeaf();
			PsiElement last  = macroCall.getLastExpansionLeaf();

			if (first == last) {
				return macroCall;
			}
		}

		return psiElement;
	}


	/**
	 * Parses a list of {@code OCLiteralExpression} elements from an {@code OCDeclarator}.
	 * This will return a list of {@code OCLiteralExpression}, only if all of it's arguments
	 * can be read as literal expressions.
	 * @param declarator A declarator list.
	 * @return A list of {@code OCLiteralExpression}, or {@code null}
	 */
	public static OCLiteralExpression[] parseLiteralArgumentList(@Nullable OCDeclarator declarator) {
		if (declarator != null) {
			OCArgumentList argumentList = declarator.getArgumentList();
			if (argumentList != null) {
				return parseLiteralArgumentList(argumentList);
			}

			OCCompoundInitializer initializer = declarator.getInitializerList();
			if (initializer != null) {
				return parseLiteralArgumentList(initializer);
			}
		}

		return null;
	}


	/**
	 * Parses a list of {@code OCLiteralExpression} elements from an {@code OCArgumentList}.
	 * This will return a list of {@code OCLiteralExpression}, only if all of it's arguments
	 * can be read as literal expressions.
	 * @param arguments An argument list to be parsed.
	 * @return A list of {@code OCLiteralExpression}, or {@code null}
	 */
	public static OCLiteralExpression[] parseLiteralArgumentList(@Nullable OCArgumentList arguments) {
		if (arguments != null) {
			return parseLiteralArgumentList(arguments.getArguments());
		}

		return null;
	}


	/**
	 * Parses a list of {@code OCLiteralExpression} elements from an {@code OCCompoundInitializer}.
	 * This will return a list of {@code OCLiteralExpression}, only if all of it's arguments
	 * can be read as literal expressions.
	 * @param arguments An argument list to be parsed.
	 * @return A list of {@code OCLiteralExpression}, or {@code null}
	 */
	public static OCLiteralExpression[] parseLiteralArgumentList(@Nullable OCCompoundInitializer arguments) {
		if (arguments != null) {
			return parseLiteralArgumentList(arguments.getInitializerExpressions());
		}

		return null;
	}


	/**
	 * Parses a list of {@code OCLiteralExpression} elements from a list of arguments.
	 * This will return a list of {@code OCLiteralExpression}, only if all of it's arguments
	 * can be read as literal expressions.
	 * @param arguments An argument list to be parsed.
	 * @return A list of {@code OCLiteralExpression}, or {@code null}
	 */
	public static OCLiteralExpression[] parseLiteralArgumentList(@NotNull java.util.List<OCExpression> arguments) {
		OCLiteralExpression[] color_arguments = null;

		for (int i = 0; i < arguments.size(); i++) {
			OCExpression expr = arguments.get(i);

			if (expr instanceof OCLiteralExpression) {
				OCLiteralExpression litExpr = (OCLiteralExpression) expr;
				OCType exprType = litExpr.getType();
				String typeName = exprType.getName();

				if (
						"int".equals(typeName)
					||	"float".equals(typeName)
				) {
					if (color_arguments == null) {
						color_arguments = new OCLiteralExpression[arguments.size()];
					}

					color_arguments[i] = litExpr;
					continue;
				}
			}

			return null;
		}

		return color_arguments;
	}


	/**
	 * Extracts an integer value from a {@link OCLiteralExpression}.
	 * If the value cannot be parsed, {@code defaultValue} will be returned.
	 * @param expression   A literal expression to be parsed.
	 * @param defaultValue A default value to be returned, if {@code expression} cannot be parsed.
	 * @return The value of {@code expression} or {@code defaultValue}.
	 */
	public static int expr2number(@Nullable OCLiteralExpression expression, int defaultValue) {
		if (expression != null) {
			String text = expression.getRawLiteralText();
			return Integer.parseInt(text);
		}

		return defaultValue;
	}


	/**
	 * Replaces an existing expression with a new value.
	 * @param old_expression The expression to be replaced.
	 * @param value          The value to be inserted instead of the original expression.
	 */
	public static void setInt(@Nullable OCLiteralExpression old_expression, int value) {
		if (old_expression != null) {
			String stringValue = Integer.toString(value);
			OCExpression new_expression = OCElementFactory.expressionFromText(stringValue, old_expression);

			if (new_expression != null) {
				old_expression.replace(new_expression);
			}
		}
	}


	/**
	 * Returns a formatted string representing the value created by the expressions list.
	 * @param type        The data type to be created with the given expressions.
	 * @param expressions A list of expressions read from a constructor argument list.
	 * @return A formatted string, or {@code null}, if the string couldn't be formatted.
	 */
	public static @Nullable String formatArguments(@NotNull CocosType type, OCLiteralExpression[] expressions) {
		try {
			switch(type) {
				case Color: {
					StringBuilder sb = new StringBuilder();
					if (expressions.length == 3 || expressions.length == 4) {
						boolean first = true;
						for(OCLiteralExpression expr : expressions) {
							if (expr == null) {
								return null;
							}

							sb.append(first ? '#' : ' ');
							int v = Integer.parseInt(expr.getRawLiteralText());
							String s = Integer.toHexString(v);

							if (v < 16) {
								sb.append('0');
							}

							sb.append(s);
							first = false;
						}
					}

					return sb.toString();
				}

				case Vector: {
					StringBuilder sb = new StringBuilder();
					sb.append('[');

					boolean first = true;
					for(OCLiteralExpression expr : expressions) {
						if (expr == null) {
							return null;
						}

						if (!first) {
							sb.append(';').append(' ');
						}

						float v = Float.parseFloat(expr.getRawLiteralText());
						String s = Float.toString(v);
						sb.append(s);

						first = false;
					}

					sb.append(']');

					return sb.toString();
				}

				case Size: {
					StringBuilder sb = new StringBuilder();
					if (expressions.length == 2) {
						OCLiteralExpression a = expressions[0];
						OCLiteralExpression b = expressions[1];

						if (a == null || b == null) {
							return null;
						}

						Float f1 = Float.parseFloat(a.getRawLiteralText());
						Float f2 = Float.parseFloat(b.getRawLiteralText());

						sb.append(f1);
						sb.append(" \u00D7 ");
						sb.append(f2);
					}

					return sb.toString();
				}
			}
		}
		catch(Throwable t) {
			t.printStackTrace();
		}

		return null;
	}
}

