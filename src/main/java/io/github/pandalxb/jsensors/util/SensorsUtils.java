/*
 * Copyright 2016-2018 Javier Garcia Alonso.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.pandalxb.jsensors.util;

import io.github.pandalxb.jsensors.manager.windows.powershell.PowerShellOperations;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Javier Garcia Alonso
 */
public class SensorsUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(PowerShellOperations.class);

	private static File tempFile = null;

	// Hides constructor
	private SensorsUtils() {

	}

	public static String generateLibTmpPath(String libName) {
		return generateLibTmpPath("/", libName);
	}

	public static String generateLibTmpPath(String path, String libName) {
	    if (tempFile == null) {
            InputStream in = SensorsUtils.class.getResourceAsStream(path + libName);
            try {
                tempFile = File.createTempFile(libName, "");
                tempFile.deleteOnExit();
                byte[] buffer = new byte[1024];
                int read;
                FileOutputStream fos = new FileOutputStream(tempFile);
                while ((read = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.close();
                in.close();
            } catch (IOException ex) {
                LOGGER.error("Cannot generate temporary file", ex);
                return "";
            }
        }

		return tempFile.getAbsolutePath();
	}

    public static String generateLibPath(String path, String libName) {
        String libsPath = System.getProperty("user.dir") + File.separator + "lib" + File.separator;
        File libFile = new File(libsPath + libName);
        try {
            boolean isSuccess = true;
            if (!libFile.exists()) {
                File libsDir = new File(libsPath);
                if (!libsDir.exists()) {
                    isSuccess = libsDir.mkdirs();
                }
                if (isSuccess) {
                    try (InputStream input = SensorsUtils.class.getResourceAsStream(path + libName);
                         FileOutputStream output = new FileOutputStream(libFile)) {
                        if (input == null) {
                            throw new RuntimeException("resources/" + path + "/" + libName + " not found");
                        }
                        // 复制输入流到文件
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Cannot generate lib file", e);
            return "";
        }
        return libFile.getAbsolutePath();
    }
}
