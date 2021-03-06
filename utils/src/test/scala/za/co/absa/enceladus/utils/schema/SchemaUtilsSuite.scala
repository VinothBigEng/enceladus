/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.enceladus.utils.schema

import org.scalatest.FunSuite
import org.apache.spark.sql.types._

class SchemaUtilsSuite extends FunSuite {

  val schema = StructType(Seq(
    StructField("a", IntegerType, false),
    StructField("b", StructType(Seq(
      StructField("c", IntegerType),
      StructField("d", StructType(Seq(
        StructField("e", IntegerType))), true)))),
    StructField("f", StructType(Seq(
      StructField("g", ArrayType.apply(StructType(Seq(
        StructField("h", IntegerType))))))))))

  val nestedSchema = StructType(Seq(
    StructField("a", IntegerType),
    StructField("b", ArrayType(StructType(Seq(
      StructField("c", StructType(Seq(
        StructField("d", ArrayType(StructType(Seq(
          StructField("e", IntegerType))))))))))))))

  val structFieldNoMetadata = StructField("a", IntegerType)

  val structFieldWithMetadataNotSourceColumn = StructField("a", IntegerType, nullable = false, new MetadataBuilder().putString("meta", "data").build)
  val structFieldWithMetadataSourceColumn = StructField("a", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "override_a").build)

  test("Testing getFieldType") {

    val a = SchemaUtils.getFieldType("a", schema)
    val b = SchemaUtils.getFieldType("b", schema)
    val c = SchemaUtils.getFieldType("b.c", schema)
    val d = SchemaUtils.getFieldType("b.d", schema)
    val e = SchemaUtils.getFieldType("b.d.e", schema)
    val f = SchemaUtils.getFieldType("f", schema)
    val g = SchemaUtils.getFieldType("f.g", schema)
    val h = SchemaUtils.getFieldType("f.g.h", schema)

    assert(a.get.isInstanceOf[IntegerType])
    assert(b.get.isInstanceOf[StructType])
    assert(c.get.isInstanceOf[IntegerType])
    assert(d.get.isInstanceOf[StructType])
    assert(e.get.isInstanceOf[IntegerType])
    assert(f.get.isInstanceOf[StructType])
    assert(g.get.isInstanceOf[ArrayType])
    assert(h.get.isInstanceOf[IntegerType])
    assert(SchemaUtils.getFieldType("z", schema).isEmpty)
    assert(SchemaUtils.getFieldType("x.y.z", schema).isEmpty)
    assert(SchemaUtils.getFieldType("f.g.h.a", schema).isEmpty)
  }

  test("Testing getFirstArrayPath") {
    assertResult("f.g")(SchemaUtils.getFirstArrayPath("f.g.h", schema))
    assertResult("f.g")(SchemaUtils.getFirstArrayPath("f.g", schema))
    assertResult("")(SchemaUtils.getFirstArrayPath("z.x.y", schema))
    assertResult("")(SchemaUtils.getFirstArrayPath("b.c.d.e", schema))
  }

  test("Testing getAllArrayPaths") {
    assertResult(Seq("f.g"))(SchemaUtils.getAllArrayPaths(schema))
    assertResult(Seq())(SchemaUtils.getAllArrayPaths(schema("b").dataType.asInstanceOf[StructType]))
  }

  test("Testing getAllArraysInPath") {
    assertResult(Seq("b", "b.c.d"))(SchemaUtils.getAllArraysInPath("b.c.d.e", nestedSchema))
  }

  test("Testing getFieldNameOverriddenByMetadata") {
    assertResult("a")(SchemaUtils.getFieldNameOverriddenByMetadata(structFieldNoMetadata))
    assertResult("a")(SchemaUtils.getFieldNameOverriddenByMetadata(structFieldWithMetadataNotSourceColumn))
    assertResult("override_a")(SchemaUtils.getFieldNameOverriddenByMetadata(structFieldWithMetadataSourceColumn))
  }

  test("Testing getFieldNullability") {
    assert(!SchemaUtils.getFieldNullability("a", schema).get)
    assert(SchemaUtils.getFieldNullability("b.d", schema).get)
    assert(SchemaUtils.getFieldNullability("x.y.z", schema).isEmpty)
  }
}