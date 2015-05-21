/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sebuilder.interpreter.datasource;

import com.sebuilder.interpreter.DataSource;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Manual-input data source.
 * @author zarkonnen
 */
public class Manual implements DataSource {
	@Override
	public List<Map<String, String>> getData(Map<String, String> config, File relativeTo) {
		return Collections.singletonList(config);
	}
}
