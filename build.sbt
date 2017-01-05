import ReleaseTransformations._

name := "bitmap-dsl"

organization := "com.adform"

licenses += ("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0"))

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalaVersion := "2.12.0"

crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1")

libraryDependencies ++= Seq(
  "org.roaringbitmap" % "RoaringBitmap" % "0.6.32",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
releaseCrossBuild := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("+publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("+sonatypeReleaseAll", _)),
  pushChanges
)
