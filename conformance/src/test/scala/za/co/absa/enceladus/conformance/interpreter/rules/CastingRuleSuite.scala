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

package za.co.absa.enceladus.conformance.interpreter.rules

import org.mockito.Mockito.{mock, when => mockWhen}
import org.scalatest.FunSuite
import za.co.absa.enceladus.conformance.CmdConfig
import za.co.absa.enceladus.conformance.interpreter.DynamicInterpreter
import za.co.absa.enceladus.dao.EnceladusDAO
import za.co.absa.enceladus.samples.CastingRuleSamples
import za.co.absa.enceladus.utils.testUtils.SparkTestBase

class CastingRuleSuite extends FunSuite with SparkTestBase {

  test("Casting conformance rule test") {

    import spark.implicits._
    val inputDf = spark.read.schema(CastingRuleSamples.ordersSchema).json(CastingRuleSamples.ordersData.toDS)

    spark.conf.set("spark.sql.session.timeZone", "GMT")

    inputDf.printSchema()
    inputDf.show

    implicit val dao: EnceladusDAO = mock(classOf[EnceladusDAO])
    implicit val progArgs = CmdConfig(reportDate = "2017-11-01")
    implicit val enableCF = false

    mockWhen (dao.getDataset("Orders Conformance", 1)) thenReturn CastingRuleSamples.ordersDS

    val mappingTablePattern = "{0}/{1}/{2}"

    import spark.implicits._
    val conformed = DynamicInterpreter.interpret(CastingRuleSamples.ordersDS, inputDf).cache

    conformed.printSchema()
    conformed.show

    val conformedJSON = conformed.orderBy($"id").toJSON.collect().mkString("\n")

    if (conformedJSON != CastingRuleSamples.conformedOrdersJSON) {
      println("EXPECTED:")
      println(CastingRuleSamples.conformedOrdersJSON)
      println("ACTUAL:")
      println(conformedJSON)
      fail("Actual conformed dataset JSON does not match the expected JSON (see above).")
    }

  }

  test("Casting rule fields validation test") {
    val schema = CastingRuleSamples.ordersSchema
    val dsName = "dataset"

    // These fields should pass the validation
    CastingRuleInterpreter.validateInputField(dsName, schema, "id")
    CastingRuleInterpreter.validateInputField(dsName, schema, "date")
    CastingRuleInterpreter.validateInputField(dsName, schema, "items.qty")
    CastingRuleInterpreter.validateOutputField(dsName, schema, "conformedvalue")
    CastingRuleInterpreter.validateOutputField(dsName, schema, "items.value")

    assert(intercept[ValidationException] {
      CastingRuleInterpreter.validateInputField(dsName, schema, "nosuchfield")
    }.getMessage contains "does not exist")

    assert(intercept[ValidationException] {
      CastingRuleInterpreter.validateInputField(dsName, schema, "items")
    }.getMessage contains "is not a primitive")

    assert(intercept[ValidationException] {
      CastingRuleInterpreter.validateOutputField(dsName, schema, "id")
    }.getMessage contains "already exists so it cannot be used")

    assert(intercept[ValidationException] {
      CastingRuleInterpreter.validateOutputField(dsName, schema, "items")
    }.getMessage contains "already exists so it cannot be used")

    assert(intercept[ValidationException] {
      CastingRuleInterpreter.validateOutputField(dsName, schema, "items.qty")
    }.getMessage contains "already exists so it cannot be used")

    assert(intercept[ValidationException] {
      CastingRuleInterpreter.validateOutputField(dsName, schema, "id.conformed")
    }.getMessage contains "is a primitive type")

    assert(intercept[ValidationException] {
      CastingRuleInterpreter.validateSameParent("id", "items.qty")
    }.getMessage contains "have different parents")

  }

}
