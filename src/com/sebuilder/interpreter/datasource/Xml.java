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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML data source compatible with the standard IDE approach.
 * @author zarkonnen
 */
public class Xml implements DataSource {
	@Override
	public List<Map<String, String>> getData(Map<String, String> config, File relativeTo) {
		ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
		File f = findFile(relativeTo, config.get("path"));
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
			NodeList rows = doc.getElementsByTagName("test");
			for (int i = 0; i < rows.getLength(); i++) {
				Node rowN = rows.item(i);
				NamedNodeMap attributes = rowN.getAttributes();
				Map<String, String> row = new HashMap<String, String>();
				for (int j = 0; j < attributes.getLength(); j++) {
					row.put(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue());
				}
				data.add(row);
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to get data.", e);
		}
		return data;
	}
}
