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

import com.sebuilder.interpreter.DataSource;
import static com.sebuilder.interpreter.datasource.Utils.findFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * JSON-based data source.
 * @author zarkonnen
 */
public class Json implements DataSource {
	@Override
	public List<Map<String, String>> getData(Map<String, String> config, File relativeTo) {
		ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
		File f = findFile(relativeTo, config.get("path"));
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			JSONTokener tok = new JSONTokener(r);
			JSONArray a = new JSONArray(tok);
			for (int i = 0; i < a.length(); i++) {
				JSONObject rowO = a.getJSONObject(i);
				Map<String, String> row = new HashMap<String, String>();
				for (Iterator<String> it = rowO.keys(); it.hasNext();) {
					String key = it.next();
					row.put(key, rowO.getString(key));
				}
				data.add(row);
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to get data.", e);
		} finally {
			try { r.close(); } catch (Exception e) {}
		}
		return data;
	}
}
