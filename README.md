# Bitmap DSL

[![Build Status](https://travis-ci.org/adform/bitmap-dsl.svg?branch=master)](https://travis-ci.org/adform/bitmap-dsl)

The aim of the project is to define operations on [Roaring Bitmaps](http://roaringbitmap.org) in human friendly way.
It uses official Java [implementation](https://github.com/RoaringBitmap/RoaringBitmap) underneath.

### Getting Started

To get started with SBT, simply add the following to your `build.sbt` file:

```scala
libraryDependencies += "com.adform" %% "bitmap-dsl" % "0.4.6"
```

The storage is not part of the library, you can choose using heap or mmap file (or other) and provide with implementation of `BitmapDB[T]` trait (see example).
It is parametrized by `T` that must have implementation of `BitmapsImpl` trait. 
Currently supported are:
* `RoaringBitmap` by `Implicits.RoaringBitmapImpl`
* `ImmutableRoaringBitmap` by `Implicits.ImmutableRoaringBitmapImpl`
* `MutableRoaringBitmap` by `Implicits.MutableRoaringBitmapImpl`
* `Future[RoaringBitmap]` by `Implicits.ConcurrentRoaringBitmapImpl`

### Example

```scala
import com.adform.bitmaps._
import org.roaringbitmap.RoaringBitmap

//example storage implementation
case class ExampleDB(map: Map[String, RoaringBitmap]) extends BitmapDB[RoaringBitmap] {
  override def getBitmap(key: String) = map.getOrElse(key, new RoaringBitmap())
  override def full = getBitmap("FULL")
}

object Example extends App {
  val students = ExampleDB(Map(
    "study|MIT" -> RoaringBitmap.bitmapOf(1, 2, 7, 9),
    "born|USA"  -> RoaringBitmap.bitmapOf(1, 2),
    "born|PL"   -> RoaringBitmap.bitmapOf(3, 9)))
  val expr =
    And(
      Get("study|MIT"),
      Or(
        Get("born|USA"),
        Get("born|PL")
      ))

  import com.adform.bitmaps.Implicits.RoaringBitmapImpl
  val result = new BitmapExecutor(students).execute(expr)
  print(s"MIT Students born in USA or PL: $result") // {1,2,9}
}
```

### Copyright and License

All code is available to you under the Apache 2.0 license.
