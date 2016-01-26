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

/**
 * <a href="https://en.wikipedia.org/wiki/Application_programming_interface">API</a>
 * (excluding SPI) - classes and interfaces meant to be used by clients.
 *
 * <p> <i>Note on backward compatibility</i>: Because interfaces from this package are
 * meant to be implemented only in the {@code AutoBuilder} library, they are subject to
 * little more <i>relaxed</i> forward compatibility restrictions. More specifically,
 * in future versions these interfaces can:
 * <ul>
 *     <li>introduce new methods</li>
 *     <li>change a type of any method return value to its subtype</li>
 *     <li>change a type of any method parameter to its supertype</li>
 * </ul>
 */
package com.github.jakubkolar.autobuilder.api;
