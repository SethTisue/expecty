/*
* Copyright 2012 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*     http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.eed3si9n.expecty

// one instance per recording
class RecorderRuntime(listener: RecorderListener[Boolean]) {
  protected var recordedValues: List[RecordedValue] = _
  protected var recordedExprs: List[RecordedExpression[Boolean]] = List.empty
  protected var recordedMessage: Function0[String] = () => ""

  def resetValues(): Unit = {
    recordedValues = List.empty
  }

  def recordValue[U](value: U, anchor: Int): U = {
    val recordedValue = RecordedValue(value, anchor)
    listener.valueRecorded(recordedValue)
    recordedValues = recordedValue :: recordedValues
    value
  }

  def recordMessage(message: => String): Unit = {
    recordedMessage = () => message
  }

  def recordExpression(text: String, ast: String, value: Boolean): Unit = {
    val recordedExpr = RecordedExpression(text, ast, value, recordedValues)
    listener.expressionRecorded(recordedExpr, recordedMessage)
    recordedExprs = recordedExpr :: recordedExprs
  }

  def completeRecording(): Unit = {
    val lastRecorded = recordedExprs.head
    val recording = Recording(lastRecorded.value, recordedExprs)
    listener.recordingCompleted(recording, recordedMessage)
  }
}
