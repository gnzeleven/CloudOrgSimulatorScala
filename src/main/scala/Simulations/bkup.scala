//package Simulations
//
//import HelperUtils.{CreateLogger, ObtainConfigReference}
//import Simulations.BasicCloudSimPlusExample.logger
//import org.cloudbus.cloudsim.core.{CloudSim, SimEntity}
//import org.cloudbus.cloudsim.brokers.DatacenterBroker
//import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
//import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
//import org.cloudbus.cloudsim.core
//import org.cloudbus.cloudsim.vms.VmSimple
//import org.cloudbus.cloudsim.core.events.SimEvent
//import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
//import org.cloudbus.cloudsim.vms.Vm
//import org.cloudsimplus.listeners.{DatacenterBrokerEventInfo, EventInfo, EventListener}
//
//import scala.collection.mutable.ListBuffer
//import org.cloudbus.cloudsim.hosts.Host
//import org.cloudbus.cloudsim.hosts.HostSimple
//import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
//import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
//import org.cloudsimplus.builders.tables.CloudletsTableBuilder
//
//import java.{lang, util}
//import java.util.{Comparator, function}
//import java.util.function.BiFunction
//import scala.collection.JavaConverters.*
//
//class BasicFirstExamplex
//object BasicFirstExamplex {
//  val config = ObtainConfigReference("cloudSimulator2") match {
//    case Some(value) => value
//    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
//  }
//  val logger = CreateLogger(classOf[BasicFirstExamplex])
//
//  def Start() =
//    val simulation:CloudSim = new CloudSim();
//  val broker0:DatacenterBroker = new DatacenterBrokerSimple(simulation);
//
//  def createVms() : List[Vm] = {
//    val vmList = new ListBuffer[Vm];
//    for (i <- 1 to config.getInt("cloudSimulator2.vm.num")) {
//      vmList += new VmSimple(
//        config.getInt("cloudSimulator2.vm.mipsCapacity"),
//        config.getInt("cloudSimulator2.vm.PEs")
//      ).setRam(config.getInt("cloudSimulator2.vm.RAMInMBs"))
//        .setBw(config.getInt("cloudSimulator2.vm.BandwidthInMBps"))
//        .setSize(config.getInt("cloudSimulator2.vm.StorageInMBs"));
//    }
//    return vmList.toList;
//  }
//
//  def createHost() : HostSimple = {
//    val peList = new ListBuffer[Pe];
//    for (i <- 1 to config.getInt("cloudSimulator2.host.PEs")) {
//      peList += new PeSimple(config.getInt("cloudSimulator2.host.mipsCapacity"));
//    }
//    return HostSimple(
//      config.getInt("cloudSimulator2.host.RAMInMBs"),
//      config.getInt("cloudSimulator2.host.BandwidthInMBps"),
//      config.getInt("cloudSimulator2.host.StorageInMBs"),
//      peList.toList.asJava
//    );
//  }
//
//  def createDatacenter() : DatacenterSimple = {
//    val hostList = new ListBuffer[Host];
//    for (i <- 1 to  config.getInt("cloudSimulator2.host.num")) {
//      hostList += createHost();
//    }
//    return new DatacenterSimple(simulation, hostList.toList.asJava);
//  }
//
//  def createCloudlets() : List[Cloudlet] = {
//    val cloudletList = new ListBuffer[Cloudlet];
//    val utilizationModel:UtilizationModelDynamic = new UtilizationModelDynamic(
//      config.getInt("cloudSimulator2.utilizationRatio")
//    );
//    for (i <- 1 to config.getInt("cloudSimulator2.cloudlet.num")) {
//      cloudletList += new CloudletSimple(
//        config.getInt("cloudSimulator2.cloudlet.length"),
//        config.getInt("cloudSimulator2.cloudlet.PEs"),
//        utilizationModel
//      ).setSizes(config.getInt("cloudSimulator2.cloudlet.size"));
//    }
//
//    return cloudletList.toList;
//  }
//
//  var datacenter0 = createDatacenter();
//  logger.info(s"Created Datacenter")
//  val vmList = createVms();
//  logger.info(s"Created " + config.getInt("cloudSimulator2.vm.num") + " VMs");
//  val cloudletList = createCloudlets();
//  logger.info(s"Created " + config.getInt("cloudSimulator2.cloudlet.num") + " Cloudlets");
//  broker0.submitVmList(vmList.asJava);
//  broker0.submitCloudletList(cloudletList.asJava);
//  logger.info("Starting cloud simulation...");
//  simulation.start();
//  new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
//
//}
