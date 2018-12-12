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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author zzzhan
 *
 */
public class NodeflowEngineTest {

	/**
	 * Test method for {@link com.github.zzzhan.nodeflow4j.NodeflowEngine#execute(java.util.Map)}.
	 */
	@Test
	public void testExecuteMapOfStringObject() {
		String nfJson;
		try {
			nfJson = IOUtils.toString(NodeflowEngineTest.class.getResourceAsStream("/NodeflowTest.nf"));
			Map<String, Object> params = new HashMap<String,Object>();
			params.put("foo", 1);
			NodeflowEngine nf = new NodeflowEngine(nfJson);
			nf.execute(params);
			assertEquals("n3", nf.getCurNode());
			Map<String, Object> output = nf.getOutput();
			assertEquals(2048, output.get("bar"));

			params = new HashMap<String,Object>();			
			params.put("foo", 2);
			nf = new NodeflowEngine(nfJson);
			long start = System.currentTimeMillis();
			nf.execute(params);
			long delay = System.currentTimeMillis()-start;
			assertEquals("n4", nf.getCurNode());
			assertTrue(delay>=3000);
			output = nf.getOutput();
			assertEquals(2024, output.get("bar"));

			params = new HashMap<String,Object>();		
			params.put("foo", 3);
			nf = new NodeflowEngine(nfJson);
			nf.execute(params);
			assertEquals("n7", nf.getCurNode());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NodeflowException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
