package com.github.jakubkolar.autobuilder.bug

import com.github.jakubkolar.autobuilder.AutoBuilder
import com.github.jakubkolar.autobuilder.specification.ImmutabilityExampleDTO
import spock.lang.Specification


class Primitives_Issue09Test extends Specification {

    def setupSpec() {
        // Can only be done once per JVM run // TODO: AB-025
        AutoBuilder.registerValue("PrimitiveFields.globalConfig", 321)
    }

    def "BuilderDSL.with(String, Object) does not work for properties of primitive types"() {
        when:
        def instance = AutoBuilder.instanceOf(PrimitiveFields)
                .with("i", 123)
                .build()

        then:
        assert instance.i == 123
    }

    def "BuilderDSL.with(Map<String, Object>) does not work for properties of primitive types"() {
        when:
        def instance = AutoBuilder.instanceOf(PrimitiveFields)
                .with(i: 123)
                .build()

        then:
        assert instance.i == 123
    }

    def "AutoBuilder.registerValue does not work for properties of primitive types"() {
        when:
        def instance = AutoBuilder.instanceOf(PrimitiveFields).build()

        then:
        assert instance.globalConfig == 321
    }

}
