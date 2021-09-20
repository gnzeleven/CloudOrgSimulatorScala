package Simulations

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.collection.JavaConverters.*
import scala.collection.mutable.ListBuffer

class CloudletSchedulerSpaceSharedExample1
object CloudletSchedulerSpaceSharedExample1 {

  val HOSTS : Int = 1;
  val HOSTS_PES : Int = 4;

  val VMS : Int = 1;
  val VMS_PES : Int = 4;

  val CLOUDLETS : Int = 4;
  val CLOUDLETS_PES : Int = 2;
  val CLOUDLETS_LENGTH : Int = 10000;

  def Start() =
    val simulation:CloudSim = new CloudSim();
    val broker0:DatacenterBroker = new DatacenterBrokerSimple(simulation);

    def createHost() : HostSimple = {
      val peList = new ListBuffer[Pe];
      for (i <- 1 to HOSTS_PES) {
        peList += new PeSimple(1000, new PeProvisionerSimple);
      }

      val ram = 2048;
      val bw = 10000;
      val storage = 1000000;

      val host = HostSimple(ram, bw, storage, peList.toList.asJava);

      host
      .setRamProvisioner(new ResourceProvisionerSimple())
      .setBwProvisioner(new ResourceProvisionerSimple())
      .setVmScheduler(new VmSchedulerTimeShared());

    return host;
  }

    def createDatacenter() : DatacenterSimple = {
      val hostList = new ListBuffer[Host];
      for (i <- 1 to  HOSTS) {
        hostList += createHost();
      }
      val dc : DatacenterSimple =  new DatacenterSimple(simulation, hostList.toList.asJava, new VmAllocationPolicySimple);
      return dc;
    }

    def createVms(): List[Vm] = {
      val vmList = new ListBuffer[Vm];
      for (i <- 1 to VMS) {
        vmList += new VmSimple(i, 1000, VMS_PES ).setRam(512)
          .setBw(1000)
          .setSize(10000)
          .setCloudletScheduler(new CloudletSchedulerSpaceShared());
      }
      return vmList.toList;
    }

    def createCloudlets() : List[Cloudlet] = {
      val cloudletList = new ListBuffer[Cloudlet];
      val utilizationModel:UtilizationModelFull = new UtilizationModelFull();

      for (i <- 1 to CLOUDLETS) {
        cloudletList += new CloudletSimple(i, CLOUDLETS_LENGTH, CLOUDLETS_PES).setFileSize(1024)
          .setOutputSize(1024)
          .setUtilizationModel(utilizationModel);
      }

      return cloudletList.toList;
    }

    val datacenter0 = createDatacenter();

    val vmList = createVms();
    val cloudletList = createCloudlets();
    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);

    simulation.start();
    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
}
