package com.pointswarm.domain.reporting

import com.pointswarm.common.dtos.HtmlContent
import com.pointswarm.domain.reporting.Report._
import com.scalasourcing.bdd.AggregateBDD
import org.scalatest.FunSuite

class ReportSuit extends FunSuite with AggregateBDD[Report]
{
    val content = HtmlContent("Content")
    val content1 = HtmlContent("Content1")
    val content2 = HtmlContent("Content2")
    val content3 = HtmlContent("Content3")

    test("Should be created")
    {
        given_nothing when_I DoReport(content) then_it_is Created(content)
    }

    test("Should be edited with different content")
    {
        given it_was Created(content1) when_I DoReport(content2) then_it_is Edited(content2)
    }

    test("Should not be edited with the same content")
    {
        given it_was Created(content) when_I DoReport(content) then_expect ContentIsTheSame()
    }

    test("Should be edited with different content for the second time")
    {
        given it_was Created(content1) and Edited(content2) when_I DoReport(content3) then_it_is Edited(content3)
    }
}
