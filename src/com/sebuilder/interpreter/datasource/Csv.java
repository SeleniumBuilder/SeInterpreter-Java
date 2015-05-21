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

package com.sebuilder.interpreter.datasource;

import au.com.bytecode.opencsv.CSVReader;
import com.sebuilder.interpreter.DataSource;
import static com.sebuilder.interpreter.datasource.Utils.findFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV-based data source.
 * @author zarkonnen
 */
public class Csv implements DataSource {
	@Override
	public List<Map<String, String>> getData(Map<String, String> config, File relativeTo) {
		ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
		File f = findFile(relativeTo, config.get("path"));
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			CSVReader csvR = new CSVReader(r);
			String[] keys = csvR.readNext();
			if (keys != null) {
				String[] line;
				int rowNumber = 1;
				while ((line = csvR.readNext()) != null) {
					rowNumber++;
					HashMap<String, String> row = new HashMap<String, String>();
					if (line.length < keys.length) {
						throw new IOException("Not enough cells in row " + rowNumber + ".");
					}
					for (int c = 0; c < keys.length; c++) {
						row.put(keys[c], line[c]);
					}
					data.add(row);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to get data.", e);
		} finally {
			try { r.close(); } catch (Exception e) {}
		}
		return data;
	}
}
