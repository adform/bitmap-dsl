package com.adform.bitmaps.util

import com.adform.bitmaps.{BitmapExecutor, BitmapOperation, BitmapsImpl}
import org.scalatest.MustMatchers


class BitmapTest[T]()(implicit bi: BitmapsImpl[T]) extends MustMatchers {

  def check(expr: BitmapOperation, db: Map[String, T], expected: T): Unit = {
    val source = TestDB(db)
    val result = new BitmapExecutor(source).execute(expr)
    result mustBe expected
  }
}

