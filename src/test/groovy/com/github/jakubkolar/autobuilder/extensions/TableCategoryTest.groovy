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

package com.github.jakubkolar.autobuilder.extensions

import spock.lang.Specification

import java.util.stream.Collectors

class TableCategoryTest extends Specification {

    def "It defines a simple DSL for creating data tables with a header"() {
        when:
        def table = TableCategory.parseTable {
            a | b | c | d
            1 | 2 | 3 | 4
            5 | 6 | 7 | 8
        }


        def rows = table.stream().collect(Collectors.toList())
        then:
        assert rows == [
                [a: 1, b: 2, c: 3, d: 4],
                [a: 5, b: 6, c: 7, d: 8],
        ]
    }

    def "It correctly overrides the | operator for all possible values"() {
        when:
        def table = TableCategory.parseTable {
            a               | b
            1L              | 2L
            BigDecimal.ZERO | BigDecimal.ONE
            true            | false
            null            | true
            [15L] as BitSet | [31L] as BitSet
        }


        def rows = table.stream().collect(Collectors.toList())
        then:
        assert rows == [
                [a: 1L, b: 2L],
                [a: BigDecimal.ZERO, b: BigDecimal.ONE],
                [a: true, b: false],
                [a: null, b: true],
                [a: [15L] as BitSet, b: [31L] as BitSet],
        ]
    }

    def "First row must be a header"() {
        when:
        def table = TableCategory.parseTable {
            1 | 2 | 3 | 4
            5 | 6 | 7 | 8
        }


        then:
        def e = thrown(RuntimeException)
        assert e.message?.toLowerCase().contains("not a header")
        assert e.message?.contains("TableRow[1, 2, 3, 4]")
    }

    def "Empty table must have at least one row (the header)"() {
        when:
        def table = TableCategory.parseTable {

        }


        then:
        def e = thrown(RuntimeException)
        assert e.message?.contains("at least 1 row")
    }

    def "Every table must have at least two columns"() {
        when:
        def table = TableCategory.parseTable {
            a
            1
        }


        then:
        def e = thrown(RuntimeException)
        assert e.message?.toLowerCase().contains("at least 2 columns")
    }

}
