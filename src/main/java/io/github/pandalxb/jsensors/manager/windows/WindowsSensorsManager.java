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
package io.github.pandalxb.jsensors.manager.windows;

import io.github.pandalxb.jsensors.manager.SensorsManager;
import io.github.pandalxb.jsensors.manager.windows.powershell.PowerShellOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MS Windows implementation of SensorManager that gets the sensors using a
 * PowerShell script and parses it into a normalized format.
 *
 * @author Javier Garcia Alonso
 */
public class WindowsSensorsManager extends SensorsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsSensorsManager.class);

    private static final String LINE_BREAK = "\r\n";

    private static final String COMPONENT_SEPARATOR = "[COMPONENT]";

    private static String currentHardwareType = null;
    private static String currentHardwareName = null;
    private static String currentSensorType = null;
    private static String currentSensorName = null;

    @Override
    public String getSensorsData() {

        String rawSensorsData = PowerShellOperations.GET.getRawSensorsData();

        if (debugMode) {
            LOGGER.info("RawSensorData: " + rawSensorsData);
        }

        String normalizedSensorsData = normalizeSensorsData(rawSensorsData);
        if (debugMode) {
            LOGGER.info("NormalizeSensorData: " + normalizedSensorsData);
        }

        return normalizedSensorsData;
    }

    private static String normalizeSensorsData(String rawSensorsData) {
        StringBuilder normalizedSensorsData = new StringBuilder();
        String[] dataLines = rawSensorsData.split("\\r?\\n");

        boolean readingHardLabel = true;
        boolean readingSensor = false;
        boolean isNeededHardware = false;
        for (final String dataLine : dataLines) {
            if ("HardwareType".equals(getKey(dataLine))) {
                currentHardwareType = getValue(dataLine);
                readingHardLabel = true;
                continue;
            }
            if (readingHardLabel) {
                if ("Name".equals(getKey(dataLine))) {
                    currentHardwareName = getValue(dataLine);
                } else if (currentHardwareType != null && currentHardwareName != null) {
                    isNeededHardware = true;
                    if ("CPU".equalsIgnoreCase(currentHardwareType)) {
                        normalizedSensorsData.append(COMPONENT_SEPARATOR).append(LINE_BREAK);
                        normalizedSensorsData.append("CPU").append(LINE_BREAK);
                    } else if (currentHardwareType.toUpperCase().startsWith("GPU")) {
                        normalizedSensorsData.append(COMPONENT_SEPARATOR).append(LINE_BREAK);
                        normalizedSensorsData.append("GPU").append(LINE_BREAK);
                    } else if ("Storage".equalsIgnoreCase(currentHardwareType) || "HDD".equalsIgnoreCase(currentHardwareType)) {
                        normalizedSensorsData.append(COMPONENT_SEPARATOR).append(LINE_BREAK);
                        normalizedSensorsData.append("DISK").append(LINE_BREAK);
                    } else if ("Motherboard".equalsIgnoreCase(currentHardwareType) || "Mainboard".equalsIgnoreCase(currentHardwareType)) {
                        normalizedSensorsData.append(COMPONENT_SEPARATOR).append(LINE_BREAK);
                        normalizedSensorsData.append("MOBO").append(LINE_BREAK);
                    } else {
                        isNeededHardware = false;
                    }
                    if (isNeededHardware) {
                        normalizedSensorsData.append("Label: ").append(currentHardwareName).append(LINE_BREAK);
                    }
                    currentHardwareType = null;
                    currentHardwareName = null;
                    readingHardLabel = false;
                }
            } else {
                if (isNeededHardware) {
                    readingSensor = addSensorsData(readingSensor, dataLine, normalizedSensorsData);
                }
            }
        }

        return normalizedSensorsData.toString();
    }

    private static boolean addSensorsData(boolean readingSensor, String dataLine, StringBuilder normalizedSensorsData) {
        if ("Hardware".equals(getKey(dataLine))) {
            return true;
        }

        if (readingSensor) {
            if ("Name".equals(getKey(dataLine))) {
                currentSensorName = getValue(dataLine);
                return true;
            } else if ("SensorType".equals(getKey(dataLine))) {
                currentSensorType = getValue(dataLine);
                return true;
            } else if ("Value".equals(getKey(dataLine))) {
                switch (currentSensorType) {
                    case "Temperature":
                        normalizedSensorsData.append("Temp ");
                        break;
                    case "Fan":
                        normalizedSensorsData.append("Fan ");
                        break;
                    case "Load":
                        normalizedSensorsData.append("Load ");
                        break;
                    default:
                        readingSensor = false;
                        break;
                }
                if (readingSensor) {
                    normalizedSensorsData.append(currentSensorName).append(": ");
                    normalizedSensorsData.append(getValue(dataLine)).append(LINE_BREAK);
                }
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private static String getKey(String line) {
        return getData(line, 0);
    }

    private static String getValue(String line) {
        return getData(line, 1);
    }

    private static String getData(String line, final int index) {
        if (line.contains(":")) {
            return line.split(":", 2)[index].trim();
        }

        return "";
    }
}
