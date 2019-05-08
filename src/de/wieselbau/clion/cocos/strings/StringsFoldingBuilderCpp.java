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

package de.wieselbau.clion.cocos.strings;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.cidr.lang.psi.OCLiteralExpression;
import de.wieselbau.clion.cocos.CocosPsiHelper;
import de.wieselbau.clion.cocos.util.FoldingBuilderBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static de.wieselbau.clion.cocos.strings.LanguageIdHelper.quote;
import static de.wieselbau.clion.cocos.strings.LanguageIdHelper.shorten;


public class StringsFoldingBuilderCpp extends FoldingBuilderBase {
	@Override
	protected @Nullable List<FoldingDescriptor> collectFoldRegions(@NotNull ASTNode astNode, @NotNull Document document) {
		Project project = astNode.getPsi().getProject();
		LanguageIdHelper languageIdHelper = LanguageIdHelper.getInstance();
		languageIdHelper.updateCache(project);
		List<FoldingDescriptor> descriptors = new ArrayList<>();

		for (OCLiteralExpression literalExpression : PsiTreeUtil.findChildrenOfType(astNode.getPsi(), OCLiteralExpression.class)) {
			if (CocosPsiHelper.isCharArray(literalExpression.getResolvedType())) {
				PsiElement elementToBeReplaced = CocosPsiHelper.resolveMacroCall(literalExpression);
				TextRange range = elementToBeReplaced.getTextRange();
				if (range.getLength() == 0) {
					continue;
				}

				String textId = literalExpression.getUnescapedLiteralText();
				String textReplacement = languageIdHelper.findTextId(textId);

				if (textReplacement != null) {
					textReplacement = shorten(textReplacement);
					textReplacement = quote(textReplacement);
					final String placeholderText = textReplacement;

					FoldingDescriptor descriptor = new FoldingDescriptor(
							elementToBeReplaced.getNode(),
							range,
							FoldingGroup.newGroup(textId)
					) {
						@Override
						public String getPlaceholderText() {
							return placeholderText;
						}
					};

					descriptors.add(descriptor);
				}
			}
		}

		return descriptors;
	}
}
