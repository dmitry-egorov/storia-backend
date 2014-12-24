package com.dmitryegorov.hellfire

import java.util
import java.util.concurrent.TimeoutException

import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}

import scala.collection.JavaConversions._
import scala.concurrent._
import scala.concurrent.duration.Duration

object Tools
{
    implicit class RichJValue(val cc: JValue) extends AnyVal
    {
        def toJava: AnyRef = cc match
        {
            case JString(value)  => value
            case JBool(value)    => value: java.lang.Boolean
            case JDecimal(value) => value
            case JDouble(value)  => value: java.lang.Double
            case JInt(value)     => value
            case JNull           => null
            case JNothing        => null
            case JObject(obj)    => mapAsJavaMap(obj.map(x => x._1 -> x._2.toJava).toMap)
            case JArray(list)    => list.map(i => i.toJava).toArray
        }
    }

    implicit class RichString(val str: String) extends AnyVal
    {
        def read[T](implicit f: Formats, m: Manifest[T]) = {
            Serialization.read[T](str)
        }
    }

    implicit class RichAnyRef[T <: AnyRef](val cc: T) extends AnyVal
    {
        def write(implicit f: Formats) = Serialization.write[T](cc)
    }

    implicit class RichAny(val cc: Any) extends AnyVal
    {
        def fromJavaToJValue: JValue =
        {
            cc match
            {
                case l: util.ArrayList[_]       => JArray(l.map(i => i.fromJavaToJValue).toList)
                case l: Array[_]                => JArray(l.map(i => i.fromJavaToJValue).toList)
                case m: java.util.HashMap[_, _] => new JObject(m.toList
                                                               .map(x => x._1.toString -> x._2.fromJavaToJValue))
                case value: String              => JString(value)
                case value: Boolean             => JBool(value)
                case value: Long                => JInt(value)
                case value: Int                 => JInt(value)
                case value: Double              => JDouble(value)
                case null                       => JNull

            }
        }

        def toJValue(implicit f: Formats) = Extraction.decompose(cc)
    }

    implicit class RichFuture[T](val future: Future[T]) extends AnyVal
    {
        def timeout(duration: Duration)(implicit ec: ExecutionContext): Future[T] =
        {
            Future.firstCompletedOf(List(future, timeoutFail[T](duration)))
        }
    }

    implicit class RichSeqFuture[T](val futures: Seq[Future[T]]) extends AnyVal
    {
        def waitAll(implicit ec: ExecutionContext) = Future.sequence(futures).map(_ => ())
        def whenAll(implicit ec: ExecutionContext) = Future.sequence(futures)
    }

    private lazy val timer = new java.util.Timer()

    private def timeoutFail[T](duration: Duration)(implicit ec: ExecutionContext): Future[T] =
    {
        val p = Promise[T]()

        if (duration != Duration.Inf)
        {
            timer.schedule(new java.util.TimerTask
            {
                def run()
                {
                    p.failure(new TimeoutException)
                }
            }, duration.toMillis)
        }

        p.future
    }
}
