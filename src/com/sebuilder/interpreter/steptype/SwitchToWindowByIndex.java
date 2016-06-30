/*
* Copyright 2016 Sauce Labs
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

package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import java.util.ArrayList;

public class SwitchToWindowByIndex implements StepType {
	@Override
	public boolean run(TestRun ctx) {
		// Converting the set into a List is hopefully OK because it's a
		// LinkedHashSet, and so the order should be the same as from the
		// server.
		ArrayList<String> handles = new ArrayList<String>(ctx.driver().getWindowHandles());
		int index = Integer.parseInt(ctx.string("index"));
		if (index >= handles.size()) {
			throw new ArrayIndexOutOfBoundsException("Cannot switch to window index " + index + ". There are only " + handles.size() + " window handles available.");
		}
		ctx.driver().switchTo().window(handles.get(index));
		return true;
	}
}
