/*
 * Copyright (c) 2017 Adrian Videnie
 *
 * This file is part of Expenses.
 *
 * Expenses is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Expenses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Expenses. If not, see <http://www.gnu.org/licenses/>.
 */

package ro.expectations.expenses.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public class FileUtils {

    public static File[] listFilesWithExtension(File path, final String extension) {

        if (!path.isDirectory() || !path.canRead()) {
            return new File[0];
        }
        File[] files = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (new File(dir, filename)).isFile() && filename.endsWith(extension);
            }
        });

        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File s1, File s2) {
                    return s2.getName().compareTo(s1.getName());
                }
            });
            return files;
        } else {
            return new File[0];
        }
    }
}
