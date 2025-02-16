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
package io.github.pandalxb.jsensors.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.pandalxb.jsensors.JSensors;
import io.github.pandalxb.jsensors.model.components.Components;

/**
 *
 * @author javier
 */
public class JSensorsIntegrationTest {
	public JSensorsIntegrationTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/*
	 * Make a real call and check that components are retrieved
	 */
	@Test
	public void testJSensorsRealReturn() {
		Map<String, String> config = new HashMap<String, String>();

		config.put("testMode", "REAL");
		
		Components components = JSensors.get.config(config).components();
		assertNotNull("Components is null", components);

		assertTrue("Components lists are not initialised",
				components.cpus != null && components.gpus != null && components.disks != null);
		
		/*assertTrue("At least one component has to be been found",
				components.cpus.size() > 0 || components.gpus.size() > 0 && components.disks.size() > 0);*/
	}

}
