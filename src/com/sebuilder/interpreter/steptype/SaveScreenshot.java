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

package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import org.openqa.selenium.OutputType;

public class SaveScreenshot implements StepType {
	@Override
	public boolean run(TestRun ctx) {
		try {
			return ((File) ctx.driver().getClass().getMethod("getScreenshotAs", OutputType.class).invoke(ctx.driver(), OutputType.FILE)).renameTo(new File(ctx.string("file")));
		} catch (NoSuchMethodException e) {
			ctx.log().fatal("Driver does not support getScreenshotAs", e);
			return false;
		} catch (IllegalAccessException e) {
			ctx.log().fatal("Driver does not support getScreenshotAs", e);
			return false;
		} catch (InvocationTargetException e) {
			ctx.log().fatal("Driver does not support getScreenshotAs", e);
			return false;
		}
	}
}
