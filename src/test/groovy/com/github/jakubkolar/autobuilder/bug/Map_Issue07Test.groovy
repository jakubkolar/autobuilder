package com.github.jakubkolar.autobuilder.bug

import com.github.jakubkolar.autobuilder.AutoBuilder
import spock.lang.Specification

class Map_Issue07Test extends Specification {

    def "BuilderDSL.with(Map properties) only considers the last property in the map"() {
        when:
        def instance = AutoBuilder.instanceOf(SeveralFields)
                .with(field1: 'ABC', field2: 123, field3: 321)
                .build()

        then:
        assert instance.field1 == 'ABC'
        assert instance.field2 == 123
        assert instance.field3 == 321
    }

    def "AutoBuilder.create(Class, Map) only considers the last property in the map"() {
        when:
        def instance = AutoBuilder.create(SeveralFields, [field1: 'ABC', field2: 123, field3: 321])

        then:
        assert instance.field1 == 'ABC'
        assert instance.field2 == 123
        assert instance.field3 == 321
    }

}
