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
package org.gflogger;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author Ruslan Cheremin, cheremin@gmail.com
 */
public class TestTypeEntityRegistry {
	public static final String DEFAULT = "DEFAULT";
	private final TypeEntityRegistry<String> registry = new TypeEntityRegistry<String>( DEFAULT );

	@Test
	public void returnDefaultValueIfNotMatched() throws Exception {
		assertNotMatched( C.class );
	}

	@Test
	public void classMatchedIfExactMatchFound() throws Exception {
		register( C.class );
		assertMatched( C.class, C.class );
	}

	@Test
	public void interfaceMatchedIfExactMatchFound() throws Exception {
		register( I.class );
		assertMatched( I.class, I.class );
	}

	@Test
	public void classMatchedIfSuperClassMatchFound() throws Exception {
		register( C.class );
		assertMatched( CC.class, C.class );
	}

	@Test
	public void classMatchedIfSuperSuperClassMatchFound() throws Exception {
		register( C.class );
		assertMatched( CCC.class, C.class );
	}

	@Test
	public void classMatchedWithNearestSuperClassIfSeveralMatchesExists() throws Exception {
		register( C.class );
		register( CC.class );

		assertMatched( CCC.class, CC.class );
	}

	@Test
	public void classMatchedWithSuperClassBeforeInterfaceIfDistanceIsSame() throws Exception {
		register( CC.class );
		register( I.class );

		assertMatched( CCI.class, CC.class );
	}

	@Test
	public void classMatchedWithInterfaceIfInterfaceCloser() {
		register( C.class );
		register( I.class );

		assertMatched( CCI.class, I.class );
	}

	@Test
	public void classMatchedWithInterfaceInSuperclassChain() {
		register( II.class );

		assertMatched( CCII_I.class, II.class );
	}

//	@Test
//	public void test1() throws Exception {
//		register( MarketData.class );
//		register( QuantumEnd.class );
//
//		assertMatched( BookImpl.class, MarketData.class );
//	}

	private void register( final Class clazz ) {
		registry.register( clazz, clazz.getSimpleName() );
	}

	private void assertMatched( final Class in,
	                            final Class matchedWith ) {
		final String matched = registry.forType( in );
		assertEquals( matchedWith.getSimpleName(), matched );
	}

	private void assertNotMatched( final Class in ) {
		final String matched = registry.forType( in );
		assertEquals( DEFAULT, matched );
	}

	public interface I {}

	public interface II extends I {}

	public static class C {}

	public static class CII implements II {}

	public static class CC extends C {}

	public static class CCC extends CC {}

	public static class CCI extends CC implements I {}

	public static class CCII_I extends CII implements I {}
}
