organization := "la.dp"
name := "dpla-data-source"
version := "0.1.0"
scalaVersion := "2.11.8"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.1.0" ,
  "org.apache.spark" %% "spark-sql" % "2.1.0" exclude("org.scalatest", "scalatest_2.11"),
  "com.amazonaws" % "aws-java-sdk" % "1.7.4" % "provided"
)