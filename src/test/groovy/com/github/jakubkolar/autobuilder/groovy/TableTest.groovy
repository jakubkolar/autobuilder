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

import java.util.stream.Collectors

@Newify(Variable)
class TableTest extends Specification {

    def "It provides a readable to-string for debugging"() {
        given:
        def header = TableRow.of(Variable('a'), Variable('b'))
        def row1 = TableRow.of(1, 2)
        def row2 = TableRow.of(3, 4)

        when:
        def table = Table.of([header, row1, row2])

        then:
        assert table.toString() ==
                "Table[header=[a, b],rows=[TableRow[1, 2], TableRow[3, 4]]]"
    }

    def "It maps the rows to property values using the header"() {
        given:
        def header = TableRow.of(Variable('a'), Variable('b'))
        def row1 = TableRow.of(1, 2)
        def row2 = TableRow.of(3, 4)
        def table = Table.of([header, row1, row2])

        when:
        def contents = table.stream().collect(Collectors.toList())

        then:
        assert contents == [
                [a: 1, b: 2],
                [a: 3, b: 4],
        ]
    }

}
