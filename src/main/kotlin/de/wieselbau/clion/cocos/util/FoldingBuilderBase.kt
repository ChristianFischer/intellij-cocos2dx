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

package de.wieselbau.clion.cocos.util

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProcessCanceledException

/**
 * Base class for all folding builders within this plugin
 * and provides some default configuration.
 */
abstract class FoldingBuilderBase : FoldingBuilder {
	/**
	 * Overridable function to collect all folding regions.
	 * @see FoldingBuilder.buildFoldRegions
	 */
	protected abstract fun collectFoldRegions(astNode: ASTNode, document: Document): List<FoldingDescriptor>?

	override fun buildFoldRegions(astNode: ASTNode, document: Document): Array<FoldingDescriptor> {
		try {
			val descriptors = collectFoldRegions(astNode, document)

			if (descriptors != null) {
				return descriptors.toTypedArray()
			}
		}
		catch(e: ProcessCanceledException) {
			throw e
		}
		catch(t: Throwable) {
			t.printStackTrace()
		}

		return FoldingDescriptor.EMPTY
	}

	override fun getPlaceholderText(astNode: ASTNode): String? {
		return "..."
	}

	override fun isCollapsedByDefault(astNode: ASTNode): Boolean {
		return true
	}
}
