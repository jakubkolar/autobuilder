/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Jakub Kolar
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

package com.github.jakubkolar.autobuilder.bug

import com.github.jakubkolar.autobuilder.AutoBuilder
import spock.lang.Specification

class Supertypes_Issue08Test extends Specification {

    static def myGlobalList = ['ABC', 'DEF'] as LinkedList<String>

    def setupSpec() {
        // Can only be done once per JVM run // TODO: AB-025
        AutoBuilder.registerValue("SupertypeFields.globalOField", myGlobalList)
        AutoBuilder.registerValue("SupertypeFields.globalQField", myGlobalList)
    }

    def "BuilderDSL.with(String, Object) does not consider the supertype of the passed-in object"() {
        given:
        def myList = ['ABC', 'DEF'] as LinkedList<String>

        when:
        def instance = AutoBuilder.instanceOf(SupertypeFields)
                .with('abstractListField', myList)
                .with('abstractCollectionField', myList)
                .with('objectField', myList)
                .with('listField', myList)
                .with('cloneableField', myList)
                .with('queueField', myList)
                .with('iterableField', myList)
                .build()

        then:
        instance.with {
            assert abstractListField.is(myList)
            assert abstractCollectionField.is(myList)
            assert objectField.is(myList)
            assert listField.is(myList)
            assert cloneableField.is(myList)
            assert queueField.is(myList)
            assert iterableField.is(myList)
            it
        }
    }

    def "BuilderDSL.with(Map<String, Object>) does not consider the supertypes of the properties"() {
        given:
        def myList = ['ABC', 'DEF'] as LinkedList<String>

        when:
        def instance = AutoBuilder.instanceOf(SupertypeFields)
            .with(
                abstractListField: myList,
                abstractCollectionField: myList,
                objectField: myList,
                listField: myList,
                cloneableField: myList,
                queueField: myList,
                iterableField: myList
            )
            .build()

        then:
        instance.with {
            assert abstractListField.is(myList)
            assert abstractCollectionField.is(myList)
            assert objectField.is(myList)
            assert listField.is(myList)
            assert cloneableField.is(myList)
            assert queueField.is(myList)
            assert iterableField.is(myList)
            it
        }
    }

    def "AutoBuilder.create(Class, Map) does not consider the supertypes of the properties"() {
        given:
        def myList = ['ABC', 'DEF'] as LinkedList<String>

        when:
        def instance = AutoBuilder.instanceOf(SupertypeFields).with(
                abstractListField: myList,
                abstractCollectionField: myList,
                objectField: myList,
                listField: myList,
                cloneableField: myList,
                queueField: myList,
                iterableField: myList,
        ).build()

        then:
        instance.with {
            assert abstractListField.is(myList)
            assert abstractCollectionField.is(myList)
            assert objectField.is(myList)
            assert listField.is(myList)
            assert cloneableField.is(myList)
            assert queueField.is(myList)
            assert iterableField.is(myList)
            it
        }
    }

    def "AutoBuilder.registerValue(String, Object) does not consider of the passed-in object"() {
        given:
        def myList = null

        when:
        def instance = AutoBuilder.instanceOf(SupertypeFields)
                .with('abstractListField', myList)
                .with('abstractCollectionField', myList)
                .with('objectField', myList)
                .with('listField', myList)
                .with('cloneableField', myList)
                .with('queueField', myList)
                .with('iterableField', myList)
                .build()

        then:
        instance.with {
            assert globalOField.is(myGlobalList)
            assert globalQField.is(myGlobalList)
            it
        }
    }

}
