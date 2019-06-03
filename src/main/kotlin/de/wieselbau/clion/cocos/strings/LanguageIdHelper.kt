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

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import java.util.*
import java.util.regex.Pattern


/**
 * Helper class to find language ID's within the current project.
 */
object LanguageIdHelper {
	val MAX_LINE_LENGTH		= 48

	val QUOTE_BEGIN			= "\u201E"
	val QUOTE_END			= "\u201C"
	val ELLIPSIS			= "\u2026"

	val PT_LINE: Pattern	= Pattern.compile("\"(.*)\"\\s+=\\s+\"(.*)\";")


	private val cache = HashMap<VirtualFile, LanguageDirectory>()


	/**
	 * Shortens a given text at the first linebreak or at [.MAX_LINE_LENGTH] characters.
	 * @param text    The text to be shortened.
	 * @return The short version of the original text.
	 */
	fun shorten(text: String): String {
		var text = text
		var was_shortened = false

		val lbr_index = text.indexOf("\\n")
		if (lbr_index != -1) {
			text = text.substring(0, lbr_index - 1)
			was_shortened = true
		}

		if (text.length > MAX_LINE_LENGTH) {
			text = text.substring(0, MAX_LINE_LENGTH)
			was_shortened = true
		}

		if (was_shortened) {
			text += "[$ELLIPSIS]"
		}

		return text
	}


	/**
	 * Adds quotes to the current text.
	 * @param text    The text to be quoted.
	 * @return The original text with additional quotes.
	 */
	fun quote(text: String): String {
		return QUOTE_BEGIN + text + QUOTE_END
	}

	/**
	 * Searches for a given text-id.
	 * @param id    The text-id to be searched for.
	 * @return The text matching the given Id, or `null` if the ID was not fount.
	 */
	fun findTextId(id: String): String? {
		for((_, value) in cache) {
			val text = value.strings[id]
			if (text != null) {
				return text
			}
		}

		return null
	}


	/**
	 * Updates the language-Id cache using the given project.
	 */
	fun updateCache(project: Project) {
		clearAllCachedFiles()

		val virtualFiles = FileBasedIndex.getInstance().getContainingFiles(
				FileTypeIndex.NAME,
				StringsFileType.INSTANCE,
				GlobalSearchScope.allScope(project)
		)

		for(file in virtualFiles) {
			val dir = file.parent
			var cached_dir = cache[dir]

			if (cached_dir == null) {
				cached_dir = LanguageDirectory(dir)
				cache[dir] = cached_dir
			}

			cached_dir.addFile(file)
		}

		for((_, value) in cache) {
			value.loadStringsIfRequired()
		}
	}


	/**
	 * Clears all cached files.
	 */
	private fun clearAllCachedFiles() {
		for((_, value) in cache) {
			value.files.clear()
		}
	}


	/**
	 * Internal class storing strings for a given directory.
	 * For each directory only one file will be selected by it's name.
	 */
	private class LanguageDirectory internal constructor(internal var directory: VirtualFile) {
		internal var files: MutableSet<VirtualFile> = HashSet()
		internal var strings: MutableMap<String, String> = HashMap()


		internal fun addFile(file: VirtualFile) {
			assert(file.parent == directory)
			files.add(file)
		}


		internal fun selectFileByName(name: String): VirtualFile? {
			var name = name
			name += ".strings"
			for(file in files) {
				if (name == file.name) {
					return file
				}
			}

			return null
		}


		internal fun selectPreferredFile(): VirtualFile? {
			val locale = Locale.getDefault()

			run {
				val file = selectFileByName(locale.toString())
				if (file != null) {
					return file
				}
			}

			run {
				val file = selectFileByName(locale.language)
				if (file != null) {
					return file
				}
			}

			run {
				val file = selectFileByName("en")
				if (file != null) {
					return file
				}
			}

			if (files.isNotEmpty()) {
				return files.iterator().next()
			}

			return null
		}


		internal fun loadStringsIfRequired() {
			if (strings.isEmpty()) {
				loadStrings()
			}
		}


		internal fun loadStrings() {
			strings.clear()

			val file = selectPreferredFile()
			if (file != null) {
				val document = FileDocumentManager.getInstance().getDocument(file)

				if (document != null) {
					val linecount = document.lineCount

					for(i in 0 until linecount) {
						val line = document.getText(TextRange(
								document.getLineStartOffset(i),
								document.getLineEndOffset(i)
						))

						if (line.isEmpty()) {
							continue
						}

						val matcher = PT_LINE.matcher(line)
						if (matcher.matches()) {
							val id = matcher.group(1)
							var value = matcher.group(2)

							if (!id.isEmpty() && !value.isEmpty()) {
								value = value.replace("\\n", "\n")
								strings[id] = value
							}
						}
					}
				}
			}
		}
	}
}
