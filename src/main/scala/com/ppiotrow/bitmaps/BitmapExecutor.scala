package com.ppiotrow.bitmaps

import scala.annotation.tailrec

class BitmapExecutor[T](val db: BitmapDB[T])(implicit bi: BitmapsImpl[T]) {
  def execute(expression: BitmapOperation): T = (expression: @unchecked) match {
    case or: Alternative => accumulateOR(or.bitmaps, Nil)
    case and: Conjunction => accumulateAND(and.bitmaps, Nil)
    case Get(id) => db.getBitmap(id)
    case Empty => bi.empty
    case Full => db.full
    case db.BitmapReference(bitmap) => bitmap
    case Negation(not) => processNegation(db.full, not)
  }

  @tailrec
  private[this] def accumulateAND(list: List[BitmapOperation], acc: List[T]): T = list match {
    case h :: tail => (h: @unchecked) match {
      case Get(id) => accumulateAND(tail, db.getBitmap(id) :: acc)
      case db.BitmapReference(bitmap) => accumulateAND(tail, bitmap :: acc)
      case negation: Negation =>
        val base = if (acc.isEmpty) db.full else bi.batchAnd(acc)
        val negated = processNegation(base, negation.not)
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
      case negation: Negation =>
        val negated = processNegation(db.full, negation.not)
        accumulateOR(tail, negated :: acc)
      case Alternative(expressions) => accumulateOR(expressions ::: tail, if (acc.isEmpty) Nil else List(bi.batchOr(acc)))
      case Conjunction(expressions) => accumulateOR(tail, accumulateAND(expressions, Nil) :: acc)
      case Empty => accumulateOR(tail, acc)
      case Full => db.full
    }
    case Nil => bi.batchOr(acc)
  }

  private[this] def processNegation(base: T, operation: BitmapOperation): T = (operation: @unchecked) match {
    case Empty => db.full
    case Full => bi.empty
    case Negation(not) => execute(not)
    case _ => bi.andNot(base, execute(operation))
  }

}
