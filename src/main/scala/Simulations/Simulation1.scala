package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import org.apache.commons.math3.ml.neuralnet.Network
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyBestFit, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.network.topologies.{BriteNetworkTopology, NetworkTopology}
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisioner, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.Pe
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.collection.JavaConverters.*
import scala.collection.mutable.ListBuffer

class Simulation1

/** Factory for [[Simulations.Simulation1]] instances. */
object Simulation1 {
  // get the config
  val config = ObtainConfigReference("simulation1") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  // create a logger object
  val logger = CreateLogger(classOf[Simulation1])

  // create a simulation object
  val simulation: CloudSim = new CloudSim();

  /** Create a list of datacenters by recursion
   * @param num_dc Number of datacenters to be created
   * @return dcList List[DatacenterSimple]
   */
  def createDatacenters(num_dc: Int): List[DatacenterSimple] = {
    val dcList = new ListBuffer[DatacenterSimple]
    dcRecursive(num_dc, 0, dcList)
    return dcList.toList
  }

  /** Function that recursively creates one datacenter
   * @param num_dc Number of datacenters to be created
   * @param dc_id id of current datacenter
   * @param dcList ListBuffer[DatacenterSimple]
   * @return Unit
   */
  def dcRecursive(num_dc: Int, dc_id: Int, dcList: ListBuffer[DatacenterSimple]): Unit = {
    if (num_dc == 0) {
      return
    }
    val dc = createDatacenter(dc_id)
    dcList += dc
    dcRecursive(num_dc - 1, dc_id + 1, dcList)
  }

  /** Function that creates one datacenter
   * @param dc_id id of current datacenter
   * @return dc DatacenterSimple
   */
  def createDatacenter(dc_id: Int): DatacenterSimple = {
    val hostList = new ListBuffer[Host]
    val num_hosts = config.getInt("simulation1.dc." + dc_id + ".numHost")
    logger.info("HOSTS: " + num_hosts)
    hostRecursive(num_hosts, dc_id, hostList)

    val architecture = config.getString("simulation1.dc." + dc_id + ".arch")
    val os = config.getString("simulation1.dc." + dc_id + ".os")
    val vmm = config.getString("simulation1.dc." + dc_id + ".vmm")
    val cost = config.getDouble("simulation1.dc." + dc_id + ".costPerSecond")
    val costRAM = config.getDouble("simulation1.dc." + dc_id + ".costRAM")
    val costStorage = config.getDouble("simulation1.dc." + dc_id + ".costStorage")
    val costBw = config.getDouble("simulation1.dc." + dc_id + ".costBw")

    val dc = new DatacenterSimple(simulation, hostList.asJava, new VmAllocationPolicySimple())
    dc.getId
    dc.getCharacteristics()
      .setArchitecture(architecture)
      .setOs(os)
      .setVmm(vmm)
      .setCostPerSecond(cost)
      .setCostPerMem(costRAM)
      .setCostPerStorage(costStorage)
      .setCostPerBw(costBw)
    return dc
  }

  /** Function that recursively creates one datacenter
   * @param num_hosts Number of hosts to be created
   * @param dc_id id of current host
   * @param hostList ListBuffer[Host]
   * @return Unit
   */
  def hostRecursive(num_hosts: Int, dc_id: Int, hostList: ListBuffer[Host]): Unit = {
    if (num_hosts == 0) {
      return
    }

    val num_pe = config.getInt("simulation1.host.PEs")
    val mips = config.getInt("simulation1.host.mips")

    val peList = new ListBuffer[Pe]
    peRecursive(num_pe, 0, mips, peList)

    val ram = config.getInt("simulation1.host.RAMInMB")
    val storage = config.getInt("simulation1.host.StorageInMB")
    val bw = config.getInt("simulation1.host.BandwidthInMBps")

    val host: HostSimple = new HostSimple(
      ram,
      bw,
      storage,
      peList.asJava
    )
    host
      .setRamProvisioner(new ResourceProvisionerSimple())
      .setBwProvisioner(new ResourceProvisionerSimple())
      .setVmScheduler(new VmSchedulerTimeShared())

    hostList += host
    hostRecursive(num_hosts - 1, dc_id + 1, hostList)
  }

  /** Function that recursively creates one datacenter
   * @param num_pe Number of PEs to be created
   * @param pe_id id of current PE
   * @param mips MIPS for the current PE
   * @param peList ListBuffer[Host]
   * @return Unit
   */
  def peRecursive(num_pe: Int, pe_id: Int, mips: Int, peList: ListBuffer[Pe]): Unit = {
    if (num_pe == 0) {
      return
    }
    peList += new PeSimple(pe_id, mips, new PeProvisionerSimple())
    peRecursive(num_pe - 1, pe_id + 1, mips, peList)
  }

  /** Create a list of VMs by recursion
   * @param num_vm Number of VMs to be created
   * @return vmList List[Vm]
   */
  def createVms(num_vm: Int): List[Vm] = {
    val vmList = new ListBuffer[Vm];
    vmRecursive(num_vm, 0, vmList)
    return vmList.toList;
  }

  /** Function that recursively creates one VM
   * @param num_vm Number of VMs to be created
   * @param vm_id id of current Vm
   * @param vmList ListBuffer[Vm]]=
   * @return Unit
   */
  def vmRecursive(num_vm: Int, vm_id: Int, vmList: ListBuffer[Vm]): Unit = {
    if (num_vm == 0) {
      return
    }

    val mips = config.getInt("simulation1.vm.mips")
    val PEs = config.getInt("simulation1.vm.PEs")
    val ram = config.getInt("simulation1.vm.RAMInMB")
    val size = config.getInt("simulation1.vm.SizeInMb")
    val bw = config.getInt("simulation1.vm.BandwidthInMBps")

    val vm = new VmSimple(vm_id, mips, PEs)
    vm
      .setRam(ram)
      .setSize(size)
      .setBw(bw)
      .setCloudletScheduler(new CloudletSchedulerTimeShared())

    vmList += vm
    vmRecursive(num_vm - 1, vm_id + 1, vmList)
  }

  /** Create a list of cloudlets by recursion
   * @param num_cloudlet Number of cloudlets to be created
   * @return cloudletList List[cloudlet]
   */
  def createCloudlets(num_cloudlet: Int): List[Cloudlet] = {
    val cloudletList = new ListBuffer[Cloudlet];
    cloudletRecursive(num_cloudlet, 0, cloudletList)
    return cloudletList.toList;
  }

  /** Function that recursively creates one VM
   * @param num_cloudlet Number of cloudlets to be created
   * @param cloudlet_id id of current cloudlet
   * @param cloudletList ListBuffer[Cloudlet]
   * @return Unit
   */
  def cloudletRecursive(num_cloudlet: Int, cloudlet_id: Int, cloudletList: ListBuffer[Cloudlet]): Unit = {
    if (num_cloudlet == 0) {
      return
    }

    val PEs = config.getInt("simulation1.cloudlet.PEs")
    val length = config.getInt("simulation1.cloudlet.length")
    val fileSize = config.getInt("simulation1.cloudlet.fileSize")
    val outputSize = config.getInt("simulation1.cloudlet.outputSize")
    val utilizationRatio = config.getDouble("simulation1.cloudlet.utilizationRatio")

    val utilizationModel: UtilizationModelFull = new UtilizationModelFull()
    val cloudlet: CloudletSimple = new CloudletSimple(length, PEs, utilizationModel)
    cloudlet
      .setFileSize(fileSize)
      .setOutputSize(outputSize)
      .setId(cloudlet_id)

    cloudletList += cloudlet
    cloudletRecursive(num_cloudlet - 1, cloudlet_id + 1, cloudletList)
  }

  /** Set up network to datacenters if isNetwork is true
   * @param dcList: List[DatacenterSimple]
   * @param broker0: DatacenterBrokerSimple
   * @return Unit
   */
  def network(dcList: List[DatacenterSimple], broker0: DatacenterBrokerSimple, simulation: CloudSim) : Unit = {
    dcList.foreach(dc =>
      setUpNetworkTopology(dc, broker0, simulation)
    )
  }

  /** Configure network topology to a single datacenter
   * @param dc: DatacenterSimple
   * @param broker0: DatacenterBrokerSimple
   * @return Unit
   */
  def setUpNetworkTopology(dc: DatacenterSimple, broker0: DatacenterBrokerSimple, simulation: CloudSim): Unit = {
    val bw = config.getDouble("simulation1.network.bw")
    val latency = config.getDouble("simulation1.network.latency")
    val networkTopology: NetworkTopology = new BriteNetworkTopology()
    simulation.setNetworkTopology(networkTopology)
    networkTopology.addLink(dc, broker0, bw, latency)
  }

  /** Helper method to print the metrics(cost mainly)
   * @param broker Current cloudlet id
   * @param num_dc Number of datacenters
   * @return Unit
   */
  def printCost(broker: DatacenterBrokerSimple, num_dc: Int): Unit = {
    print("\n")
    var totalCost: Double = 0.0
    var totalNonIdleVMs: Int = 0
    var processingTotalCost, memoryTotalCost, storageTotalCost, bwTotalCost = 0.0
    var vmList: List[VmSimple] = broker.getVmCreatedList().asScala.toList
    vmList.foreach(
      vm => {
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
      }
    )
    print("Total cost ($) for %3d created VMs from %3d in DC %d: %8.2f$ %13.2f$ %17.2f$ %12.2f$ %15.2f$%n".format(
      totalNonIdleVMs, broker.getVmsNumber(), num_dc,
      processingTotalCost, memoryTotalCost, storageTotalCost, bwTotalCost, totalCost))
  }

  /** Method to start simulation1
   * @param isNetwork A boolean representing whether network is needed or not
   * @return Unit
   */
  def Start(isNetwork: Boolean) =

    // get basic parameters from config file
    val num_dc = config.getInt("simulation1.dc.num")
    val num_vm = config.getInt("simulation1.vm.num")
    val num_cloudlet = config.getInt("simulation1.cloudlet.num")
    logger.info("DC: " + num_dc + " | VM: " + num_vm + " | CLOUDLET: " + num_cloudlet)

    // create broker
    val broker0: DatacenterBrokerSimple = new DatacenterBrokerSimple(simulation);

    // Get datacenter, vm, cloudlet lists
    var dcList = createDatacenters(num_dc);
    if (isNetwork) {
      network(dcList, broker0, simulation)
    }

    val vmList = createVms(num_vm);
    val cloudletList = createCloudlets(num_cloudlet);

    // Submit vmList and cloudletList to broker
    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);

    // Start the simulation
    logger.info("Starting cloud simulation...");
    simulation.start();

    // Build
    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();

    // Print total cost
    printCost(broker0, num_dc)
}
