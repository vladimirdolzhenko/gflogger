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
package org.gflogger.perftest;

import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * SpringTest
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class SpringTest extends AbstractSimpleTest {

	public static void main(String[] args) throws Throwable {
		final SpringTest springTest = new SpringTest();
		final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:app-context.xml");
		ctx.start();

		springTest.test();

		ctx.stop();
	}
}
