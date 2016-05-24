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

package com.github.jakubkolar.autobuilder.specification

import com.github.jakubkolar.autobuilder.AutoBuilder
import spock.lang.Specification

class ResolveSpecificTypeIT extends Specification {

    def "Resolve the specific type directly"() {
        given:
        def desiredValue = new SpecificType(BigDecimal.ONE)

        when:
        def instance = AutoBuilder.instanceOf(SpecificType)
                .with(SpecificType, desiredValue)
                .build()

        then:
        assert instance.is(desiredValue)
    }

    def "Specify a desired value for a field of a given type"() {
        given:
        def desiredValue = new SpecificType(BigDecimal.ONE)

        when:
        def instance = AutoBuilder.instanceOf(SpecificTypeDTO)
                .with(SpecificType, desiredValue)
                .build()

        then:
        assert instance.specificTypeField1.is(desiredValue)
    }

    def "Specified value is used for all fields of a given type"() {
        given:
        def desiredValue = new SpecificType(BigDecimal.ONE)

        when:
        def instance = AutoBuilder.instanceOf(SpecificTypeDTO)
                .with(SpecificType, desiredValue)
                .build()

        then:
        assert instance.specificTypeField1.is(desiredValue)
        assert instance.specificTypeField2.is(desiredValue)
    }

    def "Specified value is used for nested fields anywhere in the object graph"() {
        given:
        def desiredValue = new SpecificType(BigDecimal.ONE)

        when:
        def instance = AutoBuilder.instanceOf(NestedSpecificTypeDTO)
                .with(SpecificType, desiredValue)
                .build()

        then:
        assert !instance.notNested.is(desiredValue)
        assert instance.notNestedSpecific.is(desiredValue)
        assert instance.nested.specificTypeField1.is(desiredValue)
        assert instance.nested.specificTypeField2.is(desiredValue)
    }
}
