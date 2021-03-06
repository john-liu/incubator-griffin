/*-
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */
package org.apache.griffin.measure.batch.config.reader

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}


object ParamReaderFactory {

  val RawStringRegex = """^(?i)raw$""".r
  val LocalFsRegex = """^(?i)local$""".r
  val HdfsFsRegex = """^(?i)hdfs$""".r

  def getParamReader(filePath: String, fsType: String): ParamReader = {
    fsType match {
      case RawStringRegex() => ParamRawStringReader(filePath)
      case LocalFsRegex() => ParamFileReader(filePath)
      case HdfsFsRegex() => ParamHdfsFileReader(filePath)
      case _ => ParamHdfsFileReader(filePath)
    }
  }

}
