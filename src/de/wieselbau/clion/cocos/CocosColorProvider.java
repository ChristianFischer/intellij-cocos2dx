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

import com.intellij.openapi.editor.ElementColorProvider;
import com.intellij.psi.PsiElement;
import com.jetbrains.cidr.lang.psi.OCCallExpression;
import com.jetbrains.cidr.lang.psi.OCDeclarator;
import com.jetbrains.cidr.lang.psi.OCLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;


public class CocosColorProvider implements ElementColorProvider {
	@Nullable
	@Override
	public Color getColorFrom(@NotNull PsiElement psiElement) {
		OCLiteralExpression[] args = getColorArguments(psiElement);

		if (args != null) {
			return createColor(args);
		}

		return null;
	}

	@Override
	public void setColorTo(@NotNull PsiElement psiElement, @NotNull Color color) {
		OCLiteralExpression[] args = getColorArguments(psiElement);

		if (args != null) {
			CocosPsiHelper.setInt(args[0], color.getRed());
			CocosPsiHelper.setInt(args[1], color.getGreen());
			CocosPsiHelper.setInt(args[2], color.getBlue());

			if (args.length >= 4) {
				CocosPsiHelper.setInt(args[3], color.getAlpha());
			}
		}
	}


	/**
	 * Extracts attributes of a color expression from a given PSI element.
	 * This will only return a result, if the call contains a pure list of literal values
	 * and is not built via variable references or function calls.
	 * @param psiElement A psi element which may contain a color value.
	 * @return A list of literal expressions building a color value or {@code null},
	 * if no color can be built.
	 * @see #createColor(OCLiteralExpression[])
	 */
	private OCLiteralExpression[] getColorArguments(@NotNull PsiElement psiElement) {
		if (psiElement instanceof OCCallExpression) {
			OCCallExpression callExpr = (OCCallExpression) psiElement;

			if (CocosPsiHelper.getTypeOf(callExpr.getType()) == CocosType.Color) {
				return CocosPsiHelper.parseLiteralArgumentList(callExpr.getArguments());
			}
		}

		if (psiElement instanceof OCDeclarator) {
			OCDeclarator declaratorExpr = (OCDeclarator) psiElement;

			if (CocosPsiHelper.getTypeOf(declaratorExpr.getType()) == CocosType.Color) {
				return CocosPsiHelper.parseLiteralArgumentList(declaratorExpr.getArgumentList());
			}
		}

		return null;
	}


	/**
	 * Creates a color object from a list of numeric expressions
	 * @param args A list of numeric expressions
	 * @see #getColorArguments(PsiElement)
	 */
	private @Nullable Color createColor(@NotNull OCLiteralExpression[] args) {
		if (args.length == 3 || args.length == 4) {
			OCLiteralExpression rExpr = args[0];
			OCLiteralExpression gExpr = args[1];
			OCLiteralExpression bExpr = args[2];
			OCLiteralExpression aExpr = args.length == 4 ? args[3] : null;

			Color color = new Color(
					CocosPsiHelper.expr2number(rExpr, 0),
					CocosPsiHelper.expr2number(gExpr, 0),
					CocosPsiHelper.expr2number(bExpr, 0),
					CocosPsiHelper.expr2number(aExpr, 255)
			);

			return color;
		}

		return null;
	}
}

