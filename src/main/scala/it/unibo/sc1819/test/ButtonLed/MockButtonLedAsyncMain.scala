package it.unibo.sc1819.test.ButtonLed

import com.pi4j.io.gpio.trigger.{GpioSetStateTrigger, GpioSyncStateTrigger}
import com.pi4j.io.gpio.{GpioFactory, GpioPin, PinPullResistance, PinState, RaspiPin}

object MockButtonLedAsyncMain extends App {

  val LED_PIN = RaspiPin.GPIO_26
  val BTN_PIN = RaspiPin.GPIO_19

  val gpioManager = GpioFactory.getInstance

  val button  = gpioManager.provisionDigitalInputPin(BTN_PIN, PinPullResistance.PULL_DOWN)

  val led = gpioManager.provisionDigitalOutputPin(LED_PIN,"MockLed", PinState.LOW)

  button.addTrigger(new GpioSyncStateTrigger(led))

}
