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

import org.apache.griffin.measure.batch.config.params.env._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

@RunWith(classOf[JUnitRunner])
class ParamRawStringReaderTest extends FunSuite with Matchers with BeforeAndAfter {

  test("read config") {
    val rawString = """{"type": "hdfs", "config": {"path": "/path/to", "time": 1234567}}"""

    val reader = ParamRawStringReader(rawString)
    val paramTry = reader.readConfig[PersistParam]
    paramTry.isSuccess should be (true)
    paramTry.get should be (PersistParam("hdfs", Map[String, Any](("path" -> "/path/to"), ("time" -> 1234567))))
  }

}
