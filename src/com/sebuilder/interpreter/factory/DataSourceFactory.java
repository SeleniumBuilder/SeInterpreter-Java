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

package com.sebuilder.interpreter.factory;

import com.sebuilder.interpreter.DataSource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for data sources.
 * @author zarkonnen
 */
public class DataSourceFactory {
	public static final String DEFAULT_DATA_SOURCE_PACKAGE = "com.sebuilder.interpreter.datasource";
	
	private String customDataSourcePackage = null;

	public String getCustomDataSourcePackage() {
		return customDataSourcePackage;
	}

	/** Package from which the factory preferentially loads in data sources. */
	public void setCustomDataSourcePackage(String customDataSourcePackage) {
		this.customDataSourcePackage = customDataSourcePackage;
	}
	
	/**
	 * Lazily loaded map of data sources.
	 */
	private final HashMap<String, DataSource> sourcesMap = new HashMap<String, DataSource>();
	
	public List<Map<String, String>> getData(String sourceName, Map<String, String> config, File relativeToFile) {
		if (!sourcesMap.containsKey(sourceName)) {
			String className = sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1).toLowerCase();
			Class c = null;
			if (customDataSourcePackage != null) {
				try {
					c = Class.forName(customDataSourcePackage + "." + className);
				} catch (ClassNotFoundException cnfe) {
					// Ignore this exception.
				}
			}
			if (c == null) {
				try {
					c = Class.forName(DEFAULT_DATA_SOURCE_PACKAGE + "." + className);
				} catch (ClassNotFoundException cnfe) {
					throw new RuntimeException("No implementation class for data source \"" + sourceName + "\" could be found.", cnfe);
				}
			}
			if (c != null) {
				try {
					Object o = c.newInstance();
					sourcesMap.put(sourceName, (DataSource) o);
				} catch (InstantiationException ie) {
					throw new RuntimeException(c.getName() + " could not be instantiated.", ie);
				} catch (IllegalAccessException iae) {
					throw new RuntimeException(c.getName() + " could not be instantiated.", iae);
				} catch (ClassCastException cce) {
					throw new RuntimeException(c.getName() + " does not extend DataSource.", cce);
				}
			}
		}
		
		return sourcesMap.get(sourceName).getData(config, relativeToFile);
	}
}
