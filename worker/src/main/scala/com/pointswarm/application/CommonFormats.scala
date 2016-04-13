package com.pointswarm.application

import com.dmitryegorov.tools.serialization.SingleStringCaseClassSerializer._
import com.dmitryegorov.tools.serialization._
import com.pointswarm.common.ProviderType
import com.pointswarm.common.dtos._
import com.pointswarm.domain.common.{EventIdAgg, ProfileIdAgg}
import com.pointswarm.domain.profiling.Profile
import com.pointswarm.domain.reporting.{Event, Report}
import com.pointswarm.domain.voting.Upvote
import com.pointswarm.fireLegion._
import org.json4s._
import org.json4s.ext.EnumNameSerializer

object CommonFormats
{
    implicit val formats =
        new Formats
        {
            val dateFormat = DefaultFormats.lossless.dateFormat
            override val typeHintFieldName = "t"
        } +
        ShortTypeHints(List(
            classOf[Profile.Create],
            classOf[Profile.Created],
            Profile.AlreadyExists.getClass,

            classOf[Report.DoReport],
            classOf[Report.Added],
            classOf[Report.Edited],
            Report.ContentIsTheSame.getClass,

            classOf[Event.Create],
            classOf[Event.Created],
            Event.AlreadyExists.getClass,

            Upvote.Cast.getClass,
            Upvote.Cancel.getClass,
            Upvote.Casted.getClass,
            Upvote.Cancelled.getClass,
            Upvote.WasAlreadyCastedError.getClass,
            Upvote.WasNotCastedError.getClass
        )) +
        SingleStringCaseClassSerializer[CommandName]() +
        SingleStringCaseClassSerializer[MinionName]() +
        SingleStringCaseClassSerializer[Name]() +
        SingleStringCaseClassSerializer[Alias]() +
        SingleStringCaseClassSerializer[HtmlContent]() +
        SingleStringCaseClassSerializer[EventId]() +
        SingleStringCaseClassSerializer[ReportId]() +
        SingleStringCaseClassSerializer[ProfileId]() +
        SingleStringCaseClassSerializer[HistoryId]() +
        SingleStringCaseClassSerializer[AccountId]() +
        SingleStringCaseClassSerializer[EventIdAgg]() +
        SingleStringCaseClassSerializer[ProfileIdAgg]() +
        SingleStringCaseClassSerializer[AccountId]() +
        new DateTimeSerializer +
        new EnumNameSerializer(ProviderType)
}






