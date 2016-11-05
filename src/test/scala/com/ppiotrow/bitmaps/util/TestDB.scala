package com.ppiotrow.bitmaps.util

import com.ppiotrow.bitmaps.BitmapDB

case class TestDB[T](map: Map[String, T]) extends BitmapDB[T] {
  val FULL = "FULL"

  override def full: T = getBitmap(FULL)

  override def getBitmap(id: String): T = map.apply(id)
}
