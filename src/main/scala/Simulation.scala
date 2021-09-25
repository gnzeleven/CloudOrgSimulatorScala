/* NSC -- new Scala compiler
 * @author  Anandavignesh
 */

package Sim

import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, BasicFirstExample, Simulation1, Simulation2 , Simulation3}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

class Simulation

/** Factory for [[Sim.Simulation]] instances. */
object Simulation:
  val logger = CreateLogger(classOf[Simulation])
  // Manually set SIM_TITLE as simulation1 or simulation2 or simulation3 to run corresponding simulation
  val SIM_TITLE = "simulation2"

  /** Main Method - Triggers Simulation specified by SIM_TITLE
   * @param None
   * @return Unit
   */
  @main def runSimulation() : Unit = {
    logger.info("Constructing a cloud model... %s".format(SIM_TITLE))

    SIM_TITLE match {
      case "simulation1" => Simulation1.Start(false) // isNetwork = true if network topology has to be added
      case "simulation2" => Simulation2.Start("simulation2")
      case "simulation3" => Simulation3.Start("simulation3")
      case "basicCloudSimPlusExample" => BasicCloudSimPlusExample.Start() // for learning purpose
      case "basicFirstExample" => BasicFirstExample.Start() // for learning purpose
    }
    logger.info("Finished cloud simulation...")
  }