package com.ppiotrow.bitmaps.impl

import com.ppiotrow.bitmaps.Implicits.MutableRoaringBitmapImpl
import org.roaringbitmap.buffer.MutableRoaringBitmap.bitmapOf
import org.scalatest.{MustMatchers, WordSpecLike}

class MutableBitmapImplTest extends MustMatchers with WordSpecLike {
  "MutableRoaringBitmapImpl" must {
    "calculate batchAND" in {
      MutableRoaringBitmapImpl.batchAnd(and) mustBe bitmapOf(8, 9)
    }
    "calculate batchOR" in {
      MutableRoaringBitmapImpl.batchOr(or) mustBe bitmapOf(1, 2, 3, 4, 5, 7, 8, 9, 10)
    }
  }
  val and = ImplTestData.and.map(bits => bitmapOf(bits: _*))

  val or = ImplTestData.or.map(bits => bitmapOf(bits: _*))
}
