package ru.primetalk.synapse.core.runtime

import ru.primetalk.synapse.core.Contact

import scala.language.implicitConversions
/**
 * @author zhizhelev, 25.03.15.
 */
trait SignalsApi {
  /**
   * Signal is a pair of contact and data on it.
   * Two methods are provided to match those of pairs - _1 and _2.
   */
  case class Signal[T](contact: Contact[T], data: T) {
    def _1 = contact

    def _2 = data
  }

  /**
   * Extractor of contacts' data from result.
   */
  implicit class ContactExtractor[T](c: Contact[T]) {
    /** construct a signal on this contact. */
    def signal(d: T) = Signal(c, d)

    def createSignal(d: T) = Signal(c, d)
    /** Create signals from the given data sequence. */
    def createSignals(ds: T*): List[Signal[T]] = ds.map(Signal(c, _)).toList

    /** projection of the list of signals over the contact. Only the
      * data on the contact is retained.
      * see also #filterFunction */
    def get(signals: List[Signal[_]]): List[T] = {
      val C = c
      signals.collect {
        case Signal(C, data) => data.asInstanceOf[T]
      }
    }

    def filterFunction = (signals: List[Signal[_]]) ⇒ signals.filter(_._1 == c).map(_.asInstanceOf[Signal[T]])

    def filterNotFunction = (signals: List[Signal[_]]) ⇒ signals.filterNot(_._1 == c)
  }

  implicit class RichSignalList(signals: List[Signal[_]]) {
    /** Divides the list of signals. The first part will contain signals on the given contact.
      * the second — the rest signals. */
    def partition[T](c: Contact[T]): (List[Signal[T]], List[Signal[_]]) =
      signals.
        partition(_.contact == c).
        asInstanceOf[(List[Signal[T]], List[Signal[_]])]

    def get[T](`c`: Contact[T]): List[T] =
      signals.
        collect {
        case Signal(`c`, data) =>
          data.asInstanceOf[T]
      }


  }

  /** One may use notation (contact -> data) to represent a signal*/
  implicit def pairToSignal[T](p: (Contact[T], T)): Signal[T] = Signal(p._1, p._2)


}