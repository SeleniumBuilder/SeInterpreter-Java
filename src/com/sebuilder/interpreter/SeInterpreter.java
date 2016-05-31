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

import com.sebuilder.interpreter.factory.ScriptFactory;
import com.sebuilder.interpreter.factory.StepTypeFactory;
import com.sebuilder.interpreter.factory.TestRunFactory;
import com.sebuilder.interpreter.webdriverfactory.Firefox;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An interpreter for Builder JSON tests. Given one or more JSON script files, it plays them back
 * using the Java WebDriver bindings.
 * @author zarkonnen
 */
public class SeInterpreter {
	public static WebDriverFactory DEFAULT_DRIVER_FACTORY = new Firefox();
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: [--driver=<drivername] [--driver.<configkey>=<configvalue>...] [--implicitlyWait=<ms>] [--pageLoadTimeout=<ms>] [--stepTypePackage=<package name>] <script path>...");
			System.exit(0);
		}
		
		Log log = LogFactory.getFactory().getInstance(SeInterpreter.class);
		
		WebDriverFactory wdf = DEFAULT_DRIVER_FACTORY;
		ScriptFactory sf = new ScriptFactory();
		StepTypeFactory stf = new StepTypeFactory();
		sf.setStepTypeFactory(stf);
		TestRunFactory trf = new TestRunFactory();
		sf.setTestRunFactory(trf);

		ArrayList<String> paths = new ArrayList<String>();
		HashMap<String, String> driverConfig = new HashMap<String, String>();
		for (String s : args) {
			if (s.startsWith("--")) {
				String[] kv = s.split("=", 2);
				if (kv.length < 2) {
					log.fatal("Driver configuration option \"" + s + "\" is not of the form \"--driver=<name>\" or \"--driver.<key>=<value\".");
					System.exit(1);
				}
				if (s.startsWith("--implicitlyWait")) {
					trf.setImplicitlyWaitDriverTimeout(Integer.parseInt(kv[1]));
				} else if (s.startsWith("--pageLoadTimeout")) {
					trf.setPageLoadDriverTimeout(Integer.parseInt(kv[1]));
				} else if (s.startsWith("--stepTypePackage")) {
					stf.setPrimaryPackage(kv[1]);
				} else if (s.startsWith("--driver.")) {
					driverConfig.put(kv[0].substring("--driver.".length()), kv[1]);
				} else if (s.startsWith("--driver")) {
					try {
						wdf = (WebDriverFactory) Class.forName("com.sebuilder.interpreter.webdriverfactory." + kv[1]).newInstance();
					} catch (ClassNotFoundException e) {
						log.fatal("Unknown WebDriverFactory: " + "com.sebuilder.interpreter.webdriverfactory." + kv[1], e);
					} catch (InstantiationException e) {
						log.fatal("Could not instantiate WebDriverFactory " + "com.sebuilder.interpreter.webdriverfactory." + kv[1], e);
					} catch (IllegalAccessException e) {
						log.fatal("Could not instantiate WebDriverFactory " + "com.sebuilder.interpreter.webdriverfactory." + kv[1], e);
					}
				} else {
					paths.add(s);
				}
			} else {
				paths.add(s);
			}
		}
		
		if (paths.isEmpty()) {
			log.info("Configuration successful but no paths to scripts specified. Exiting.");
			System.exit(0);
		}
				
		for (String path : paths) {
			try {
				TestRun lastRun = null;
				for (Script script : sf.parse(new File(path))) {
					for (Map<String, String> data : script.dataRows) {
						try {
							lastRun = script.testRunFactory.createTestRun(script, log, wdf, driverConfig, data, lastRun);
							if (lastRun.finish()) {
								log.info(script.name + " succeeded");
							} else {
								log.info(script.name + " failed");
							}
						} catch (Exception e) {
							log.info(script.name + " failed", e);
						}
					}
				}
			} catch (Exception e) {
				log.fatal("Run error.", e);
				System.exit(1);
			}
		}
	}	
}
