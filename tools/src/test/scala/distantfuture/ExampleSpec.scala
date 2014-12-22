package distantfuture

import org.scalatest._

class ExampleSpec extends FlatSpec with Matchers {

    "Observables" should "work" in
    {
        //        val cancellation = new CancellationSource
        //
        //        val f =
        //            Observable
        //            .timer(0.1 seconds, 0.5 second)
        //            .completeWith(cancellation)
        //            .concatMapF(
        //                    i =>
        //                        {
        //                            println(s"Received $i")
        //                            Futuristic.timeout((), 1.2 seconds)
        //                            .map(_ => println(s"---Processed $i"))
        //                        })
        //            .countF
        //
        //        Thread.sleep((3 seconds).toMillis)
        //        cancellation.cancel()
        //        Await.result(f, 30 seconds)
    }
}