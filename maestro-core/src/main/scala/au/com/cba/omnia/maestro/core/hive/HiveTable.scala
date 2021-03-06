//   Copyright 2014 Commonwealth Bank of Australia
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package au.com.cba.omnia.maestro.core.hive

import org.apache.hadoop.fs.Path

import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.conf.HiveConf.ConfVars.METASTOREWAREHOUSE

import com.twitter.scalding.TupleSetter

import com.twitter.scrooge.ThriftStruct

import au.com.cba.omnia.maestro.core.partition.Partition

import au.com.cba.omnia.ebenezer.scrooge.hive._

/** Information need to address/describe a specific partitioned hive table.*/
case class HiveTable[A <: ThriftStruct : Manifest , B : Manifest : TupleSetter](
  database: String, table: String, partition: Partition[A, B], externalPath: Option[String] = None
) {
  /** Fully qualified SQL reference to table.*/
  val name: String = s"$database.$table"

  /** List of partition column names and type (string by default). */ 
  val partitionMetadata: List[(String, String)] = partition.fieldNames.map(n => (n, "string"))

  /** Path of the table. */
  lazy val path = new Path(externalPath.getOrElse(
    s"${(new HiveConf()).getVar(METASTOREWAREHOUSE)}/$database.db/$table"
  ))

  /** Creates a scalding source to read from the hive table.*/
  def source(): PartitionHiveParquetScroogeSource[A] =
    PartitionHiveParquetScroogeSource[A](database, table, partitionMetadata)

  /** Creates a scalding sink to write to the hive table.*/
  def sink(append: Boolean = true) =
    PartitionHiveParquetScroogeSink[B, A](database, table, partitionMetadata, externalPath, append)
}

/** Alternative contructors for HiveTable. */
object HiveTable {
  /** Information need to address/describe a specific partitioned hive table.*/
  def apply[A <: ThriftStruct : Manifest , B : Manifest : TupleSetter](
    database: String, table: String, partition: Partition[A, B], path: String
  ) = new HiveTable(database, table, partition, Some(path))
}
