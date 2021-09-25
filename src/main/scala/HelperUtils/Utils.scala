/* NSC -- new Scala compiler
 * @author  Anandavignesh
 */

package HelperUtils
import HelperUtils.CreateLogger
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}

import scala.collection.JavaConverters.*
import scala.util.Random

class Utils

/** Factory for [[HelperUtils.Utils]] instances. */
object Utils {
  // Create a logger object of with class Utils
  val logger = CreateLogger(classOf[Utils])

  /** Create a list of datacenters
   * @param num_dc Number of datacenters to be created
   * @param simulation CloudSim() object
   * @param sim_title The title of the simulation
   * @return dcList List[DatacenterSimple]
   */
  def createDatacenters(num_dc: Int, simulation: CloudSim, sim_title: String): List[DatacenterSimple] = {
    logger.info("To Do: Create %d datacenters".format(num_dc))
    val dcList = (1 to num_dc).map(
      dc_id => createDatacenter(dc_id, simulation, sim_title)
    )
    logger.info("%d datacenters created...".format(dcList.length))
    return dcList.toList
  }

  /** Create a single datacenter
   * @param dc_id Current datacenter id
   * @param simulation CloudSim() object
   * @param sim_title The title of the simulation
   * @return dc DatacenterSimple object
   */
  def createDatacenter(dc_id: Int, simulation: CloudSim, sim_title: String): DatacenterSimple = {
    val config = ObtainConfigReference(sim_title) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val num_hosts = config.getInt(sim_title + '.' + dc_id + '.' + "host" + "." + "num")
    val hostList = (1 to num_hosts).map(
      host_id => Utils.createHost(host_id, dc_id, sim_title)
    )

    // Randomly picks a value for architecture, os, and vmm based on the service
    val service = config.getString(sim_title + "." + "service").toString()
    val rand = Random
    // If the service is IaaS, randomly pick otherwise, configs are rigid
    val arg1, arg2, arg3 =
      if (service == "IaaS") rand.nextInt(2)
      else 0
    val dcConfig = config.getConfig(sim_title + "." + dc_id + '.' + "dc")

    val architecture = dcConfig.getList("arch")
    val os = dcConfig.getList("os")
    val vmm = dcConfig.getList("vmm")

    val cost = dcConfig.getDouble("costPerSecond")
    val costRAM = dcConfig.getDouble("costRAM")
    val costStorage = dcConfig.getDouble("costStorage")
    val costBw = dcConfig.getDouble("costBw")

    // Create a new DatacenterSimple object
    val dc = new DatacenterSimple(simulation, hostList.asJava, new VmAllocationPolicyBestFit())
    dc.getId
    dc.getCharacteristics()
      .setArchitecture(architecture.unwrapped().get(arg1).toString)
      .setOs(os.unwrapped().get(arg2).toString)
      .setVmm(vmm.unwrapped().get(arg3).toString)
      .setCostPerSecond(cost)
      .setCostPerMem(costRAM)
      .setCostPerStorage(costStorage)
      .setCostPerBw(costBw)
    logger.info("Created Datacenter %d with %d hosts".format(dc_id, num_hosts))
    return dc
  }

  /** Create a single host
   * @param host_id Current host id
   * @param dc_id Current datacenter id
   * @param sim_title The title of the simulation
   * @return host HostSimple object
   */
  def createHost(host_id: Int, dc_id: Int, sim_title: String): HostSimple = {
    val config = ObtainConfigReference(sim_title) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val hostConfig = config.getConfig(sim_title + "." + dc_id + '.' + "host")
    val num_pes = hostConfig.getInt("PE")
    val mips = hostConfig.getInt("mips")
    val peList = (1 to num_pes).map(
      pe_id => new PeSimple(pe_id, mips, new PeProvisionerSimple())
    )

    val ramProvisioner = new ResourceProvisionerSimple()
    val bwProvisioner = new ResourceProvisionerSimple()

    val ram = hostConfig.getInt("RAMInMB")
    val bw = hostConfig.getInt("BandwidthInMBps")
    val storage = hostConfig.getInt("StorageInMB")

    // Create a new HostSimple object
    val host = HostSimple(ram, bw, storage, peList.asJava)
    host
      .setRamProvisioner(new ResourceProvisionerSimple())
      .setBwProvisioner(new ResourceProvisionerSimple())
      .setVmScheduler(new VmSchedulerSpaceShared())
      .setId(host_id)

    return host
  }

  /** Create a list of VMs
   * @param num_vms Number of VMs to be created
   * @param sim_title The title of the simulation
   * @return vmList List[Vm]
   */
  def createVms(num_vms: Int, sim_title: String): List[Vm] = {
    logger.info("To Do: Create %d VMs".format(num_vms))
    val vmList = (1 to num_vms).map(
      vm_id => createVm(vm_id, sim_title)
    )
    logger.info("%d VMs created...".format(vmList.length))
    return vmList.toList;
  }

  /** Create a single VM
   * @param vm_id Current vm id
   * @param sim_title The title of the simulation
   * @return vm VM object
   */
  def createVm(vm_id: Int, sim_title: String): VmSimple = {
    val config = ObtainConfigReference(sim_title) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val vmConfig = config.getConfig(sim_title + "." + "vm")
    val mips = vmConfig.getInt("mips")
    val PEs = vmConfig.getInt("PE")
    val ram = vmConfig.getInt("RAMInMB")
    val size = vmConfig.getInt("SizeInMB")
    val bw = vmConfig.getInt("BandwidthInMBps")

    // Create a new VmSimple object and set required configurations
    val vm: VmSimple = new VmSimple(vm_id, mips, PEs)
    vm
      .setRam(ram)
      .setSize(size)
      .setBw(bw)
      .setCloudletScheduler(new CloudletSchedulerSpaceShared())
    logger.info("Created VM %d with %d PEs".format(vm_id, PEs))
    return vm
  }

  /** Create a list of cloudlets
   * @param sim_title The title of the simulation
   * @return cloudletList List[Cloudlet]
   */
  def createCloudlets(sim_title: String): List[Cloudlet] = {
    val config = ObtainConfigReference(sim_title) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val service = config.getString(sim_title + "." + "service").toString()
    val num_cloudlets_list = config.getList(sim_title + '.' + "cloudlet" + "." + "num")
    val rand = Random
    val arg1 = if (service == "IaaS" || service == "PaaS") rand.nextInt(3)
               else 0
    val num_cloudlets = num_cloudlets_list.unwrapped().get(arg1).asInstanceOf[Int]
    logger.info("To Do: Create %d cloudlets".format(num_cloudlets))
    val cloudletList = (1 to num_cloudlets).map(
      cloudlet_id => createCloudlet(cloudlet_id, sim_title)
    )
    logger.info("%d cloudlets created...".format(cloudletList.length))
    return cloudletList.toList;
  }

  /** Create a list of cloudlets
   * @param cloudlet_id Current cloudlet id
   * @param sim_title The title of the simulation
   * @return cloudlet CloudletSimple object
   */
  def createCloudlet(cloudlet_id: Int, sim_title: String): CloudletSimple = {
    val config = ObtainConfigReference(sim_title) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val service = config.getString(sim_title + "." + "service").toString()
    val cloudletConfig = config.getConfig(sim_title + "." + "cloudlet")
    val PEs = cloudletConfig.getList("PE")
    val length = cloudletConfig.getList("length")
    val fileSize = cloudletConfig.getList("fileSize")
    val outputSize = cloudletConfig.getList("outputSize")
    val utilizationRatio = config.getDouble(sim_title + "." + "utilizationRatio")

    val utilizationModel: UtilizationModelDynamic = new UtilizationModelDynamic(utilizationRatio)

    val rand = Random
    val arg1, arg2, arg3, arg4 =
      if (service == "IaaS" || service == "PaaS") rand.nextInt(3) else 0

    val cloudlet: CloudletSimple = new CloudletSimple(
      cloudlet_id,
      length.unwrapped().get(arg1).asInstanceOf[Int],
      PEs.unwrapped().get(arg2).asInstanceOf[Int])
    cloudlet
      .setFileSize(fileSize.unwrapped().get(arg3).asInstanceOf[Int])
      .setOutputSize(outputSize.unwrapped().get(arg4).asInstanceOf[Int])
      .setUtilizationModel(utilizationModel)
    logger.info("Created cloudlet %d of length %d".format(cloudlet_id, length.unwrapped().get(arg1).asInstanceOf[Int]))
    return cloudlet
  }

  /** Helper method to print the metrics(cost mainly)
   * @param broker Current cloudlet id
   * @param sim_title The title of the simulation
   * @return Unit
   */
  def printCost(broker: DatacenterBrokerSimple, sim_title: String): Unit = {
    logger.info(s"Inside print cost function... Starting...")
    print("\n")
    val config = ObtainConfigReference(sim_title) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val num_dc = config.getInt(sim_title + '.' + "dc" + '.' + "num")
    var totalCost: Double = 0.0
    var totalNonIdleVMs: Int = 0
    var processingTotalCost, memoryTotalCost, storageTotalCost, bwTotalCost = 0.0
    var vmList: List[VmSimple] = broker.getVmCreatedList().asScala.toList
    vmList.map(vm => {
      val vmCost: VmCost = new VmCost(vm)
      processingTotalCost += vmCost.getProcessingCost()
      memoryTotalCost += vmCost.getMemoryCost()
      storageTotalCost += vmCost.getStorageCost()
      bwTotalCost += vmCost.getBwCost()

      totalCost += vmCost.getTotalCost()
      if (vm.getTotalExecutionTime() > 0) {
        totalNonIdleVMs += 1
      }
      print(vmCost)
      print("\n")
    })
    logger.info("Total cost is %.2f$".format(totalCost))
    print("Total cost ($) for %2d created VMs from %2d in %d DCs : %8.2f$ %13.2f$ %17.2f$ %12.2f$ %15.2f$%n".format(
      totalNonIdleVMs, broker.getVmsNumber(), num_dc,
      processingTotalCost, memoryTotalCost, storageTotalCost, bwTotalCost, totalCost))
  }
}
