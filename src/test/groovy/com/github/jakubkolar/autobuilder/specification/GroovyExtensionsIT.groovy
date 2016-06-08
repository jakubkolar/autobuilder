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

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

import static com.github.jakubkolar.autobuilder.AutoBuilder.a
import static com.github.jakubkolar.autobuilder.specification.City.*

class GroovyExtensionsIT extends Specification {

    def "Create a single object directly"() {
        when:
        Person person = Person.of {
            (lastName, firstName) = ['Granger', 'Hermione']
            age = 18
            address.city = LONDON
        }

        then:
        person.with {
            assert lastName == 'Granger'
            assert firstName == 'Hermione'
            assert age == 18
            assert address.city == LONDON
            assert id != null
            assert created != null
            it
        }
    }

    def "Builder supports coercion to the target type and its supertypes"() {
        when:
        def person = a(Person).with(lastName: 'Granger', firstName: 'Hermione') as Person
        def str = a(String) as CharSequence

        then:
        assert person instanceof Person
        assert str instanceof CharSequence
        assert [person.lastName, person.firstName] == ['Granger', 'Hermione']
    }

    def "Builder fails when trying to coerce to a non-compatible type"() {
        when:
        a(Person).with(lastName: 'Granger', firstName: 'Hermione') as Address

        then:
        def e = thrown(GroovyCastException)
        assert e.message?.contains("Cannot cast")
        assert e.message?.contains("${Person.name}")
        assert e.message?.contains("${Address.name}")
    }

    def "Create a collection from a simple table"() {
        when:
        def people = a Person with(emailVerified: true) fromTable {
            login      | email
            'harryp'   | 'seeker731@gryffindor.com'
            'grangerh' | 'readinglover@spew.org'
            'albus'    | 'apwbd@beards.com'
        }

        then:
        people.each { u ->
            assert u.id != null
            assert u.created != null
            assert u.address != null
            assert !u.deletionFlag
            assert u.emailVerified
        }
        def actual = prettyPrintSimple(people)
        def expected = prettyPrint(
                'Person[harryp, seeker731@gryffindor.com]',
                'Person[grangerh, readinglover@spew.org]',
                'Person[albus, apwbd@beards.com]'
        )
        assert actual == expected
    }

    def "Create a collection from a simple table - using the class"() {
        when:
        List<Person> people = Person.fromTable {
            emailVerified = true

            //---------------------------------------
            login      | email
            'harryp'   | 'seeker731@gryffindor.com'
            'grangerh' | 'readinglover@spew.org'
            'albus'    | 'apwbd@beards.com'
            //---------------------------------------

            deletionFlag = true
        }

        then:
        people.each { u ->
            assert u.id != null
            assert u.created != null
            assert u.address != null
            assert u.deletionFlag
            assert u.emailVerified
        }
        def actual = prettyPrintSimple(people)
        def expected = prettyPrint(
                'Person[harryp, seeker731@gryffindor.com]',
                'Person[grangerh, readinglover@spew.org]',
                'Person[albus, apwbd@beards.com]'
        )
        assert actual == expected
    }

    def "Create an empty collection from an empty table"() {
        when:
        def people = a Person fromTable {
            login | email
        }

        then:
        assert people.empty
    }

    def "Create an empty collection from an empty table - using the class"() {
        when:
        def people = Person.fromTable {
            login | email
        }

        then:
        assert people.empty
    }


    def "Create a collection from a table with nested properties"() {
        given:
        def today = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC)

        when:
        def people = a Person with(Instant, today) fromTable {
            firstName  | middleNames            | lastName     | age | address.city
            'Harry'    | ['James']              | 'Potter'     | 17  | LITTLE_WHINGING
            'Ronald'   | ['Bilius']             | 'Weasley'    | 17  | OTTERY_ST_CATCHPOLE
            'Hermione' | ['Jean']               | 'Granger'    | 18  | LONDON
            'Albus'    | ['Percival','W.','B.'] | 'Dumbledore' | 115 | HOGWARTS
        }

        then:
        people.each { u ->
            assert u.id != null
            assert u.created.is(today)
            assert u.lastModified.is(today)
        }
        def actual = prettyPrint(people)
        def expected = prettyPrint(
                'Person[name=Harry James Potter, age=17, city=LITTLE_WHINGING]',
                'Person[name=Ronald Bilius Weasley, age=17, city=OTTERY_ST_CATCHPOLE]',
                'Person[name=Hermione Jean Granger, age=18, city=LONDON]',
                'Person[name=Albus Percival W. B. Dumbledore, age=115, city=HOGWARTS]',
        )
        assert actual == expected
    }

    def "Create a collection from a table with nested properties - using the class"() {
        when:
        // TODO: So far there is no way to invoke other methods on the contextual BuilderDSL
        // (like .with(Instant, today) in the previous test)
        List<Person> people = Person.fromTable {
            // age = 17 FIXME: AB-025 Local Named resolver should allow registering a field twice

            firstName  | middleNames            | lastName     | age | address.city
            'Harry'    | ['James']              | 'Potter'     | 17  | LITTLE_WHINGING
            'Ronald'   | ['Bilius']             | 'Weasley'    | 17  | OTTERY_ST_CATCHPOLE
            'Hermione' | ['Jean']               | 'Granger'    | 18  | LONDON
            'Albus'    | ['Percival','W.','B.'] | 'Dumbledore' | 115 | HOGWARTS
        }

        then:
        people.each { u ->
            assert u.id != null
        }
        def actual = prettyPrint(people)
        def expected = prettyPrint(
                'Person[name=Harry James Potter, age=17, city=LITTLE_WHINGING]',
                'Person[name=Ronald Bilius Weasley, age=17, city=OTTERY_ST_CATCHPOLE]',
                'Person[name=Hermione Jean Granger, age=18, city=LONDON]',
                'Person[name=Albus Percival W. B. Dumbledore, age=115, city=HOGWARTS]',
        )
        assert actual == expected
    }

    // TODO: test BuilderDSL.asType extension

    String prettyPrintSimple(List<Person> people) {
        people.collect {
            "Person[$it.login, $it.email]"
        }
        .sort()
        .join('\n')
    }

    String prettyPrint(List<Person> people) {
        people.collect {
            "Person[name=${[it.firstName, *it.middleNames, it.lastName].join(' ')}, " +
                    "age=$it.age, city=$it.address.city]"
        }
        .sort()
        .join('\n')
    }

    String prettyPrint(String... people) {
        people.toSorted().join('\n')
    }

}
