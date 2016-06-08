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

import spock.lang.Specification

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

import static com.github.jakubkolar.autobuilder.AutoBuilder.a
import static com.github.jakubkolar.autobuilder.specification.City.*

class GroovyExtensionsIT extends Specification {

    def "Create a single object directly"() {
        when:
        User user = User.of {
            (lastName, firstName) = ['Granger', 'Hermione']
            age = 18
            address.city = LONDON
        }

        then:
        user.with {
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
        def user = a(User).with(lastName: 'Granger', firstName: 'Hermione') as User
        def str = a(String) as CharSequence

        then:
        assert user instanceof User
        assert str instanceof CharSequence
        assert [user.lastName, user.firstName] == ['Granger', 'Hermione']
    }

    def "Builder fails when trying to coerce to a non-compatible type"() {
        when:
        a(User).with(lastName: 'Granger', firstName: 'Hermione') as Address

        then:
        def e = thrown(ClassCastException)
        assert e.message == "Cannot cast ${User.name} to ${Address.name}"
    }

    def "Create a collection from a simple table"() {
        when:
        def users = a User with(emailVerified: true) fromTable {
            login      | email
            'harryp'   | 'seeker731@gryffindor.com'
            'grangerh' | 'readinglover@spew.org'
            'albus'    | 'apwbd@beards.com'
        }

        then:
        users.each { u ->
            assert u.id != null
            assert u.created != null
            assert u.address != null
            assert !u.deletionFlag
            assert u.emailVerified
        }
        def actual = prettyPrintSimple(users)
        def expected = prettyPrint(
                'User[harryp, seeker731@gryffindor.com]',
                'User[grangerh, readinglover@spew.org]',
                'User[albus, apwbd@beards.com]'
        )
        assert actual == expected
    }

    def "Create a collection from a simple table - using the class"() {
        when:
        List<User> users = User.fromTable {
            emailVerified = true
            login      | email
            'harryp'   | 'seeker731@gryffindor.com'
            'grangerh' | 'readinglover@spew.org'
            'albus'    | 'apwbd@beards.com'
        }

        then:
        users.each { u ->
            assert u.id != null
            assert u.created != null
            assert u.address != null
            assert !u.deletionFlag
            assert u.emailVerified
        }
        def actual = prettyPrintSimple(users)
        def expected = prettyPrint(
                'User[harryp, seeker731@gryffindor.com]',
                'User[grangerh, readinglover@spew.org]',
                'User[albus, apwbd@beards.com]'
        )
        assert actual == expected
    }

    def "Create an empty collection from an empty table"() {
        when:
        def users = a User fromTable {
            login | email
        }

        then:
        assert users.empty
    }

    def "Create an empty collection from an empty table - using the class"() {
        when:
        def users = User.fromTable {
            login | email
        }

        then:
        assert users.empty
    }


    def "Create a collection from a table with nested properties"() {
        given:
        def today = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC)

        when:
        def users = a User with(Instant, today) fromTable {
            firstName  | middleNames            | lastName     | age | address.city
            'Harry'    | ['James']              | 'Potter'     | 17  | LITTLE_WHINGING
            'Ronald'   | ['Bilius']             | 'Weasley'    | 17  | OTTERY_ST_CATCHPOLE
            'Hermione' | ['Jean']               | 'Granger'    | 18  | LONDON
            'Albus'    | ['Percival','W.','B.'] | 'Dumbledore' | 115 | HOGWARTS
        }

        then:
        users.each { u ->
            assert u.id != null
            assert u.created.is(today)
            assert u.lastModified.is(today)
        }
        def actual = prettyPrint(users)
        def expected = prettyPrint(
                'User[name=Harry James Potter, age=17, city=LITTLE_WHINGING]',
                'User[name=Ronald Bilius Weasley, age=17, city=OTTERY_ST_CATCHPOLE]',
                'User[name=Hermione Jean Granger, age=18, city=LONDON]',
                'User[name=Albus Percival W. B. Dumbledore, age=115, city=HOGWARTS]',
        )
        assert actual == expected
    }

    def "Create a collection from a table with nested properties - using the class"() {
        when:
        // TODO: So far there is no way to invoke other methods on the contextual BuilderDSL
        // (like .with(Instant, today) in the previous test)
        List<User> users = User.fromTable {
            firstName  | middleNames            | lastName     | age | address.city
            'Harry'    | ['James']              | 'Potter'     | 17  | LITTLE_WHINGING
            'Ronald'   | ['Bilius']             | 'Weasley'    | 17  | OTTERY_ST_CATCHPOLE
            'Hermione' | ['Jean']               | 'Granger'    | 18  | LONDON
            'Albus'    | ['Percival','W.','B.'] | 'Dumbledore' | 115 | HOGWARTS
        }

        then:
        users.each { u ->
            assert u.id != null
        }
        def actual = prettyPrint(users)
        def expected = prettyPrint(
                'User[name=Harry James Potter, age=17, city=LITTLE_WHINGING]',
                'User[name=Ronald Bilius Weasley, age=17, city=OTTERY_ST_CATCHPOLE]',
                'User[name=Hermione Jean Granger, age=18, city=LONDON]',
                'User[name=Albus Percival W. B. Dumbledore, age=115, city=HOGWARTS]',
        )
        assert actual == expected
    }

    // TODO: test BuilderDSL.asType extension

    String prettyPrintSimple(List<User> users) {
        users.collect {
            "User[$it.login, $it.email]"
        }
        .sort()
        .join('\n')
    }

    String prettyPrint(List<User> users) {
        users.collect {
            "User[name=${[it.firstName, *it.middleNames, it.lastName].join(' ')}, " +
                    "age=$it.age, city=$it.address.city]"
        }
        .sort()
        .join('\n')
    }

    String prettyPrint(String... users) {
        users.toSorted().join('\n')
    }

}
