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
package com.sebuilder.interpreter;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * A Selenium 2 step.
 *
 * @author jkowalczyk
 */
public class Step {
	/**
	 * Whether the step is negated. Only relevant for Assert/Verify/WaitFor
	 * steps.
	 */
	public boolean negated;
	
	/** The custom name of the step, if any. */
	public String name;

	public StepType type;
	public HashMap<String, String> stringParams = new HashMap<String, String>();
	public HashMap<String, Locator> locatorParams = new HashMap<String, Locator>();

	public boolean isNegated() { return negated; }

	public Step(StepType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String toPrettyString() {
		StringBuilder sb = new StringBuilder();
		if (name != null) {
			sb.append(name).append(": ");
		}
		if (type instanceof Assert) {
			sb.append("assert").append(((Assert) type).getter.getClass().getSimpleName());
		} else if (type instanceof Verify) {
			sb.append("verify").append(((Verify) type).getter.getClass().getSimpleName());
		} else if (type instanceof WaitFor) {
			sb.append("waitFor").append(((WaitFor) type).getter.getClass().getSimpleName());
		} else if (type instanceof Store) {
			sb.append("store").append(((Store) type).getter.getClass().getSimpleName());
		} else {
			sb.append(type.getClass().getSimpleName());
		}
		
		for (Map.Entry<String, String> pe : stringParams.entrySet()) {
			sb.append(" ").append(pe.getKey()).append("=").append(pe.getValue());
		}
		for (Map.Entry<String, Locator> le : locatorParams.entrySet()) {
			sb.append(" ").append(le.getKey()).append("=").append(le.getValue().toPrettyString());
		}
		
		return sb.toString();
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject o = new JSONObject();
		if (name != null) {
			o.put("step_name", name);
		}
		if (type instanceof Assert) {
			o.put("type", "assert" + ((Assert) type).getter.getClass().getSimpleName());
		} else if (type instanceof Verify) {
			o.put("type", "verify" + ((Verify) type).getter.getClass().getSimpleName());
		} else if (type instanceof WaitFor) {
			o.put("type", "waitFor" + ((WaitFor) type).getter.getClass().getSimpleName());
		} else if (type instanceof Store) {
			o.put("type", "store" + ((Store) type).getter.getClass().getSimpleName());
		} else {
			o.put("type", type.getClass().getSimpleName());
		}
		o.put("negated", negated);
		for (Map.Entry<String, String> pe : stringParams.entrySet()) {
			o.put(pe.getKey(), pe.getValue());
		}
		for (Map.Entry<String, Locator> le : locatorParams.entrySet()) {
			o.put(le.getKey(), le.getValue().toJSON());
		}

		return o;
	}
}
