package it.unibo.sc1819.worker.bracket

import com.pi4j.io.gpio.{GpioFactory, GpioPinDigitalOutput, Pin, PinPullResistance, PinState, RaspiPin}
import com.pi4j.io.gpio.trigger.{GpioSetStateTrigger, GpioSyncStateTrigger}
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger
import java.util.concurrent.Callable

import io.vertx.scala.core.Vertx
import it.unibo.sc1819.util.messages.{LockBikeMessage, Topics}


/**
  * A trait to represent a Rack Bracket, a place where to put the bike to be locked.
  * On the physical plan, the bracket will include a mechanism which will contain a ethernet interface
  * to connect the bike and power via POE(Power Over Ethernet); it will also include a locker to prevent the
  * bike to be stolen, this lock will be open when a user unlocks the bike via web and will be
  * closed when the bike is fit on the mechanism
  */
trait RackBracket {

  /**
    * Define the IP address of the exposed ethernet interface
    * @return a string containing the IP String.
    */
  def ipAddress:String

  /**
    * Define is the bracket is locked
    * @return true if a bike is currently locked in the rack, false otherwise
    */
  def isLocked:Boolean

  /**
    * Lock a Bike inside the bracket.
    */
  def lockBike():Unit

  /**
    * Unlock a rack from the bracket.
    */
  def unlockBike():Unit

}

object RackBracket {

  /**
    * Standard constructor for the RackBracket object.
    * @param ipAddress the ip address associated to the bracket.
    * @param physicLayerMapper the configuration to be provided
    * @return a new Bracket object.
    */
  def apply(ipAddress:String, physicLayerMapper: PhysicLayerMapper, vertxContext:Vertx): RackBracket =
    new RackBracketImpl(ipAddress, physicLayerMapper, vertxContext)

  private class RackBracketImpl(override val ipAddress:String, val pinConfiguration:PhysicLayerMapper, val vertxContext:Vertx) extends RackBracket {

    var isLocked:Boolean = false
    var freeLed:GpioPinDigitalOutput = _
    var lockingLed: GpioPinDigitalOutput = _
    val eventBus = vertxContext.eventBus()

    setup()

    override def lockBike(): Unit = {
        isLocked = true
        freeLed low()
        sendLockNotification
    }

    override def unlockBike(): Unit = {
      isLocked = false
      freeLed high()
      lockingLed low()
    }

    /**
      * Setup for the initial bracket,
      */
    private def setup(): Unit = {
      val gpioManager = GpioFactory.getInstance
      val sensorButton  = gpioManager.provisionDigitalInputPin(pinConfiguration.presenceSensorPin, PinPullResistance.PULL_DOWN)
      lockingLed = gpioManager.provisionDigitalOutputPin(pinConfiguration.lockerActuatorPin,"LockingActuator", PinState.LOW)
      freeLed = gpioManager.provisionDigitalOutputPin(pinConfiguration.unlockedBikeFlagPin,"FreeLed", PinState.HIGH)

      sensorButton addTrigger new GpioSetStateTrigger(PinState.HIGH, lockingLed, PinState.HIGH)

      sensorButton addTrigger checkAndLock
    }

    /**
      * Check if a bike is already locked, and if not lock one
      */
    private def checkAndLock = {
      if(!isLocked) {
        lockBike()
      }
    }

    private def sendLockNotification = {
      eventBus.publish(Topics.WORKER_TOPIC, LockBikeMessage(ipAddress))
    }


  }

  /**
    * Implict to convert a integer to a GPIO pin.
    * @param pinToConvert the integer to convert to a pin
    * @return a GPIO object of the specified pin
    */
  implicit def IntToPinConverter(pinToConvert:Int):Pin = {
    RaspiPin.allPins().toStream.find(p => p.getAddress == pinToConvert).get
  }

  /**
    * Convert a scala callback to a GPIO compatibile one
    * @param callback a function from () to Unit to convert
    * @return a GPIO callback.
    */
  implicit def ScalaToCallable(callback: => Unit):GpioCallbackTrigger = {
    new GpioCallbackTrigger(new Callable[Void]() {
      @throws[Exception]
      override def call: Void = {
        callback
        null
      }
    })
  }
}

/**
  * Simple case class to wrap in stylish way the pin configuration.
  * @param presenceSensorPin the pin on which the input is received
  * @param lockerActuatorPin the pin on which the locker is connected
  * @param unlockedBikeFlagPin the pin on which the flag input is configurated
  */
case class PhysicLayerMapper(presenceSensorPin:Int, lockerActuatorPin:Int, unlockedBikeFlagPin:Int)



