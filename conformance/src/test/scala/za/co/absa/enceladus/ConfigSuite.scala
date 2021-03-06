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

package za.co.absa.enceladus

import java.time.ZonedDateTime

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite
import za.co.absa.enceladus.conformance.{CmdConfig, DynamicConformanceJob}
import za.co.absa.enceladus.model.Dataset

class ConfigSuite extends FunSuite {

  private val year = "2018"
  private val month = "12"
  private val day = "31"
  private val dateTokens = Array(year, month, day)
  private val hdfsRawPath = "/bigdatahdfs/datalake/raw/system/feed"
  private val hdfsPublishPath = "/bigdatahdfs/datalake/publish/system/feed"
  private val hdfsPublishPathOverride = "/bigdatahdfs/datalake/publish/system/feed/override"
  private val datasetName = "test-dataset-name"
  private val datasetVersion = 2
  private val description = None
  private val schemaName = "test-schema-name"
  private val schemaVersion = 4
  private val dateCreated = ZonedDateTime.now()
  private val userCreated = "user"
  private val lastUpdated = ZonedDateTime.now()
  private val userUpdated = "user"
  private val reportDate = s"$year-$month-$day"
  private val reportVersion = 3
  private val disabled = false
  private val dateDisabled = None
  private val userDisabled = None
  private val rawFormat = "parquet"
  private val folderPrefix = s"year=$year/month=$month/day=$day"
  private val infoDateColumn = "enceladus_info_date"
  private val infoVersionColumn = "enceladus_info_version"

  test("folder-prefix parameter") {
    val cmdConfigNoFolderPrefix = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString))
    assert(cmdConfigNoFolderPrefix.datasetName === datasetName)
    assert(cmdConfigNoFolderPrefix.datasetVersion === datasetVersion)
    assert(cmdConfigNoFolderPrefix.reportDate === reportDate)
    assert(cmdConfigNoFolderPrefix.reportVersion === reportVersion)
    assert(cmdConfigNoFolderPrefix.folderPrefix.isEmpty)
    assert(cmdConfigNoFolderPrefix.publishPathOverride.isEmpty)
    val cmdConfigFolderPrefix = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--folder-prefix", folderPrefix))
    assert(cmdConfigFolderPrefix.datasetName === datasetName)
    assert(cmdConfigFolderPrefix.datasetVersion === datasetVersion)
    assert(cmdConfigFolderPrefix.reportDate === reportDate)
    assert(cmdConfigFolderPrefix.reportVersion === reportVersion)
    assert(cmdConfigFolderPrefix.folderPrefix.nonEmpty)
    assert(cmdConfigFolderPrefix.folderPrefix.get === folderPrefix)
    assert(cmdConfigFolderPrefix.publishPathOverride.isEmpty)
    val cmdConfigPublishPathOverride = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--debug-set-publish-path", hdfsPublishPathOverride))
    assert(cmdConfigPublishPathOverride.datasetName === datasetName)
    assert(cmdConfigPublishPathOverride.datasetVersion === datasetVersion)
    assert(cmdConfigPublishPathOverride.reportDate === reportDate)
    assert(cmdConfigPublishPathOverride.reportVersion === reportVersion)
    assert(cmdConfigPublishPathOverride.folderPrefix.isEmpty)
    assert(cmdConfigPublishPathOverride.publishPathOverride.nonEmpty)
    assert(cmdConfigPublishPathOverride.publishPathOverride.get === hdfsPublishPathOverride)
    val cmdConfigPublishPathOverrideAndFolderPrefix = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--debug-set-publish-path", hdfsPublishPathOverride,
        "--folder-prefix", folderPrefix))
    assert(cmdConfigPublishPathOverrideAndFolderPrefix.datasetName === datasetName)
    assert(cmdConfigPublishPathOverrideAndFolderPrefix.datasetVersion === datasetVersion)
    assert(cmdConfigPublishPathOverrideAndFolderPrefix.reportDate === reportDate)
    assert(cmdConfigPublishPathOverrideAndFolderPrefix.reportVersion === reportVersion)
    assert(cmdConfigFolderPrefix.folderPrefix.nonEmpty)
    assert(cmdConfigFolderPrefix.folderPrefix.get === folderPrefix)
    assert(cmdConfigPublishPathOverrideAndFolderPrefix.publishPathOverride.nonEmpty)
    assert(cmdConfigPublishPathOverrideAndFolderPrefix.publishPathOverride.get === hdfsPublishPathOverride)
  }

  test("Test buildPublishPath") {
    val conformanceDataset = Dataset(
      datasetName,
      datasetVersion,
      description,
      hdfsRawPath,
      hdfsPublishPath,
      schemaName,
      schemaVersion,
      dateCreated,
      userCreated,
      lastUpdated,
      userUpdated,
      disabled,
      dateDisabled,
      userDisabled,
      List()
    )
    val cmdConfigNoFolderPrefix = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString))
    val cmdConfigFolderPrefix = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--folder-prefix", folderPrefix))
    val cmdConfigPublishPathOverride = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--debug-set-publish-path", hdfsPublishPathOverride))
    val cmdConfigPublishPathOverrideAndFolderPrefix = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--folder-prefix", folderPrefix,
        "--debug-set-publish-path", hdfsPublishPathOverride))
    val publishPathNoFolderPrefix = DynamicConformanceJob.buildPublishPath(infoDateColumn, infoVersionColumn, cmdConfigNoFolderPrefix, conformanceDataset)
    assert(publishPathNoFolderPrefix === s"$hdfsPublishPath/$infoDateColumn=$reportDate/$infoVersionColumn=$reportVersion")
    val publishPathFolderPrefix = DynamicConformanceJob.buildPublishPath(infoDateColumn, infoVersionColumn, cmdConfigFolderPrefix, conformanceDataset)
    assert(publishPathFolderPrefix === s"$hdfsPublishPath/$folderPrefix/$infoDateColumn=$reportDate/$infoVersionColumn=$reportVersion")
    val publishPathPublishPathOverride = DynamicConformanceJob.buildPublishPath(infoDateColumn, infoVersionColumn, cmdConfigPublishPathOverride, conformanceDataset)
    assert(publishPathPublishPathOverride === hdfsPublishPathOverride)
    val publishPathPublishPathOverrideAndFolderPrefix = DynamicConformanceJob.buildPublishPath(infoDateColumn, infoVersionColumn, cmdConfigPublishPathOverrideAndFolderPrefix, conformanceDataset)
    assert(publishPathPublishPathOverrideAndFolderPrefix === hdfsPublishPathOverride)
  }

}
