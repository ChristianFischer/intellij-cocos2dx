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

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.cidr.lang.psi.*;
import de.wieselbau.clion.cocos.util.FoldingBuilderBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * Folds declarations of some simple types of cocos2dx like Color- or Vector-types
 */
public class CocosTypesFoldingBuilder extends FoldingBuilderBase {
	@Override
	protected @Nullable List<FoldingDescriptor> collectFoldRegions(@NotNull ASTNode astNode, @NotNull Document document) {
		List<FoldingDescriptor> descriptors = new ArrayList<>();

		for (OCCallExpression callExpr : PsiTreeUtil.findChildrenOfType(astNode.getPsi(), OCCallExpression.class)) {
			CocosType type = CocosPsiHelper.getTypeOf(callExpr.getType());
			if (type == null) {
				continue;
			}

			OCLiteralExpression[] arguments = CocosPsiHelper.parseLiteralArgumentList(callExpr.getArgumentList());
			if (arguments == null) {
				continue;
			}

			final String placeholder = CocosPsiHelper.formatArguments(type, arguments);
			if (placeholder == null) {
				continue;
			}

			TextRange range = callExpr.getTextRange();
			if (range.getLength() <= 0) {
				continue;
			}

			FoldingDescriptor descriptor = new FoldingDescriptor(
					callExpr.getNode(),
					range,
					FoldingGroup.newGroup(callExpr.getText())
			) {
				@Override
				public String getPlaceholderText() {
					return placeholder;
				}
			};

			descriptors.add(descriptor);
		}

		for (OCDeclarator declExpr : PsiTreeUtil.findChildrenOfType(astNode.getPsi(), OCDeclarator.class)) {
			OCLiteralExpression[] arguments = null;
			TextRange range = null;
			ASTNode node = null;

			CocosType type = CocosPsiHelper.getTypeOf(declExpr.getType());
			if (type == null) {
				continue;
			}

			{
				OCArgumentList argumentList = declExpr.getArgumentList();
				if (argumentList != null) {
					arguments = CocosPsiHelper.parseLiteralArgumentList(argumentList);
					node = argumentList.getNode();
					range = new TextRange(
							argumentList.getTextRange().getStartOffset() + 1,
							argumentList.getTextRange().getEndOffset() - 1
					);
				}

				OCCompoundInitializer initializerList = declExpr.getInitializerList();
				if (initializerList != null) {
					arguments = CocosPsiHelper.parseLiteralArgumentList(initializerList);
					node = initializerList.getNode();
					range = new TextRange(
							initializerList.getTextRange().getStartOffset() + 1,
							initializerList.getTextRange().getEndOffset() - 1
					);
				}
			}

			if (arguments == null) {
				continue;
			}

			final String placeholder = CocosPsiHelper.formatArguments(type, arguments);
			if (placeholder == null) {
				continue;
			}

			if (range.getLength() <= 0) {
				continue;
			}

			FoldingDescriptor descriptor = new FoldingDescriptor(
					node,
					range,
					FoldingGroup.newGroup(node.getText())
			) {
				@Override
				public String getPlaceholderText() {
					return placeholder;
				}
			};

			descriptors.add(descriptor);
		}

		return descriptors;
	}
}
