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

import org.scalatest.FunSuite
import za.co.absa.enceladus.model.Dataset
import za.co.absa.enceladus.standardization.{CmdConfig, StandardizationJob}

class ConfigSuite extends FunSuite {

  private val year = "2018"
  private val month = "12"
  private val day = "31"
  private val dateTokens = Array(year, month, day)
  private val hdfsRawPath = "/bigdatahdfs/datalake/raw/system/feed"
  private val hdfsRawPathOverride = "/bigdatahdfs/datalake/raw/system/feed/override"
  private val hdfsPublishPath = "/bigdatahdfs/datalake/publish/system/feed"
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
  private val disabled = false
  private val dateDisabled = None
  private val userDisabled = None
  private val reportVersion = 3
  private val rawFormat = "parquet"
  private val folderPrefix = s"year=$year/month=$month/day=$day"

  test("folder-prefix parameter") {
    val cmdConfigNoFolderPrefix = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--raw-format", rawFormat))
    assert(cmdConfigNoFolderPrefix.datasetName === datasetName)
    assert(cmdConfigNoFolderPrefix.datasetVersion === datasetVersion)
    assert(cmdConfigNoFolderPrefix.reportDate === reportDate)
    assert(cmdConfigNoFolderPrefix.reportVersion === reportVersion)
    assert(cmdConfigNoFolderPrefix.rawFormat === rawFormat)
    assert(cmdConfigNoFolderPrefix.folderPrefix.isEmpty)
    assert(cmdConfigNoFolderPrefix.rawPathOverride.isEmpty)
    val cmdConfigFolderPrefix = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--raw-format", rawFormat,
        "--folder-prefix", folderPrefix))
    assert(cmdConfigFolderPrefix.datasetName === datasetName)
    assert(cmdConfigFolderPrefix.datasetVersion === datasetVersion)
    assert(cmdConfigFolderPrefix.reportDate === reportDate)
    assert(cmdConfigFolderPrefix.reportVersion === reportVersion)
    assert(cmdConfigFolderPrefix.rawFormat === rawFormat)
    assert(cmdConfigFolderPrefix.folderPrefix.nonEmpty)
    assert(cmdConfigFolderPrefix.folderPrefix.get === folderPrefix)
    assert(cmdConfigFolderPrefix.rawPathOverride.isEmpty)
    val cmdConfigRawPathOverride = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--raw-format", rawFormat,
        "--debug-set-raw-path", hdfsRawPathOverride))
    assert(cmdConfigRawPathOverride.datasetName === datasetName)
    assert(cmdConfigRawPathOverride.datasetVersion === datasetVersion)
    assert(cmdConfigRawPathOverride.reportDate === reportDate)
    assert(cmdConfigRawPathOverride.reportVersion === reportVersion)
    assert(cmdConfigRawPathOverride.rawFormat === rawFormat)
    assert(cmdConfigRawPathOverride.folderPrefix.isEmpty)
    assert(cmdConfigRawPathOverride.rawPathOverride.nonEmpty)
    assert(cmdConfigRawPathOverride.rawPathOverride.get === hdfsRawPathOverride)
  }

  test("Test buildRawPath") {
    val standardiseDataset = Dataset(
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
        "--report-version", reportVersion.toString,
        "--raw-format", rawFormat))
    val cmdConfigFolderPrefix = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--folder-prefix", folderPrefix,
        "--raw-format", rawFormat))
    val cmdConfigRawPathOverride = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--debug-set-raw-path", hdfsRawPathOverride,
        "--raw-format", rawFormat))
    val cmdConfigRawPathOverrideAndFolderPrefix = CmdConfig.getCmdLineArguments(
      Array(
        "--dataset-name", datasetName,
        "--dataset-version", datasetVersion.toString,
        "--report-date", reportDate,
        "--report-version", reportVersion.toString,
        "--folder-prefix", folderPrefix,
        "--debug-set-raw-path", hdfsRawPathOverride,
        "--raw-format", rawFormat))
    val publishPathNoFolderPrefix = StandardizationJob.buildRawPath(cmdConfigNoFolderPrefix, standardiseDataset, dateTokens)
    assert(publishPathNoFolderPrefix === s"${standardiseDataset.hdfsPath}/${dateTokens(0)}/${dateTokens(1)}/${dateTokens(2)}/v${cmdConfigNoFolderPrefix.reportVersion}")
    val publishPathFolderPrefix = StandardizationJob.buildRawPath(cmdConfigFolderPrefix, standardiseDataset, dateTokens)
    assert(publishPathFolderPrefix === s"${standardiseDataset.hdfsPath}/$folderPrefix/${dateTokens(0)}/${dateTokens(1)}/${dateTokens(2)}/v${cmdConfigFolderPrefix.reportVersion}")
    val publishPathRawPathOverride = StandardizationJob.buildRawPath(cmdConfigRawPathOverride, standardiseDataset, dateTokens)
    assert(publishPathRawPathOverride === hdfsRawPathOverride)
    val publishPathRawPathOverrideAndFolderPrefix = StandardizationJob.buildRawPath(cmdConfigRawPathOverrideAndFolderPrefix, standardiseDataset, dateTokens)
    assert(publishPathRawPathOverrideAndFolderPrefix === hdfsRawPathOverride)
  }

}
