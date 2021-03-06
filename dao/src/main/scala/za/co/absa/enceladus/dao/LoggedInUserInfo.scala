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

package za.co.absa.enceladus.dao

import java.io.IOException

import org.apache.hadoop.security.UserGroupInformation
import org.apache.log4j.LogManager

/**
  * This is an Object to get logged in user info
  * should take the user from
  * 1) Kerberos token (if any active),
  * 2) only then signed in user
  **/

object LoggedInUserInfo {
  private val log = LogManager.getLogger("ControlFrameworkREST")
  def getUserName: String = {
    val keytabUserInfo = getUserFromKeytab
    keytabUserInfo match {
      case Some(user) => user
      case None => System.getProperty("user.name")
    }
  }

  private def getUserFromKeytab: Option[String] = {
    try {
      val ugi: UserGroupInformation = UserGroupInformation.getCurrentUser
      Some(ugi.getShortUserName)
    }
    catch {
      case e: IOException =>{
        log.error(s"Exception while fetching user group information :${e.getMessage} ")
        None
      }
    }
  }
}
