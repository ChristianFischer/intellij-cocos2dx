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

package de.wieselbau.clion.cocos.strings

import com.intellij.openapi.fileTypes.LanguageFileType

import javax.swing.*


class StringsFileType protected constructor() : LanguageFileType(StringsLanguage.INSTANCE) {

	override fun getName(): String {
		return "Strings File"
	}

	override fun getDescription(): String {
		return "Strings File"
	}

	override fun getDefaultExtension(): String {
		return "strings"
	}

	override fun getIcon(): Icon? {
		return Icons.FILE_TYPE
	}

	companion object {
		val INSTANCE = StringsFileType()
	}
}
