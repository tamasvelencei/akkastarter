name := "udemy-akka"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.19"

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % akkaVersion,
	"com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
	"org.eclipse.milo" % "opc-ua-stack" % "0.2.1" pomOnly()
)
