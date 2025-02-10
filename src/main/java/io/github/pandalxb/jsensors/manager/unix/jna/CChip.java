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
package io.github.pandalxb.jsensors.manager.unix.jna;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Javier Garcia Alonso
 */
public class CChip extends Structure {

	public String prefix;
	public CBus bus;
	public String path;
	public int addr;

	@Override
	protected List getFieldOrder() {
		return Arrays.asList("prefix", "bus", "path", "addr");
	}

}
