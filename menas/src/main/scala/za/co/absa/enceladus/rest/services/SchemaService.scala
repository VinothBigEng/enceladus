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

package za.co.absa.enceladus.rest.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import za.co.absa.enceladus.model.{Schema, UsedIn}
import za.co.absa.enceladus.rest.repositories.{DatasetMongoRepository, MappingTableMongoRepository, SchemaMongoRepository}

import scala.concurrent.Future

@Service
class SchemaService @Autowired()(schemaMongoRepository: SchemaMongoRepository,
                                 datasetMongoRepository: DatasetMongoRepository,
                                 mappingTableMongoRepository: MappingTableMongoRepository)
  extends VersionedModelService(schemaMongoRepository) {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def getUsedIn(name: String, version: Option[Int]): Future[UsedIn] = {
    for {
      usedInD <- datasetMongoRepository.getUsedIn("schemaName", "schemaVersion", name, version)
      usedInM <- mappingTableMongoRepository.getUsedIn("schemaName", "schemaVersion", name, version)
    } yield UsedIn(Some(usedInD), Some(usedInM))
  }

  override def update(username: String, schema: Schema): Future[Schema] = {
    super.update(username, schema.name) { latest =>
      latest.setDescription(schema.description).asInstanceOf[Schema]
    }
  }

  override def create(newSchema: Schema, username: String): Future[Schema] = {
    val schema = Schema(name = newSchema.name, description = newSchema.description)
    super.create(schema, username)
  }

}
