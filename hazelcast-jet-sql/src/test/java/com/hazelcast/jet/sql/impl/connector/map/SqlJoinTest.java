/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
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

package com.hazelcast.jet.sql.impl.connector.map;

import com.google.common.collect.ImmutableMap;
import com.hazelcast.jet.sql.SqlTestSupport;
import com.hazelcast.jet.sql.impl.connector.map.model.Person;
import com.hazelcast.jet.sql.impl.connector.map.model.PersonId;
import com.hazelcast.jet.sql.impl.connector.test.TestBatchSqlConnector;
import com.hazelcast.sql.SqlService;
import com.hazelcast.sql.impl.QueryException;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.hazelcast.sql.impl.type.QueryDataType.INT;
import static com.hazelcast.sql.impl.type.QueryDataType.VARCHAR;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SqlJoinTest extends SqlTestSupport {

    private static SqlService sqlService;

    @BeforeClass
    public static void setUpClass() {
        initialize(2, null);
        sqlService = instance().getSql();
    }

    @Test
    public void test_innerJoin() {
        String leftName = randomName();
        TestBatchSqlConnector.create(sqlService, leftName, 3);

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                1, "value-1",
                2, "value-2",
                3, "value-3"
        ));

        assertRowsAnyOrder(
                "SELECT l.v, m.this " +
                "FROM " + leftName + " l " +
                "INNER JOIN " + mapName + " m ON l.v = m.__key",
                asList(
                        new Row(1, "value-1"),
                        new Row(2, "value-2")
                )
        );
    }

    @Test
    public void test_innerJoinWithConditionInWhereClause() {
        String leftName = randomName();
        TestBatchSqlConnector.create(sqlService, leftName, 3);

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                1, "value-1",
                2, "value-2",
                3, "value-3"
        ));

        assertRowsAnyOrder(
                "SELECT l.v, m.this " +
                "FROM " + leftName + " l, " + mapName + " m " +
                "WHERE l.v = m.__key",
                asList(
                        new Row(1, "value-1"),
                        new Row(2, "value-2")
                )
        );
    }

    @Test
    public void test_innerJoinUsing() {
        String leftName = randomName();
        TestBatchSqlConnector.create(
                sqlService,
                leftName,
                singletonList("__key"),
                singletonList(INT),
                asList(new String[]{"0"}, new String[]{"1"}, new String[]{"2"})
        );

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                1, "value-1",
                2, "value-2",
                3, "value-3"
        ));

        assertRowsAnyOrder(
                "SELECT l.__key, m.this " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m USING (__key)",
                asList(
                        new Row(1, "value-1"),
                        new Row(2, "value-2")
                )
        );
    }

    @Test
    public void test_innerJoinNull() {
        String leftName = randomName();
        TestBatchSqlConnector.create(
                sqlService,
                leftName,
                singletonList("v"),
                singletonList(INT),
                asList(new String[]{"0"}, new String[]{null}, new String[]{"2"})
        );

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                1, "value-1",
                2, "value-2",
                3, "value-3"
        ));

        assertRowsAnyOrder(
                "SELECT l.v, m.this " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v = m.__key",
                singletonList(new Row(2, "value-2"))
        );
    }

    @Test
    public void test_innerJoinFilter() {
        String leftName = randomName();
        TestBatchSqlConnector.create(sqlService, leftName, 3);

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                1, "value-1",
                2, "value-2",
                3, "value-3"
        ));

        assertRowsAnyOrder(
                "SELECT l.v, m.this " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v = m.__key " +
                "WHERE m.__key < 2",
                singletonList(new Row(1, "value-1"))
        );
    }

    @Test
    public void test_innerJoinProject() {
        String leftName = randomName();
        TestBatchSqlConnector.create(sqlService, leftName, 3);

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                1, "value-1",
                2, "value-2",
                3, "value-3"
        ));

        assertRowsAnyOrder(
                "SELECT l.v, m.this || '-s' " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v = m.__key ",
                asList(
                        new Row(1, "value-1-s"),
                        new Row(2, "value-2-s")
                )
        );
    }

    @Test
    public void test_innerJoinConditionProject() {
        String leftName = randomName();
        TestBatchSqlConnector.create(sqlService, leftName, 3);

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                1, "value-1",
                2, "value-2",
                3, "value-3"
        ));

        assertRowsAnyOrder(
                "SELECT l.v, m.__key, m.this " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v = 2 * m.__key",
                singletonList(new Row(2, 1, "value-1"))
        );
    }

    @Test
    public void test_innerJoinOnValue() {
        String leftName = randomName();
        TestBatchSqlConnector.create(sqlService, leftName, 3);

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                "value-1", 1,
                "value-2", 2,
                "value-3", 3
        ));

        assertRowsAnyOrder(
                "SELECT l.v, m.__key " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v = m.this",
                asList(
                        new Row(1, "value-1"),
                        new Row(2, "value-2")
                )
        );
    }

    @Test
    public void test_innerJoinNonEqui() {
        String leftName = randomName();
        TestBatchSqlConnector.create(sqlService, leftName, 4);

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                1, "value-1",
                2, "value-2",
                3, "value-3"
        ));

        assertRowsAnyOrder(
                "SELECT l.v, m.__key, m.this " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v > m.__key",
                asList(
                        new Row(2, 1, "value-1"),
                        new Row(3, 1, "value-1"),
                        new Row(3, 2, "value-2")
                )
        );
    }

    @Test
    public void test_innerJoinEquiAndNonEqui() {
        String leftName = randomName();
        TestBatchSqlConnector.create(
                sqlService,
                leftName,
                asList("v1", "v2"),
                asList(INT, INT),
                asList(new String[]{"0", "0"}, new String[]{"1", "0"}, new String[]{"2", "2"})
        );

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                1, "value-1",
                2, "value-2",
                3, "value-3"
        ));

        assertRowsAnyOrder(
                "SELECT l.v1, l.v2, m.__key, m.this " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v1 = m.__key AND l.v2 != m.__key",
                singletonList(new Row(1, 0, 1, "value-1"))
        );
    }

    @Test
    public void test_innerJoinMulti() {
        String leftName = randomName();
        TestBatchSqlConnector.create(sqlService, leftName, 3);

        String mapName1 = randomName();
        instance().getMap(mapName1).putAll(ImmutableMap.of(
                1, "value-1.1",
                2, "value-1.2",
                3, "value-1.3"
        ));

        String mapName2 = randomName();
        instance().getMap(mapName2).putAll(ImmutableMap.of(
                1, "value-2.1",
                2, "value-2.2",
                3, "value-2.3"
        ));

        assertRowsAnyOrder(
                "SELECT l.v, m1.this, m2.this " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName1 + " m1 ON l.v = m1.__key " +
                "JOIN " + mapName2 + " m2 ON l.v + m1.__key > m2.__key",
                asList(
                        new Row(1, "value-1.1", "value-2.1"),
                        new Row(2, "value-1.2", "value-2.1"),
                        new Row(2, "value-1.2", "value-2.2"),
                        new Row(2, "value-1.2", "value-2.3")
                )
        );
    }

    @Test
    public void test_innerJoinPartOfTheCompositeKey() {
        String leftName = randomName();
        TestBatchSqlConnector.create(
                sqlService,
                leftName,
                singletonList("v"),
                singletonList(INT),
                asList(new String[]{"0"}, new String[]{null}, new String[]{"2"})
        );

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                new Person(null, "value-1"), new PersonId(),
                new Person(2, "value-2"), new PersonId(),
                new Person(3, "value-3"), new PersonId()
        ));

        assertRowsEventuallyInAnyOrder(
                "SELECT l.v, m.name, m.id " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v = m.id",
                singletonList(new Row(2, "value-2", 2))
        );
    }

    @Test
    public void test_innerJoinFullCompositeKeyConjunction() {
        String leftName = randomName();
        TestBatchSqlConnector.create(
                sqlService,
                leftName,
                asList("v1", "v2"),
                asList(INT, VARCHAR),
                asList(new String[]{"0", "value-0"}, new String[]{"1", null}, new String[]{"2", "value-2"})
        );

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                new Person(1, null), new PersonId(),
                new Person(2, "value-2"), new PersonId(),
                new Person(3, "value-3"), new PersonId()
        ));

        assertRowsEventuallyInAnyOrder(
                "SELECT l.v1, l.v2, m.id, m.name " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v1 = m.id AND l.v2 = m.name",
                singletonList(new Row(2, "value-2", 2, "value-2"))
        );
    }

    @Test
    public void test_innerJoinFullCompositeKeyDisjunction() {
        String leftName = randomName();
        TestBatchSqlConnector.create(
                sqlService,
                leftName,
                asList("v1", "v2"),
                asList(INT, VARCHAR),
                asList(new String[]{"0", "value-0"}, new String[]{"1", null}, new String[]{"2", "value-2"})
        );

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                new Person(1, "value-1"), new PersonId(),
                new Person(2, "value-2"), new PersonId(),
                new Person(3, "value-3"), new PersonId()
        ));

        assertRowsEventuallyInAnyOrder(
                "SELECT l.v1, l.v2, m.id, m.name " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v1 = m.id OR l.v2 = m.name",
                asList(
                        new Row(1, null, 1, "value-1"),
                        new Row(2, "value-2", 2, "value-2")
                )
        );
    }

    @Test
    public void test_innerJoinPartOfTheCompositeValue() {
        String leftName = randomName();
        TestBatchSqlConnector.create(
                sqlService,
                leftName,
                singletonList("v"),
                singletonList(VARCHAR),
                asList(new String[]{"value-0"}, new String[]{"value-1"}, new String[]{"value-2"})
        );

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                new PersonId(1), new Person(0, "value-1"),
                new PersonId(2), new Person(0, "value-2"),
                new PersonId(3), new Person(0, "value-3")
        ));

        assertRowsEventuallyInAnyOrder(
                "SELECT l.v, m.id " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v = m.name",
                asList(
                        new Row("value-1", 1),
                        new Row("value-2", 2)
                )
        );
    }

    @Test
    public void test_innerJoinKeyAndValue() {
        String leftName = randomName();
        TestBatchSqlConnector.create(sqlService, leftName, 3);

        String mapName = randomName();
        instance().getMap(mapName).putAll(ImmutableMap.of(
                1, new Person(0, "value-1"),
                2, new Person(2, "value-2"),
                3, new Person(0, "value-3")
        ));

        assertRowsEventuallyInAnyOrder(
                "SELECT l.v, m.id, m.name " +
                "FROM " + leftName + " l " +
                "JOIN " + mapName + " m ON l.v = m.__key AND l.v = m.id",
                singletonList(new Row(2, 2, "value-2"))
        );
    }

    @Test
    public void test_innerJoinWithSubqueryFails() {
        String leftName = randomName();
        TestBatchSqlConnector.create(sqlService, leftName, 1);

        String mapName = randomName();
        instance().getMap(mapName).put(1, "value-1");

        assertThatThrownBy(() ->
                sqlService.execute(
                        "SELECT 1 " +
                        "FROM " + leftName + " AS l " +
                        "JOIN (SELECT * FROM " + mapName + ") AS m ON l.v = m.__key"
                ))
                .hasCauseInstanceOf(QueryException.class)
                .hasMessageContaining("Subquery on the right side of a join not supported");

        // SELECT * FROM left JOIN (SELECT SUM(__key) AS __key FROM map) AS m ON l.v = m.__key
        // SELECT * FROM left JOIN LATERAL (SELECT __key, this FROM map WHERE __key < 3) AS m ON 1 = 1
        // SELECT * FROM left JOIN LATERAL (SELECT __key FROM map WHERE __key > l.v) m ON 1 = 1
    }
}