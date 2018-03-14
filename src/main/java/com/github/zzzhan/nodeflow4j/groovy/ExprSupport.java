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

import java.util.Hashtable;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * @author zzzhan
 *
 */
public class ExprSupport {

	private static final Object lock = new Object();
    private static final GroovyShell shell;
 
    private static Hashtable<String, Script> cache = new Hashtable<String, Script>();
    static {
        CompilerConfiguration cfg = new CompilerConfiguration();
        cfg.setScriptBaseClass(MyBasicScript.class.getName());
  
        shell = new GroovyShell(cfg);
    }
 
    public static Object parseExpr(String expr) {
        Script s = getScriptFromCache(expr);
        return s.run();
    }
 
    public static Object parseExpr(String expr, Map<?, ?> map) {
        Binding binding = new Binding(map);
        Script script = getScriptFromCache(expr);
        script.setBinding(binding);
        return script.run();
    }
 
    private static Script getScriptFromCache(String expr) {
        if (cache.contains(expr)) {
            return cache.get(expr);
        }
        synchronized (lock) {
            if (cache.contains(expr)) {
                return cache.get(expr);
            }
            Script script = shell.parse(expr);
            cache.put(expr, script);
            return script;
        }
    }
}
