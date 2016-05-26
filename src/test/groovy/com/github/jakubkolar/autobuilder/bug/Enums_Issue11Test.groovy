package com.github.jakubkolar.autobuilder.bug

import com.github.jakubkolar.autobuilder.AutoBuilder
import spock.lang.Specification

class Enums_Issue11Test extends Specification {

    def "AutoBuilder does not resolve enums"() {
        when:
        def instance = AutoBuilder.instanceOf(EnumFields).build()

        then:
        assert instance.e != null
    }

}
