/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Jakub Kolar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.jakubkolar.autobuilder;

import com.github.jakubkolar.autobuilder.api.BuilderDSL;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;

import java.io.Serializable;

public class AutoBuilderTest {

    @Test
    public void testNothingJustPrintTheFields() {
        TestClass t = AutoBuilder.instanceOf(TestClass.class).build();
        System.out.println(t);
    }

    public static class TestClass {
        private String string;
        private Serializable serializable;
        private Object o;
        private Integer i;
        private short aShortt;
        private EmptyEnum e;
        private int[] array;
        private static final boolean [] a = null;

        enum EmptyEnum {  }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @Test
    public void customerAndAddress_TestFromJavadoc() {
        BuilderDSL<Customer> aCustomer = AutoBuilder.instanceOf(Customer.class);
        Customer c = aCustomer.with("address", "Vodickova").build();
        System.out.println(c);
    }

    private static class Customer {
        String name;
        Address address;

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    private static class Address {
        String street;
        String city;
        String postalCode;

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
