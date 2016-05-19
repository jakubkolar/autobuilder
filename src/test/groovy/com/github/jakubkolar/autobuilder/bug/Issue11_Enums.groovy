package com.github.jakubkolar.autobuilder.bug

import com.github.jakubkolar.autobuilder.AutoBuilder
import spock.lang.Specification


class Issue11_Enums extends Specification {

    def "AutoBuilder does not resolve enums"() {
        when:
        def instance = AutoBuilder.instanceOf(EnumFileds).build()

        then:
        assert instance.e != null
    }

}
