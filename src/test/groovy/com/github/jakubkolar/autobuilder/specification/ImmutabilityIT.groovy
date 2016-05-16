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
import com.github.jakubkolar.autobuilder.api.BuilderDSL
import spock.lang.Specification

class ImmutabilityIT extends Specification {

    static BuilderDSL<ImmutabilityExampleDTO> noGlobalConfBuilder

    def setupSpec() {
        noGlobalConfBuilder = AutoBuilder.instanceOf(ImmutabilityExampleDTO)
        // Can only be done once per JVM run // TODO: AB-025
        AutoBuilder.registerValue("ImmutabilityExampleDTO.globalOverride", "Global Custom Value")
    }

    def "Builder is immutable, new builder is disconnected"() {
        given:
        def builder = AutoBuilder.instanceOf(ImmutabilityExampleDTO)

        when:
        def newBuilder = builder.with("field", "Custom Value")

        then:
        assert !newBuilder.is(builder)
        assert builder.build().field != "Custom Value"
        assert newBuilder.build().field == "Custom Value"
    }

    def "Change of the global config does not affect existing builders"() {
        expect:
        assert noGlobalConfBuilder.build().globalOverride != "Global Custom Value"
        assert AutoBuilder.instanceOf(ImmutabilityExampleDTO).build().globalOverride == "Global Custom Value"
    }

    def "Created products are disconnected from the builder and do not affect each other"() {
        given:
        def builder = AutoBuilder.instanceOf(ImmutabilityExampleDTO).with("field", "Custom Value")

        when:
        def dto1 = builder.build()
        def dto2 = builder.build()
        builder = builder.with("globalOverride", "Custom Value 2")
        def dto3 = builder.build()
        dto1.field = "Custom Value 3"

        then:
        assert !dto1.is(dto2)
        assert !dto1.is(dto3)
        assert !dto2.is(dto3)
        assert dto1.field == "Custom Value 3"
        assert dto1.globalOverride != "Custom Value 2"
        assert dto2.field == "Custom Value"
        assert dto2.globalOverride != "Custom Value 2"
        assert dto3.field == "Custom Value"
        assert dto3.globalOverride == "Custom Value 2"
    }
}
