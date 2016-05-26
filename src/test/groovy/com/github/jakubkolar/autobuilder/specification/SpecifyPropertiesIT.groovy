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

class SpecifyPropertiesIT extends Specification {

    def "Specify a single property"() {
        when:
        def instance = AutoBuilder.instanceOf(BuiltInResolversDTO)
                .with("strField", "Custom Value")
                .build()

        then:
        assert instance.strField == "Custom Value"
    }

    def "Specify multiple properties"() {
        given:
        def sb = new StringBuilder('SB')
        def obj = new Object()

        when:
        def instance = AutoBuilder.instanceOf(BuiltInResolversDTO)
        // Strings
                .with('strField', 'Str')
                .with('charSequenceField', 'ChSeq')
                .with('strBuilderField', sb)
        // Primitives
                .with('byteField', 0xFF as byte)
                .with('shortField', 0 as short)
                .with('intField', 1)
                .with('longField', 2L)
                .with('floatField', 2.71828f)
                .with('doubleField', 3.14159d)
                .with('charField', 'c' as char)
        // Wrappers
                .with('byteWrapperField', 0xFF as Byte)
                .with('shortWrapperField', 0 as Short)
                .with('intWrapperField', 1)
                .with('longWrapperField', 2L)
                .with('floatWrapperField', 2.71828f)
                .with('doubleWrapperField', 3.14159d)
                .with('charWrapperField', 'c' as Character)
        // Enum
                .with('enumField', NonEmptyEnum.CONST1)
                .with('emptyEnumField', null)
        // Collections & maps
                .with('collectionField', ['A', 'B'])
                .with('listField', ['A', 'B'])
                .with('setField', [1, 2].toSet())
                .with('sortedSetField', [1, 2] as TreeSet)
                .with('mapField', [a: 1, b: 2])
                .with('sortedMapField', [a: 1, b: 2] as TreeMap)
        // Arrays
                .with('objArrayField', [1, 'A'] as Object[])
                .with('objArrayField2', ['a', 'b'] as Character[])
                .with('primitiveArrayField', ['c', 'd'] as char[])
        // Special
                .with('objectField', obj)
                .with('numberField', BigDecimal.valueOf(999))
                .with('serializableField', BigInteger.valueOf(123))
        // Comparables
                .with('cStrField', 'cStr')
                .with('cIntegerField', 37)
                .with('cEnumField', NonEmptyEnum.CONST2)
                .with('cEmptyEnumField', null)
        .build()

        then:
        instance.with {
        // Strings
            assert strField == 'Str'
            assert charSequenceField == 'ChSeq'

            assert strBuilderField.is(sb)
        // Primitives
            assert byteField == 0xFF as byte
            assert shortField == 0 as short
            assert intField == 1
            assert longField == 2L
            assert floatField == 2.71828f
            assert doubleField == 3.14159d
            assert charField == 'c' as char
        // Wrappers
            assert byteWrapperField == 0xFF as Byte
            assert shortWrapperField == 0 as Short
            assert intWrapperField == 1
            assert longWrapperField == 2L
            assert floatWrapperField == 2.71828f
            assert doubleWrapperField == 3.14159d
            assert charWrapperField == 'c' as Character
        // Enum
            assert enumField == NonEmptyEnum.CONST1
            assert emptyEnumField == null
        // Collections & maps
            assert collectionField == ['A', 'B']
            assert listField == ['A', 'B']
            assert setField == [1, 2].toSet()
            assert sortedSetField == [1, 2] as TreeSet
            assert mapField == [a: 1, b: 2]
            assert sortedMapField == [a: 1, b: 2] as TreeMap
        // Arrays
            assert objArrayField == [1, 'A'] as Object[]
            assert objArrayField2 == ['a', 'b'] as Character[]
            assert primitiveArrayField == ['c', 'd'] as char[]
        // Special
            assert objectField.is(obj)
            assert numberField == BigDecimal.valueOf(999)
            assert serializableField == BigInteger.valueOf(123)
        // Comparables
            assert cStrField == 'cStr'
            assert cIntegerField == 37
            assert cEnumField == NonEmptyEnum.CONST2
            it
        }
    }

    def "Specify multiple properties using a Map"() {
        when:
        def instance = AutoBuilder.instanceOf(BuiltInResolversDTO)
                .with(strField: 'ABC', intField: 1, primitiveArrayField: ['x', 'y'] as char[])
                .build()

        then:
        assert instance.strField == 'ABC'
        assert instance.intField == 1
        assert instance.primitiveArrayField == ['x', 'y'] as char[]
    }

    def "Create the object directly"() {
         use(AutoBuilder) {
            when:
            def instance = BuiltInResolversDTO.class.create(intField: 1, strField: 'ABC')

            then:
            assert instance.intField == 1
            assert instance.strField == 'ABC'
        }
    }

    def "Specify nested properties using the dot notation"() {
        when:
        def instance = AutoBuilder.instanceOf(NestedExampleDTO)
                .with("nested.strField", "Custom Value")
                .build()

        then:
        assert instance.nested.strField == "Custom Value"
    }
}
