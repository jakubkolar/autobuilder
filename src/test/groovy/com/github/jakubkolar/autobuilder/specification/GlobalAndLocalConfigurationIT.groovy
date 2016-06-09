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
import com.github.jakubkolar.autobuilder.spi.ValueResolver
import spock.lang.Specification

import javax.annotation.Nullable

class GlobalAndLocalConfigurationIT extends Specification {

    def setupSpec() {
        // Needs to be done only once per JVM run // TODO: AB-025
        AutoBuilder.registerValue("ValueResolverExampleDTO.named", "Global Named Value")
        AutoBuilder.registerResolver { type, typeInfo, name, annotations ->
            if (name == "ValueResolverExampleDTO.valueRes") {
                return "Global Value Resolver"
            }
            throw new UnsupportedOperationException("Cannot resolve")
        }

        // Named overrides the resolver
        AutoBuilder.registerValue("ValueResolverExampleDTO.override", "Global Named Override")
        AutoBuilder.registerResolver { type, typeInfo, name, annotations ->
            if (name == "ValueResolverExampleDTO.override") {
                return "Global Value Resolver Override"
            }
            throw new UnsupportedOperationException("Cannot resolve")
        }

        // Field ValueResolverExampleDTO.annotated cannot be resolved without a resolver
        AutoBuilder.registerResolver { type, typeInfo, name, annotations ->
            if (name == "ValueResolverExampleDTO.annotated") {
                return null
            }
            throw new UnsupportedOperationException("Cannot resolve")
        }
    }

    def "Use a global named value"() {
        when:
        def instance = AutoBuilder.instanceOf(ValueResolverExampleDTO).build()

        then:
        assert instance.named == "Global Named Value"
    }

    def "Local named value overrides global"() {
        when:
        def instance = AutoBuilder.instanceOf(ValueResolverExampleDTO)
                .with("named", "Local Named Value")
                .build()

        then:
        assert instance.named == "Local Named Value"
    }

    def "Use a global value resolver"() {
        when:
        def instance = AutoBuilder.instanceOf(ValueResolverExampleDTO).build()

        then:
        assert instance.valueRes == "Global Value Resolver"
    }

    def "Local value resolver overrides global value resolver"() {
        when:
        def instance = AutoBuilder.instanceOf(ValueResolverExampleDTO)
            .with({ type, typeInfo, name, annotations ->
                if (name == "ValueResolverExampleDTO.valueRes") {
                    return "Local Value Resolver"
                }
                throw new UnsupportedOperationException("Cannot resolve")
            } as ValueResolver)
            .build()

        then:
        instance.valueRes == "Local Value Resolver"
    }

    def "Local value resolver overrides global named value"() {
        when:
        def instance = AutoBuilder.instanceOf(ValueResolverExampleDTO)
            .with({ type, typeInfo, name, annotations ->
                if (name == "ValueResolverExampleDTO.named") {
                    return "Local Value Resolver"
                }
                throw new UnsupportedOperationException("Cannot resolve")
            } as ValueResolver)
            .build()

        then:
        instance.named == "Local Value Resolver"
    }

    def "Local named value overrides local value resolver"() {
        when:
        def instance = AutoBuilder.instanceOf(ValueResolverExampleDTO)
            .with("named", "Local Named Value")
            .with({ type, typeInfo, name, annotations ->
                if (name == "ValueResolverExampleDTO.named") {
                    return "Local Value Resolver"
                }
                throw new UnsupportedOperationException("Cannot resolve")
            } as ValueResolver)
            .build()

        then:
        instance.named == "Local Named Value"
    }

    def "Global named value overrides global value resolver"() {
        when:
        def instance = AutoBuilder.instanceOf(ValueResolverExampleDTO).build()

        then:
        assert instance.override == "Global Named Override"
    }

    @SuppressWarnings(["GroovyAssignabilityCheck", "GroovyUntypedAccess", "GroovyPointlessArithmetic"])
    def "Value resolver is correctly invoked"() {
        given:
        def mock = Mock(ValueResolver);

        when:
        AutoBuilder.instanceOf(ValueResolverExampleDTO).with(mock).build()

        then:
        1 * mock.resolve(_, _, "ValueResolverExampleDTO.annotated", *_) >> { type, typeInfo, name, annotations ->
            assert type == Comparable.class
            assert typeInfo.get().toString() == "java.lang.Comparable<? extends java.lang.Comparable<? super T>>"
            assert annotations.collect { it.annotationType() } == [Nullable, Deprecated]

            "Mocked value"
        }
        mock.resolve(_, _, _, *_) >> { throw new UnsupportedOperationException("Cannot resolve") }
    }
}
