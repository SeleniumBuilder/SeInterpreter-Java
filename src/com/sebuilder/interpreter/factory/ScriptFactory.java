/*
 * Copyright 2012 Sauce Labs
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

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Step;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Factory to create Script objects from a string, a reader or JSONObject.
 *
 * @author jkowalczyk
 */
public class ScriptFactory {
	StepTypeFactory stepTypeFactory = new StepTypeFactory();
	TestRunFactory testRunFactory = new TestRunFactory();
	DataSourceFactory dataSourceFactory = new DataSourceFactory();

	public void setStepTypeFactory(StepTypeFactory stepTypeFactory) {
		this.stepTypeFactory = stepTypeFactory;
	}

	public void setTestRunFactory(TestRunFactory testRunFactory) {
		this.testRunFactory = testRunFactory;
	}
	
	public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
		this.dataSourceFactory = dataSourceFactory;
	}

	/**
	 * @return A new instance of script
	 */
	public Script create() {
		Script script = new Script();
		script.testRunFactory = testRunFactory;
		return script;
	}

	/**
	 * @param o A JSONObject describing a script or a suite.
	 * @param sourceFile Optionally. the file the JSON was loaded from.
	 * @return A script, ready to run.
	 * @throws IOException If anything goes wrong with interpreting the JSON.
	 */
	public List<Script> parse(JSONObject o, File sourceFile) throws IOException {
		if (o.optString("type", "script").equals("suite")) {
			return parseSuite(o, sourceFile);
		} else {
			return parseScript(o, sourceFile);
		}
	}
	
	public List<Script> parseScript(JSONObject o, File f) throws IOException {
		try {
			if (!o.get("seleniumVersion").equals("2")) {
				throw new IOException("Unsupported Selenium version: \"" + o.get("seleniumVersion") + "\".");
			}
			if (o.getInt("formatVersion") > 2) {
				throw new IOException("Unsupported Selenium script format version: \"" + o.get("formatVersion") + "\".");
			}
			JSONArray stepsA = o.getJSONArray("steps");
			ArrayList<Script> scripts = new ArrayList<Script>();
			Script script = new Script();
			if (f != null) {
				script.name = f.getPath();
			}
			scripts.add(script);
			for (int i = 0; i < stepsA.length(); i++) {
				JSONObject stepO = stepsA.getJSONObject(i);
				Step step = new Step(stepTypeFactory.getStepTypeOfName(stepO.getString("type")));
				step.negated = stepO.optBoolean("negated", false);
				step.name = stepO.optString("step_name", null);
				script.steps.add(step);
				JSONArray keysA = stepO.names();
				for (int j = 0; j < keysA.length(); j++) {
					String key = keysA.getString(j);
					if (key.equals("type") || key.equals("negated")) {
						continue;
					}
					if (stepO.optJSONObject(key) != null) {
						step.locatorParams.put(key, new Locator(
								stepO.getJSONObject(key).getString("type"),
								stepO.getJSONObject(key).getString("value")));
					} else {
						step.stringParams.put(key, stepO.getString(key));
					}
				}
			}
			if (o.has("data")) {
				JSONObject data = o.getJSONObject("data");
				String sourceName = data.getString("source");
				HashMap<String, String> config = new HashMap<String, String>();
				if (data.has("configs") && data.getJSONObject("configs").has(sourceName)) {
					JSONObject cfg = data.getJSONObject("configs").getJSONObject(sourceName);
					for (Iterator<String> it = cfg.keys(); it.hasNext();) {
						String key = it.next();
						config.put(key, cfg.getString(key));
					}
				}
				script.dataRows = dataSourceFactory.getData(sourceName, config, f.getAbsoluteFile().getParentFile());
			}
			return scripts;
		} catch (JSONException e) {
			throw new IOException("Could not parse script.", e);
		}
	}
	
	public List<Script> parseSuite(JSONObject o, File suiteFile) throws IOException {
		try {
			ArrayList<Script> scripts = new ArrayList<Script>();
			JSONArray scriptLocations = o.getJSONArray("scripts");
			for (int i = 0; i < scriptLocations.length(); i++) {
				JSONObject script = scriptLocations.getJSONObject(i);
				String where = script.getString("where");
				// TODO handle 'where' types other than 'local'
				String path = script.getString("path");
				File f = new File(path);
				if (f.exists()) {
					scripts.addAll(parse(f));
				} else {
					f = new File(suiteFile.getAbsoluteFile().getParentFile(), path);
					if (f.exists()) {
						scripts.addAll(parse(f));
					} else {
						throw new IOException("Script file " + path + " not found.");
					}
				}
			}
			boolean shareState = o.optBoolean("shareState", false);
			if (shareState && scripts.size() > 1) {
				for (Script s : scripts) {
					s.closeDriver = false;
					s.usePreviousDriverAndVars = true;
				}
				scripts.get(0).usePreviousDriverAndVars = false;
				scripts.get(scripts.size() - 1).closeDriver = true;
			}
			return scripts;
		} catch (JSONException e) {
			throw new IOException("Could not parse suite.", e);
		}
	}

	/**
	 * @param jsonString A JSON string describing a script or suite.
	 * @param sourceFile Optionally. the file the JSON was loaded from.
	 * @return A script, ready to run.
	 * @throws IOException If anything goes wrong with interpreting the JSON, or
	 * with the Reader.
	 * @throws JSONException If the JSON can't be parsed.
	 */
	public List<Script> parse(String jsonString, File sourceFile) throws IOException, JSONException {
		return parse(new JSONObject(new JSONTokener(jsonString)), sourceFile);
	}

	/**
	 * @param reader A Reader pointing to a JSON stream describing a script or suite.
	 * @param sourceFile Optionally. the file the JSON was loaded from.
	 * @return A list of scripts, ready to run.
	 * @throws IOException If anything goes wrong with interpreting the JSON, or
	 * with the Reader.
	 * @throws JSONException If the JSON can't be parsed.
	 */
	public List<Script> parse(Reader reader, File sourceFile) throws IOException, JSONException {
		return parse(new JSONObject(new JSONTokener(reader)), sourceFile);
	}
	
	/**
	 * @param f A File pointing to a JSON file describing a script or suite.
	 * @return A list of scripts, ready to run.
	 * @throws IOException If anything goes wrong with interpreting the JSON, or
	 * with the Reader.
	 * @throws JSONException If the JSON can't be parsed.
	 */
	public List<Script> parse(File f) throws IOException, JSONException {
		BufferedReader r = null;
		try {
			return parse(r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8")), f);
		} finally {
			try { r.close(); } catch (Exception e) {}
		}
	}
}