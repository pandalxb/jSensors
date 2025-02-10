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
package io.github.pandalxb.jsensors.manager.windows.powershell;

import io.github.pandalxb.jsensors.util.SensorsUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Javier Garcia Alonso
 */
class PowerShellScriptHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(PowerShellOperations.class);

	private static final String LINE_BREAK = "\r\n";

	private static File tmpFile = null;

	private static String singleLineCommand = null;

	private static LibType libType;

	static {
		intLibType();
	}

	private static void intLibType() {
		try {
			WinReg.HKEY hKey = WinReg.HKEY_LOCAL_MACHINE;
			String keyPath = "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full";
			String itemName = "Release";
			boolean isItemExist = Advapi32Util.registryValueExists(hKey, keyPath, itemName);
			if(!isItemExist) {
				libType = LibType.TYPE_OPEN;
				return;
			}
			int release = Advapi32Util.registryGetIntValue(hKey, keyPath, itemName);
			if (release < 461808) {
				// lower than .NET 4.7.2
				// .NET Framework 4.7.2 最低的Release值为461808
				libType = LibType.TYPE_OPEN;
				return;
			}
			String version = PowerShellOperations.GET.getEnvironmentVersion();
			String[] versionArray = version.split("\\.");
			int versionMajor = Integer.parseInt(versionArray[0]);
			int versionMinor = Integer.parseInt(versionArray[1]);
			int versionBuild = Integer.parseInt(versionArray[2]);
			int versionRevision = Integer.parseInt(versionArray[3]);
			if(versionMajor > 4 || (versionMajor == 4 && versionMinor > 0) || (versionMajor == 4 && versionMinor == 0 && versionBuild > 30319) || (versionMajor == 4 && versionMinor == 0 && versionBuild == 30319 && versionRevision >= 42000)) {
				// 4.0.30319.42000 or later
				// CLR 版本 4.0.30319.42000 支持从 .NET Framework 4.6 开始的 .NET Framework 版本
				libType = LibType.TYPE_LIBRE;
			} else {
				libType = LibType.TYPE_OPEN;
			}
		} catch (Exception e) {
			LOGGER.error("intLibType error:", e);
			libType = LibType.TYPE_OPEN;
		}
	}

	enum LibType {
		TYPE_LIBRE,
		TYPE_OPEN
	}

	// Hides constructor
	private PowerShellScriptHelper() {
	}

	private static String dllImport() {
		StringBuilder code = new StringBuilder();

		if (LibType.TYPE_LIBRE.equals(libType)) {
			code.append("[System.Reflection.Assembly]::LoadFile(\"").append(SensorsUtils.generateLibPath("/lib/win/", "HidSharp.dll")).append("\") | Out-Null;").append(LINE_BREAK);
			code.append("[System.Reflection.Assembly]::LoadFile(\"").append(SensorsUtils.generateLibPath("/lib/win/", "LibreHardwareMonitorLib.dll")).append("\") | Out-Null;").append(LINE_BREAK);
		} else {
			code.append("[System.Reflection.Assembly]::LoadFile(\"").append(SensorsUtils.generateLibPath("/lib/win/", "OpenHardwareMonitorLib.dll")).append("\") | Out-Null;").append(LINE_BREAK);
		}

		return code.toString();
	}

	private static String newComputerInstance() {
		StringBuilder code = new StringBuilder();

		if (LibType.TYPE_LIBRE.equals(libType)) {
			code.append("$PC = New-Object LibreHardwareMonitor.Hardware.Computer;").append(LINE_BREAK);

			code.append("$PC.IsMotherboardEnabled = $true;").append(LINE_BREAK);
			code.append("$PC.IsCpuEnabled = $true;").append(LINE_BREAK);
			code.append("$PC.IsMemoryEnabled = $true;").append(LINE_BREAK);
			code.append("$PC.IsGpuEnabled = $true;").append(LINE_BREAK);
			code.append("$PC.IsControllerEnabled = $true;").append(LINE_BREAK);
			code.append("$PC.IsStorageEnabled = $true;").append(LINE_BREAK);
		} else {
			code.append("$PC = New-Object OpenHardwareMonitor.Hardware.Computer;").append(LINE_BREAK);

			code.append("$PC.MainboardEnabled = $true;").append(LINE_BREAK);
			code.append("$PC.CPUEnabled = $true;").append(LINE_BREAK);
			code.append("$PC.RAMEnabled = $true;").append(LINE_BREAK);
			code.append("$PC.GPUEnabled = $true;").append(LINE_BREAK);
			code.append("$PC.FanControllerEnabled = $true;").append(LINE_BREAK);
			code.append("$PC.HDDEnabled = $true;").append(LINE_BREAK);
		}

		return code.toString();
	}

	private static String sensorsQueryLoop() {
		StringBuilder code = new StringBuilder();

		code.append("try").append(LINE_BREAK);
		code.append("{").append(LINE_BREAK);
		code.append("$PC.Open();").append(LINE_BREAK);
		code.append("}").append(LINE_BREAK);
		code.append("catch").append(LINE_BREAK);
		code.append("{").append(LINE_BREAK);
		code.append("$PC.Open();").append(LINE_BREAK);
		code.append("};").append(LINE_BREAK);

		code.append("ForEach ($hw in $PC.Hardware)").append(LINE_BREAK);
		code.append("{").append(LINE_BREAK);
		code.append("$hw;").append(LINE_BREAK);
		code.append("$hw.Update();").append(LINE_BREAK);
		code.append("ForEach ($subhw in $hw.SubHardware)").append(LINE_BREAK);
		code.append("{").append(LINE_BREAK);
		code.append("$subhw.Update();").append(LINE_BREAK);
		code.append("ForEach ($sensor in $subhw.Sensors)").append(LINE_BREAK);
		code.append("{").append(LINE_BREAK);
		code.append("$sensor;").append(LINE_BREAK);
		code.append("Write-Host \"\";").append(LINE_BREAK);
		code.append("}").append(LINE_BREAK);
		code.append("};").append(LINE_BREAK);
		code.append("ForEach ($sensor in $hw.Sensors)").append(LINE_BREAK);
		code.append("{").append(LINE_BREAK);
		code.append("$sensor;").append(LINE_BREAK);
		code.append("Write-Host \"\";").append(LINE_BREAK);
		code.append("}").append(LINE_BREAK);
		code.append("}");

		return code.toString();
	}

	static String generateScript() {
		FileWriter writer = null;
		String scriptPath = null;

		if (tmpFile == null) {
			try {
				tmpFile = File.createTempFile("jsensors_" + new Date().getTime(), ".ps1");
				tmpFile.deleteOnExit();
				writer = new FileWriter(tmpFile);
				writer.write(getPowerShellScript());
				writer.flush();
				writer.close();
			} catch (Exception ex) {
				LOGGER.error("Cannot create PowerShell script file", ex);
				return "Error";
			} finally {
				try {
					if (writer != null) {
						writer.close();
					}
				} catch (IOException ioe) {
					LOGGER.warn("Error when finish writing Powershell script file", ioe);
				}
			}
		}

		return tmpFile.getAbsolutePath();
	}

	private static String getPowerShellScript() {
		StringBuilder script = new StringBuilder();

		script.append(dllImport());
		script.append(newComputerInstance());
		script.append(sensorsQueryLoop());

		return script.toString();
	}

	static String getPowerShellScriptForSingleLine() {
		if(singleLineCommand == null) {
			singleLineCommand = getPowerShellScript().replaceAll(LINE_BREAK, "");
		}
		return singleLineCommand;
	}
}
