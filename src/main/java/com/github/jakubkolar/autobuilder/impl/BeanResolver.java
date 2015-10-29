/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Jakub Kolar
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

package com.github.jakubkolar.autobuilder.impl;

import com.google.common.base.Preconditions;
import com.github.jakubkolar.autobuilder.spi.ValueResolver;
import org.objenesis.Objenesis;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class BeanResolver implements ValueResolver {

    private final Objenesis objenesis;

    @Nullable
    private ValueResolver fieldsResolver;

    @Inject
    public BeanResolver(Objenesis objenesis) {
        this.objenesis = objenesis;
    }

    @Nullable
    @Override
    public <T> T resolve(Class<T> type, String name, Collection<Annotation> annotations) {
        try {
            T instance = objenesis.newInstance(type);

            // TODO: unsupported operation exception here?
            Preconditions.checkNotNull(instance);

            // Now try to initialize the fields
            for (Field field : type.getDeclaredFields()) {
                field.setAccessible(true);
                // TODO: try to catch field related exceptions?
                Object fieldValue = resolveField(name, field);
                field.set(instance, fieldValue);
            }

            return instance;
        }
        // Any kind of exceptions can happen here, because with Objenesis,
        // reflection and who-knows-what-other hacks are involved
        catch (Exception | InstantiationError e) { //TODO: catch the Inst.Error or not?
            throw new UnsupportedOperationException(
                String.format(
                    "Cannot resolve value for type %s with name %s annotated with %s because of %s: %s",
                    type.toString(), name, annotations.toString(), e.getClass().getSimpleName(), e.getMessage()),
                e);
        }
    }

    private Object resolveField(String beanName, Field field) {
        Preconditions.checkNotNull(fieldsResolver, "Field resolver was not properly initialized!");
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();
        List<Annotation> annotations = Arrays.asList(field.getAnnotations());
        return fieldsResolver.resolve(fieldType, beanName + "." + fieldName, annotations);
    }

    public void setFieldsResolver(@Nonnull ValueResolver fieldsResolver) {
        this.fieldsResolver = fieldsResolver;
    }
}