package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.provisioners.{ResourceProvisioner, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.Pe
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.Vm
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.collection.JavaConverters.*
import scala.collection.mutable.ListBuffer

class BasicFirstExample

/** Factory for [[Simulations.BasicFirstExample]] instances. */
object BasicFirstExample {
  val config = ObtainConfigReference("cloudSimulator2") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  val logger = CreateLogger(classOf[BasicFirstExample])

  /** Method to start BasicFirstExample
   * It is just the same as the cloudsimplus' BasicFirstExample.java
   * Created this to learn Scala and play around with Cloudsimplus
   * @param sim_title The title of the simulation
   * @return Unit
   */
  def Start() =
    val simulation:CloudSim = new CloudSim();
    val broker0:DatacenterBroker = new DatacenterBrokerSimple(simulation);

    def createVms(): List[Vm] = {
      val vmList = new ListBuffer[Vm];
      for (i <- 1 to config.getInt("cloudSimulator2.vm.num")) {
        vmList += new VmSimple(
          config.getInt("cloudSimulator2.vm.mips"),
          config.getInt("cloudSimulator2.vm.PEs")
        ).setRam(config.getInt("cloudSimulator2.vm.RAMInMB"))
          .setBw(config.getInt("cloudSimulator2.vm.BandwidthInMBps"))
          .setSize(config.getInt("cloudSimulator2.vm.StorageInMB"))
          .setCloudletScheduler(new CloudletSchedulerSpaceShared());
      }
      return vmList.toList;
    }

    def createHost() : HostSimple = {
      val peList = new ListBuffer[Pe];
      for (i <- 1 to config.getInt("cloudSimulator2.host.PEs")) {
        peList += new PeSimple(config.getInt("cloudSimulator2.host.mips"));
      }

      val ramProvisioner = new ResourceProvisionerSimple();
      val bwProvisioner = new ResourceProvisionerSimple();

      val host = HostSimple(
        config.getInt("cloudSimulator2.host.RAMInMB"),
        config.getInt("cloudSimulator2.host.BandwidthInMBps"),
        config.getInt("cloudSimulator2.host.StorageInMB"),
        peList.toList.asJava
      );

      host
        .setRamProvisioner(new ResourceProvisionerSimple())
        .setBwProvisioner(new ResourceProvisionerSimple())
        .setVmScheduler(new VmSchedulerTimeShared());

      return host;
    }

    def createDatacenter() : DatacenterSimple = {
      val hostList = new ListBuffer[Host];
      for (i <- 1 to  config.getInt("cloudSimulator2.host.num")) {
        hostList += createHost();
      }
      val dc : DatacenterSimple =  new DatacenterSimple(simulation, hostList.toList.asJava);
      dc.getCharacteristics().setArchitecture("x64");
      return dc;
    }

    def createCloudlets() : List[Cloudlet] = {
      val cloudletList = new ListBuffer[Cloudlet];
      val utilizationModel:UtilizationModelDynamic = new UtilizationModelDynamic(
        config.getDouble("cloudSimulator2.utilizationRatio")
      );

      for (i <- 1 to config.getInt("cloudSimulator2.cloudlet.num")) {
        cloudletList += new CloudletSimple(
          config.getInt("cloudSimulator2.cloudlet.length"),
          config.getInt("cloudSimulator2.cloudlet.PEs"),
          utilizationModel
        ).setSizes(config.getInt("cloudSimulator2.cloudlet.size"));
      }

      return cloudletList.toList;
    }

    var datacenter0 = createDatacenter();
    val vmList = createVms();
    val cloudletList = createCloudlets();
    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);
    logger.info("Starting cloud simulation...");
    simulation.start();
    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();

}
