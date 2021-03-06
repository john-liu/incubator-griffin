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
package org.apache.griffin.measure.batch.result

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

@RunWith(classOf[JUnitRunner])
class AccuracyResultTest extends FunSuite with BeforeAndAfter with Matchers {

  test ("update") {
    val result = AccuracyResult(10, 100)
    val delta = AccuracyResult(3, 10)
    result.update(delta) should be (AccuracyResult(3, 100))
  }

  test ("eventual") {
    val result1 = AccuracyResult(10, 100)
    result1.eventual should be (false)

    val result2 = AccuracyResult(0, 100)
    result2.eventual should be (true)
  }

  test ("differsFrom") {
    val result = AccuracyResult(10, 100)
    result.differsFrom(AccuracyResult(11, 100)) should be (true)
    result.differsFrom(AccuracyResult(10, 110)) should be (true)
    result.differsFrom(AccuracyResult(10, 100)) should be (false)
  }

  test ("matchPercentage") {
    val result1 = AccuracyResult(10, 100)
    result1.matchPercentage should be (90.0)

    val result2 = AccuracyResult(10, 0)
    result2.matchPercentage should be (0.0)
  }

}
