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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;


public class StringsLineMarkerXml extends RelatedItemLineMarkerProvider {
	@Override
	public void collectNavigationMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<? super RelatedItemLineMarkerInfo> result, boolean forNavigation) {
		for(PsiElement psiElement : elements) {
			if (psiElement instanceof XmlAttribute) {
				LanguageIdHelper languageIdHelper = LanguageIdHelper.getInstance();

				XmlAttribute xmlAttribute = (XmlAttribute)psiElement;
				XmlAttributeValue value = xmlAttribute.getValueElement();
				if (value == null) {
					continue;
				}

				TextRange range = value.getTextRange();
				if (range.getLength() == 0) {
					continue;
				}

				String textId = value.getValue();
				if (textId == null) {
					continue;
				}

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
