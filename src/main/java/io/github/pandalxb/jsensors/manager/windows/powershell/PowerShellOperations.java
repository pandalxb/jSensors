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

import io.github.pandalxb.jpowershell.PowerShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Javier Garcia Alonso
 */
public enum PowerShellOperations {
	GET;

	private static final Logger LOGGER = LoggerFactory.getLogger(PowerShellOperations.class);

	private static final PowerShell powerShell;

	static {
		powerShell = PowerShell.openSession();
	}

	PowerShellOperations() {

	}

	public static boolean isAdministrator() {
		String command = "([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] \"Administrator\")";

		return "true".equalsIgnoreCase(powerShell.executeCommand(command).getCommandOutput());
	}

	public String getRawSensorsData() {
		return powerShell.executeCommand(PowerShellScriptHelper.getPowerShellScriptForSingleLine()).getCommandOutput();
	}

	public String getEnvironmentVersion() {
		return powerShell.executeCommand("[Environment]::Version.ToString()").getCommandOutput();
	}
}
