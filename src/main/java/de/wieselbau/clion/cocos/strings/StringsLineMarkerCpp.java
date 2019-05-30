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

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.jetbrains.cidr.lang.psi.OCLiteralExpression;
import de.wieselbau.clion.cocos.CocosPsiHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;


public class StringsLineMarkerCpp extends RelatedItemLineMarkerProvider {
	@Override
	public void collectNavigationMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<? super RelatedItemLineMarkerInfo> result, boolean forNavigation) {
		for(PsiElement psiElement : elements) {
			if (psiElement instanceof OCLiteralExpression) {
				LanguageIdHelper languageIdHelper = LanguageIdHelper.getInstance();

				OCLiteralExpression literalExpression = (OCLiteralExpression)psiElement;

				if (CocosPsiHelper.isCharArray(literalExpression.getResolvedType())) {
					String textId = literalExpression.getUnescapedLiteralText();
					String textReplacement = languageIdHelper.findTextId(textId);

					if (textReplacement != null) {
						NavigationGutterIconBuilder<PsiElement> builder =
								NavigationGutterIconBuilder.create(Icons.REFERENCE)
								.setTarget(psiElement)
								.setTooltipText(textReplacement)
						;

						RelatedItemLineMarkerInfo<PsiElement> marker = builder.createLineMarkerInfo(psiElement);

						result.add(marker);
					}
				}
			}
		}
	}
}
