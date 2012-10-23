/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gflogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Ruslan Cheremin, cheremin@gmail.com
 */
public final class TypeEntityRegistry<T> {

	private final T defaultEntity;

	private final ConcurrentMap<Class, T> entities = new ConcurrentHashMap<Class, T>();

	public TypeEntityRegistry( final T defaultEntity ) {
		this.defaultEntity = defaultEntity;
	}

	public void register( final Class<?> clazz, final T entity ) {
		entities.put( clazz, entity );
	}

	public void clear() {
		entities.clear();
	}

	public T forType( final Class<?> type ) {
		T entity = entities.get( type );
		if( entity != null ) {
			return entity;
		}


		// lookup over classes: class - super class - super - super class and so on
		final LookupResult<T> sclassResult = lookupSuperclassChain( type );

		final LookupResult<T> ifaceResult = lookupInterfacesChain( type, 0 );

		final LookupResult<T> bestResult = LookupResult.bestOf(
				sclassResult,
				ifaceResult
		);

		if( bestResult.isFound() ) {
			entity = bestResult.entity();
		} else {
			entity = defaultEntity;
		}

		//cache for next search to be faster
		entities.put( type, entity );
		return entity;
	}

	private LookupResult<T> lookupSuperclassChain( final Class<?> startingWith ) {
		double minWeight = Integer.MAX_VALUE;
		T matchedEntity = null;
		Class matchedType = null;

		int depth = 0;
		for( Class clazz = startingWith;
			 clazz != null;
			 clazz = clazz.getSuperclass() ) {

			final T entity = entities.get( clazz );
			if( entity != null ) {
				return LookupResult.byClass(
						clazz,
						entity,
						depth
				);
			} else {
				final LookupResult<T> ifaceResult = lookupInterfacesChain( clazz, depth );
				if( ifaceResult.isFound() && ifaceResult.weight() < minWeight ) {
					matchedType = ifaceResult.actualTypeMatched();
					minWeight = ifaceResult.weight();
					matchedEntity = ifaceResult.entity();
				}
			}
			depth++;
		}
		if( matchedEntity == null ) {
			return LookupResult.NOT_FOUND;
		}
		return LookupResult.byClass( matchedType, matchedEntity, minWeight );
	}

	private LookupResult<T> lookupInterfacesChain( final Class<?> startingWith,
												   final int depth ) {
		double minWeight = Integer.MAX_VALUE;
		T matchedEntity = null;
		Class matchedIface = null;

		for( final Class<?> iface : startingWith.getInterfaces() ) {
			final T entity = entities.get( iface );
			if( entity != null ) {
				return LookupResult.byInterface(
						iface,
						entity,
						depth + 1
				);
			}
			final LookupResult<T> result = lookupInterfacesChain( iface, depth + 1 );
			if( result.isFound() && result.weight() < minWeight ) {
				minWeight = result.weight();
				matchedEntity = result.entity();
				matchedIface = result.actualTypeMatched();
			}
		}
		if( matchedEntity == null ) {
			return LookupResult.NOT_FOUND;
		}
		return LookupResult.<T>byInterface(
				matchedIface,
				matchedEntity,
				depth + minWeight
		);
	}

	private static class LookupResult<T> {
		public static final LookupResult NOT_FOUND = new LookupResult( null, null, Integer.MAX_VALUE );

		private Class actualTypeMatched;
		private T entity;
		private double weight;

		private LookupResult() {
		}

		public static <T> LookupResult byClass( final Class actualTypeMatched,
												final T entity,
												final double weight ) {
			return new LookupResult<T>(
					actualTypeMatched,
					entity,
					weight
			);
		}

		public static <T> LookupResult byInterface( final Class actualTypeMatched,
													final T entity,
													final double weight ) {
			return new LookupResult<T>(
					actualTypeMatched,
					entity,
					weight + 0.5// interfaces have penalty
			);
		}

		private LookupResult( final Class actualTypeMatched,
							  final T entity,
							  final double weight ) {
			setup( actualTypeMatched, entity, weight );
		}

		private void setup( final Class actualTypeMatched,
							final T entity,
							final double weight ) {
			this.actualTypeMatched = actualTypeMatched;
			this.entity = entity;
			this.weight = weight;
		}

		public Class actualTypeMatched() {
			return actualTypeMatched;
		}

		public T entity() {
			return entity;
		}

		public double weight() {
			return weight;
		}

		public boolean isFound() {
			return entity != null;
		}

		public static <T> LookupResult<T> bestOf( final LookupResult<T> sr1, final LookupResult<T> sr2 ) {
			return sr1.weight <= sr2.weight ? sr1 : sr2;
		}

	}
}
