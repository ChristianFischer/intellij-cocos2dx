/*
 * Copyright (C) 2018
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

package de.wieselbau.clion.cocos.strings;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.cidr.lang.psi.OCLiteralExpression;
import de.wieselbau.clion.cocos.CocosPsiHelper;
import org.jetbrains.annotations.NotNull;


public class StringsAnnotatorCpp implements Annotator {
	@Override
	public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
		if (psiElement instanceof OCLiteralExpression) {
			LanguageIdHelper languageIdHelper = LanguageIdHelper.getInstance();

			OCLiteralExpression literalExpression = (OCLiteralExpression)psiElement;

			if (CocosPsiHelper.isCharArray(literalExpression.getType())) {
				TextRange range = literalExpression.getTextRange();
				if (range.getLength() == 0) {
					return;
				}

				String textId = literalExpression.getUnescapedLiteralText();
				String textReplacement = languageIdHelper.findTextId(textId);

				if (textReplacement != null) {
					Annotation annotation = annotationHolder.createInfoAnnotation(
							psiElement,
							textReplacement
					);

					annotation.setTextAttributes(DefaultLanguageHighlighterColors.CONSTANT);
				}
			}
		}
	}
}
