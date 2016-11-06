import ReleaseTransformations._

name := "bitmap-dsl"

organization := "com.github.ppiotrow"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.roaringbitmap" % "RoaringBitmap" % "0.6.26",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)



releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)
