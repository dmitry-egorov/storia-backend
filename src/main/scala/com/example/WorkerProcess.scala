package com.example

import scala.util.Properties

object WorkerProcess {
  def main(args: Array[String]) {
    val port = Properties.envOrElse("PORT", "8080").toInt

  }
}
