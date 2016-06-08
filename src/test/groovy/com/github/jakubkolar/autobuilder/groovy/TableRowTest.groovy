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

package com.github.jakubkolar.autobuilder.groovy

import spock.lang.Specification

@Newify(Variable)
class TableRowTest extends Specification {

    def "It is created from the first two elements"() {
        when:
        def row = TableRow.of(Variable('A'), 'B')

        then:
        assert row.toString() == 'TableRow[Variable[A], B]'
    }

    def "It allows new elements to be added using the | operator"() {
        given:
        def row = TableRow.of(Variable('A'), 'B')

        when:
        row |= "C"

        then:
        assert row.toString() == 'TableRow[Variable[A], B, C]'
    }

    def "Row with variables only is considered a header row"() {
        given:
        def row = TableRow.of(Variable('a'), Variable('b')) | Variable('c')

        when:
        def header = row.toHeader()

        then:
        assert header == [a: 0, b: 1, c:2]
    }

    def "Row can be converted to properties using positions from a header row"() {
        given:
        def row = TableRow.of('value of a', 'value of b') | 'value of c'

        when:
        def properties = row.toProperties([a: 0, b: 1, c: 2])

        then:
        assert properties == [a: 'value of a', b: 'value of b', c: 'value of c']
    }

    def "Row with a non-variable cannot be a header"() {
        given:
        def row = TableRow.of(Variable('a'), Variable('b')) | 'c'

        when:
        row.toHeader()

        then:
        def e = thrown(IllegalStateException)
        assert e.message.contains("element 'c'")
    }

    def "Every position defined by a header must exist in the row"() {
        given:
        def row = TableRow.of('value of a', 'value of b') | 'value of c'

        when:
        row.toProperties([a: 0, b: 1, c: 2, xyz: 99])

        then:
        def e = thrown(IllegalStateException)
        assert e.message?.contains("property 'xyz'")
    }

}
