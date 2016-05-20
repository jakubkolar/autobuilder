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

package com.github.jakubkolar.autobuilder.specification;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public class BuiltInResolversDTO {

    // Strings
    String strField;
    CharSequence charSequenceField;
    StringBuilder strBuilderField;

    // Primitives
    byte byteField;
    short shortField;
    int intField;
    long longField;
    float floatField;
    double doubleField;
    char charField;

    // Wrappers
    Byte byteWrapperField;
    Short shortWrapperField;
    Integer intWrapperField;
    Long longWrapperField;
    Float floatWrapperField;
    Double doubleWrapperField;
    Character charWrapperField;

    // Enum
    NonEmptyEnum enumField;
    EmptyEnum emptyEnumField;

    // Collections & maps
    Collection<?> collectionField;
    List<?> listField;
    Set<?> setField;
    SortedSet<?> sortedSetField;
    Map<?, ?> mapField;
    SortedMap<?, ?> sortedMapField;

    // Arrays
    Object[] objArrayField;
    Character[] objArrayField2;
    char[] primitiveArrayField;

    // Special
    Object objectField;
    Number numberField;
    Serializable serializableField;

    // Comparables
    Comparable<String> cStrField;
    Comparable<Integer> cIntegerField;
    Comparable<NonEmptyEnum> cEnumField;

    // This actually should not be resolved: the only possible value would be 'null' because
    // EmptyEnum has no constants. But 'null' is not a good choice for Comparable, it is better
    // to only allow it for just EmptyEnum instances and nothing else
    //Comparable<EmptyEnum> cEmptyEnumField;

}
