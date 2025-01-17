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

package org.apache.flink.table.utils;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.catalog.Column;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.data.DecimalData;
import org.apache.flink.table.data.GenericArrayData;
import org.apache.flink.table.data.GenericMapData;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/** Tests for {@link PrintUtils}. */
public class PrintUtilsTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

    @Test
    public void testArrayToString() {
        RowData row =
                GenericRowData.of(
                        new GenericArrayData(new int[] {1, 2}),
                        new GenericArrayData(new int[] {3, 4}),
                        new GenericArrayData(
                                new Object[] {
                                    new GenericArrayData(new int[] {5, 6}),
                                    new GenericArrayData(new int[] {7, 8})
                                }),
                        new GenericArrayData(
                                new Object[] {
                                    new GenericArrayData(new int[] {9, 10}),
                                    new GenericArrayData(new int[] {11, 12})
                                }),
                        new GenericArrayData(
                                new Object[] {
                                    TimestampData.fromLocalDateTime(
                                            LocalDateTime.parse("2021-04-18T18:00:00.123456")),
                                    TimestampData.fromLocalDateTime(
                                            LocalDateTime.parse("2021-04-18T18:00:00.000001"))
                                }),
                        new GenericArrayData(
                                new Object[] {
                                    new GenericArrayData(
                                            new Object[] {
                                                TimestampData.fromInstant(Instant.ofEpochMilli(1)),
                                                TimestampData.fromInstant(Instant.ofEpochMilli(10))
                                            }),
                                    new GenericArrayData(
                                            new Object[] {
                                                TimestampData.fromInstant(Instant.ofEpochSecond(1)),
                                                TimestampData.fromInstant(Instant.ofEpochSecond(10))
                                            })
                                }),
                        new GenericArrayData(new int[] {1123, 2123}));

        ResolvedSchema resolvedSchema =
                ResolvedSchema.of(
                        Arrays.asList(
                                Column.physical("f0", DataTypes.ARRAY(DataTypes.INT())),
                                Column.physical("f1", DataTypes.ARRAY(DataTypes.INT())),
                                Column.physical(
                                        "f2", DataTypes.ARRAY(DataTypes.ARRAY(DataTypes.INT()))),
                                Column.physical(
                                        "f3", DataTypes.ARRAY(DataTypes.ARRAY(DataTypes.INT()))),
                                Column.physical("f4", DataTypes.ARRAY(DataTypes.TIMESTAMP(6))),
                                Column.physical(
                                        "f5",
                                        DataTypes.ARRAY(
                                                DataTypes.ARRAY(DataTypes.TIMESTAMP_LTZ(3)))),
                                Column.physical("f6", DataTypes.ARRAY(DataTypes.TIME()))));
        assertEquals(
                "[[1, 2], [3, 4], [[5, 6], [7, 8]], [[9, 10], [11, 12]],"
                        + " [2021-04-18 18:00:00.123456, 2021-04-18 18:00:00.000001],"
                        + " [[1970-01-01 00:00:00.001, 1970-01-01 00:00:00.010],"
                        + " [1970-01-01 00:00:01.000, 1970-01-01 00:00:10.000]],"
                        + " [00:00:01, 00:00:02]]",
                Arrays.toString(PrintUtils.rowToString(row, resolvedSchema, UTC_ZONE_ID)));
    }

    @Test
    public void testNestedRowToString() {
        RowData row =
                GenericRowData.of(
                        new GenericArrayData(new int[] {1, 2}),
                        GenericRowData.of(
                                StringData.fromString("hello"),
                                new GenericArrayData(new boolean[] {true, false}),
                                new GenericArrayData(
                                        new Object[] {
                                            TimestampData.fromTimestamp(
                                                    Timestamp.valueOf(
                                                            "2021-04-18 18:00:00.123456")),
                                            TimestampData.fromTimestamp(
                                                    Timestamp.valueOf("2021-04-18 18:00:00.000001"))
                                        }),
                                new GenericArrayData(
                                        new Object[] {
                                            TimestampData.fromEpochMillis(100L),
                                            TimestampData.fromEpochMillis(200L)
                                        })),
                        new GenericArrayData(
                                new Object[] {
                                    new GenericArrayData(new int[] {1, 10}),
                                    new GenericArrayData(new int[] {2, 20})
                                }),
                        new GenericArrayData(new int[] {3000, 4000}));

        ResolvedSchema resolvedSchema =
                ResolvedSchema.of(
                        Arrays.asList(
                                Column.physical("f0", DataTypes.ARRAY(DataTypes.INT())),
                                Column.physical(
                                        "f1",
                                        DataTypes.ROW(
                                                DataTypes.STRING(),
                                                DataTypes.ARRAY(DataTypes.BOOLEAN()),
                                                DataTypes.ARRAY(DataTypes.TIMESTAMP(6)),
                                                DataTypes.ARRAY(DataTypes.TIMESTAMP_LTZ(6)))),
                                Column.physical(
                                        "f2", DataTypes.ARRAY(DataTypes.ARRAY(DataTypes.INT()))),
                                Column.physical("f3", DataTypes.ARRAY(DataTypes.TIME()))));
        assertEquals(
                "[[1, 2], +I[hello, [true, false],"
                        + " [2021-04-18 18:00:00.123456, 2021-04-18 18:00:00.000001],"
                        + " [1970-01-01 00:00:00.100000, 1970-01-01 00:00:00.200000]], [[1, 10], [2, 20]],"
                        + " [00:00:03, 00:00:04]]",
                Arrays.toString(PrintUtils.rowToString(row, resolvedSchema, UTC_ZONE_ID)));
    }

    @Test
    public void testNestedMapToString() {
        Map<TimestampData, TimestampData> map = new HashMap<>();
        map.put(TimestampData.fromEpochMillis(1000), TimestampData.fromEpochMillis(2000));
        map.put(TimestampData.fromEpochMillis(2000), TimestampData.fromEpochMillis(4000));

        RowData row =
                GenericRowData.of(
                        new GenericArrayData(new int[] {1, 2}),
                        GenericRowData.of(StringData.fromString("hello"), new GenericMapData(map)));

        ResolvedSchema resolvedSchema =
                ResolvedSchema.of(
                        Arrays.asList(
                                Column.physical("f0", DataTypes.ARRAY(DataTypes.INT())),
                                Column.physical(
                                        "f1",
                                        DataTypes.ROW(
                                                DataTypes.STRING(),
                                                DataTypes.MAP(
                                                        DataTypes.TIMESTAMP_LTZ(3),
                                                        DataTypes.TIMESTAMP_LTZ(3))))));
        assertEquals(
                "[[1, 2], +I[hello,"
                        + " {1970-01-01 00:00:01.000=1970-01-01 00:00:02.000, 1970-01-01 00:00:02.000=1970-01-01 00:00:04.000}]]",
                Arrays.toString(PrintUtils.rowToString(row, resolvedSchema, UTC_ZONE_ID)));
    }

    @Test
    public void testCharFullWidth() {
        char[] chars = new char[] {'A', 'a', ',', '中', '，', 'こ'};
        boolean[] expected = new boolean[] {false, false, false, true, true, true};

        for (int i = 0; i < chars.length; i++) {
            assertEquals(expected[i], PrintUtils.isFullWidth(Character.codePointAt(chars, i)));
        }
    }

    @Test
    public void testStringDisplayWidth() {
        List<String> data =
                Arrays.asList(
                        "abcdefg,12345,ABC",
                        "to be or not to be that's a question.",
                        "这是一段中文",
                        "これは日本語をテストするための文です");
        int[] expected = new int[] {17, 37, 12, 36};

        for (int i = 0; i < data.size(); i++) {
            assertEquals(expected[i], PrintUtils.getStringDisplayWidth(data.get(i)));
        }
    }

    @Test
    public void testPrintWithEmptyResult() {
        PrintUtils.printAsTableauForm(
                getSchema(), Collections.emptyIterator(), new PrintWriter(outContent), UTC_ZONE_ID);

        assertEquals("Empty set" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testPrintWithEmptyResultAndRowKind() {
        PrintUtils.printAsTableauForm(
                getSchema(),
                Collections.emptyIterator(),
                new PrintWriter(outContent),
                PrintUtils.MAX_COLUMN_WIDTH,
                "",
                true, // derive column width by type
                true,
                UTC_ZONE_ID);

        assertEquals("Empty set" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testPrintWithEmptyResultAndDeriveColumnWidthByContent() {
        PrintUtils.printAsTableauForm(
                getSchema(),
                Collections.emptyIterator(),
                new PrintWriter(outContent),
                PrintUtils.MAX_COLUMN_WIDTH,
                "",
                false, // derive column width by content
                false,
                UTC_ZONE_ID);

        assertEquals("Empty set" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testPrintWithMultipleRows() {
        PrintUtils.printAsTableauForm(
                getSchema(), getData().iterator(), new PrintWriter(outContent), UTC_ZONE_ID);

        // note: the expected result may look irregular because every CJK(Chinese/Japanese/Korean)
        // character's
        // width < 2 in IDE by default, every CJK character usually's width is 2, you can open this
        // source file
        // by vim or just cat the file to check the regular result.
        // The last row of `varchar` value will pad with two ' ' before the column.
        // Because the length of `これは日本語をテストするた` plus the length of `...` is 29,
        // no more Japanese character can be added to the line.
        assertEquals(
                "+---------+-------------+----------------------+--------------------------------+----------------+----------------------------+"
                        + System.lineSeparator()
                        + "| boolean |         int |               bigint |                        varchar | decimal(10, 5) |                  timestamp |"
                        + System.lineSeparator()
                        + "+---------+-------------+----------------------+--------------------------------+----------------+----------------------------+"
                        + System.lineSeparator()
                        + "|  (NULL) |           1 |                    2 |                            abc |        1.23000 | 2020-03-01 18:39:14.000000 |"
                        + System.lineSeparator()
                        + "|   false |      (NULL) |                    0 |                                |        1.00000 | 2020-03-01 18:39:14.100000 |"
                        + System.lineSeparator()
                        + "|    true |  2147483647 |               (NULL) |                        abcdefg |    12345.00000 | 2020-03-01 18:39:14.120000 |"
                        + System.lineSeparator()
                        + "|   false | -2147483648 |  9223372036854775807 |                         (NULL) |    12345.06789 | 2020-03-01 18:39:14.123000 |"
                        + System.lineSeparator()
                        + "|    true |         100 | -9223372036854775808 |                     abcdefg111 |         (NULL) | 2020-03-01 18:39:14.123456 |"
                        + System.lineSeparator()
                        + "|  (NULL) |          -1 |                   -1 | abcdefghijklmnopqrstuvwxyza... |   -12345.06789 |                     (NULL) |"
                        + System.lineSeparator()
                        + "|  (NULL) |          -1 |                   -1 |                   这是一段中文 |   -12345.06789 | 2020-03-04 18:39:14.000000 |"
                        + System.lineSeparator()
                        + "|  (NULL) |          -1 |                   -1 |  これは日本語をテストするた... |   -12345.06789 | 2020-03-04 18:39:14.000000 |"
                        + System.lineSeparator()
                        + "+---------+-------------+----------------------+--------------------------------+----------------+----------------------------+"
                        + System.lineSeparator()
                        + "8 rows in set"
                        + System.lineSeparator(),
                outContent.toString());
    }

    @Test
    public void testPrintWithMultipleRowsAndRowKind() {
        PrintUtils.printAsTableauForm(
                getSchema(),
                getData().iterator(),
                new PrintWriter(outContent),
                PrintUtils.MAX_COLUMN_WIDTH,
                "",
                true, // derive column width by type
                true,
                UTC_ZONE_ID);

        // note: the expected result may look irregular because every CJK(Chinese/Japanese/Korean)
        // character's
        // width < 2 in IDE by default, every CJK character usually's width is 2, you can open this
        // source file
        // by vim or just cat the file to check the regular result.
        // The last row of `varchar` value will pad with two ' ' before the column.
        // Because the length of `これは日本語をテストするた` plus the length of `...` is 29,
        // no more Japanese character can be added to the line.
        assertEquals(
                "+----+---------+-------------+----------------------+--------------------------------+----------------+----------------------------+"
                        + System.lineSeparator()
                        + "| op | boolean |         int |               bigint |                        varchar | decimal(10, 5) |                  timestamp |"
                        + System.lineSeparator()
                        + "+----+---------+-------------+----------------------+--------------------------------+----------------+----------------------------+"
                        + System.lineSeparator()
                        + "| +I |         |           1 |                    2 |                            abc |        1.23000 | 2020-03-01 18:39:14.000000 |"
                        + System.lineSeparator()
                        + "| +I |   false |             |                    0 |                                |        1.00000 | 2020-03-01 18:39:14.100000 |"
                        + System.lineSeparator()
                        + "| -D |    true |  2147483647 |                      |                        abcdefg |    12345.00000 | 2020-03-01 18:39:14.120000 |"
                        + System.lineSeparator()
                        + "| +I |   false | -2147483648 |  9223372036854775807 |                                |    12345.06789 | 2020-03-01 18:39:14.123000 |"
                        + System.lineSeparator()
                        + "| +I |    true |         100 | -9223372036854775808 |                     abcdefg111 |                | 2020-03-01 18:39:14.123456 |"
                        + System.lineSeparator()
                        + "| -U |         |          -1 |                   -1 | abcdefghijklmnopqrstuvwxyza... |   -12345.06789 |                            |"
                        + System.lineSeparator()
                        + "| +U |         |          -1 |                   -1 |                   这是一段中文 |   -12345.06789 | 2020-03-04 18:39:14.000000 |"
                        + System.lineSeparator()
                        + "| -D |         |          -1 |                   -1 |  これは日本語をテストするた... |   -12345.06789 | 2020-03-04 18:39:14.000000 |"
                        + System.lineSeparator()
                        + "+----+---------+-------------+----------------------+--------------------------------+----------------+----------------------------+"
                        + System.lineSeparator()
                        + "8 rows in set"
                        + System.lineSeparator(),
                outContent.toString());
    }

    @Test
    public void testPrintWithMultipleRowsAndDeriveColumnWidthByContent() {
        PrintUtils.printAsTableauForm(
                getSchema(),
                getData().subList(0, 3).iterator(),
                new PrintWriter(outContent),
                PrintUtils.MAX_COLUMN_WIDTH,
                "",
                false, // derive column width by content
                true,
                UTC_ZONE_ID);

        assertEquals(
                "+----+---------+------------+--------+---------+----------------+----------------------------+"
                        + System.lineSeparator()
                        + "| op | boolean |        int | bigint | varchar | decimal(10, 5) |                  timestamp |"
                        + System.lineSeparator()
                        + "+----+---------+------------+--------+---------+----------------+----------------------------+"
                        + System.lineSeparator()
                        + "| +I |         |          1 |      2 |     abc |        1.23000 | 2020-03-01 18:39:14.000000 |"
                        + System.lineSeparator()
                        + "| +I |   false |            |      0 |         |        1.00000 | 2020-03-01 18:39:14.100000 |"
                        + System.lineSeparator()
                        + "| -D |    true | 2147483647 |        | abcdefg |    12345.00000 | 2020-03-01 18:39:14.120000 |"
                        + System.lineSeparator()
                        + "+----+---------+------------+--------+---------+----------------+----------------------------+"
                        + System.lineSeparator()
                        + "3 rows in set"
                        + System.lineSeparator(),
                outContent.toString());
    }

    private ResolvedSchema getSchema() {
        return ResolvedSchema.of(
                Column.physical("boolean", DataTypes.BOOLEAN()),
                Column.physical("int", DataTypes.INT()),
                Column.physical("bigint", DataTypes.BIGINT()),
                Column.physical("varchar", DataTypes.STRING()),
                Column.physical("decimal(10, 5)", DataTypes.DECIMAL(10, 5)),
                Column.physical("timestamp", DataTypes.TIMESTAMP(6)));
    }

    private List<RowData> getData() {
        List<Row> data = new ArrayList<>();
        data.add(
                Row.ofKind(
                        RowKind.INSERT,
                        null,
                        1,
                        2L,
                        "abc",
                        BigDecimal.valueOf(1.23),
                        Timestamp.valueOf("2020-03-01 18:39:14")));
        data.add(
                Row.ofKind(
                        RowKind.INSERT,
                        false,
                        null,
                        0L,
                        "",
                        BigDecimal.valueOf(1),
                        Timestamp.valueOf("2020-03-01 18:39:14.1")));
        data.add(
                Row.ofKind(
                        RowKind.DELETE,
                        true,
                        Integer.MAX_VALUE,
                        null,
                        "abcdefg",
                        BigDecimal.valueOf(12345),
                        Timestamp.valueOf("2020-03-01 18:39:14.12")));
        data.add(
                Row.ofKind(
                        RowKind.INSERT,
                        false,
                        Integer.MIN_VALUE,
                        Long.MAX_VALUE,
                        null,
                        BigDecimal.valueOf(12345.06789),
                        Timestamp.valueOf("2020-03-01 18:39:14.123")));
        data.add(
                Row.ofKind(
                        RowKind.INSERT,
                        true,
                        100,
                        Long.MIN_VALUE,
                        "abcdefg111",
                        null,
                        Timestamp.valueOf("2020-03-01 18:39:14.123456")));
        data.add(
                Row.ofKind(
                        RowKind.UPDATE_BEFORE,
                        null,
                        -1,
                        -1L,
                        "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz",
                        BigDecimal.valueOf(-12345.06789),
                        null));
        data.add(
                Row.ofKind(
                        RowKind.UPDATE_AFTER,
                        null,
                        -1,
                        -1L,
                        "这是一段中文",
                        BigDecimal.valueOf(-12345.06789),
                        Timestamp.valueOf("2020-03-04 18:39:14")));
        data.add(
                Row.ofKind(
                        RowKind.DELETE,
                        null,
                        -1,
                        -1L,
                        "これは日本語をテストするための文です",
                        BigDecimal.valueOf(-12345.06789),
                        Timestamp.valueOf("2020-03-04 18:39:14")));
        return data.stream()
                .map(
                        row ->
                                GenericRowData.ofKind(
                                        row.getKind(),
                                        row.getField(0),
                                        row.getField(1),
                                        row.getField(2),
                                        (row.getField(3) == null)
                                                ? null
                                                : StringData.fromString(row.getFieldAs(3)),
                                        (row.getField(4) == null)
                                                ? null
                                                : DecimalData.fromBigDecimal(
                                                        row.getFieldAs(4), 10, 5),
                                        (row.getField(5) == null)
                                                ? null
                                                : TimestampData.fromTimestamp(row.getFieldAs(5))))
                .collect(Collectors.toList());
    }
}
