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

package za.co.absa.enceladus.utils.types

import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import scala.util.Try
import org.apache.spark.sql.types._

object Defaults {

  /** A function which defines default values for primitive types */
  def getGlobalDefault(dt: DataType): Any =
    dt match {
      case _: IntegerType   => 0
      case _: FloatType     => 0f
      case _: ByteType      => 0.toByte
      case _: ShortType     => 0.toShort
      case _: DoubleType    => 0.0d
      case _: LongType      => 0l
      case _: StringType    => ""
      case _: DateType      => new Date(0) //linux epoch
      case _: TimestampType => new Timestamp(0)
      case _: BooleanType   => false
      case t: DecimalType => {
        val rest = t.precision - t.scale
        new java.math.BigDecimal(("0" * rest) + "." + ("0" * t.scale))
      }
      case _ => throw new IllegalStateException(s"No default value defined for data type ${dt.typeName}")
    }

  /** A function which defines default formats for primitive types */
  def getGlobalFormat(dt: DataType): String =
    dt match {
      case DateType      => "yyyy-MM-dd"
      case TimestampType => "yyyy-MM-dd HH:mm:ss"
      case _             => throw new IllegalStateException(s"No default format defined for data type ${dt.typeName}")
    }

  /** A function to parse default based on type **/
  private def parseDefault(dt: DataType, value: String, dtPattern: Option[String] = None): Any = {
    dt match {
      case _: IntegerType   => value.toInt
      case _: FloatType     => value.toFloat
      case _: ShortType     => value.toShort
      case _: DoubleType    => value.toDouble
      case _: LongType      => value.toLong
      case _: StringType    => value
      case _: DateType      => {
        val format = new SimpleDateFormat(dtPattern.getOrElse(throw new IllegalArgumentException("No date format specified.")))
        new Date(format.parse(value).getTime)
      }
      case _: TimestampType => {
        val format = new SimpleDateFormat(dtPattern.getOrElse(throw new IllegalArgumentException("No date format specified.")))
        new Timestamp(format.parse(value).getTime)
      }
      case _: BooleanType   => value.toBoolean
      case t: DecimalType   => new java.math.BigDecimal(value)
      case _                => throw new IllegalStateException(s"No default value defined for data type ${dt.typeName}")
    }
  }

  /** Wrapper which returns the parsed default value for datetype and timestamptype returns ,default value for other primtive type
    * from metadata first (if any) and the global one otherwise */
  def getDefaultValue(st: StructField): Any = {
    val format = st.dataType match {
      case _: DateType | _: TimestampType => Some(getFormat(st))
      case _                              => None
    }

    getDefaultOpt(st) match {
      case Some(default) => parseDefault(st.dataType, default, format)
      case None          => getGlobalDefault (st.dataType)
    }
  }

  /** Get option of a default */
  def getDefaultOpt(st: StructField): Option[String] = Try(st.metadata.getString("default")).toOption

  /** Get option of a format ~ pattern */
  def getFormatOpt(st: StructField): Option[String] = Try(st.metadata.getString("pattern")).toOption

  /** Wrapper which returns the default format from metadata first (if any) and the global one otherwise */
  def getFormat(st: StructField): String = {
    getFormatOpt(st).getOrElse(getGlobalFormat(st.dataType))
  }

}
