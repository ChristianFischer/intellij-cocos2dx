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

package de.wieselbau.clion.cocos.util;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for all folding builders within this plugin
 * and provides some default configuration.
 */
public abstract class FoldingBuilderBase implements FoldingBuilder {
	/**
	 * Overridable function to collect all folding regions.
	 * @see FoldingBuilder#buildFoldRegions(ASTNode, Document)
	 */
	protected abstract @Nullable List<FoldingDescriptor> collectFoldRegions(@NotNull ASTNode astNode, @NotNull Document document);

	@NotNull
	@Override
	public final FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode astNode, @NotNull Document document) {
		try {
			List<FoldingDescriptor> descriptors = collectFoldRegions(astNode, document);

			if (descriptors != null) {
				return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
			}
		}
		catch(ProcessCanceledException e) {
			throw e;
		}
		catch(Throwable t) {
			t.printStackTrace();
		}

		return FoldingDescriptor.EMPTY;
	}

	@Nullable
	@Override
	public String getPlaceholderText(@NotNull ASTNode astNode) {
		return "...";
	}

	@Override
	public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
		return true;
	}
}
