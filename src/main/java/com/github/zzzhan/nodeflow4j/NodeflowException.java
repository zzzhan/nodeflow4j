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

/**
 * @author zzzhan
 *
 */
public class NodeflowException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] params;

	public NodeflowException(String code, String... params) {
		super(code);
		this.params = params;
	}

	public NodeflowException(String code, Throwable t) {
		super(code, t);
	}

	public NodeflowException(String code, Throwable t, String... params) {
		super(code, t);
		this.params = params;
	}

	public String[] getParams() {
		return params;
	}

	public void setParams(String[] params) {
		this.params = params;
	}
}
