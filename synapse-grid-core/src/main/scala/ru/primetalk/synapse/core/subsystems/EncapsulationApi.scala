package ru.primetalk.synapse.core.subsystems

import ru.primetalk.synapse.core.ext.SystemBuilderApi

/**
 * An API for creating an envelope around a subsystem while preserving outer interface.
 * This is useful when we have a few similar systems.
 *
 * One may also create encapsulation from a StaticSystem.encapsulate.
 * However, it will not have the same outer interface.
 *
 * @author zhizhelev, 29.03.15.
 */
trait EncapsulationApi extends SystemBuilderApi {

  object SimpleOuterInterfaceBuilder extends OuterInterfaceBuilder {
    override def setSystemName(name: String) = {}
    override def input[T](internalName: String): Contact[T] = contact[T](internalName)

    override def output[T](internalName: String): Contact[T] = contact[T](internalName)
  }

  class EmbeddedOuterInterfaceBuilder(name:String)(implicit sb:SystemBuilder) extends OuterInterfaceBuilder{
    override def setSystemName(name1: String) = sb.setSystemName(name + "." + name1)

    override def input[T](internalName: String): Contact[T] = sb.input(name + "." + internalName)

    override def output[T](internalName: String): Contact[T] = sb.output(name + "." + internalName)
  }
  /** Usage: see defineEncapsulation*/
  abstract class EncapsulationBuilder[Outer](name:String)(outerDefinition:OuterInterfaceBuilder=>Outer) {
    protected
    implicit val sb = new SystemBuilderC(name)

    val outer = outerDefinition(new EmbeddedOuterInterfaceBuilder(name)(sb))

    def toStaticSystem = sb.toStaticSystem

//    def toComponent:Component = sb.toStaticSystem
  }
  /** Usage:
    * class OuterInterface(b:OuterInterfaceBuilder){
    *   def in1 = b.input[Int]("in1")
    *   def out1 = b.output[Int]("out1")
    * }
    * class OuterImplementation(name:String) extends EncapsulationBuilder(name)(new OuterInterface){
    *   in1 >> out1
    * }
    * defineEncapsulation(new OuterImplementation("mySystem1"))
    * */
  def defineEncapsulation[Outer](en:EncapsulationBuilder[Outer])(implicit sb:SystemBuilder):Outer = {
    sb.addSubsystem(en.toStaticSystem)
    en.outer
  }
//  implicit class EncapsulationBuilderE(sb: BasicSystemBuilder) {
//    /** Creates both outer interface of a system and internal implementation.
//      *
//      */
//    def defineEncapsulatedSystem[S](name: String)(f: OuterInterfaceBuilder => S)(body: S => StaticSystem) = {
//      val innerSb = new SystemBuilderC(name)
//      val system = f(new EmbeddedOuterInterfaceBuilder(name)(innerSb))
//      sb.addSubsystem(system)
//    }
//  }

}
