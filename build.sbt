lazy val baseSettings = Seq(
    name := "NetworksLabSummer2020"
  , organization := "ru.pashnik"
  , version := "0.1.0-SNAPSHOT"
  , scalaVersion := "2.12.8"
  , sbtVersion := "1.2.8"
  , scalacOptions ++= Seq(
      "-feature"
    , "-unchecked"
    , "-deprecation"
    , "-Xfatal-warnings"
    , "-Ypartial-unification"
    , "-language:higherKinds"
  )
  , resourceDirectory in Compile := baseDirectory.value / "resources"
)

lazy val http4sVersion     = "0.20.11"
lazy val circeVersion      = "0.11.1"
lazy val scalaTestVersion  = "3.0.4"
lazy val logbackVersion    = "1.2.3"
lazy val catsVersion       = "2.0.0"
lazy val fs2Version        = "2.1.0"
lazy val pureConfigVersion = "0.12.2"
lazy val newTypeVersion    = "0.4.3"

lazy val deps = Seq(
    "org.http4s"            %% "http4s-blaze-server"      % http4sVersion
  , "org.http4s"            %% "http4s-blaze-client"      % http4sVersion
  , "org.http4s"            %% "http4s-async-http-client" % http4sVersion
  , "org.http4s"            %% "http4s-circe"             % http4sVersion
  , "org.http4s"            %% "http4s-dsl"               % http4sVersion
  , "io.circe"              %% "circe-generic"            % circeVersion
  , "io.circe"              %% "circe-literal"            % circeVersion
  , "org.typelevel"         %% "cats-effect"              % catsVersion
  , "org.typelevel"         %% "cats-core"                % catsVersion
  , "org.scalatest"         %% "scalatest"                % scalaTestVersion
  , "ch.qos.logback"        % "logback-classic"           % logbackVersion
  , "com.github.pureconfig" %% "pureconfig"               % pureConfigVersion
  , "co.fs2"                %% "fs2-core"                 % fs2Version
  , "co.fs2"                %% "fs2-io"                   % fs2Version
  , "io.estatico"           %% "newtype"                  % newTypeVersion
).map(_ withSources () withJavadoc ())

lazy val assemblySettings = Seq(
    assemblyJarName in assembly := name.value + ".jar"
  , assemblyMergeStrategy in assembly := {
    case PathList("META-INF", _ @_*) => MergeStrategy.discard
    case _                           => MergeStrategy.first
  }
)

lazy val global = (project in file("."))
  .settings(baseSettings, assemblySettings, libraryDependencies ++= deps)
