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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class Table {

    private final Map<String, Integer> header;
    private final List<TableRow> rows;

    private Table(TableRow headerRow) {
        this.header = headerRow.toHeader();
        this.rows = new ArrayList<>();
    }

    public static Table of(List<TableRow> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("No rows defined: every table must have"
                + " at least 1 row (the header). Also please check that your table"
                + " has at least 2 columns separated by the '|' operator (technical "
                + "restriction)");
        }

        Table table = new Table(rows.get(0));

        for (int i = 1; i < rows.size(); i++) {
            table.add(rows.get(i));
        }

        return table;
    }

    public void add(TableRow row) {
        rows.add(row);
    }

    public Stream<Map<String, Object>> stream() {
        return rows.stream().map(row -> row.toProperties(header));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("header", header.keySet())
                .append("rows", rows)
                .toString();
    }
}
