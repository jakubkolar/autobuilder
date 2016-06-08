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

class TableDSLTest extends Specification {

    def builder = new BuilderStub()

    def "It defines a simple assignment DSL for creating objects using AutoBuilder"() {
        when:
        def result = TableDSL.parseSingle builder, {
            a = 1
            b = 2
            c = 3
        }

        then:
        assert result == [a: 1, b: 2, c: 3]
    }

    def "Nested properties in the assignment DSL are specified using a dot '.'"() {
        when:
        def result = TableDSL.parseSingle builder, {
            a = 1
            b.c = 2
            d.e.f = 3
        }

        then:
        assert result == [a: 1, 'b.c': 2, 'd.e.f': 3]
    }


    def "It defines a simple DSL for creating data tables with a header"() {
        when:
        def table = TableDSL.parseTable builder, {
            a | b | c | d
            1 | 2 | 3 | 4
            5 | 6 | 7 | 8
        }

        then:
        assert table == [
                [a: 1, b: 2, c: 3, d: 4],
                [a: 5, b: 6, c: 7, d: 8],
        ]
    }

    def "Nested properties in the table DSL are specified using a dot '.'"() {
        when:
        def table = TableDSL.parseTable builder, {
            a | b.c | d.e.f
            1 | 2   | 3
            4 | 5   | 6
        }

        then:
        assert table == [
                ['a': 1, 'b.c': 2, 'd.e.f': 3],
                ['a': 4, 'b.c': 5, 'd.e.f': 6],
        ]
    }

    def "Table DSL and assignment DSL can be used together"() {
        when:
        def table = TableDSL.parseTable builder, {
            x = 'X'
            y.z = 41.99

            a | b.c
            1 | 2
            3 | 4
        }

        then:
        assert table == [
                ['a': 1, 'b.c': 2, 'x': 'X', 'y.z': 41.99],
                ['a': 3, 'b.c': 4, 'x': 'X', 'y.z': 41.99],
        ]
    }

    def "Table DSL correctly interprets the | operator for all possible values"() {
        when:
        def table = TableDSL.parseTable builder, {
            a               | b
            1L              | 2L
            BigDecimal.ZERO | BigDecimal.ONE
            true            | false
            null            | true
            null            | null
            [1,2,3]         | [4,5]
            [15L] as BitSet | [31L] as BitSet
        }

        then:
        assert table == [
                [a: 1L, b: 2L],
                [a: BigDecimal.ZERO, b: BigDecimal.ONE],
                [a: true, b: false],
                [a: null, b: true],
                [a: null, b: null],
                [a: [1, 2, 3], b: [4, 5]],
                [a: [15L] as BitSet, b: [31L] as BitSet],
        ]
    }

    def "First row must be a header"() {
        when:
        TableDSL.parseTable builder, {
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
        TableDSL.parseTable builder, {

        }


        then:
        def e = thrown(RuntimeException)
        assert e.message?.contains("at least 1 row")
    }

    def "Every table must have at least two columns"() {
        when:
        TableDSL.parseTable builder, {
            a
            1
        }


        then:
        def e = thrown(RuntimeException)
        assert e.message?.toLowerCase().contains("at least 2 columns")
    }

}
