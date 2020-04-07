/*
 * Copyright (©) 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.zzzhan.nodeflow4j;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zzzhan.nodeflow4j.groovy.ExprSupport;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * @author zzzhan
 *
 */
public class NodeflowEngine {
	private static final Logger logger = LoggerFactory.getLogger(NodeflowEngine.class);
	private static final String KEY_INDEXS = "indexs";
	private static final String KEY_NODES = "nodes";
	private static final String KEY_ID = "id";
	private static final String KEY_NEXT = "next";
	private static final String KEY_ERROR = "error";
	private static final String KEY_MANUAL = "manual";
	private static final String KEY_CHECK = "check";
	private static final String KEY_COMPONENT = "component";
	private static final String KEY_OPT = "opt";
	private static final String KEY_TYPE = "type";
	private static final String KEY_TYPE_NUMERIC = "NUMERIC";
	private static final String KEY_TYPE_LONG = "LONG";
	private static final String KEY_TYPE_INT = "INT";
	private static final String KEY_TYPE_DOUBLE = "DOUBLE";
	private static final String KEY_IDX = "idx";// index key
	private static final String KEY_NAME = "name";// index name
	private static final String KEY_VALUE = "value";// index name
	private static final String KEY_MODE = "mode";// 1:input,2:output,3:input&output
	private static final String KEY_REGX = "regx";
	private static final String KEY_REGX_NAME = "regxName";
	private static final Map<String, NfComponent> INIT_COMPONENTS = new HashMap<String, NfComponent>();
	private static final Map<String, NfComponent> COMPONENTS = new HashMap<String, NfComponent>();
	static {
		ServiceLoader<NfComponent> services = ServiceLoader.load(NfComponent.class);
		for (NfComponent service : services) {
			INIT_COMPONENTS.put(service.spiId(), service);
		}
	}
	private JsonObject nf = null;
	private JsonArray indexs = null;
	private JsonArray nodes = null;
	private Map<String, Object> ctx;
	private String curNode = null;
	private String lastNode = null;
	private Map<String, Object> output;
	private Map<String, Object> input;
	private boolean finished;

	public NodeflowEngine(String nfjson) {
		Gson gson = new Gson();
		nf = gson.fromJson(nfjson, JsonObject.class);
		logger.debug(nf.toString());
		indexs = nf.getAsJsonArray(KEY_INDEXS);
		nodes = nf.getAsJsonArray(KEY_NODES);
	}

	public static void register(NfComponent c) {
		if (componentExists(c)) {
			logger.warn("The commponent[{}] register duplicated:{}", c.spiId(), c.getClass().getName());
		}
		COMPONENTS.put(c.spiId(), c);
	}

	public static boolean componentExists(NfComponent c) {
		return COMPONENTS.containsKey(c.spiId()) || INIT_COMPONENTS.containsKey(c.spiId());
	}

	public static NfComponent findComponent(String spiId) {
		NfComponent c = COMPONENTS.get(spiId);
		if (c == null) {
			c = INIT_COMPONENTS.get(spiId);
		}
		return c;
	}

	public void execute() throws NodeflowException {
		execute(new HashMap<String, Object>());
	}

	public void execute(Map<String, Object> ctx) throws NodeflowException {
		this.ctx = ctx;
		List<Map<String, Object>> list = fetchInputOutput(ctx);
		input = list.get(0);
		output = list.get(1);
		if (curNode == null || "".equals(curNode.trim())) {
			JsonObject first = nodes.get(0).getAsJsonObject();
			next(first, null, 0);
		} else {
			next(findNode(curNode, nodes), findNode(lastNode, nodes), 0);
		}
	}

	public Map<String, Object> getOutput() {
		if (output != null) {
			for (String key : output.keySet()) {
				output.put(key, ctx.get(key));
			}
		}
		return output;
	}

	public Map<String, Object> getInput() {
		return this.input;
	}

	private static int getAsInt(JsonObject obj, String key, int def) {
		JsonElement item = obj.get(key);
		return item == null || item.isJsonNull() ? def : item.getAsInt();
	}

	private static String getAsString(JsonObject obj, String key, String def) {
		JsonElement item = obj.get(key);
		return item == null || item.isJsonNull() ? def : item.getAsString();
	}

	private static Object parseType(String type, Object val) {
		switch (type.toUpperCase()) {
		case KEY_TYPE_INT:
			val = val == null ? 0 : Integer.parseInt(val.toString());
			break;
		case KEY_TYPE_LONG:
			val = val == null ? 0 : Long.parseLong(val.toString());
			break;
		case KEY_TYPE_DOUBLE:
			val = val == null ? 0 : Double.parseDouble(val.toString());
			break;
		case KEY_TYPE_NUMERIC:
			val = val == null ? 0 : new BigDecimal(val.toString());
			break;
		}
		return val;
	}

	public List<Map<String, Object>> fetchInputOutput(Map<String, Object> ctx) throws NodeflowException {
		Map<String, Object> input = new HashMap<String, Object>();
		Map<String, Object> output = new HashMap<String, Object>();
		for (int i = 0; indexs != null && i < indexs.size(); i++) {
			JsonObject indexItem = indexs.get(i).getAsJsonObject();
			int opt = getAsInt(indexItem, KEY_OPT, 0);
			int mode = getAsInt(indexItem, KEY_MODE, 1);
			String idx = indexItem.get(KEY_IDX).getAsString();
			String idxName = getAsString(indexItem, KEY_NAME, idx);
			String pattern = getAsString(indexItem, KEY_REGX, "");
			String type = getAsString(indexItem, KEY_TYPE, null);
			String patternName = getAsString(indexItem, KEY_REGX_NAME, idxName);
			Object val = ctx.get(idx);
			if (val == null) {
				val = getAsString(indexItem, KEY_VALUE, idx);
			}
			if (type != null) {
				val = parseType(type, val);
			}
			if ((mode & 1) == 1) {
				if (opt == 0) {
					if (val == null || val.toString().trim().equals("")) {
						throw new IllegalArgumentException(idx + "-" + idxName);
					}
				}
				if (pattern != null && !pattern.equals("") && val != null && !val.toString().trim().equals("")) {
					if (!val.toString().matches(pattern)) {
						throw new IllegalArgumentException(idx + "-" + patternName);
					}
				}
				ctx.put(idx, val);
				input.put(idx, val);
			} else if ((mode & 2) == 2) {
				output.put(idx, val);
			}
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list.add(input);
		list.add(output);
		return list;
	}

	private void next(JsonObject node, JsonObject last, int deep) throws NodeflowException {
		String nodeId = node.get(KEY_ID).getAsString();
		logger.debug("node:{}-{}", this, nodeId);
		curNode = nodeId;
		boolean manual = false;
		JsonElement item = node.get(KEY_MANUAL);
		if (item != null) {
			String action = item.getAsString();
			manual = !"false".equalsIgnoreCase(action);
		}
		if (last != null) {
			lastNode = last.get(KEY_ID).getAsString();
		}
		logger.debug("manual:{}, deep:{}, last:{}", manual, deep, last);
		if (manual && deep != 0) {
			return;
		}
		JsonElement componentId = node.get(KEY_COMPONENT);
		if (componentId != null) {
			String[] componentIds = componentId.getAsString().split(",");
			for(String key:componentIds) {
				NfComponent cpt = findComponent(key);
				if (cpt != null) {
					Type type = new TypeToken<Map<String, Object>>() {
					}.getType();
					Map<String, Object> nodemap = new Gson().fromJson(node, type);
					Map<String, Object> nfmap = new Gson().fromJson(nf, type);
					cpt.execute(nfmap, nodemap, ctx);
				} else {
					throw new RuntimeException("Component unfound:" + componentId);
				}
			}
		}
		JsonElement next = node.get(KEY_NEXT);
		String to = null;
		if (next != null && next.isJsonPrimitive()) {
			to = parseNext(next.getAsString());			
			next(findNode(to, nodes), node, ++deep);
		} else if (next != null && next.isJsonArray()) {
			JsonArray nexts = next.getAsJsonArray();
			for (int i = 0; i < nexts.size(); i++) {
				JsonObject it = nexts.get(i).getAsJsonObject();
				JsonElement cond = it.get(KEY_CHECK);
				String condition = null;
				if (cond != null && !"".equals((condition = cond.getAsString()).trim())) {
					if ((Boolean) ExprSupport.parseExpr(condition, ctx)) {
						to = parseNext(it.get(KEY_NEXT).getAsString());
						next(findNode(to, nodes), node, ++deep);
						break;
					} else {
						JsonElement errorEl = it.get(KEY_ERROR);
						if (errorEl != null) {
							throw new NodeflowException(errorEl.getAsString());
						}
					}
				} else {
					to = parseNext(it.get(KEY_NEXT).getAsString());
					next(findNode(to, nodes), node, ++deep);
					break;
				}
			}
		} else {
			finished = true;
		}
	}
	
	private String parseNext(String next) {
		if(next.indexOf("=") != -1||
				next.indexOf(">") != -1||
				next.indexOf("<") != -1) {
			next = (String) ExprSupport.parseExpr(next, ctx);
		}
		return next;
	}

	private static JsonObject findNode(String id, JsonArray arr) {
		JsonObject item = null;
		for (int i = 0; i < arr.size(); i++) {
			item = arr.get(i).getAsJsonObject();
			if (item.get(KEY_ID)!=null&&id.equals(item.get(KEY_ID).getAsString())) {
				return item;
			}
		}
		return null;
	}

	public Map<String, Object> getCtx() {
		return ctx;
	}

	public String getCurNode() {
		return curNode;
	}

	public void setCurNode(String curNode) {
		this.curNode = curNode;
	}

	public String getLastNode() {
		return lastNode;
	}

	public void setLastNode(String lastNode) {
		this.lastNode = lastNode;
	}

	public static void clearComponents() {
		COMPONENTS.clear();
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	
	public JsonObject findNodeById(String nodeId) {
		return findNode(nodeId, nodes);
	}
}
