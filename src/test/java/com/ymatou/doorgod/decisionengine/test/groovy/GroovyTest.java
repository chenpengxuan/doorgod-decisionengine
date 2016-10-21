/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test.groovy;

import static javax.script.ScriptContext.ENGINE_SCOPE;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

/**
 * @author luoshiqian 2016/10/20 12:43
 */
public class GroovyTest {

    ScriptEngine engine;
    ScriptEngineManager factory;

    @Before
    public void before() {
        factory = new ScriptEngineManager();
        engine = factory.getEngineByName("groovy");
        ActiveDeviceId activeDeviceId = new ActiveDeviceId();
        factory.put("activeDeviceId",activeDeviceId);
    }

    @Test
    public void jsrTest() throws ScriptException {

        Integer sum = (Integer) engine.eval("(1..10).sum()");
        assertEquals(new Integer(55), sum);

    }

    @Test
    public void testWithArgs() throws Exception {
        engine.put("first", "HELLO");
        engine.put("second", "world");
        String result = (String) engine.eval("first.toLowerCase() + ' ' + second.toUpperCase()");
        assertEquals("hello WORLD", result);
    }

    @Test
    public void testInvokeFunc() throws Exception {
        String fact = "def factorial(n) { n == 1 ? 1 : n * factorial(n - 1) }";
        engine.eval(fact);
        Invocable inv = (Invocable) engine;
        Object[] params = {5};
        Object result = inv.invokeFunction("factorial", params);
        assertEquals(new Integer(120), result);
    }


    @Test
    public void testInvokeWithMap() throws Exception {

        String mapTest = "def isMatching(map) {" +
                "if(map['deviceId'] == 'XXXXXXXXX')" +
                "{ return true}" +
                "else {return false}" +
                "}";

        Map<String, String> map = Maps.newHashMap();
        map.put("deviceId", "XXXXXXXXX");
        map.put("IP", "127.0.0.1");

        engine.eval(mapTest);
        Invocable inv = (Invocable) engine;

        Object result = inv.invokeFunction("isMatching", map);
        assertEquals(true, result);

        map.put("deviceId", "bbbbbb");
        Object result2 = inv.invokeFunction("isMatching", map);
        assertEquals(false, result2);
    }



    @Test
    public void testInvokeWithMapAndStaticClass() throws Exception {

        String scriptStr = "def isMatching(context) {" +
                "return activeDeviceId.isValid(context['deviceId'])"+
                "}";

        ActiveDeviceId activeDeviceId = new ActiveDeviceId();

        Map<String, String> map = Maps.newHashMap();
        map.put("deviceId", "XXXXXXXXX");
        map.put("IP", "127.0.0.1");

        engine.put("activeDeviceId",activeDeviceId);
        engine.eval(scriptStr);
        Invocable inv = (Invocable) engine;

        Object result = inv.invokeFunction("isMatching", map);
        assertEquals(true, result);

        map.put("deviceId", "bbbbbb");
        Object result2 = inv.invokeFunction("isMatching", map);
        assertEquals(false, result2);
    }


    @Test
    public void testInvokeMultiFuncWithMapAndStaticClass() throws Exception {

        String scriptStr = "def isMatching(context) {" +
                "return activeDeviceId.isValid(context['deviceId'])"+
                "};" +
                " def realMathing(context){" +
                " println(context);"+
                " println(activeDeviceId);"+
                "isMatching(context)}"
                ;

        String scriptStr2 = "def isMatching(context) {" +
                "return activeDeviceId.isValid(context['deviceId'])"+
                "};" +
                " def realMathing(context){" +
                " println(context);"+
                " println(activeDeviceId);"+
                "isMatching(context)}"
                ;
        ScriptEngine engine1 = factory.getEngineByName("groovy");
        ScriptEngine engine2 = factory.getEngineByName("groovy");
        Map<String, String> map = Maps.newHashMap();
        map.put("deviceId", "XXXXXXXXX");
        map.put("IP", "127.0.0.1");
        engine1.eval(scriptStr);
        System.out.println(engine1.getContext());
        Invocable inv = (Invocable)engine1;
        engine2.eval(scriptStr2);
        Invocable inv1 = (Invocable)engine2;


        Object result = inv.invokeFunction("realMathing", map);
        assertEquals(true, result);

        map.put("deviceId", "bbbbbb");
        Object result2 = inv.invokeFunction("realMathing", map);
        assertEquals(false, result2);

        inv1.invokeFunction("realMathing",map);
    }

    static class ActiveDeviceId {
        public static boolean isValid(String deviceId) {
            if (deviceId.equals("XXXXXXXXX")) {
                return true;
            } else {
                return false;
            }
        }
    }



}
