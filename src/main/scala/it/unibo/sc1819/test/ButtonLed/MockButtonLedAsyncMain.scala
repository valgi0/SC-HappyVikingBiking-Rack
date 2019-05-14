package it.unibo.sc1819.test.ButtonLed

import com.pi4j.io.gpio.trigger.{GpioSetStateTrigger, GpioSyncStateTrigger}
import com.pi4j.io.gpio.{GpioFactory, GpioPin, PinPullResistance, PinState, RaspiPin}
import io.vertx.scala.core.Vertx
import it.unibo.sc1819.util.messages.{EventBusMessage, LockBikeMessage, Topics}
import it.unibo.sc1819.worker.bracket.{PhysicLayerMapper, RackBracket}

object MockButtonLedAsyncMain extends App {

 /* val SECOND_LED_PIN = RaspiPin.GPIO_23
  val LED_PIN = RaspiPin.GPIO_24
  val BTN_PIN = RaspiPin.GPIO_25

  println(BTN_PIN.getAddress)

  val gpioManager = GpioFactory.getInstance

  val button  = gpioManager.provisionDigitalInputPin(BTN_PIN, PinPullResistance.PULL_DOWN)

  val led = gpioManager.provisionDigitalOutputPin(LED_PIN,"MockLed", PinState.LOW)

  val freeLed = gpioManager.provisionDigitalOutputPin(SECOND_LED_PIN,"FreeLed", PinState.HIGH)

  button.addTrigger(new GpioSetStateTrigger(PinState.HIGH, freeLed, PinState.LOW))
  button.addTrigger(new GpioSetStateTrigger(PinState.LOW, freeLed, PinState.HIGH))

  button.addTrigger(new GpioSyncStateTrigger(led))

  while(true) {

  }*/

  val vertx = Vertx.vertx
  val testBracket = RackBracket("1.1.1.1", PhysicLayerMapper(25, 24, 23), vertx)

  vertx.eventBus.consumer[String](Topics.LOCK_WORKER_TOPIC).handler(message => {
   println(message.body())
  })

  while(true) {
    Thread.sleep(5000)
    testBracket.unlockBike()
  }

}
