package com.pointswarm.minions.aggregator

import com.pointswarm.domain.reporting.Report
import com.scalasourcing.backend.EventStorage
import org.json4s.Formats

import scala.concurrent.ExecutionContext

class ReportAggregator(val eventStorage: EventStorage)(implicit f: Formats, ec: ExecutionContext) extends Aggregator[Report.Id, Report]
