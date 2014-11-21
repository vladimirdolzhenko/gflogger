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

import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestDefaultObjectFormatterFactory {

	@Test
	public void testAObjectFormatter() throws Exception {
		final DefaultObjectFormatterFactory objectFormatterFactory = new DefaultObjectFormatterFactory();

		final ObjectFormatter<A> aObjectFormatter = new ObjectFormatter<A>(){
			@Override
			public void append(A obj, GFLogEntry entry) {
				entry.append("A");
			}

			@Override
			public String toString() {
				return "AFormatter";
			}

		};
		objectFormatterFactory.registerObjectFormatter(A.class, aObjectFormatter);

		// this is instance of anonymous class that implements A
		final ObjectFormatter objectFormatter =
			objectFormatterFactory.getObjectFormatter(new A(){});

		assertSame(aObjectFormatter, objectFormatter);
	}

	@Test
	public void testABCImpl2ObjectFormatter() throws Exception {
		final DefaultObjectFormatterFactory objectFormatterFactory = new DefaultObjectFormatterFactory();

		final ObjectFormatter<A> aObjectFormatter = new ObjectFormatter<A>(){
			@Override
			public void append(A obj, GFLogEntry entry) {
				entry.append("A");
			}

			@Override
			public String toString() {
				return "AFormatter";
			}

		};
		objectFormatterFactory.registerObjectFormatter(A.class, aObjectFormatter);

		final ObjectFormatter objectFormatter =
				objectFormatterFactory.getObjectFormatter(new ABCImpl2());

		assertSame(aObjectFormatter, objectFormatter);
	}

	@Test
	public void testABC2ObjectFormatter() throws Exception {
		final DefaultObjectFormatterFactory objectFormatterFactory = new DefaultObjectFormatterFactory();

		final ObjectFormatter<A> aObjectFormatter = new ObjectFormatter<A>(){
			@Override
			public void append(A obj, GFLogEntry entry) {
				entry.append("A");
			}

			@Override
			public String toString() {
				return "AFormatter";
			}

		};
		objectFormatterFactory.registerObjectFormatter(A.class, aObjectFormatter);

		final ObjectFormatter objectFormatter =
				objectFormatterFactory.getObjectFormatter(new ABC2(){});

		assertSame(aObjectFormatter, objectFormatter);
	}

	@Test
	public void testABCImpl2ABCImplObjectFormatter() throws Exception {
		final DefaultObjectFormatterFactory objectFormatterFactory = new DefaultObjectFormatterFactory();

		final ObjectFormatter<D> dObjectFormatter = new ObjectFormatter<D>(){
			@Override
			public void append(D obj, GFLogEntry entry) {
				entry.append("D");
			}

			@Override
			public String toString() {
				return "DFormatter";
			}
		};

		final ObjectFormatter<ABCImpl> abcImplObjectFormatter = new ObjectFormatter<ABCImpl>(){
			@Override
			public void append(ABCImpl obj, GFLogEntry entry) {
				entry.append("ABCImpl");
			}

			@Override
			public String toString() {
				return "ABCImplFormatter";
			}

		};
		objectFormatterFactory.registerObjectFormatter(D.class, dObjectFormatter);
		objectFormatterFactory.registerObjectFormatter(ABCImpl.class, abcImplObjectFormatter);

		final ObjectFormatter objectFormatter =
				objectFormatterFactory.getObjectFormatter(new ABCImpl2());

		assertSame(abcImplObjectFormatter, objectFormatter);
	}

	@Test
	public void testMDObjectFormatter() throws Exception {
		final DefaultObjectFormatterFactory objectFormatterFactory = new DefaultObjectFormatterFactory();

		final ObjectFormatter<AMD> amdObjectFormatter = new ObjectFormatter<AMD>(){
			@Override
			public void append(AMD obj, GFLogEntry entry) {
				entry.append("AMD");
			}

			@Override
			public String toString() {
				return "AMDFormatter";
			}
		};

		final ObjectFormatter<QE> qeObjectFormatter = new ObjectFormatter<QE>(){
			@Override
			public void append(QE obj, GFLogEntry entry) {
				entry.append("QE");
			}

			@Override
			public String toString() {
				return "QEFormatter";
			}

		};
		objectFormatterFactory.registerObjectFormatter(AMD.class, amdObjectFormatter);
		objectFormatterFactory.registerObjectFormatter(QE.class, qeObjectFormatter);

		final ObjectFormatter objectFormatter =
				objectFormatterFactory.getObjectFormatter(new BImpl());

		assertSame(amdObjectFormatter, objectFormatter);
	}

	private static interface A {}
	private static interface B {}
	private static interface C {}

	private static interface D {}

	private static interface ABC extends A, B, C {}

	private static interface ABC2 extends ABC {}

	private static class ABCImpl implements ABC {}
	private static class ABCImpl2 extends ABCImpl implements D {}


	private static interface MD {}

	private static interface QE extends MD {}

	private static abstract class AMD implements MD {}

	private static class BRU extends AMD {}

	private static class BImpl extends BRU {}
}
