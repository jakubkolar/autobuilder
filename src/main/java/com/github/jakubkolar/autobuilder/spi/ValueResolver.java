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

package com.github.jakubkolar.autobuilder.spi;

import com.google.common.annotations.Beta;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

/**
 * Value resolver is a <em>pluggable</em> component that can be used to resolve instances
 * of any type based on metadata.
 *
 * <p> Main purpose is to provide a flexible resolution algorithm to the {@link
 * com.github.jakubkolar.autobuilder.AutoBuilder}. Value resolvers can be combined or
 * chained to achieve complex and completely configurable strategies for instance
 * resolution. The ultimate goal of {@code AutoBuilder} is to be able to resolve objects
 * of <em>any java type</em>.
 *
 * <h3>Types of resolvers</h3>
 *
 * <p> Resolver implementations are free to choose whatever resolution approach is needed.
 * <em id="greedy_resolver">Greedy</em> resolvers would resolve as many types as they can and are best suited
 * as the most generic resolvers that are tried if the type cannot be resolved by any other more
 * specific resolver. An example are the <em id="builtin_resolver">built-in resolvers</em> that can resolve some common
 * types and their supertypes from {@link java.lang} and {@link java.util}.
 *
 * <p> <em id="general_resolver">General</em> resolvers resolve only single, usually common type (directly from JDK or from a
 * well known library), and are meant to be widely used. Example would be {@link
 * java.math.BigDecimal} and {@link com.github.jakubkolar.autobuilder.resolvers.BigDecimalResolver}.
 *
 * <p> <em id="specific_resolver">Specific</em> resolvers may be used to resolve types specific for a given
 * project or to override other resolvers to resolve a common type in a specific way.
 * Their usage depends on use case, and they will often be used as a local configuration
 * - see {@link com.github.jakubkolar.autobuilder.api.BuilderDSL#with(ValueResolver)}.
 *
 * <p> <em id="bean_resolver">Bean resolver</em> is a special resolver within the {@code AutoBuilder} library
 * that is invoked when the type cannot be resolved by any other registered resolver. It
 * first tries to instantiate the target type, and then tries to resolve each property of
 * the resulting object recursively using the same resolution process that was used to
 * resolve the original type. It may happen that the target type cannot be instantiated,
 * and in this case the {@link com.github.jakubkolar.autobuilder.api.BuilderDSL#build()}
 * will fail with an {@code UnsupportedOperationException}.
 *
 * <h3>Resolution process</h3>
 *
 * <p> The actual resolution of an instance with the {@code AutoBuilder} is implemented
 * using
 * the <em>chain of responsibility</em> pattern. Known resolvers are tried one by
 * one
 * and the first one that is able to handle the resolution request is used. The chain of
 * resolvers is composed of two parts - local part and global part. The local part is
 * specific to each {@link com.github.jakubkolar.autobuilder.api.BuilderDSL} and is fully
 * specified by the user, without affecting other {@code BuilderDSL} objects. The global
 * part,
 * on the other hand, is managed by the {@code AutoBuilder} class and is supplied to
 * every {@code BuilderDSL} object as it is created.
 *
 * <p> The resolution process first tries the local resolvers, and then the global ones.
 * In addition to that, each sub-chain begins with a special resolver that is tried first
 * and that contains specific 'named values', which
 * allow the user to quickly specify plain value-based overrides
 * without the need to implement a full {@code ValueResolver}. To contribute to these named values,
 * use {@link com.github.jakubkolar.autobuilder.api.BuilderDSL#with(String, Object)},
 * or {@link com.github.jakubkolar.autobuilder.api.BuilderDSL#with(Map)} (local part),
 * and {@link com.github.jakubkolar.autobuilder.AutoBuilder#registerValue(String, Object,
 * Annotation...)}
 * (global part). After these named values, in each (local and global) sub-chain,
 * custom user registered resolvers are tried in a LIFO manner (last registered resolver is tried first).
 *
 * <p> If none of these resolvers can handle the request, the {@code AutoBuilder} then
 * proceeds with the <a href="#builtin_resolver">built-in resolvers</a> and
 * the <a href="#bean_resolver">bean resolver</a>. So, in the end the resolution process
 * looks like this:
 * <pre>
 * Resolver Chain
 * ├── Local resolvers (per each {@link com.github.jakubkolar.autobuilder.api.BuilderDSL}, mostly <a href="#specific_resolver">specific resolvers</a>)
 * │   ├── Named values (specified using {@link com.github.jakubkolar.autobuilder.api.BuilderDSL#with(String, Object)} or {@link com.github.jakubkolar.autobuilder.api.BuilderDSL#with(Map)}
 * │   ├── User resolver 1 (specified using {@link com.github.jakubkolar.autobuilder.api.BuilderDSL#with(ValueResolver)}
 * │   ├── User resolver 2
 * │   └── ...
 * ├── Global resolvers (available to every {@link com.github.jakubkolar.autobuilder.api.BuilderDSL}, mostly <a href="#general_resolver">general resolvers</a>)
 * │   ├── Global named values (specified using {@link com.github.jakubkolar.autobuilder.AutoBuilder#registerValue})
 * │   ├── User resolver 1 (specified using {@link @see com.github.jakubkolar.autobuilder.AutoBuilder#registerResolver}
 * │   ├── User resolver 2
 * │   └── 01 Named values
 * ├── Built-in resolvers (non-customizable, available to all builders off-the-shelf, <a href="#greedy_resolver">greedy</a>)
 * │   ├── 01 stringResolver
 * │   ├── 02 primitiveTypeResolver
 * │   ├── 03 enumResolver
 * │   ├── 04 collectionResolver
 * │   └── 05 arrayResolver
 * └── Bean resolver
 * </pre>
 *
 * <h3>How to register a resolver</h3>
 *
 * <h3>Resolvers available off-the-shelf</h3>
 *
 * <h3>Custom resolvers</h3>
 *
 * @author Jakub Kolar
 * @see com.github.jakubkolar.autobuilder.api.BuilderDSL#with(ValueResolver)
 * @see com.github.jakubkolar.autobuilder.api.BuilderDSL#with(String, Object)
 * @see com.github.jakubkolar.autobuilder.api.BuilderDSL#with(Map)
 * @see com.github.jakubkolar.autobuilder.AutoBuilder#registerResolver
 * @see com.github.jakubkolar.autobuilder.AutoBuilder#registerValue
 * @since 0.0.1
 */
@Beta
public interface ValueResolver {

    /**
     * Resolves an instance of type {@code T}.
     * <p>
     * The implementation can choose whatever strategy is appropriate. For example,
     * it can create a fresh new object, or it can return an instance from a pool,
     * or lookup a named singleton, etc.
     *
     * @param type        the class object for the requested type
     * @param name        the name of the resolved object
     *                    (meaning depends on the context in which the resolution is
     *                    requested, e.g. it may be a field name)
     * @param annotations additional metadata hints for the resolution
     *                    (content depends on the resolution context, e.g. it can be
     *                    annotations found on a field)
     * @param <T>         the type of the result
     * @return the resolved instance of type {@code T}, including {@code null} as a
     * valid return value
     * @throws UnsupportedOperationException if the instance with the given metadata
     *                                       cannot be resolved by this resolver
     */
    @Nullable
    <T> T resolve(Class<T> type, String name, Collection<Annotation> annotations);

}
