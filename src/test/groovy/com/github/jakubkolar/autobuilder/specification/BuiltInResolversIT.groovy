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

class BuiltInResolversIT extends Specification {

    def "It should resolve an instance with basic fields"() {
        expect:
        AutoBuilder.instanceOf(BuiltInResolversDTO).build() != null
    }

    def "It should resolve non-null values for the fields"() {
        when:
        def instance = AutoBuilder.instanceOf(BuiltInResolversDTO).build()

        then:
        instance.with {
            // Strings
            assert strField != null
            assert charSequenceField != null
            assert strBuilderField != null

            // Primitives
            assert byteField != null
            assert shortField != null
            assert intField != null
            assert longField != null
            assert floatField != null
            assert doubleField != null
            assert charField != null

            // Wrappers
            assert byteWrapperField != null
            assert shortWrapperField != null
            assert intWrapperField != null
            assert longWrapperField != null
            assert floatWrapperField != null
            assert doubleWrapperField != null
            assert charWrapperField != null

            // Enum
            assert enumField != null
            assert emptyEnumField == null // Only valid value here

            // Collections & maps
            assert collectionField != null
            assert listField != null
            assert setField != null
            assert sortedSetField != null
            assert mapField != null
            assert sortedMapField != null

            // Arrays
            assert objArrayField != null
            assert objArrayField2 != null
            assert primitiveArrayField != null

            // Special
            assert objectField != null
            assert numberField != null
            assert serializableField != null

            // Comparables
            assert cStrField != null
            assert cIntegerField != null
            assert cEnumField != null
            assert cEmptyEnumField == null // The same as emptyEnumField
            it
        }
    }

}
