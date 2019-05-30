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

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Helper class to find language ID's within the current project.
 */
public class LanguageIdHelper {
	public static final int MAX_LINE_LENGTH		= 48;

	public static final String QUOTE_BEGIN		= "\u201E";
	public static final String QUOTE_END		= "\u201C";
	public static final String ELLIPSIS			= "\u2026";

	public static final Pattern PT_LINE			= Pattern.compile("\"(.*)\"\\s+=\\s+\"(.*)\";");



	private Map<VirtualFile, LanguageDirectory> cache = new HashMap<>();



	private static LanguageIdHelper instance = new LanguageIdHelper();

	/**
	 * Get the current instance of LanguageIdHelper.
	 */
	public static LanguageIdHelper getInstance() {
		return instance;
	}


	private LanguageIdHelper() {
	}


	/**
	 * Shortens a given text at the first linebreak or at {@link #MAX_LINE_LENGTH} characters.
	 * @param text	The text to be shortened.
	 * @return The short version of the original text.
	 */
	public static @NotNull String shorten(@NotNull String text) {
		boolean was_shortened = false;

		int lbr_index = text.indexOf("\\n");
		if (lbr_index != -1) {
			text = text.substring(0, lbr_index - 1);
			was_shortened = true;
		}

		if (text.length() > MAX_LINE_LENGTH) {
			text = text.substring(0, MAX_LINE_LENGTH);
			was_shortened = true;
		}

		if (was_shortened) {
			text += '[' + ELLIPSIS + ']';
		}

		return text;
	}


	/**
	 * Adds quotes to the current text.
	 * @param text	The text to be quoted.
	 * @return The original text with additional quotes.
	 */
	public static @NotNull String quote(@NotNull String text) {
		return QUOTE_BEGIN + text + QUOTE_END;
	}


	/**
	 * Searches for a given text-id.
	 * @param id	The text-id to be searched for.
	 * @return The text matching the given Id, or {@code null} if the ID was not fount.
	 */
	public @Nullable String findTextId(@NotNull String id) {
		for(Map.Entry<VirtualFile, LanguageDirectory> entry : cache.entrySet()) {
			String text = entry.getValue().strings.get(id);
			if (text != null) {
				return text;
			}
		}

		return null;
	}


	/**
	 * Updates the language-Id cache using the given project.
	 */
	public void updateCache(@NotNull Project project) {
		clearAllCachedFiles();

		Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(
				FileTypeIndex.NAME,
				StringsFileType.INSTANCE,
				GlobalSearchScope.allScope(project)
		);

		for(VirtualFile file : virtualFiles) {
			VirtualFile dir = file.getParent();
			LanguageDirectory cached_dir = cache.get(dir);

			if (cache.containsKey(dir)) {
				cached_dir.addFile(file);
			}
			else {
				cached_dir = new LanguageDirectory(dir);
				cache.put(dir, cached_dir);
			}

			cached_dir.addFile(file);
		}

		for(Map.Entry<VirtualFile, LanguageDirectory> entry : cache.entrySet()) {
			entry.getValue().loadStringsIfRequired();
		}
	}


	/**
	 * Clears all cached files.
	 */
	private void clearAllCachedFiles() {
		for(Map.Entry<VirtualFile, LanguageDirectory> entry : cache.entrySet()) {
			entry.getValue().files.clear();
		}
	}


	/**
	 * Internal class storing strings for a given directory.
	 * For each directory only one file will be selected by it's name.
	 */
	private class LanguageDirectory {
		VirtualFile			directory;
		Set<VirtualFile>	files			= new HashSet<>();
		Map<String,String>	strings			= new HashMap<>();


		LanguageDirectory(VirtualFile directory) {
			this.directory = directory;
		}


		void addFile(@NotNull VirtualFile file) {
			assert file.getParent().equals(directory);
			files.add(file);
		}


		@Nullable VirtualFile selectFileByName(@NotNull String name) {
			name += ".strings";
			for(VirtualFile file : files) {
				if (name.equals(file.getName())) {
					return file;
				}
			}

			return null;
		}


		@Nullable VirtualFile selectPreferredFile() {
			Locale locale = Locale.getDefault();

			{
				VirtualFile file = selectFileByName(locale.toString());
				if (file != null) {
					return file;
				}
			}

			{
				VirtualFile file = selectFileByName(locale.getLanguage());
				if (file != null) {
					return file;
				}
			}

			{
				VirtualFile file = selectFileByName("en");
				if (file != null) {
					return file;
				}
			}

			if (!files.isEmpty()) {
				return files.iterator().next();
			}

			return null;
		}


		void loadStringsIfRequired() {
			if (strings.isEmpty()) {
				loadStrings();
			}
		}


		void loadStrings() {
			strings.clear();

			VirtualFile file = selectPreferredFile();
			if (file != null) {
				Document document = FileDocumentManager.getInstance().getDocument(file);

				if (document != null) {
					int linecount = document.getLineCount();

					for(int i=0; i<linecount; i++) {
						String line = document.getText(new TextRange(
								document.getLineStartOffset(i),
								document.getLineEndOffset(i)
						));

						if (line.isEmpty()) {
							continue;
						}

						Matcher matcher = PT_LINE.matcher(line);
						if (matcher.matches()) {
							String id = matcher.group(1);
							String value = matcher.group(2);

							if (!id.isEmpty() && !value.isEmpty()) {
								value = value.replace("\\n", "\n");
								strings.put(id, value);
							}
						}
					}
				}
			}
		}
	}
}
