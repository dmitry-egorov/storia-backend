package com.pointswarm.domain.common

import com.pointswarm.common.dtos.HtmlContent
import org.scalatest.{FunSuite, Matchers}

class HtmlContentSuite extends FunSuite with Matchers {
    test("Should be created") {
                                  HtmlContent("abc").value should equal("abc")
                              }

    test("Should not construct from string with over 65k characters") {
                                                                          intercept[IllegalArgumentException] {
                                                                                                                  HtmlContent(generateString(65001))
                                                                                                              }
                                                                      }

    def generateString(n: Int): String = List.fill(n)("W").mkString("")
}
