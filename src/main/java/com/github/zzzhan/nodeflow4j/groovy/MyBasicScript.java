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
package com.github.zzzhan.nodeflow4j.groovy;

import java.lang.reflect.Method;

import groovy.lang.Script;

/**
 * @author zzzhan
 *
 */
public class MyBasicScript extends Script {
	@Override
	public Object run() {
		// show usage
		Method[] methods = MyBasicScript.class.getDeclaredMethods();
		StringBuilder sb = new StringBuilder();
		for (Method method : methods) {
			sb.append(method);
		}

		return sb.substring(0, sb.length() - 1);
	}

	public static Object nvl(Object str, Object val) {
		return str == null || "".equals(str) ? val : str;
	}

	public static boolean empty(Object str) {
		return str == null || "".equals(str.toString().trim());
	}

	public static double min(double... val) {
		double min = val[0];
		for (int i = 1; i < val.length; i++) {
			min = Math.min(min, val[i]);
		}
		return min;
	}

	public static double max(double... val) {
		double max = val[0];
		for (int i = 1; i < val.length; i++) {
			max = Math.max(max, val[i]);
		}
		return max;
	}
}
