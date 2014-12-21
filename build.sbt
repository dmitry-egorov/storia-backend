enablePlugins(JavaAppPackaging)

import Dependencies._

lazy val tools = (project in file("tools")).
                 settings(Commons.settings: _*).
                 settings(organization := "com.dmitryegorov", name := "tools", version := "0.0.1").
                 settings(libraryDependencies ++= toolsDependencies)

lazy val hellfire = (project in file("hellfire")).
                    settings(Commons.settings: _*).
                    settings(organization := "com.dmitryegorov", name := "hellfire", version := "0.0.1").
                    settings(libraryDependencies ++= hellfireDependencies)

lazy val scalasourcing = (project in file("scalasourcing")).
                         settings(Commons.settings: _*).
                         settings(organization := "com.scalasourcing", name := "scalasourcing", version := "0.0.1").
                         settings(libraryDependencies ++= scalasourcingDependencies).
                         dependsOn(hellfire)

lazy val worker = (project in file("worker")).
                  settings(Commons.settings: _*).
                  settings(organization := "com.pointswarm", name := "storia-worker", version := "0.0.1").
                  settings(libraryDependencies ++= storiaWorkerDependencies).
                  dependsOn(scalasourcing, tools, hellfire)

