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
package com.github.zzzhan.nodeflow4j.component;

import java.util.Map;

import com.github.zzzhan.nodeflow4j.NfComponent;

/**
 * @author zzzhan
 *
 */
public class TestCustomComponent implements NfComponent {

	/* (non-Javadoc)
	 * @see com.github.zzzhan.nodeflow4j.NfComponent#execute(java.util.Map, java.util.Map)
	 */
	public void execute(Map<String, Object> node, Map<String, Object> ctx) {
		int bar = (Integer)ctx.get("bar");
		ctx.put("bar", bar*2);
	}

	/* (non-Javadoc)
	 * @see com.github.zzzhan.nodeflow4j.NfComponent#spiId()
	 */
	public String spiId() {
		return "testCustomComponent";
	}

}
