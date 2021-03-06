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

package za.co.absa.enceladus.rest.controllers

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.apache.spark.sql.SparkSession
import org.springframework.web.bind.annotation.GetMapping

case class Test(a: Int, b: String)

@RestController
@RequestMapping(Array("/api/spark"))
class SparkController {
  val spark = SparkSession.builder().master("local[4]").appName("Menas Spark controller").getOrCreate()

  @GetMapping(path = Array("/version"))
  def sparkVersion(): String = spark.sparkContext.version

}