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
import com.google.common.collect.ImmutableCollection
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSortedMap
import com.google.common.collect.ImmutableSortedSet
import spock.lang.Specification

class LibraryExtensionIT extends Specification {

    def "Big Decimal Resolver"() {
        when:
        def instance = AutoBuilder.instanceOf(ExtensionExampleDTO).build()

        then:
        assert instance.decimalField != null
    }

    def "Guava Resolver"() {
        when:
        def instance = AutoBuilder.instanceOf(ExtensionExampleDTO).build()

        then:
        instance.with {
            collectionField != null
            listField != null
            setField != null
            sortedSetField != null
            mapField != null
            sortedMapField != null
            it
        }
    }

    def "Custom extension resolver loaded by ServiceLoader"() {
        when:
        def instance = AutoBuilder.instanceOf(ExtensionExampleDTO).build()

        then:
        assert instance.extensionTestResolverField == 'Custom Value For @Incubating Field'
    }

}
