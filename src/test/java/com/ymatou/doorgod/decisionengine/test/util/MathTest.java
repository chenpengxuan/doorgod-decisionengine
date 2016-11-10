/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test.util;

import com.google.common.math.LongMath;
import org.junit.Test;

import java.math.RoundingMode;

/**
 * @author luoshiqian 2016/11/10 18:43
 */
public class MathTest {

    @Test
    public void mathTest(){
        System.out.println(LongMath.sqrt(100, RoundingMode.HALF_DOWN));
        System.out.println(LongMath.pow(3,4));
        System.out.println(LongMath.pow(2,3));
        System.out.println(LongMath.pow(2,0));
    }
}
