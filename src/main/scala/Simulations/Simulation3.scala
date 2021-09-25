/* NSC -- new Scala compiler
 * @author  Anandavignesh
 */

package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference, Utils}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisioner, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.Pe
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.collection.JavaConverters.*
import scala.collection.mutable.ListBuffer

class Simulation3

/** Factory for [[Simulations.Simulation3]] instances. */
object Simulation3 {
  val logger = CreateLogger(classOf[Simulation3])

  /** Method to start simulation2
   * @param sim_title The title of the simulation
   * @return Unit
   */
  def Start(sim_title: String) : Unit = {
    val config = ObtainConfigReference(sim_title) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }

    // Create a simulation and broker object
    logger.info("Creating a new CloudSim() object for simulation3...")
    val simulation: CloudSim = new CloudSim()
    val broker0: DatacenterBrokerSimple = new DatacenterBrokerSimple(simulation)
    logger.info("Broker created - " + broker0.getClass)

    // Get the required parameters from the config file
    val NUM_DC = config.getInt(sim_title + '.' + "dc" + "." + "num")
    val NUM_VM = config.getInt(sim_title + '.' + "vm" + "." + "num")

    // Create dc, vm, cloudlet lists
    var dcList = Utils.createDatacenters(NUM_DC, simulation, sim_title);
    val vmList = Utils.createVms(NUM_VM, sim_title);
    val cloudletList = Utils.createCloudlets(sim_title);

    // Submit the vm, cloudlet lists to the broker
    logger.info("Submitting VM and cloudlets to the broker...")
    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);

    // Start the simulation
    logger.info("*In a futuristic voice*")
    logger.info("Fasten your seatbelts, we are entering into the world of simulation")
    simulation.start();

    // Build and print cost
    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
    Utils.printCost(broker0, sim_title)
  }
}