package com.github.jakubkolar.autobuilder.bug;

import java.util.AbstractCollection;
import java.util.AbstractSequentialList;
import java.util.List;
import java.util.Queue;

public class SupertypeFields {

    // Let's use java.util.LinkedList

    // 1. Superclasses (direct & indirect)
    AbstractSequentialList<?> abstractListField;
    AbstractCollection<?> abstractCollectionField;
    Object objectField;

    // 2. Implemented interfaces
    // a) direct
    List<?> listField;
    Cloneable cloneableField;
    // b) indirect
    Queue<?> queueField;
    Iterable<?> iterableField;

    // 3. Global config
    Object globalOField;
    Queue<?> globalQField;
}
