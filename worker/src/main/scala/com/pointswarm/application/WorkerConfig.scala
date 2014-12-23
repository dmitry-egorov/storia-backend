package com.pointswarm.application

import scala.util.Properties

object WorkerConfig
{
    lazy val elasticUrl = Properties
                          .envOrElse("BONSAI_URL", "https://7aj1pw8c:5jx5fndr5kvxwp8o@jasmine-4056315.us-east-1.bonsai.io:443")
    lazy val fbUrl = Properties.envOrElse("FBURL", "https://storia-local.firebaseio.com/")
    //    lazy val fbUrl = Properties.envOrElse("FBURL", "https://storia-stage.firebaseio.com/")
    //    lazy val fbUrl = Properties.envOrElse("FBURL", "https://storia-test.firebaseio.com/")
    //    lazy val fbUrl = Properties.envOrElse("FBURL", "https://storia-dev.firebaseio.com/")
}
