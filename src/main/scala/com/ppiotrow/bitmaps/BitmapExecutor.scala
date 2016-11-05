package com.ppiotrow.bitmaps

import scala.annotation.tailrec

class BitmapExecutor[T](val db: BitmapDB[T])(implicit bi: BitmapsImpl[T]) {
  import BitmapExecutor._
  def execute(expression: BitmapOperation): T = expression match {
    case or: Alternative => accumulateOR(or.bitmaps, Nil)
    case and: Conjunction => accumulateAND(and.bitmaps, Nil)
    case Get(id) => db.getBitmap(id)
    case Empty => bi.empty
    case Full => db.full
    case db.BitmapReference(bitmap) => bitmap
    case _ => throw BitmapExecutionException("Logical expression should start from OR or AND operation")
  }

  @tailrec
  private[this] def accumulateAND(list: List[BitmapOperation], acc: List[T]): T = list match {
    case h :: tail => (h: @unchecked) match {
      case Get(id) => accumulateAND(tail, db.getBitmap(id) :: acc)
      case db.BitmapReference(bitmap) => accumulateAND(tail, bitmap :: acc)
      case negation: Negation =>
        val toNegate = if (acc.isEmpty) db.full else bi.batchAnd(acc)
        val negated = bi.andNot(toNegate, processNegation(negation.not))
        accumulateAND(tail, negated :: Nil)
      case Alternative(expressions) => accumulateAND(tail, accumulateOR(expressions, Nil) :: acc)
      case Conjunction(expressions) => accumulateAND(expressions ::: tail, if (acc.isEmpty) Nil else List(bi.batchAnd(acc)))
      case Empty => bi.empty
      case Full => accumulateAND(tail, acc)
    }
    case Nil => bi.batchAnd(acc)
  }

  @tailrec
  private[this] def accumulateOR(list: List[BitmapOperation], acc: List[T]): T = list match {
    case h :: tail => (h: @unchecked) match {
      case Get(id) => accumulateOR(tail, db.getBitmap(id) :: acc)
      case db.BitmapReference(bitmap) => accumulateOR(tail, bitmap :: acc)
      case negation: Negation => throw BitmapExecutionException("Unsupported negation in alternative")
      case Alternative(expressions) => accumulateOR(expressions ::: tail, if (acc.isEmpty) Nil else List(bi.batchOr(acc)))
      case Conjunction(expressions) => accumulateOR(tail, accumulateAND(expressions, Nil) :: acc)
      case Empty => accumulateOR(tail, acc)
      case Full => db.full
    }
    case Nil => bi.batchOr(acc)
  }

  private[this] def processNegation(operation: BitmapOperation): T = (operation: @unchecked) match {
    case or: Alternative => accumulateOR(or.bitmaps, Nil)
    case and: Conjunction => accumulateAND(and.bitmaps, Nil)
    case Get(id) => db.getBitmap(id)
    case db.BitmapReference(bitmap) => bitmap
    case Empty => db.full
    case Full => bi.empty
    case Negation(not) => throw BitmapExecutionException("Unsupported double negation")
  }

}

object BitmapExecutor {
  case class BitmapExecutionException(msg: String) extends RuntimeException(msg)
}
