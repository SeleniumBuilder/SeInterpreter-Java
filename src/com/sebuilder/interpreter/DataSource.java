/*
* Copyright 2014 Sauce Labs
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

package com.sebuilder.interpreter;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A method of acquiring data rows for data-driven playback. Implementing classes should be located
 * in com.sebuilder.interpreter.datasource .
 * @author zarkonnen
 */
public interface DataSource {
	public List<Map<String, String>> getData(Map<String, String> config, File relativeTo);
}
