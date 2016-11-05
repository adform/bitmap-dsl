name := "bitmap-dsl"

organization := "com.ppiotrow"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.roaringbitmap" % "RoaringBitmap" % "0.6.26",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

