/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.util.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/** An ExtendedIterator that is created lazily.
 * This is useful when constructing an iterator is expensive and 
 * you'd prefer to delay doing it until certain it's actually needed.
 * For example, if you have <code>iterator1.andThen(iterator2)</code>
 * you could implement iterator2 as a LazyIterator.  
 * The sequence to be defined is defined by the subclass's definition 
 * of create().  That is called exactly once on the first attempt 
 * to interact with the LazyIterator.  
 */
abstract public class LazyIterator<T> implements ExtendedIterator<T> {

	private ExtendedIterator<T> it=null;

	/** An ExtendedIterator that is created lazily. 
	 * This constructor has very low overhead - the real work is 
	 * delayed until the first attempt to use the iterator.
	 */
	public LazyIterator() {
	}

	@Override
    public boolean hasNext() {
		lazy();
		return it.hasNext();
	}

	@Override
    public T next() {
		lazy();
		return it.next();
	}

	@Override
    public void remove() {
		lazy();
		it.remove();
	}

	// This calls unlazy as the iterator chain is created.
    // Too early - wait until the chain is used.
//	@Override
//    public ExtendedIterator<T> filterKeep(Predicate<T> f) {
//		lazy();
//		return it.filterKeep(f);
//	}
//
//	@Override
//    public ExtendedIterator<T> filterDrop(Predicate<T> f) {
//		lazy();
//		return it.filterDrop(f);
//	}
	
	// Don't unlazy until hasNext of the filter is called. 
	@Override
	public FilterIterator<T> filterKeep( Predicate<T> f )
	{ return new FilterIterator<>( f, this ); }

	@Override
	public FilterIterator<T> filterDrop( final Predicate<T> f )
	{ return new FilterIterator<>( f.negate(), this ); }

	@Override
    public <U> ExtendedIterator<U> mapWith(Function<T,U> map1) {
	    return new Map1Iterator<>(map1, this) ;
		//lazy();
		//return it.mapWith(map1);
	}

	@Override
    public void close() {
		lazy();
		it.close();
			
	}
	
	@Override
	public T removeNext() {
		lazy();
		return it.removeNext();
	}

	@Override
	public <X extends T> ExtendedIterator<T> andThen( Iterator<X> other ){
		return NiceIterator.andThen(this, other);
	}

	@Override
	public List<T> toList() {
		return NiceIterator.asList(this);
	}

	@Override
	public Set<T> toSet() {
		return NiceIterator.asSet(this);
	}
	 
	private void lazy() {
		if (it == null)
			it = create();
	}

	/** The subclass must define this to return
	 * the ExtendedIterator to invoke. This method will be
	 * called at most once, on the first attempt to
	 * use the iterator.
	 * From then on, all calls to this will be passed
	 * through to the returned Iterator.
	 * @return The parent iterator defining the sequence.
	 */
	public abstract ExtendedIterator<T> create();

}
