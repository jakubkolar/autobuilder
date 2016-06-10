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

class GroovyObjectsIT extends Specification {

    def "Resolve a java based groovy object"() {
        when:
        def javaPOGO = AutoBuilder.instanceOf(JavaPOGO).build()

        then:
        assert javaPOGO != null
        assert javaPOGO.someField != null
        assert javaPOGO.metaClass != null
    }

    def "If java based groovy object defines its own meta-class it is not resolved"() {
        when:
        AutoBuilder.instanceOf(JavaPOGO.WithAnotherMetaClassField).build()

        then:
        def e = thrown(UnsupportedOperationException)
        assert e.message?.contains("groovy.lang.MetaClass")
        assert e.message?.contains("WithAnotherMetaClassField.metaClass")
    }

    def "Resolve a normal groovy based groovy object"() {
        when:
        def groovyPOGO = AutoBuilder.instanceOf(GroovyPOGO).build()

        then:
        assert groovyPOGO != null
        assert groovyPOGO.someField != null
        assert groovyPOGO.metaClass != null
    }

}
