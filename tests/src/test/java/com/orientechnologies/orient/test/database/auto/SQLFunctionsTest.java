/*
 * Copyright 1999-2010 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.test.database.auto;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;
import com.orientechnologies.orient.core.sql.OSQLEngine;
import com.orientechnologies.orient.core.sql.functions.OSQLFunctionAbstract;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Test(groups = "sql-select")
public class SQLFunctionsTest {
	private ODatabaseDocument	database;

	@Parameters(value = "url")
	public SQLFunctionsTest(String iURL) {
		database = new ODatabaseDocumentTx(iURL);
	}

	@Test
	public void queryMax() {
		database.open("admin", "admin");
		List<ODocument> result = database.command(new OSQLSynchQuery<ODocument>("select max(nr) as max from Account")).execute();

		Assert.assertTrue(result.size() == 1);
		for (ODocument d : result) {
			Assert.assertNotNull(d.field("max"));
		}

		database.close();
	}

	@Test
	public void queryMin() {
		database.open("admin", "admin");
		List<ODocument> result = database.command(new OSQLSynchQuery<ODocument>("select min(nr) as min from Account")).execute();

		Assert.assertTrue(result.size() == 1);
		for (ODocument d : result) {
			Assert.assertNotNull(d.field("min"));

			Assert.assertEquals(((Number) d.field("min")).longValue(), 0l);
		}

		database.close();
	}

	@Test
	public void queryCount() {
		database.open("admin", "admin");
		List<ODocument> result = database.command(new OSQLSynchQuery<ODocument>("select count(*) as total from Account")).execute();

		Assert.assertTrue(result.size() == 1);
		for (ODocument d : result) {
			Assert.assertNotNull(d.field("total"));
			Assert.assertTrue(((Number) d.field("total")).longValue() > 0);
		}

		database.close();
	}

	@Test
	public void queryComposedAggregates() {
		database.open("admin", "admin");
		List<ODocument> result = database.command(
				new OSQLSynchQuery<ODocument>(
						"select min(nr) as min, max(nr) as max, average(nr) as average, count(nr) as total from Account")).execute();

		Assert.assertTrue(result.size() == 1);
		for (ODocument d : result) {
			Assert.assertNotNull(d.field("min"));
			Assert.assertNotNull(d.field("max"));
			Assert.assertNotNull(d.field("average"));
			Assert.assertNotNull(d.field("total"));

			Assert.assertTrue(((Number) d.field("max")).longValue() > ((Number) d.field("average")).longValue());
			Assert.assertTrue(((Number) d.field("average")).longValue() > ((Number) d.field("min")).longValue());
			Assert.assertTrue(((Number) d.field("total")).longValue() >= ((Number) d.field("max")).longValue());
		}

		database.close();
	}

	@Test
	public void queryFormat() {
		database.open("admin", "admin");
		List<ODocument> result = database.command(
				new OSQLSynchQuery<ODocument>("select format('%d - %s (%s)', nr, street, type, dummy ) as output from Account")).execute();

		Assert.assertTrue(result.size() > 1);
		for (ODocument d : result) {
			Assert.assertNotNull(d.field("output"));
		}

		database.close();
	}

	@Test(expectedExceptions = OCommandSQLParsingException.class)
	public void queryUndefinedFunction() {
		database.open("admin", "admin");
		database.command(new OSQLSynchQuery<ODocument>("select blaaaa(salary) as max from Account")).execute();
		database.close();
	}

	@Test
	public void queryCustomFunction() {
		database.open("admin", "admin");

		OSQLEngine.getInstance().registerFunction("bigger", new OSQLFunctionAbstract("bigger", 2, 2) {
			public String getSyntax() {
				return "bigger(<first>, <second>)";
			}

			public Object execute(final Object[] iParameters) {
				if (iParameters[0] == null || iParameters[1] == null)
					// CHECK BOTH EXPECTED PARAMETERS
					return null;

				if (!(iParameters[0] instanceof Number) || !(iParameters[1] instanceof Number))
					// EXCLUDE IT FROM THE RESULT SET
					return null;

				// USE DOUBLE TO AVOID LOSS OF PRECISION
				final double v1 = ((Number) iParameters[0]).doubleValue();
				final double v2 = ((Number) iParameters[1]).doubleValue();

				return Math.max(v1, v2);
			}

			public boolean aggregateResults() {
				return false;
			}
		});

		List<ODocument> result = database.command(new OSQLSynchQuery<ODocument>("select from Account where bigger(nr,1000) = 1000"))
				.execute();

		Assert.assertTrue(result.size() != 0);
		for (ODocument d : result) {
			Assert.assertTrue((Long) d.field("nr") <= 1000);
		}

		OSQLEngine.getInstance().unregisterInlineFunction("bigger");
		database.close();
	}
}
