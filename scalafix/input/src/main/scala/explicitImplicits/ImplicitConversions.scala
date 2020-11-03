/*
rules = ExplicitImplicits
*/
package explicitImplicits

import language.implicitConversions

object ImplicitConversions {

  class EmptyType private ()

  // force types to be fully qualified
  type Int = EmptyType

  trait Loggable {
    def message: String
  }

  trait LowPriorityLoggableImplicits {
    implicit def defaultLoggable(value: Any): Loggable = new Loggable {
      def message: String = String.valueOf(value)
    }
  }

  object Loggable extends LowPriorityLoggableImplicits {
    implicit def showableToLoggableConversion[T](value: T)(implicit evidence: Show[T]): Loggable = new Loggable {
      def message: String = evidence.show(value)
    }
  }

  object Logger {
    def log(loggable: Loggable): Unit = println(loggable.message)
  }

  trait Show[-T] {
    def show(t: T): String
  }

  implicit object ShowInt extends Show[_root_.scala.Int] {
    def show(i: _root_.scala.Int): String = String.valueOf(i)
  }

  // test basic conversion that takes `Any`
  def logHello: Unit =
    Logger.log("hello")

  // test higher priority conditional conversion that will use `Show[Int]` as evidence
  def log23: Unit =
    Logger.log(23)

  def localConversion: Unit = {
    implicit def localConversionToLoggable(b: Boolean): Loggable = new Loggable {
      def message: String = String.valueOf(b)
    }
    Logger.log(true)
  }

}
