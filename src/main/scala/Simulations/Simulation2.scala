package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
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
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.collection.JavaConverters.*
import scala.collection.mutable.ListBuffer

class Simulation2
object Simulation2 {
  val SIM_TITLE = "simulation2"
  val config = ObtainConfigReference(SIM_TITLE) match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  val logger = CreateLogger(classOf[BasicFirstExample])
  val simulation:CloudSim = new CloudSim();

  def Start() = {
    val broker0 : DatacenterBrokerSimple = new DatacenterBrokerSimple(simulation)
    val NUM_DC = config.getInt(SIM_TITLE + '.' + "dc" + "." + "num")
    val NUM_VM = config.getInt(SIM_TITLE + '.' + "vm" + "." + "num")
    val NUM_CLOUDLET = config.getInt(SIM_TITLE + '.' + "cloudlet" + "." + "num")
    var dcList = createDatacenters(NUM_DC);
    val vmList = createVms(NUM_VM);
    val cloudletList = createCloudlets(NUM_CLOUDLET);
    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);
    logger.info("Starting cloud simulation...");
    simulation.start();
    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
    printCost(broker0, dcList)
  }

  def createDatacenters(num_dc : Int) : List[DatacenterSimple] = {
    val dcList = (1 to num_dc).map(
      dc_id => createDatacenter(dc_id)
    )
    return dcList.toList
  }

  def createDatacenter(dc_id : Int) : DatacenterSimple = {
    val num_hosts = config.getInt(SIM_TITLE + '.' + dc_id + '.' + "host" + "." + "num")
    val hostList = (1 to num_hosts).map(
      host_id => createHost(host_id, dc_id)
    )
    val architecture = config.getString(SIM_TITLE + '.' + dc_id + '.' + "dc" + "." +"arch")
    val os = config.getString(SIM_TITLE + '.' + dc_id + '.' + "dc" + "." + "os")
    val vmm = config.getString(SIM_TITLE + '.' + dc_id + '.' + "dc" + "." + "vmm")
    val cost = config.getDouble(SIM_TITLE + '.' + dc_id + '.' + "dc" + "." + "costPerSecond")
    val costRAM = config.getDouble(SIM_TITLE + '.' + dc_id + '.' + "dc" + "." + "costRAM")
    val costStorage = config.getDouble(SIM_TITLE + '.' + dc_id + '.' + "dc" + "." + "costStorage")
    val costBw = config.getDouble(SIM_TITLE + '.' + dc_id + '.' + "dc" + "." + "costBw")

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

  def createHost(host_id : Int, dc_id : Int) : HostSimple = {
    val num_pes = config.getInt(SIM_TITLE + '.' + dc_id + '.' + "host" + "." + "PE")
    val mips = config.getInt(SIM_TITLE + '.' + dc_id + '.' + "host" + "." + "mips")
    val peList = (1 to num_pes).map(
      pe_id => new PeSimple(pe_id, mips, new PeProvisionerSimple())
    )

    val ramProvisioner = new ResourceProvisionerSimple()
    val bwProvisioner = new ResourceProvisionerSimple()

    val ram = config.getInt(SIM_TITLE + '.' + dc_id + '.' + "host" + "." + "RAMInMB")
    val bw = config.getInt(SIM_TITLE + '.' + dc_id + '.' + "host" + "." + "BandwidthInMBps")
    val storage = config.getInt(SIM_TITLE + '.' + dc_id + '.' + "host" + "." + "StorageInMB")

    val host = HostSimple(ram, bw, storage, peList.asJava)
    host
      .setRamProvisioner(new ResourceProvisionerSimple())
      .setBwProvisioner(new ResourceProvisionerSimple())
      .setVmScheduler(new VmSchedulerTimeShared())
      .setId(host_id)

    return host
  }

  def createVms(num_vms : Int) : List[Vm] = {
    val vmList = (1 to num_vms).map(
      vm_id => createVm(vm_id)
    )
    return vmList.toList;
  }

  def createVm(vm_id : Int) : VmSimple = {
    val mips = config.getInt(SIM_TITLE + '.' + "vm" + "." + "mips")
    val PEs = config.getInt(SIM_TITLE + '.' + "vm" + "." + "PE")
    val ram = config.getInt(SIM_TITLE + '.' + "vm" + "." + "RAMInMB")
    val size = config.getInt(SIM_TITLE + '.' + "vm" + "." + "SizeInMB")
    val bw = config.getInt(SIM_TITLE + '.' + "vm" + "." + "BandwidthInMBps")

    val vm : VmSimple = new VmSimple(vm_id, mips, PEs)
    vm
      .setRam(ram)
      .setSize(size)
      .setBw(bw)
      .setCloudletScheduler(new CloudletSchedulerTimeShared())

    return vm
  }

  def createCloudlets(num_cloudlets : Int) : List[Cloudlet] = {
    val cloudletList = (1 to num_cloudlets).map(
      cloudlet_id => createCloudlet(cloudlet_id)
    )
    return cloudletList.toList;
  }

  def createCloudlet(cloudlet_id : Int) : CloudletSimple = {
    val PEs = config.getInt(SIM_TITLE + '.' + "cloudlet" + "." + "PE")
    val length = config.getInt(SIM_TITLE + '.' + "cloudlet" + "." + "length")
    val fileSize = config.getInt(SIM_TITLE + '.' + "cloudlet" + "." + "fileSize")
    val outputSize = config.getInt(SIM_TITLE + '.' + "cloudlet" + "." + "outputSize")
    val utilizationRatio = config.getDouble(SIM_TITLE + '.' + "cloudlet" + "." + "utilizationRatio")

    val utilizationModel : UtilizationModelFull = new UtilizationModelFull()

    val cloudlet : CloudletSimple = new CloudletSimple(length, PEs, utilizationModel)
    cloudlet
      .setFileSize(fileSize)
      .setOutputSize(outputSize)
      .setId(cloudlet_id)

    return cloudlet
  }

  def printCost(broker : DatacenterBrokerSimple, dc: List[DatacenterSimple]) : Unit = {
    print("\n")
    var totalCost: Double = 0.0
    var totalNonIdleVMs : Int = 0
    var processingTotalCost, memoryTotalCost, storageTotalCost, bwTotalCost = 0.0
    var vmList : List[VmSimple] = broker.getVmCreatedList().asScala.toList
    for (vm <- vmList) {
      val vmCost : VmCost = new VmCost(vm)
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
    print("Total cost ($) for %3d created VMs from %3d in DC %d: %8.2f$ %13.2f$ %17.2f$ %12.2f$ %15.2f$%n".format(
      totalNonIdleVMs, broker.getVmsNumber(), dc.head.getId(),
      processingTotalCost, memoryTotalCost, storageTotalCost, bwTotalCost, totalCost))
  }
}
