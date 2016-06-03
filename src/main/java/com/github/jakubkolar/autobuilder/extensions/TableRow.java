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

package com.github.jakubkolar.autobuilder.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class TableRow {

    private final List<Object> data;

    private TableRow(List<Object> data) {
        this.data = data;
    }

    public static TableRow of(Object firstElement, Object secondElement) {
        List<Object> data = new ArrayList<>();
        data.add(firstElement);
        data.add(secondElement);
        return new TableRow(data);
    }

    /**
     * Implementation of the '|' operator for more contents to the table row.
     */
    public TableRow or(Object argument) {
        data.add(argument);
        return this;
    }

    public Map<String, Integer> toHeader() {
        Map<String, Integer> result = new HashMap<>();

        for (int i = 0; i < data.size(); i++) {
            Object element = data.get(i);
            if (!(element instanceof Variable)) {
                throw new IllegalStateException("Not a header: element '" + element
                        + "' in row '" + this + "' is not a variable"
                        + " (all elements in a header row must be variables)");
            }

            result.put(((Variable) element).getName(), i);
        }

        return result;
    }

    public Map<String, Object> toProperties(Map<String, Integer> header) {
        Map<String, Object> result = new HashMap<>();
        header.forEach((property, index) -> {
            if (index < 0 || index >= data.size()) {
                throw new IllegalStateException(
                    "Malformed table: cannot retrieve property '" + property
                    + "' at column '" + index + "' from row '" + this + "', "
                    + "the column does not exist in the row");
            }
            Object value = data.get(index);
            result.put(property, value);
        });
        return result;
    }

    @Override
    public String toString() {
        return "TableRow" + data;
    }
}
