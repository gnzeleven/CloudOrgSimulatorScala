import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, BasicFirstExample, CloudletSchedulerSpaceSharedExample1, Simulation1, Simulation2}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    //BasicCloudSimPlusExample.Start()
    //BasicFirstExample.Start()
    //CloudletSchedulerSpaceSharedExample1.Start();
    //Simulation1.Start()
    Simulation2.Start()
    logger.info("Finished cloud simulation...")

class Simulation