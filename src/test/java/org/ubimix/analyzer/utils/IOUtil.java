/* ************************************************************************** *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This file is licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ************************************************************************** */
package org.ubimix.analyzer.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author kotelnikov
 */
public class IOUtil {

    public static void copy(InputStream input, OutputStream output)
        throws IOException {
        try {
            try {
                byte[] buf = new byte[1024 * 10];
                int len;
                while ((len = input.read(buf)) > 0) {
                    output.write(buf, 0, len);
                }
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
    }

    public static boolean delete(File file) {
        boolean result = true;
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list != null) {
                for (File child : list) {
                    result &= delete(child);
                    if (!result) {
                        break;
                    }
                }
            }
        }
        return result && file.delete();
    }

    public static String readString(File file) throws IOException {
        InputStream input = new FileInputStream(file);
        return readString(input);
    }

    public static String readString(InputStream input) throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(input, out);
            byte[] array = out.toByteArray();
            return new String(array, "UTF-8");
        } finally {
            input.close();
        }
    }

    public static void writeString(File file, String str) throws IOException {
        OutputStream output = new FileOutputStream(file);
        writeString(output, str);
    }

    public static void writeString(OutputStream out, String str)
        throws IOException {
        try {
            byte[] array = str.getBytes("UTF-8");
            ByteArrayInputStream in = new ByteArrayInputStream(array);
            copy(in, out);
        } finally {
            out.close();
        }
    }

}
