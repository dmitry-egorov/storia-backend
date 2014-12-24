enablePlugins(JavaAppPackaging)

import Dependencies._

lazy val tools = (project in file("tools")).
                 settings(Commons.settings: _*).
                 settings(organization := "com.dmitryegorov", name := "tools", version := "0.0.1").
                 settings(libraryDependencies ++= toolsDependencies)

lazy val futuristic = (project in file("futuristic")).
                      settings(Commons.settings: _*).
                      settings(organization := "com.dmitryegorov", name := "futuristic", version := "0.0.1").
                      settings(libraryDependencies ++= futuristicDependencies)

lazy val hellfire = (project in file("hellfire")).
                    settings(Commons.settings: _*).
                    settings(organization := "com.dmitryegorov", name := "hellfire", version := "0.0.1").
                    settings(libraryDependencies ++= hellfireDependencies)

lazy val scalasourcing = (project in file("scalasourcing")).
                         settings(Commons.settings: _*).
                         settings(organization := "com.scalasourcing", name := "scalasourcing", version := "0.0.1").
                         settings(libraryDependencies ++= scalasourcingDependencies).
                         dependsOn(hellfire, futuristic)

lazy val scalasourcingFirebase = (project in file("scalasourcing-firebase")).
                                 settings(Commons.settings: _*).
                                 settings(organization := "com.scalasourcing", name := "scalasourcing-firebase", version := "0.0.1")
                                 .
                                 settings(libraryDependencies ++= scalasourcingFirebaseDependencies).
                                 dependsOn(hellfire, futuristic, scalasourcing)

lazy val worker = (project in file("worker")).
                  settings(Commons.settings: _*).
                  settings(organization := "com.pointswarm", name := "storia-worker", version := "0.0.1").
                  settings(libraryDependencies ++= storiaWorkerDependencies).
                  dependsOn(scalasourcing, tools, hellfire, futuristic, scalasourcingFirebase)
