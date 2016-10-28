/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.script;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author luoshiqian 2016/10/20 15:34
 */
@Component
public class ScriptEngines implements ApplicationListener<ApplicationReadyEvent>{

    private static final Logger logger = LoggerFactory.getLogger(ScriptEngines.class);

    private static String isMatchingScript = "def isMatching(ctx) {%s};";

    /**
     * key: ruleName
     * value:
     *      key:scriptText
     *      value:complied engine
     */
    public static final Map<String, Pair<String, ScriptEngine>> ruleScriptEngines = new ConcurrentHashMap<>();

    private static final ScriptEngineManager factory = new ScriptEngineManager();

    @Autowired
    private List<ScriptBean> scriptBeanList;

    public ScriptEngine fillScript(String ruleName, String script) {

        // 为空则无需script
        if (StringUtils.isBlank(script)) {
            // 如果更新为空
            if (ruleScriptEngines.get(ruleName) != null) {
                ruleScriptEngines.remove(ruleName);
            }
            return null;
        }
        String wrappedScript = String.format(isMatchingScript, script);

        Pair<String, ScriptEngine> pair = ruleScriptEngines.get(ruleName);
        if (null == pair) {
            ScriptEngine engine = factory.getEngineByName("groovy");
            evalScript(engine, wrappedScript);
            pair = new MutablePair<>(wrappedScript, engine);
            ruleScriptEngines.put(ruleName, pair);
        } else {
            if (!pair.getKey().equals(wrappedScript)) {
                evalScript(pair.getValue(), wrappedScript);
            }
        }
        return pair.getValue();
    }

    public boolean execIsMatching(String ruleName, Object... param) {

        Pair<String,ScriptEngine> pair = ruleScriptEngines.get(ruleName);
        if (pair == null) { // 规则 没有设置 script 返回true 需要累计
            return true;
        }
        try {
            Object result = invokeFunction(pair.getValue(), "isMatching", param);
            return (Boolean) result;
        } catch (Exception e) {// 出现异常 不累计
            logger.error("execIsMatching error. rule:{}", ruleName, e);
            return false;
        }
    }

    private void evalScript(ScriptEngine engine, String script) {
        try {
            engine.eval(script);
        } catch (ScriptException e) {
            logger.error("eval script error. script:{}", script, e);
        }
    }

    public Object invokeFunction(ScriptEngine engine, String method, Object... param) throws Exception {

        Invocable invocable = (Invocable) engine;

        Object result = invocable.invokeFunction(method, param);

        return result;
    }

    /**
     * 放入spring bean
     * @param name
     * @param obj
     */
    public static void putContext(String name, Object obj) {
        factory.put(name, obj);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        scriptBeanList.stream().forEach(scriptBean -> {
            String simpleName = AopUtils.getTargetClass(scriptBean).getSimpleName();
            if (simpleName.contains("Impl")) {
                simpleName = simpleName.replace("Impl", "");
            }
            String name = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1, simpleName.length());
            putContext(name,scriptBean);
        });
    }
}
